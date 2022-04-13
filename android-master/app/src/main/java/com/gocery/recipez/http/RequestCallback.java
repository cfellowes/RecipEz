package com.gocery.recipez.http;

public interface RequestCallback<T> {

    void onCompleteRequest(T data);
}
