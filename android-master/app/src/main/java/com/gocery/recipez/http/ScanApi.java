package com.gocery.recipez.http;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import com.gocery.recipez.data.ItemInfo;
import com.gocery.recipez.data.ItemSuggestion;
import com.gocery.recipez.data.ScanResult;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

/**
 * The ScanApi class follows the Singleton pattern and exposes a public API for communicating with
 * the receipt scanning backend.
 */
public class ScanApi extends Api {

    private static final String route = "/api/scan";

    private static ScanApi instance;

    public static ScanApi getInstance() {
        if (instance == null) {
            instance = new ScanApi();
        }
        return instance;
    }

    /**
     * Passes an image of a receipt to the backend to be scanned for items. The result of the scan
     * is then passed back through the RequestCallback object.
     *
     * @param imageStream The image to be scanned.
     * @param callback    The callback listener to return the ScanResult objects to.
     */
    public void scanReceipt(InputStream imageStream, final RequestCallback<ScanResult[]> callback) {
        String base64String = Base64.encodeToString(getImageBytes(imageStream), Base64.DEFAULT);

        new HttpTask(baseUrl + route).setRequestCallback(new RequestCallback<String>() {
            @Override
            public void onCompleteRequest(String json) {
                callback.onCompleteRequest(parseJson(json));
            }
        }).execute("json", base64String);
    }

    private byte[] getImageBytes(InputStream imageStream) {
        ByteArrayOutputStream outBytes = new ByteArrayOutputStream();
        Bitmap bitmap = BitmapFactory.decodeStream(imageStream);
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outBytes);
        return outBytes.toByteArray();
    }

    private ScanResult[] parseJson(String json) {
        try {
            JSONArray jsonResults = new JSONArray(json);
            ScanResult[] results = new ScanResult[jsonResults.length()];
            for (int i = 0; i < jsonResults.length(); i++) {
                JSONObject jsonResult = jsonResults.getJSONObject(i);
                results[i] = new ScanResult();
                results[i].name = jsonResult.getString("item").toLowerCase();
                ItemInfo itemInfo = new ItemInfo();
                itemInfo.quantity = jsonResult.getDouble("quantity");
                if (jsonResult.has("units")) {
                    itemInfo.unit = jsonResult.getString("units");
                }
                results[i].itemInfo = itemInfo;

                JSONArray suggestions = jsonResult.getJSONArray("suggestions");
                for (int k = 0; k < suggestions.length(); k++) {
                    JSONObject suggestion = suggestions.getJSONObject(k);
                    ItemSuggestion itemSuggestion = new ItemSuggestion();
                    itemSuggestion.itemId = suggestion.getString("id");
                    itemSuggestion.score = suggestion.getDouble("score");
                    results[i].suggestions.add(itemSuggestion);
                }
            }
            return results;
        }
        catch (NullPointerException | JSONException e) {
            System.err.println("JSON string: " + json);
            e.printStackTrace();
        }
        return new ScanResult[0];
    }
}
