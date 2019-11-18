package com.example.whereismybike;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

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
import com.google.android.gms.tasks.OnSuccessListener;

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
public class FragmentMarkLocation extends Fragment implements OnMapReadyCallback {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    FusedLocationProviderClient client;
    GoogleMap map;
    Location currentLocation;
    List<Address> addresses;
    Marker marker;

    public FragmentMarkLocation() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment FragmentMarkLocation.
     */
    // TODO: Rename and change types and number of parameters
    public static FragmentMarkLocation newInstance(String param1, String param2) {
        FragmentMarkLocation fragment = new FragmentMarkLocation();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        //Get current fused location
        final OnMapReadyCallback mapCallBack = this;
        client = LocationServices.getFusedLocationProviderClient(getMainActivity());
        client.getLastLocation().addOnSuccessListener(getMainActivity(), new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if(location != null) {
                    currentLocation = location;
                    LatLng loc = new LatLng(currentLocation.getLatitude(),currentLocation.getLongitude());
                    getAddress(loc);

                    SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
                    mapFragment.getMapAsync(mapCallBack);
                }
            }
        });
    }

    //Get address by decoding location
    public String getAddress(LatLng loc){
        try {
            Geocoder geo = new Geocoder(getMainActivity(), Locale.getDefault());
            addresses = geo.getFromLocation(loc.latitude, loc.longitude, 1);
            if (addresses.isEmpty()) {
                System.out.println("Waiting for Location");
            }
            else {
                if (addresses.size() > 0) {
                    String address = addresses.get(0).getThoroughfare() + " " + addresses.get(0).getFeatureName();
                    if(address.contains("null") || address.contains("Unnamed")){
                        return loc.latitude + "," + loc.longitude;
                    }
                    Toast.makeText(getMainActivity(), "Address:- " + address, Toast.LENGTH_LONG).show();
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
                Navigation.findNavController(view).navigate(R.id.action_markLocationFragment_to_savedLocationFragment);
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

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;

        LatLng markerPosition = new LatLng(currentLocation.getLatitude(),currentLocation.getLongitude());

        marker = map.addMarker(new MarkerOptions()
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
                marker.setTitle(getAddress(marker.getPosition()));
                setCameraPosition(marker.getPosition());
            }
        });
    }
    private void setCameraPosition(LatLng position){
        CameraPosition cameraPosition = new CameraPosition.Builder().target(position).zoom(17).build();
        map.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
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
