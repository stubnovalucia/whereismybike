package com.hci.whereismybike;

import android.content.Context;
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
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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

    //Firebase
    private StorageReference mStorageRef;
    private DatabaseReference mDatabaseRef;
    private FirebaseAuth auth;
    private FirebaseUser user;
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
        auth = FirebaseAuth.getInstance();

        //get the signed in user
        user = auth.getCurrentUser();
        userID = user.getUid();

        mDatabaseRef = FirebaseDatabase.getInstance().getReference().child("users").child(userID);

        GetData();
        GetPicture();
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
                sharedViewModel.setSavedBike(false);
                Navigation.findNavController(view).navigate(R.id.action_fragmentRetrieveLocation_to_fragmentMain);
            }
        });

        bikePhotoView = view.findViewById(R.id.bikePhotoView);
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
    public void GetData(){
        mDatabaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                GenericTypeIndicator<Map<String, String>> genericTypeIndicator = new GenericTypeIndicator<Map<String, String>>(){};
                Map<String, String> map = dataSnapshot.getValue(genericTypeIndicator);
                String address = map.get("address");
                String date = map.get("date");
                String note = map.get("note");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
