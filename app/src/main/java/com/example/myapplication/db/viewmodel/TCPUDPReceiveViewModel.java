package com.example.myapplication.db.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class TCPUDPReceiveViewModel extends ViewModel {
    private final MutableLiveData<byte[]> espReceiveDataMutableLiveData = new MutableLiveData<>();

    public void setData(byte[] espReceiveData) {
        espReceiveDataMutableLiveData.postValue(espReceiveData);
    }

    public LiveData<byte[]> getData() {
        return espReceiveDataMutableLiveData;
    }

}
