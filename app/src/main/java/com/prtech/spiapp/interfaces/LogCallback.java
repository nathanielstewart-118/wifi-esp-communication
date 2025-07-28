package com.prtech.spiapp.interfaces;

public interface LogCallback {
    void onSuccess();
    void onFailure(Exception e);
}
