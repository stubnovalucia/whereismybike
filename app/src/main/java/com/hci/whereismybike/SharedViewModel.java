package com.hci.whereismybike;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

/**
 * SharedViewModel: SharedViewModel for sharing data bewteen fragments
 *
 * @author Lucia Stubnova
 *
 * Lucia Stubnova: Main author
 *
 */

public class SharedViewModel extends ViewModel {
    private final MutableLiveData<Boolean> savedBike = new MutableLiveData<Boolean>();

    public void setSavedBike(Boolean saved) {
        savedBike.setValue(saved);
    }

    public LiveData<Boolean> getSavedBike() {
        return savedBike;
    }
}
