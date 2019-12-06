package com.hci.whereismybike;

import android.content.Context;
import android.graphics.Bitmap;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.Navigation;

import android.os.Environment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;
import java.util.Locale;

/**
 * FragmentMarkLocation class: Fragment that will have a map view and a button for saving location.
 *
 * @author Lucia Stubnova
 *
 * Lucia Stubnova: Main author
 * Dominykas Rumsa: Integrated map view and its functionality
 *
 */
public class FragmentMarkLocation extends Fragment implements OnMapReadyCallback{
    private OnFragmentInteractionListener mListener;
    private GoogleMap map;
    private Location currentLocation;
    private FileOutputStream out;

    private SharedViewModel sharedViewModel;

    private StorageReference mStorageRef;
    private String userID;

    public FragmentMarkLocation() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment FragmentMarkLocation.
     */
    public static FragmentMarkLocation fragmentMarkLocation() {
        FragmentMarkLocation fragment = new FragmentMarkLocation();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        sharedViewModel = ViewModelProviders.of(getActivity()).get(SharedViewModel.class);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        mStorageRef = FirebaseStorage.getInstance().getReference();

        //get the signed in user
        FirebaseUser user = auth.getCurrentUser();
        userID = user.getUid();

        //Get current fused location
        final OnMapReadyCallback mapCallBack = this;
        FusedLocationProviderClient client = LocationServices.getFusedLocationProviderClient(getMainActivity());

        client.getLastLocation().addOnSuccessListener(getMainActivity(), new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if(location != null) {
                    currentLocation = location;
                    LatLng loc = new LatLng(currentLocation.getLatitude(),currentLocation.getLongitude());
                    //Store data
                    sharedViewModel.setLatLng(loc);
                    sharedViewModel.setAddress(getAddress(loc));

                    SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
                    mapFragment.getMapAsync(mapCallBack);
                }
            }
        });
    }

    //Get address by decoding location
    public String getAddress(LatLng loc){
        try {
            Geocoder geo = new Geocoder(getContext(), Locale.getDefault());
            List<Address> addresses = geo.getFromLocation(loc.latitude, loc.longitude, 1);
            if (addresses.isEmpty()) {
                System.out.println("Waiting for Location");
            }
            else {
                if (addresses.size() > 0) {
                    String address = addresses.get(0).getThoroughfare() + " " + addresses.get(0).getFeatureName();
                    if(address.contains("null") || address.contains("Unnamed")){
                        return loc.latitude + "," + loc.longitude;
                    }
                    Toast toast = Toast.makeText(getMainActivity(), "Address: " + address, Toast.LENGTH_SHORT);
                    toast.setGravity(Gravity.BOTTOM,0,250);
                    toast.show();
                    return address;
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private MainActivity getMainActivity (){
        return (MainActivity) getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_mark_location, container, false);
        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button markLocationButton = view.findViewById(R.id.markLocationButton);
        markLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               takeMapSnapshot(view);
            }
        });
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

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        LatLng markerPosition = new LatLng(currentLocation.getLatitude(),currentLocation.getLongitude());

        Marker marker = map.addMarker(new MarkerOptions()
            .position(markerPosition)
            .title(getAddress(markerPosition)));
        marker.setDraggable(true);

        setCameraPosition(markerPosition);

        // https://stackoverflow.com/a/23590087
        map.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override
            public void onMarkerDragStart(Marker marker) {

            }

            @Override
            public void onMarkerDrag(Marker marker) {

            }

            @Override
            public void onMarkerDragEnd(Marker marker) {
                sharedViewModel.setLatLng(marker.getPosition());
                sharedViewModel.setAddress(getAddress(marker.getPosition()));
                marker.setTitle(getAddress(marker.getPosition()));
                setCameraPosition(marker.getPosition());
            }
        });
    }
    private void setCameraPosition(LatLng position){
        CameraPosition cameraPosition = new CameraPosition.Builder().target(position).zoom(17).build();
        map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    //https://stackoverflow.com/a/20118233
    private void takeMapSnapshot(final View view){
        map.snapshot(new GoogleMap.SnapshotReadyCallback() {
            public void onSnapshotReady(Bitmap bitmap) {
                try {
                    // Write image to memory
                    System.out.println("Creating file");
                    File storageDir = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                    File image = File.createTempFile(
                            "map",  /* prefix */
                            ".jpeg",         /* suffix */
                            storageDir      /* directory */
                    );
                    System.out.println("file created");
                    out = new FileOutputStream(image);
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
                    sharedViewModel.setMap(image);
                    Navigation.findNavController(view).navigate(R.id.action_markLocationFragment_to_savedLocationFragment);
                    uploadFile(image);
                }catch (Exception e){
                    System.out.println("Exception: "+e.getMessage());
                }
            }
        });
    }
    private void uploadFile(File file){
        StorageReference storageReference = mStorageRef.child("images/users/" + userID + "/map.jpg");
        Uri contentUri = Uri.fromFile(file);
        storageReference.putFile(contentUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        System.out.println("FILE UPLOADED");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        System.out.println("Exception "+exception.getMessage());
                    }
                });
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
}