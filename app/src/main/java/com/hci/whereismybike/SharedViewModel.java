package com.hci.whereismybike;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.maps.model.LatLng;

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
    private LatLng latLng;
    private String address;
    private String dateandtime;
    private String note;

    public void setSavedBike(Boolean saved) {savedBike.setValue(saved);}

    public LiveData<Boolean> getSavedBike() {
        return savedBike;
    }

    public void setLatLng(LatLng loc){latLng =loc;}

    public LatLng getLatLng() {
        return latLng;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getAddress() {
        return address;
    }

    public String getDateandtime() {
        return dateandtime;
    }

    public void setDateandtime(String dateandtime) {
        this.dateandtime = dateandtime;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
