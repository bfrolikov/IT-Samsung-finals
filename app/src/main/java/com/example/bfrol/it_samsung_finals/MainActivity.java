package com.example.bfrol.it_samsung_finals;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.UriMatcher;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.functions.FirebaseFunctions;
import com.google.firebase.functions.HttpsCallableResult;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity implements UserProfileFragment.ProfileFragmentInterface {
    static final int MENU_WITH_SEARCH = 0;
    static final int MENU_WITHOUT_SEARCH = 1;
    static final int CAMERA_REQUEST_CODE = 0;
    static final int GALLERY_REQEST_CODE = 1;
    static final String USER_PROFILE_FRAGMENT_TAG = "userpf";
    static final String LOADED_USER_TAG = "loadeduser";
    static final String USERS_ARRAY_TAG = "usersarray";
    private Toolbar toolbar;
    private Menu menu;
    private Uri imageUri;
    private BottomNavigationView navigation;
    static Drawable userImage;
    static CustomRecyclerViewAdapter adapter; //adapter for the search fragment
    static User currentUser; //a static field for the user class stored in cache
    FirebaseAuth firebaseAuth;
    FirebaseFirestore database;
    FirebaseFunctions firebaseFunctions;
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            //here the bottom navigation events are handled and the fragments in the main_fragment_space are replaced
            switch (item.getItemId()) {
                case R.id.navigation_profile:
                    //the user profile menu was opened so an instance of UserProfileFragment is inflated and inserted into the UI
                    //in case the user is stored in cache
                    if (currentUser != null)
                        constructFragment(new UserProfileFragment());
                    updateMenu(MENU_WITHOUT_SEARCH);
                    return true;
                case R.id.navigation_messages:
                    if (currentUser != null)
                        constructFragment(new MessageFragment());
                    updateMenu(MENU_WITHOUT_SEARCH);
                    return true;
                case R.id.navigation_search:
                    //the search menu was opened so an instance of SearchFragment is inflated and inserted into the UI
                    if (currentUser != null)
                        constructFragment(new SearchFragment());
                    updateMenu(MENU_WITH_SEARCH);
                    return true;
                case R.id.navigation_excursions:
                    if(currentUser!=null)
                        constructFragment(new ExcursionsFragment());
                    updateMenu(MENU_WITHOUT_SEARCH);
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        database = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFunctions = FirebaseFunctions.getInstance();
        navigation = findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        constructFragment(new ProgressBarFragment());
        DocumentReference docRef = database.collection("users").document(firebaseAuth.getCurrentUser().getUid());
        docRef.get().addOnSuccessListener(documentSnapshot -> {
            currentUser = documentSnapshot.toObject(User.class);
            int id = navigation.getSelectedItemId();
            switch (id)
            {
                case R.id.navigation_profile:
                    constructFragment(new UserProfileFragment());
                    Log.v("navid","prof");
                    break;
                case R.id.navigation_messages:
                    constructFragment(new MessageFragment());
                    Log.v("navid","msg");
                    break;
                case R.id.navigation_search:
                    constructFragment(new SearchFragment());
                    Log.v("navid","srch");
                    break;
                case R.id.navigation_excursions:
                    constructFragment(new ExcursionsFragment());
                    Log.v("navid","exc");
                    break;
            }
        });
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        adapter = new CustomRecyclerViewAdapter(new ArrayList<>());
        userImage = getDrawable(R.drawable.user_placeholder);
        loadProfileImageFromCloud();

    }

    private int checkForPermission(String permission) {
        return ContextCompat.checkSelfPermission(this, permission);
    }

    private void requestRuntimePermission(String permission, int requestCode) {
    /*    if(ActivityCompat.shouldShowRequestPermissionRationale(this,permission))
        {
            Toast.makeText(this,explanation,Toast.LENGTH_LONG).show();
        }*/
        ActivityCompat.requestPermissions(this, new String[]{permission}, requestCode);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case CAMERA_REQUEST_CODE: {
                if (grantResults.length == 0 || grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA))
                        Toast.makeText(this, getString(R.string.camera_permission_explanation), Toast.LENGTH_LONG).show();
                    else
                        Toast.makeText(this, getString(R.string.camera_permission_denied_explanation), Toast.LENGTH_SHORT).show();
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

    private void updateMenu(final int type) {
        //the app is meant to have dynamically changing menu so this function configures different menu
        //types to use in the application. Called on startup in onCreateOptionsMenu and when the bottom navigation item is clicked
        if (menu == null) return;
        MenuInflater menuInflater = getMenuInflater();
        menu.clear();
        if (type == MENU_WITH_SEARCH) {
            menuInflater.inflate(R.menu.toolbar_menu_with_search, menu);
            SearchView menuSearch = (SearchView) menu.findItem(R.id.menu_search).getActionView();
            menuSearch.setIconifiedByDefault(false);
            menuSearch.setQueryHint(getResources().getString(R.string.search_hint));
            menuSearch.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String s) {
                    hideKeyboard();
                    final String searchRequest = s;
                    //the search goes here
                    constructFragment(new ProgressBarFragment());
                   /* database.collection("users").whereGreaterThanOrEqualTo("firstName",s)
                            .get()
                            .addOnSuccessListener(querySnapshot -> {
                                adapter.setDataArray((ArrayList<DocumentSnapshot>)querySnapshot.getDocuments());
                                adapter.notifyDataSetChanged();
                                constructFragment(new SearchFragment());
                            }); //TODO fail handling
                            */
                    HttpHelper httpHelper = new HttpHelper();
                    try {
                        httpHelper.run(s);
                    } catch (Exception e) {
                        Log.e("Error:",e.getLocalizedMessage());
                    }
                    return true;
                }

                @Override
                public boolean onQueryTextChange(String s) {
                    return true;
                }
            });
        } else if (type == MENU_WITHOUT_SEARCH) {
            menuInflater.inflate(R.menu.toolbar_menu_without_search, menu);
            toolbar.setTitle(" ");
        }
    }

    public void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }

    }

    @Override
    public void onUserSignOut() {
        //user signed out in the fragment
        FirebaseAuth.getInstance().signOut();
        Intent openLoginActivity = new Intent(this, LoginActivity.class);
        openLoginActivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);// clear activity history
        startActivity(openLoginActivity);
        finish();
    }

    @Override
    public void onCameraOpened() {
        if (checkForPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED) {
            requestRuntimePermission(Manifest.permission.CAMERA, CAMERA_REQUEST_CODE);
            return;
            //if the user hasn't given the permission to use the camera
        }
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File file = new File(getExternalCacheDir(), "file" + String.valueOf(System.currentTimeMillis()) + ".jpg");
        imageUri = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".fileprovider", file);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        // cameraIntent.putExtra("return-data",true);
        cameraIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        cameraIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);
    }
    @Override
    public void onGalleryOpened() {
        Intent openGalleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        openGalleryIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivityForResult(Intent.createChooser(openGalleryIntent, getString(R.string.select_from_gallery)), GALLERY_REQEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK) {
            //open cropper
            CropImage.activity(imageUri).setFixAspectRatio(true).setAspectRatio(1, 1).
                    setMaxCropResultSize(2500, 2500).
                    setCropShape(CropImageView.CropShape.OVAL).start(this);
        } else if (requestCode == GALLERY_REQEST_CODE && data != null) {
            imageUri = data.getData();
            //open cropper
            CropImage.activity(imageUri).setFixAspectRatio(true).setAspectRatio(1, 1).
                    setMaxCropResultSize(2500, 2500).
                    setCropShape(CropImageView.CropShape.OVAL).start(this);
        } else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK && data != null) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            imageUri = result.getUri();
            try {
                InputStream inputStream = getContentResolver().openInputStream(imageUri);
                userImage = Drawable.createFromStream(inputStream, imageUri.toString());
                uploadProfileImageToCloud(imageUri);
            } catch (FileNotFoundException e) {
                userImage = getResources().getDrawable(R.drawable.user_placeholder);
            }
            updateImageInFragment();
        } else {
            Toast.makeText(this, getString(R.string.error), Toast.LENGTH_SHORT).show();
        }
    }

    void constructFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        if (fragment instanceof UserProfileFragment)
            fragmentTransaction.replace(R.id.fragment_area, fragment, USER_PROFILE_FRAGMENT_TAG);
        else
            fragmentTransaction.replace(R.id.fragment_area, fragment);
        fragmentTransaction.commit();
        //helps construct the fragment
    }

    private void loadProfileImageFromCloud() {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageReference = storage.getReference("images/" + firebaseAuth.getCurrentUser().getUid() + ".jpg");
        try {
            File buffer = File.createTempFile("images", "jpg");
            storageReference.getFile(buffer).
                    addOnSuccessListener(taskSnapshot -> {
                        userImage = Drawable.createFromPath(buffer.getAbsolutePath());
                        updateImageInFragment();
                        buffer.deleteOnExit();
                    }).
                    addOnFailureListener(e -> {
                    });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void uploadProfileImageToCloud(Uri fileUri) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageReference = storage.getReference("images/" + firebaseAuth.getCurrentUser().getUid() + ".jpg");
        storageReference.putFile(fileUri).
                addOnSuccessListener(taskSnapshot -> {
                }).
                addOnFailureListener(e -> {
                });
    }

    private void updateImageInFragment() {
        UserProfileFragment fragment = (UserProfileFragment) getSupportFragmentManager().findFragmentByTag(USER_PROFILE_FRAGMENT_TAG);
        if (fragment != null)
            fragment.changeImage();
    }
    class HttpHelper //this helps to execute the cloud function for finding users in the database
    {
        private final OkHttpClient client = new OkHttpClient();
        public void run(String s) throws Exception
        {
            String url = "https://us-central1-xsharing-c7dd4.cloudfunctions.net/findUsers?text="+s;
            Request request = new Request.Builder().url(url).build();
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    Log.e("Error:",e.getLocalizedMessage());
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);
                    Type listType = new TypeToken<List<User>>(){}.getType();
                    Gson gson = new Gson();
                    ArrayList<User> newUsers = gson.fromJson(response.body().string(),listType);
                    adapter.setDataArray(newUsers);
                    adapter.notifyDataSetChanged();
                    if(navigation.getSelectedItemId()==R.id.navigation_search)
                        constructFragment(new SearchFragment());
                }
            });
        }
    }

}


//recycler view adapter for the search items
class CustomRecyclerViewAdapter extends RecyclerView.Adapter<CustomRecyclerViewAdapter.CustomViewHolder> {
    private ArrayList<User> dataArray;

    public CustomRecyclerViewAdapter(ArrayList<User> dataArray) {
        this.dataArray = dataArray;
    }

    @NonNull
    @Override
    public CustomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_item_with_rating, parent, false);
        return new CustomViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull CustomViewHolder customViewHolder, int position) {
        User loadedUser = dataArray.get(position);
        customViewHolder.itemView.setOnClickListener(caller -> {
            Intent openSelectedUserProfileActivity = new Intent(customViewHolder.usrDemands.getContext(), SelectedUserProfileActivity.class);
            Bundle bundle = new Bundle();
            bundle.putSerializable(MainActivity.LOADED_USER_TAG, loadedUser);
            openSelectedUserProfileActivity.putExtras(bundle);
            customViewHolder.itemView.getContext().startActivity(openSelectedUserProfileActivity);
        });
        customViewHolder.usrDemands.setText(loadedUser.getDemands());
        customViewHolder.usrUsername.setText(loadedUser.getFirstName() + " " + loadedUser.getLastName());
        customViewHolder.usrRating.setRating(loadedUser.getRating());
        customViewHolder.usrProfileImage.setImageDrawable(customViewHolder.usrProfileImage.getContext().getDrawable(R.drawable.user_placeholder));
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference reference = storage.getReference("images/" + loadedUser.getuID() + ".jpg");
        GlideApp.with(customViewHolder.usrProfileImage).load(reference).diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true).into(customViewHolder.usrProfileImage);//TODO caching!
    }

    @Override
    public int getItemCount() {
        return dataArray.size();
    }


    class CustomViewHolder extends RecyclerView.ViewHolder {
        TextView usrUsername, usrDemands;
        RatingBar usrRating;
        ImageView usrProfileImage;

        CustomViewHolder(@NonNull View itemView) {
            super(itemView);
            usrUsername = itemView.findViewById(R.id.usr_username);
            usrDemands = itemView.findViewById(R.id.usr_demands);
            usrRating = itemView.findViewById(R.id.usr_rating);
            usrProfileImage = itemView.findViewById(R.id.usr_profile_image);
        }
    }

    void setDataArray(ArrayList<User> dataArray) {
        this.dataArray = dataArray;
    }
}
