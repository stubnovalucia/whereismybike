package com.hci.whereismybike;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.ViewModelProviders;

import android.Manifest;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;


/**
 * MainActivity class: activity that is opened when the app is launched.
 *
 * @author Dominykas Rumsa
 * @author Lucia Stubnova
 *
 * Dominykas Rumsa: Main author
 * Lucia Stubnova: Added to main class fragment implementation
 *
 */
public class MainActivity extends AppCompatActivity  implements
        FragmentMarkLocation.OnFragmentInteractionListener,
        FragmentMain.OnFragmentInteractionListener,
        FragmentSavedLocation.OnFragmentInteractionListener,
        FragmentSavePicture.OnFragmentInteractionListener,
        FragmentSettings.OnFragmentInteractionListener,
        FragmentRetrieveLocation.OnFragmentInteractionListener,
        FragmentSignIn.OnFragmentInteractionListener {

    private SharedViewModel sharedViewModel;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Runtime permissions
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 101);
            return;
        }

        //Temporary code for conditional rendering
        sharedViewModel = ViewModelProviders.of(this).get(SharedViewModel.class);
        sharedViewModel.setSavedBike(false);

    }

    //To be done
    @Override
    public void onFragmentInteraction(Uri uri) {

    }

}


