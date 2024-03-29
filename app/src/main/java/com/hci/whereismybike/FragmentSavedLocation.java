package com.hci.whereismybike;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
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
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
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
import java.util.Locale;

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
    private String userID;

    //imageview displaying map
    private ImageView mapView;
    private File map;

    private String currentPhotoPath;
    private EditText noteText;

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
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();

        //get the signed in user
        userID = user.getUid();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_saved_location, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();

        if (sharedViewModel.getNote() != null && !sharedViewModel.getNote().isEmpty()) {
            Button addNoteBtn = getView().findViewById(R.id.addNoteButton);
            addNoteBtn.setVisibility(View.GONE);
            CardView noteCard = getView().findViewById(R.id.noteCard);
            noteCard.setVisibility(View.VISIBLE);

            EditText noteText = getView().findViewById(R.id.noteText);
            noteText.setText(sharedViewModel.getNote());
        }

        if (sharedViewModel.getBikePicture() != null && currentPhotoPath != null && !currentPhotoPath.isEmpty()) {
            Button takePictureBtn = getView().findViewById(R.id.takePictureButton);
            takePictureBtn.setVisibility(View.GONE);
            CardView imageCard = getView().findViewById(R.id.imageCard);
            imageCard.setVisibility(View.VISIBLE);

            Glide.with(getActivity()).load(currentPhotoPath).into(bikePhotoView);

        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //listener for take a picture button
        Button takePictureButton = view.findViewById(R.id.takePictureButton);
        takePictureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dispatchTakePictureIntent();
            }
        });

        //listener for add note button
        Button addNoteButton = view.findViewById(R.id.addNoteButton);
        addNoteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                noteText = getView().findViewById(R.id.noteText);
                Button addNoteBtn = getView().findViewById(R.id.addNoteButton);
                addNoteBtn.setVisibility(View.GONE);
                CardView noteCard = getView().findViewById(R.id.noteCard);
                noteCard.setVisibility(View.VISIBLE);
                noteCard.requestFocus();
            }
        });

        //listener for save location button
        Button saveLocationButton = view.findViewById(R.id.saveLocationButton);
        saveLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                noteText = getView().findViewById(R.id.noteText);
                if(!noteText.getText().toString().isEmpty()) {
                    sharedViewModel.setNote(noteText.getText().toString());
                }

                uploadDataToFirebase();
                sharedViewModel.setSavedBike(true);

                Navigation.findNavController(view).navigate(R.id.action_savedLocationFragment_to_fragmentMain);

                // Snackbar for location confirmation
                Snackbar snackbar = Snackbar.make(view, "Location saved",
                        Snackbar.LENGTH_SHORT);
                snackbar.setAction("UNDO", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        sharedViewModel.setSavedBike(false);
                        getActivity().onBackPressed();
                    }
                });
                snackbar.setActionTextColor(getResources().getColor(R.color.white));
                View sbView = snackbar.getView();
                sbView.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.lightGreen));
                snackbar.show();
            }
        });

        //listener for delete photo button
        ImageButton deletePhotoButton = view.findViewById(R.id.deletePhoto);
        deletePhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Button takePictureBtn = getView().findViewById(R.id.takePictureButton);
                takePictureBtn.setVisibility(View.VISIBLE);
                CardView imageCard = getView().findViewById(R.id.imageCard);
                imageCard.setVisibility(View.GONE);
                sharedViewModel.setBikePictureTaken("false");
                sharedViewModel.setBikePicture(null);
            }
        });

        //listener for delete note button
        ImageButton deleteNoteButton = view.findViewById(R.id.deleteNote);
        deleteNoteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                noteText.setText("");
                Button addNoteBtn = getView().findViewById(R.id.addNoteButton);
                addNoteBtn.setVisibility(View.VISIBLE);
                CardView noteCard = getView().findViewById(R.id.noteCard);
                noteCard.setVisibility(View.GONE);
            }
        });

        //note changes
        final EditText noteText = view.findViewById(R.id.noteText);
        noteText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
             @Override
             public void onFocusChange(View v, boolean hasFocus) {
                 // When focus is lost check that the text field has valid values.
                 if (!hasFocus) {
                     Button addNoteBtn = getView().findViewById(R.id.addNoteButton);
                     addNoteBtn.setVisibility(View.VISIBLE);
                     CardView noteCard = getView().findViewById(R.id.noteCard);
                     noteCard.setVisibility(View.GONE);
                 }
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

        EditText address = view.findViewById(R.id.address);
        address.setText(sharedViewModel.getAddress());

        noteText.setText(sharedViewModel.getNote());

        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm, MMM d", Locale.getDefault());
        String currentDateandTime = sdf.format(new Date());
        sharedViewModel.setDateandtime(currentDateandTime);
        TextView date = view.findViewById(R.id.date);
        date.setText(currentDateandTime);

        // Get image of map from local storage
        try {
            Glide.with(getContext()).load(Uri.fromFile(sharedViewModel.getMap())).into(mapView);
        }catch (Exception e){
            System.out.println(e.getMessage());
            // Get it from firebase storage
            GetMap();
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

        sharedViewModel.setBikePicture(f);
        sharedViewModel.setBikePictureTaken("true");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {

            //hide take picture button and show the photo card
            Button takePictureBtn = getView().findViewById(R.id.takePictureButton);
            takePictureBtn.setVisibility(View.GONE);
            CardView imageCard = getView().findViewById(R.id.imageCard);
            imageCard.setVisibility(View.VISIBLE);

            Glide.with(getActivity()).load(currentPhotoPath).into(bikePhotoView);
            galleryAddPic();
        }
    }

    private void uploadPhoto(Uri file){
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
        if(sharedViewModel.getBikePicture() != null) {
            uploadPhoto(Uri.fromFile(sharedViewModel.getBikePicture()));
        }
        //Firebase real time
        mDatabaseRef.child("users").child(userID).child("address").setValue(sharedViewModel.getAddress());
        mDatabaseRef.child("users").child(userID).child("date").setValue(sharedViewModel.getDateandtime());
        if(sharedViewModel.getNote() != null){
            mDatabaseRef.child("users").child(userID).child("note").setValue(sharedViewModel.getNote());
        }
        if(sharedViewModel.getBikePictureTaken().isEmpty()){
            sharedViewModel.setBikePictureTaken("false");
        }
        mDatabaseRef.child("users").child(userID).child("picture").setValue(sharedViewModel.getBikePictureTaken());
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
            StorageReference storageReference = mStorageRef.child("images/users/" + userID + "/map.jpg");
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
