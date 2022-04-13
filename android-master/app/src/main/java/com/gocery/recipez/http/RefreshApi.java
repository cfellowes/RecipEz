package com.gocery.recipez.http;

/**
 * The RefreshApi class follows the Singleton pattern and exposes a public API for refreshing the
 * backend server.
 */
public class RefreshApi extends Api {

    private static final String route = "/api/refresh";

    private static RefreshApi instance;

    public static RefreshApi getInstance() {
        if (instance == null) {
            instance = new RefreshApi();
        }
        return instance;
    }

    /**
     * Refreshes the backend server. This method is only called once when the app is started.
     */
    public void refresh() {
        new HttpTask(baseUrl + route).execute();
    }
}
