package com.hci.whereismybike;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.maps.model.LatLng;

import java.io.File;

/**
 * SharedViewModel: SharedViewModel for sharing data bewteen fragments
 *
 * @author Lucia Stubnova
 *
 * Lucia Stubnova: Main author
 *
 * Dominykas Rumsa: added parameters for storing and retrieving data
 */

public class SharedViewModel extends ViewModel {
    private final MutableLiveData<Boolean> savedBike = new MutableLiveData<Boolean>();
    private GoogleSignInClient mGoogleSignInClient;
    private LatLng latLng;
    private String address;
    private String dateandtime;
    private String note;
    private File map;
    private File bikePicture;
    private String bikePictureTaken;

    public GoogleSignInClient getmGoogleSignInClient() {
        return mGoogleSignInClient;
    }

    public void setmGoogleSignInClient(GoogleSignInClient mGoogleSignInClient) {
        this.mGoogleSignInClient = mGoogleSignInClient;
    }

    public void setSavedBike(Boolean saved) {savedBike.setValue(saved);}

    public LiveData<Boolean> getSavedBike() {
        return savedBike;
    }

    public void setLatLng(LatLng loc){latLng =loc;}

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

    public File getMap() {
        return map;
    }

    public void setMap(File map) {
        this.map = map;
    }

    public File getBikePicture() {
        return bikePicture;
    }

    public void setBikePicture(File bikePicture) {
        this.bikePicture = bikePicture;
    }

    public String getBikePictureTaken() {
        return bikePictureTaken;
    }

    public void setBikePictureTaken(String bikePictureTaken) {
        this.bikePictureTaken = bikePictureTaken;
    }
}

