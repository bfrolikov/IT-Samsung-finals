package com.example.bfrol.it_samsung_finals;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class SelectedUserProfileActivity extends AppCompatActivity {
    public static final String USER_KEY = "UserKey";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selected_user_profile);
        Intent callerIntent = getIntent();
        User currentUser = (User)callerIntent.getExtras().getSerializable(MainActivity.LOADED_USER_TAG);
        fillUI(currentUser);
    }
    private void fillUI(User user)
    {
        CircleImageView selUserImage = findViewById(R.id.sel_user_image);
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference imageReference = storage.getReference("images/"+user.getuID()+".jpg");
        GlideApp.with(selUserImage).load(imageReference).diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true).into(selUserImage);//TODO caching!
        ((TextView)findViewById(R.id.sel_user_name)).setText(user.getFirstName()+" "+user.getLastName());
        ((TextView)findViewById(R.id.sel_user_demands_text)).setText(user.getDemands());
        ((TextView)findViewById(R.id.sel_user_location)).setText(getResources().getString(R.string.location)+" "+user.getCity()+", "+user.getCountry());
        Button selUserContactButton = findViewById(R.id.sel_user_contact_button);
        selUserContactButton.setOnClickListener(caller->{
            FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
            if (!firebaseUser.getUid().equals(user.getuID()))
            {
                Intent openChatActivity = new Intent(this,ChatActivity.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable(USER_KEY,user);
                openChatActivity.putExtras(bundle);
                startActivity(openChatActivity);
            }
        });
    }
}
