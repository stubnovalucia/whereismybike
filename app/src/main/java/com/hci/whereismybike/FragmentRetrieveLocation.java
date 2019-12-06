package com.hci.whereismybike;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.Navigation;

import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * FragmentRetrieveLocation class: Fragment that will have a map view and a button for saving location.
 *
 * @author Lucia Stubnova
 *
 * Lucia Stubnova: Main author
 *
 */
public class FragmentRetrieveLocation extends Fragment {

    private OnFragmentInteractionListener mListener;
    private SharedViewModel sharedViewModel;

    //imageview displaying bike photo
    private ImageView bikePhotoView;
    private File image;

    //imageview displaying map
    private ImageView mapView;
    private File map;

    //Firebase
    private StorageReference mStorageRef;
    private DatabaseReference mDatabaseRef;
    private String userID;

    public FragmentRetrieveLocation() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment FragmentRetrieveLocation.
     */
    public static FragmentRetrieveLocation fragmentRetrieveLocation() {
        FragmentRetrieveLocation fragment = new FragmentRetrieveLocation();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedViewModel = ViewModelProviders.of(getActivity()).get(SharedViewModel.class);

        //Firebase
        mStorageRef = FirebaseStorage.getInstance().getReference();
        FirebaseAuth auth = FirebaseAuth.getInstance();

        //get the signed in user
        FirebaseUser user = auth.getCurrentUser();
        userID = user.getUid();

        mDatabaseRef = FirebaseDatabase.getInstance().getReference().child("users").child(userID);

        if(sharedViewModel.getAddress().equals("")){
            GetData();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_fragment_retrieve_location, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //listener for found it button
        Button foundItButton = view.findViewById(R.id.foundItButton);
        foundItButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());

                alertDialogBuilder.setTitle("FOUND IT?");
                alertDialogBuilder.setMessage("If you confirm, saved location will be lost.");

                alertDialogBuilder.setPositiveButton(R.string.found_it, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        sharedViewModel.setSavedBike(false);
                        Navigation.findNavController(getView()).navigate(R.id.action_fragmentRetrieveLocation_to_fragmentMain);
                    }
                });
                alertDialogBuilder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
                alertDialog.getButton(alertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.blueGrey));
                alertDialog.getButton(alertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.lightGreen));
            }
        });

        //listener for found it button
        FloatingActionButton getDirectionsButton = view.findViewById(R.id.getDirectionsButton);
        //code modified from: https://stackoverflow.com/questions/2662531/launching-google-maps-directions-via-an-intent-on-android/44365894#44365894
        getDirectionsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            Uri.Builder builder = new Uri.Builder();
            builder.scheme("https")
                    .authority("www.google.com")
                    .appendPath("maps")
                    .appendPath("dir")
                    .appendPath("")
                    .appendQueryParameter("api", "1")
                    .appendQueryParameter("destination", sharedViewModel.getLatLng().latitude + "," + sharedViewModel.getLatLng().longitude);
            String url = builder.build().toString();
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            startActivity(i);
            }
        });

        bikePhotoView = view.findViewById(R.id.bikePhotoView);
        if (sharedViewModel.getBikePictureTaken()){
            GetPicture();
        }

        EditText address = view.findViewById(R.id.address);
        address.setText(sharedViewModel.getAddress());

        TextView date = view.findViewById(R.id.date);
        date.setText(sharedViewModel.getDateandtime());

        TextView noteText = view.findViewById(R.id.noteText);
        noteText.setText(sharedViewModel.getNote());

        mapView = view.findViewById(R.id.mapView);
        try {
            Glide.with(getContext()).load(Uri.fromFile(sharedViewModel.getMap())).into(mapView);
        }catch (Exception e){
            System.out.println(e.getMessage());
            // Get it from firebase storage
            GetMap();
        }
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }

    public void GetPicture () {
        try{
            Glide.with(getActivity()).load(Uri.fromFile(sharedViewModel.getBikePicture())).into(bikePhotoView);
            return;
        }catch (Exception e){
            System.out.println("EXCEPTION");
            System.out.println(e.getMessage());
            try {
                // Create an image file name
                String imageFileName = "bike";
                File storageDir = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                image = File.createTempFile(imageFileName,"jpg",storageDir);
            } catch (IOException ie){
                ie.printStackTrace();
            }

            if (image != null){
                StorageReference storageReference = mStorageRef.child("images/users/" + userID + "/bike.jpg");
                storageReference.getFile(image)
                        .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                System.out.println("File succesfully downloaded");
                                Glide.with(getActivity()).load(Uri.fromFile(image)).into(bikePhotoView);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle failed download
                        // ...
                    }
                });
            }
        }
    }
    public void GetData(){
        mDatabaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                GenericTypeIndicator<Map<String, String>> genericTypeIndicator = new GenericTypeIndicator<Map<String, String>>(){};
                Map<String, String> dataMap = dataSnapshot.getValue(genericTypeIndicator);
                sharedViewModel.setAddress(dataMap.get("address"));
                sharedViewModel.setDateandtime(dataMap.get("date"));
                sharedViewModel.setNote(dataMap.get("note"));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
    public void GetMap () {
        try {
            // Create an image file name
            String imageFileName = "map";
            File storageDir = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            map = File.createTempFile(imageFileName,"jpg",storageDir);
        } catch (IOException ie){
            ie.printStackTrace();
        }

        if (map != null){
            StorageReference storageReference = mStorageRef.child("images/users/" + userID + "/map.jpeg");
            storageReference.getFile(map)
                    .addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                            System.out.println("File succesfully downloaded");
                            Glide.with(getActivity()).load(Uri.fromFile(map)).into(mapView);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle failed download
                    // ...
                }
            });
        }
    }
}
