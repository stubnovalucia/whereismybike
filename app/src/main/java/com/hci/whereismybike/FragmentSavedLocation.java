package com.hci.whereismybike;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.Navigation;

import android.os.Environment;
import android.provider.MediaStore;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.app.Activity.RESULT_OK;


/**
 * FragmentSavedLocation: Fragment displaying location and other additional information about the parked bike.
 *
 * @author Lucia Stubnova
 *
 * Lucia Stubnova: Main author
 *
 */
public class FragmentSavedLocation extends Fragment {

    //imageview displaying bike photo
    private ImageView bikePhotoView;

    private OnFragmentInteractionListener mListener;

    private SharedViewModel sharedViewModel;

    //Firebase
    private StorageReference mStorageRef;
    private DatabaseReference mDatabaseRef;
    private FirebaseAuth auth;
    private FirebaseUser user;
    private String userID;

    //imageview displaying bike photo
    private ImageView mapView;
    private File map;

    String currentPhotoPath;

    public FragmentSavedLocation() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment FragmentSavedLocation.
     */
    public static FragmentSavedLocation fragmentSavedLocation() {
        FragmentSavedLocation fragment = new FragmentSavedLocation();
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
        mDatabaseRef = FirebaseDatabase.getInstance().getReference();
        auth = FirebaseAuth.getInstance();

        //get the signed in user
        user = auth.getCurrentUser();
        userID = user.getUid();

        GetMap();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_saved_location, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //listener for take a picture button
        Button takePictureButton = view.findViewById(R.id.takePictureButton);
        takePictureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Navigation.findNavController(view).navigate(R.id.action_savedLocationFragment_to_fragmentTakePicture);
                dispatchTakePictureIntent();
            }
        });

        //listener for save location button
        Button saveLocationButton = view.findViewById(R.id.saveLocationButton);
        saveLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadDataToFirebase();
                sharedViewModel.setSavedBike(true);
                Navigation.findNavController(view).navigate(R.id.action_savedLocationFragment_to_fragmentMain);
            }
        });

        mapView = view.findViewById(R.id.mapView);
        bikePhotoView = view.findViewById(R.id.bikePhotoView);

        // listener for map click
        mapView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                getActivity().onBackPressed();
            }
        });

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


    //Camera feature

    /**
     *
     */
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    static final int REQUEST_TAKE_PHOTO = 1;

    /**
     *
     */
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File

            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(getActivity(),
                        "com.hci.whereismybike.MainActivity",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    private void galleryAddPic() {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(currentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        getActivity().sendBroadcast(mediaScanIntent);

        uploadFile(contentUri);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        System.out.println("activityResult " );
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
            System.out.println("success");
            Glide.with(getActivity()).load(currentPhotoPath).into(bikePhotoView);
            //Navigation.findNavController(getView()).navigate(R.id.action_savedLocationFragment_to_fragmentSavePicture);
            galleryAddPic();
        }
    }
    private void uploadFile(Uri file){
        StorageReference storageReference = mStorageRef.child("images/users/" + userID + "/bike.jpg");
        storageReference.putFile(file)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        System.out.println("FILE UPLOADED");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        System.out.println(exception.getMessage());
                    }
                });
    }

    private void uploadDataToFirebase () {
        //Firebase real time
        mDatabaseRef.child("users").child(userID).child("address").setValue("test address");
        mDatabaseRef.child("users").child(userID).child("date").setValue("20197987");
        mDatabaseRef.child("users").child(userID).child("note").setValue("Test note 123");
//        if(note != null){
//            mDatabaseRef.child("users").child(userID).child("date").child("note").setValue("lol lol lol");
//        }
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
            StorageReference storageReference = mStorageRef.child("images/users/" + userID + "/map.png");
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
