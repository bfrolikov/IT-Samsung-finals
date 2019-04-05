package com.example.bfrol.it_samsung_finals;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class UserProfileFragment extends Fragment {
    private UserSignOutListener signOutCallback;//interface to tell the activity to close when the user has logged out
    @Nullable
    @Override
    //this fragment is responsible for the user profile
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View inflatedView = inflater.inflate(R.layout.fragment_user_profile, container, false);
        updateUI(inflatedView);
        return inflatedView;
    }
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof Activity) {
            Activity attachActivity = (Activity) context;
            signOutCallback = (UserSignOutListener) attachActivity;
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Button signOutButton = view.findViewById(R.id.prof_sign_out_button);
        signOutButton.setOnClickListener(v ->
        {
            signOutCallback.onUserSignOut();//user has signed out, signal the activity
        });
    }

    public interface UserSignOutListener {
        void onUserSignOut();
    }

    private void updateUI(View uiView) {
        ((TextView)uiView.findViewById(R.id.prof_username_label)).setText(MainActivity.currentUser.getFirstName()+" "+MainActivity.currentUser.getLastName());
        ((TextView)uiView.findViewById(R.id.prof_city_label)).setText(MainActivity.currentUser.getCity());
        ((EditText)uiView.findViewById(R.id.prof_city)).setText(MainActivity.currentUser.getCity());
        ((EditText)uiView.findViewById(R.id.prof_social_media)).setText(MainActivity.currentUser.getSocialMediaLink());
        ((EditText)uiView.findViewById(R.id.prof_country)).setText(MainActivity.currentUser.getCountry());
        ((EditText)uiView.findViewById(R.id.prof_demands)).setText(MainActivity.currentUser.getDemands());
    }
}
