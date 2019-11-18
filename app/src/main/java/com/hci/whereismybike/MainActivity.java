package com.hci.whereismybike;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

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
        FragmentTakePicture.OnFragmentInteractionListener,
        FragmentRetrieveLocation.OnFragmentInteractionListener,
        FragmentSignIn.OnFragmentInteractionListener,
        FragmentSavedLocation.OnDataPass {


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
    }

    //To be done
    @Override
    public void onFragmentInteraction(Uri uri) {

    }


    @Override
    public void onSaveBike(Boolean savedBike) {
        //To be done - data passing from fragment to activity
//        this.savedBike = savedBike;
//        bundle.putBoolean("savedbike", savedBike);
//        Bundle bundle = new Bundle();
//        bundle.putBoolean("savedBike", savedBike);
//        FragmentMain fragmentMain = new FragmentMain();
//        fragmentMain.setArguments(bundle);
    }
}

