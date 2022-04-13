from datetime import datetime
import firebase_admin
import firebase_admin.auth as auth
import firebase_admin.db as firebase
import flask
import heapq
import jellyfish
import json
import munkres
import random
import regex
import requests
import veryfi
import urllib.parse

SPLIT = regex.compile(r"[\p{P}\s]+")  # split on punctuation/whitespace

# Firebase creds
creds = firebase_admin.credentials.Certificate("creds.json")
firebase_admin.initialize_app(creds, {"databaseURL": "https://gocery-cse110.firebaseio.com"})

app = flask.Flask(__name__)
hungarian = munkres.Munkres()  # for matching Spoonacular ingredients

veryfi_client = None  # Veryfi API key/client
spoonacular_key = None  # Spoonacular API key
listener_ref = None  # Firebase listener handle for /items

item_abbreviations = {}  # maps {"phonetic": "id"}
items = {}  # maps {"id": ("name", number of words)}


def add_item(itemid, item):
    """
    Add a new (id, name) pairing to the local store.

    :param itemid: The ID of the item in Firebase.
    :param item: The item's name.
    """
    global items
    global item_abbreviations

    # Skip if we already have it
    # (and it's the same name? the name could change so if it's different keep going?)
    if itemid in items and items[itemid][0] == item["name"]:
        return

    # Split into individual words.
    name_parts = SPLIT.split(item["name"])

    # Store (name, number of words) for normalizing scores later.
    items[itemid] = (item["name"], len(name_parts))

    # For each *unique* word...
    for part in set(name_parts):
        # Store in buckets based on phonetic abbbreviation.
        part = jellyfish.match_rating_codex(part)

        if part not in item_abbreviations:
            item_abbreviations[part] = [itemid]
        else:
            item_abbreviations[part].append(itemid)


@app.before_first_request
def fetch_items():
    """Initializes a Firebase listener for adding new/existing items to the local store."""
    global items
    global item_abbreviations
    global listener_ref

    # If we already have a listener open, close it
    # and clear out the old items.
    if listener_ref is not None:
        listener_ref.close()
        item_abbreviations = {}
        items = {}

    def listener(event):
        global items
        data = event.data

        if data is None:
            # Deletion event
            del items[event.path[1:]]
        elif isinstance(data, dict):
            if event.path != "/":
                # Single item was added
                add_item(event.path[1:], data)

    it_ref = firebase.reference("/items")

    # Add the full dataset synchronously *now*, then ignore
    # the full update the async listener gets and just add individual
    # item updates
    for item in it_ref.get().items():
        add_item(*item)

    listener_ref = it_ref.listen(listener)


@app.before_first_request
def fetch_keys():
    """Fetches API keys for Veryfi and Spoonacular."""
    global veryfi_client
    global spoonacular_key

    veryfi_keys = firebase.reference("/apikeys/veryfi").get()
    veryfi_client = veryfi.Client(**veryfi_keys)
    spoonacular_key = firebase.reference("/apikeys/spoonacular").get()


@app.route("/api/refresh", methods=("POST",), strict_slashes=False)
def force_refresh():
    """
    /api/refresh force-refreshes items and API keys.
    (It also can be used to force GAE to start an instance.)

    :returns: 200
    """
    fetch_items()
    fetch_keys()

    return "", 200


@app.route("/api/scan", methods=("POST",), strict_slashes=False)
def scan_receipt():
    """
    /api/scan takes in a Base64-encoded JPEG as POST body and sends it
    to Veryfi for scanning, then matches each line item on the receipt
    with zero or more possible ingredients in Firebase.

    :returns: 500 if an error occurs when sending to Veryfi,
    400 if no image was sent or Veryfi returns no line items,
    otherwise 200 with a JSON body of the form:

    [
        {
            "item": "text on receipt...",
            "quantity": quantity (optional),
            "units": units (optional),
            "suggestions": [
                {"id": possible ingredient id, "score": numeric score (higher better)},
                ...
            ]
        },
        ...
    ]
    """
    img = flask.request.data

    # Skip if no image
    if not img:
        return "", 400

    # Format the filename for Veryfi as
    # <timestamp>.<random bits to avoid collision>.jpg
    timestamp = datetime.now().isoformat()
    nonce = str(random.getrandbits(16))

    try:
        response = veryfi_client._request("POST", "/documents/", {
            "file_name": timestamp + "." + nonce + "." + ".jpg",
            "file_data": img.decode("utf-8"),
            "categories": ["Grocery"],
            "auto_delete": False
        })
    except Exception as e:
        # Check if there's a status code
        if hasattr(e, "code"):
            return "", e.code
        else:
            return "", 500

    if "line_items" in response:
        # Get receipt line items
        line_items = response["line_items"]
        response = []

        for item in line_items:
            # Any text?
            if "description" in item and item["description"]:
                obj = {"item": item["description"], "suggestions": []}

                # Add quantity and units if they're present
                if "quantity" in item and item["quantity"]:
                    obj["quantity"] = item["quantity"]

                    if "unit_of_measure" in item and item["unit_of_measure"]:
                        obj["units"] = item["unit_of_measure"]

                # Get unique words from the receipt text
                name_parts = set([jellyfish.match_rating_codex(part) for part in SPLIT.split(item["description"])])
                matches = {}

                for part in name_parts:
                    # Do we have a matching bucket?
                    if part in item_abbreviations:
                        for itemid in item_abbreviations[part]:
                            # Skip if not in items -- this can happen if it
                            # was just deleted.
                            if itemid not in items:
                                continue

                            # Add to the score based on how many words we matched out of the
                            # number of words in the ingredient's name.
                            # Scores are negative so that minimizing them
                            # means maximizing the actual score.
                            if itemid not in matches:
                                matches[itemid] = [-1 / items[itemid][1], itemid]
                            else:
                                matches[itemid][0] -= 1 / items[itemid][1]

                matches = list(matches.values())
                heapq.heapify(matches)

                # Partial sort: get the top 5 suggestions
                while len(matches) > 0 and len(obj["suggestions"]) < 5:
                    match = heapq.heappop(matches)

                    # Hard cutoff of 1/3rd.
                    if match[0] <= -0.33:
                        obj["suggestions"].append({"id": match[1], "score": -match[0]})

                response.append(obj)

        return json.dumps(response), 200

    # No items returned.
    return "", 400


@app.route("/api/recipes", methods=("POST",), strict_slashes=False)
def suggest_recipes():
    """
    /api/receipes takes a form with pantry=pantry id, token=firebase user token, and num=number of recipes to suggests
    and returns recipe suggestions from Spoonacular based on items present in the pantry.

    :returns: 500 if Spoonacular produces an error, 400 if the token is invalid or the user doesn't own the
    pantry, 200 otherwise with a JSON body of the form:

    [
        {
            "recipe": "...recipe name",
            "instructions": "recipe instructions, newline deliminated",
            "items": [
                {
                    "item": "...name",
                    "id": firebase ID (empty if we don't have the ingredient),
                    "quantity": "...",
                    "units": "..."
                },
                ...
            ]
        },
        ...
    ]
    """
    if "token" not in flask.request.form or "pantry" not in flask.request.form:
        return "", 400

    # Exchange the token for the user ID w/ Firebase.
    token = flask.request.form["token"]
    user = auth.verify_id_token(token)

    if "uid" not in user:
        return "", 400

    uid = user["uid"]

    # Does the user actually have access to this pantry?
    pantry = flask.request.form["pantry"]
    pantry_exists = firebase.reference("/users/" + uid + "/pantries/" + pantry).get()

    if pantry_exists is None:
        return "", 400

    query_string = []  # url-encoded ingredient names to put in the query string
    used_items = []  # item names, ID used to match them up later
    pantry_items = firebase.reference("/pantries/" + pantry + "/items").get(shallow=True)

    for item in pantry_items.keys():
        # Pretty unlikely to happen, but if we don't have the item data yet go get it.
        if item not in items:
            itemdata = firebase.reference("/items/" + item).get()
            if itemdata is None:
                continue
            else:
                add_item(item, itemdata)

        # Be sure to URL encode the name
        name = items[item][0]
        query_string.append(urllib.parse.quote(name))
        used_items.append((name, item))

    # Make the query (default to 10 recipes if we weren't asked for a specific number)
    query_string = ",+".join(query_string)
    response = requests.get("https://api.spoonacular.com/recipes/findByIngredients?ingredients=" + query_string +
                            "&number=" + flask.request.form.get("num", "10") +
                            "&apiKey=" + spoonacular_key +
                            "&ranking=1")

    if response.status_code != 200:
        return "", 500

    recipes = []

    for recipe in response.json():
        missed = recipe["missedIngredientCount"]
        used = recipe["usedIngredientCount"]

        # Missing ingredients must not outweigh found ingredients by more than 2:1
        if missed / (missed + used) <= 0.67:
            obj = {"recipe": recipe["title"], "instructions": ""}

            # Add the missing ingredients but with no ID
            obj["items"] = [{"item": item["name"],
                             "quantity": item["amount"],
                             "units": item["unit"]} for item in recipe["missedIngredients"]]

            # Get which ingredients of ours were used by subtracting the unused ingredients
            unused = set(item["originalString"] for item in recipe["unusedIngredients"])
            used_spoonacular = [item for item in recipe["usedIngredients"]]
            used_ours = [item for item in used_items if item[0] not in unused]

            # Match our ingredients with the names of the ingredients in the recipe.
            # Compute edit distance between each pair of ingredients, then use the
            # Hungarian algorithm to get the set of matchings that minimizes the edit distance.
            costs = [[-jellyfish.jaro_winkler(x[0], y["name"]) for y in used_spoonacular] for x in used_ours]
            pairs = hungarian.compute(costs)

            for pair in pairs:
                # Add the used ingredients with their ID in Firebase
                obj["items"].append({
                    "item": used_spoonacular[pair[1]]["name"],
                    "id": used_ours[pair[0]][1],
                    "quantity": used_spoonacular[pair[1]]["amount"],
                    "units": used_spoonacular[pair[1]]["unit"]
                })

            # Now go get the instructions
            instructions = requests.get("https://api.spoonacular.com/recipes/" + str(recipe["id"]) +
                                        "/analyzedInstructions?apiKey=" + spoonacular_key)

            if instructions.status_code == 200:
                instructions = instructions.json()
                obj["instructions"] = "\n".join([step["step"] for component in instructions
                                                 for step in component["steps"]])

            recipes.append(obj)

    return json.dumps(recipes), 200


if __name__ == "__main__":
    app.run(host="127.0.0.1", port=8000, debug=True)
