package com.example.bfrol.it_samsung_finals;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;

public class UserProfileFragment extends Fragment {
    private UserSignOutListener signOutCallback;
    @Nullable
    @Override
    //this fragment is responsible for the user profile
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_user_profile,container,false);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if(context instanceof Activity)
        {
            Activity attachActivity = (Activity)context;
            signOutCallback = (UserSignOutListener) attachActivity;
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Button signOutButton = view.findViewById(R.id.prof_sign_out_button);
        signOutButton.setOnClickListener(v->
        {
            signOutCallback.onUserSignOut();
        });
    }
    public interface UserSignOutListener
    {
        void onUserSignOut();
    }
}
