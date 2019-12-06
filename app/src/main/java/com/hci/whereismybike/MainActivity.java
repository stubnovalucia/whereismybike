package com.hci.whereismybike;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.NavController;
import androidx.navigation.NavGraph;
import androidx.navigation.NavInflater;
import androidx.navigation.fragment.NavHostFragment;

import android.Manifest;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

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
        FragmentSettings.OnFragmentInteractionListener,
        FragmentRetrieveLocation.OnFragmentInteractionListener,
        FragmentSignIn.OnFragmentInteractionListener {

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
        SharedViewModel sharedViewModel = ViewModelProviders.of(this).get(SharedViewModel.class);
        sharedViewModel.setSavedBike(false);

        //Checks if signed in - modified code from: https://stackoverflow.com/a/53992737
        NavHostFragment navHost = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.navHostFragment);
        NavController navController = navHost.getNavController();

        NavInflater navInflater = navController.getNavInflater();
        NavGraph graph = navInflater.inflate(R.navigation.nav_graph);

        if (isSignedIn()) {
            graph.setStartDestination(R.id.fragmentMain);
        } else {
            graph.setStartDestination(R.id.fragmentSignIn);
        }

        navController.setGraph(graph);
    }

    private boolean isSignedIn() {
        return GoogleSignIn.getLastSignedInAccount(getApplicationContext()) != null;
    }

    //To be done
    @Override
    public void onFragmentInteraction(Uri uri) {

    }

}


