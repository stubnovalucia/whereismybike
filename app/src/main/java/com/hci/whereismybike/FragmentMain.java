package com.hci.whereismybike;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.Navigation;

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
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.util.Map;

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

        Button markLocationButton = view.findViewById(R.id.markLocationButton);

        //Conditional navigation
        if (sharedViewModel.getSavedBike().getValue()) {
            markLocationButton.setText(getResources().getString(R.string.show_it_button));
        } else {
            markLocationButton.setText(getResources().getString(R.string.mark_it_button));
        }

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
}
