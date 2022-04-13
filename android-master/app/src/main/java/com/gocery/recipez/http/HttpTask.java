package com.gocery.recipez.http;

import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import javax.net.ssl.HttpsURLConnection;

public class HttpTask extends AsyncTask<String, Void, String> {

    private String url;
    private RequestCallback<String> callback;

    HttpTask(String url) {
        super();
        this.url = url;
    }

    HttpTask setRequestCallback(RequestCallback<String> callback) {
        this.callback = callback;
        return this;
    }

    @Override
    protected String doInBackground(String... strings) {
        try {
            HttpsURLConnection connection = (HttpsURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Accept", "application/json");
            connection.setDoOutput(true);
            connection.setDoInput(true);

            if (strings.length > 0) {
                if (strings[0].equals("json")) {
                    connection.setRequestProperty("Content-Type", "application/json; utf-8");
                }
                else {
                    connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; utf-8");
                }

                OutputStream outputStream = connection.getOutputStream();
                outputStream.write(strings[1].getBytes(StandardCharsets.UTF_8));
            }

            InputStreamReader inputStreamReader = new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            StringBuilder response = new StringBuilder();
            String responseLine;
            while ((responseLine = bufferedReader.readLine()) != null) {
                response.append(responseLine.trim());
            }
            System.out.println("Response: " + response.toString());
            return response.toString();
        }
        catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    @Override
    protected void onPostExecute(String response) {
        if (callback != null) {
            callback.onCompleteRequest(response);
        }
    }
}
