package com.example.whereismybike;

import androidx.appcompat.app.AppCompatActivity;

import android.net.Uri;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity  implements FragmentMarkLocation.OnFragmentInteractionListener, FragmentMain.OnFragmentInteractionListener, FragmentSavedLocation.OnFragmentInteractionListener, FragmentSavePicture.OnFragmentInteractionListener, FragmentSettings.OnFragmentInteractionListener, FragmentTakePicture.OnFragmentInteractionListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}


