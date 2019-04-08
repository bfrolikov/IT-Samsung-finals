package com.example.bfrol.it_samsung_finals;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.UriMatcher;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.File;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements UserProfileFragment.ProfileFragmentInterface {
    static final int MENU_WITH_SEARCH = 0;
    static final int MENU_WITHOUT_SEARCH = 1;
    static final int CAMERA_REQUEST_CODE = 0;
    private Toolbar toolbar;
    private Menu menu;
    static CustomRecyclerViewAdapter adapter;
    static User currentUser; //a static field for the user class stored in cache
    FirebaseAuth firebaseAuth;
    FirebaseFirestore database;
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            //here the bottom navigation events are handled and the fragments in the main_fragment_space are replaced
            switch (item.getItemId()) {
                case R.id.navigation_profile:
                    //the user profile menu was opened so an instance of UserProfileFragment is inflated and inserted into the UI
                    //in case the user is stored in cache
                    if(currentUser!=null)
                        constructFragment(new UserProfileFragment());
                    else
                    {
                        constructFragment(new ProgressBarFragment());
                        DocumentReference docRef = database.collection("users").document(firebaseAuth.getCurrentUser().getUid());
                        docRef.get().addOnSuccessListener(documentSnapshot -> {
                            currentUser = documentSnapshot.toObject(User.class);
                            constructFragment(new UserProfileFragment());
                        });
                    }
                    updateMenu(MENU_WITHOUT_SEARCH);
                    return true;
                case R.id.navigation_messages:
                    updateMenu(MENU_WITHOUT_SEARCH);
                    return true;
                case R.id.navigation_search:
                    //the search menu was opened so an instance of SearchFragment is inflated and inserted into the UI
                    constructFragment(new SearchFragment());
                    updateMenu(MENU_WITH_SEARCH);
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        database = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        adapter = new CustomRecyclerViewAdapter(new ArrayList<>());

    }
    private int checkForPermission(String permission)
    {
        return ContextCompat.checkSelfPermission(this, permission);
    }
    private void requestRuntimePermission(String permission,int requestCode) {
    /*    if(ActivityCompat.shouldShowRequestPermissionRationale(this,permission))
        {
            Toast.makeText(this,explanation,Toast.LENGTH_LONG).show();
        }*/
        ActivityCompat.requestPermissions(this,new String[]{permission},requestCode);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode)
        {
            case CAMERA_REQUEST_CODE:
            {
              if(grantResults.length==0 || grantResults[0]==PackageManager.PERMISSION_DENIED)
              {
                  if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.CAMERA))
                      Toast.makeText(this,getString(R.string.camera_permission_explanation),Toast.LENGTH_LONG).show();
                  else
                      Toast.makeText(this,getString(R.string.camera_permission_denied_explanation),Toast.LENGTH_SHORT).show();
              }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        updateMenu(MENU_WITH_SEARCH);
        return true;
    }
    private void updateMenu(final int type)
    {
        //the app is meant to have dynamically changing menu so this function configures different menu
        //types to use in the application. Called on startup in onCreateOptionsMenu and when the bottom navigation item is clicked
        if(menu == null) return;
        MenuInflater menuInflater = getMenuInflater();
        menu.clear();
        if(type == MENU_WITH_SEARCH)
        {
            menuInflater.inflate(R.menu.toolbar_menu_with_search,menu);
            SearchView menuSearch = (SearchView) menu.findItem(R.id.menu_search).getActionView();
            menuSearch.setIconifiedByDefault(false);
            menuSearch.setQueryHint(getResources().getString(R.string.search_hint));
            menuSearch.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String s) {
                    hideKeyboard();
                    //the search goes here
                    constructFragment(new ProgressBarFragment());
                    database.collection("users").whereEqualTo("firstName",s)
                            .get()
                            .addOnSuccessListener(querySnapshot -> {
                                adapter.setDataArray((ArrayList<DocumentSnapshot>)querySnapshot.getDocuments());
                                adapter.notifyDataSetChanged();
                                constructFragment(new SearchFragment());
                            }); //TODO fail handling
                    return true;
                }

                @Override
                public boolean onQueryTextChange(String s) {
                    return true;
                }
            });
        }
        else if(type == MENU_WITHOUT_SEARCH)
        {
            menuInflater.inflate(R.menu.toolbar_menu_without_search,menu);
            toolbar.setTitle(" ");
        }
    }
    public void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }

    }

    @Override
    public void onUserSignOut() {
        //user signed out in the fragment
        FirebaseAuth.getInstance().signOut();
        Intent openLoginActivity = new Intent(this,LoginActivity.class);
        openLoginActivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);// clear activity history
        startActivity(openLoginActivity);
        finish();
    }

    @Override
    public void onCameraOpened() {
        if (checkForPermission(Manifest.permission.CAMERA)==PackageManager.PERMISSION_DENIED)
        {
            requestRuntimePermission(Manifest.permission.CAMERA,CAMERA_REQUEST_CODE);
            return;
            //if the user hasn't given the permission to use the camera
        }
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File file = new File(Environment.getExternalStorageDirectory(),"file"+String.valueOf(System.currentTimeMillis())+".jpg");
        Uri uri = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID+".fileprovider",file);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT,uri);
        cameraIntent.putExtra("return-data",true);
        cameraIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivityForResult(cameraIntent,CAMERA_REQUEST_CODE);
    }

    @Override
    public void onGalleryOpened() {

    }

    void constructFragment(Fragment fragment)
    {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragment_area,fragment);
        fragmentTransaction.commit();
        //helps construct the fragment
    }
}