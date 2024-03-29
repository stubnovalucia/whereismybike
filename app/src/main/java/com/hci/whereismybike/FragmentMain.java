package com.hci.whereismybike;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.Navigation;

import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.File;

/**
 * FragmentMain class: Initial application element that will be shown at start up.
 *
 * @author Lucia Stubnova
 *
 * Lucia Stubnova: Main author
 *
 */
public class FragmentMain extends Fragment {

    private OnFragmentInteractionListener mListener;
    private SharedViewModel sharedViewModel;

    public FragmentMain() {
        // Required empty public constructor
    }

    //Firebase
    private DatabaseReference mDatabaseRef;
    private String userID;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment FragmentMain.
     */
    public static FragmentMain fragmentMain() {
        FragmentMain fragment = new FragmentMain();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedViewModel = ViewModelProviders.of(getActivity()).get(SharedViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_fragment_main, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final Button markLocationButton = view.findViewById(R.id.markLocationButton);

        //Firebase
        FirebaseAuth auth = FirebaseAuth.getInstance();
        //get the signed in user
        FirebaseUser user = auth.getCurrentUser();
        userID = user.getUid();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference().child("users");

        mDatabaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(!dataSnapshot.hasChild(userID)){
                    sharedViewModel.setSavedBike(false);
                    markLocationButton.setText(getResources().getString(R.string.mark_it_button));
                    sharedViewModel.setAddress("");
                    sharedViewModel.setDateandtime("");
                    sharedViewModel.setNote("");
                    sharedViewModel.setBikePictureTaken("");
                    sharedViewModel.setBikePicture(null);
                } else {
                    sharedViewModel.setSavedBike(true);
                    markLocationButton.setText(getResources().getString(R.string.show_it_button));
                    GetData();
                }
                markLocationButton.setVisibility(View.VISIBLE);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        //listener for settings icon
        ImageView settingsCog = view.findViewById(R.id.settingsCog);
        settingsCog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Navigation.findNavController(view).navigate(R.id.action_fragmentMain_to_fragmentSettings);
            }
        });

        //listener for mark/show location button
        markLocationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (sharedViewModel.getSavedBike().getValue()) {
                    Navigation.findNavController(view).navigate(R.id.action_fragmentMain_to_fragmentRetrieveLocation);
                } else {
                    Navigation.findNavController(view).navigate(R.id.action_fragmentMain_to_markLocationFragment);
                }
            }
        });

        //if signed in, sets the user in sharedview
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        if(sharedViewModel.getmGoogleSignInClient() == null){
            sharedViewModel.setmGoogleSignInClient(GoogleSignIn.getClient(getActivity(), gso));
        }

        //Disable the back button
        //https://stackoverflow.com/a/36129029
        view.setFocusableInTouchMode(true);
        view.requestFocus();
        view.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (keyCode == KeyEvent.KEYCODE_BACK) {

                        return true;
                    }
                }
                return false;
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

    private void GetData(){
        mDatabaseRef = FirebaseDatabase.getInstance().getReference().child("users").child(userID);
        mDatabaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                sharedViewModel.setAddress(dataSnapshot.child("address").getValue(String.class));
                sharedViewModel.setDateandtime(dataSnapshot.child("date").getValue(String.class));
                sharedViewModel.setNote(dataSnapshot.child("note").getValue(String.class));
                sharedViewModel.setBikePictureTaken(dataSnapshot.child("picture").getValue(String.class));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }
}
