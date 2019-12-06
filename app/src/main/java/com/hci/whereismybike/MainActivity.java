package com.hci.whereismybike;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.ViewModelProviders;

import android.Manifest;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.util.Map;


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

    private SharedViewModel sharedViewModel;

    private DatabaseReference mDatabaseRef;
    private String userID;

    private ValueEventListener valueEvent = new ValueEventListener() {
        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            if(!dataSnapshot.hasChild(userID)){
                System.out.println("EMPTY");
                sharedViewModel.setSavedBike(false);

            } else {
                System.out.println("FRAGMENT MAIN");
                // GetData();
            }

        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {

        }
    };
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

        FirebaseAuth auth = FirebaseAuth.getInstance();
        //get the signed in user
        FirebaseUser user = auth.getCurrentUser();
        userID = user.getUid();

        mDatabaseRef = FirebaseDatabase.getInstance().getReference("users");
        mDatabaseRef.addListenerForSingleValueEvent(valueEvent);

    }

    //To be done
    @Override
    public void onFragmentInteraction(Uri uri) {

    }
    private void GetData(){
        mDatabaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                GenericTypeIndicator<Map<String, String>> genericTypeIndicator = new GenericTypeIndicator<Map<String, String>>(){};
                Map<String, String> dataMap = dataSnapshot.getValue(genericTypeIndicator);
                sharedViewModel.setAddress(dataMap.get("address"));
                sharedViewModel.setDateandtime(dataMap.get("date"));
                sharedViewModel.setNote(dataMap.get("note"));
                sharedViewModel.setBikePictureTaken(dataMap.get("picture").equals("true"));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

}


