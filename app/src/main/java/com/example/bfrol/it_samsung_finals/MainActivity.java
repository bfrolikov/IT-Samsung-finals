package com.example.bfrol.it_samsung_finals;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    public static final int MENU_WITH_SEARCH = 0;
    public static final int MENU_WITHOUT_SEARCH = 1;
    private Toolbar toolbar;
    private Menu menu;
    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            //here the bottom navigation events are handled and the fragments in the main_fragment_space are replaced
            switch (item.getItemId()) {
                case R.id.navigation_profile:
                    //the user profile menu was opened so an instance of UserProfileFragment is inflated and inserted into the UI
                    UserProfileFragment userProfileFragment = new UserProfileFragment();
                    FragmentTransaction userProfileFragmentTransaction = getSupportFragmentManager().beginTransaction();
                    userProfileFragmentTransaction.replace(R.id.fragment_area,userProfileFragment);
                    userProfileFragmentTransaction.commit();
                    updateMenu(MENU_WITHOUT_SEARCH);
                    return true;
                case R.id.navigation_messages:
                    updateMenu(MENU_WITHOUT_SEARCH);
                    return true;
                case R.id.navigation_search:
                    //the search menu was opened so an instance of SearchFragment is inflated and inserted into the UI
                    SearchFragment searchFragment = new SearchFragment();
                    FragmentTransaction searchFragmentTransaction = getSupportFragmentManager().beginTransaction();
                    searchFragmentTransaction.replace(R.id.fragment_area,searchFragment);
                    updateMenu(MENU_WITH_SEARCH);
                    searchFragmentTransaction.commit();
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
                    Toast.makeText(getApplicationContext(),s,Toast.LENGTH_LONG).show();
                    hideKeyboard();
                    //the search goes here
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
        }
    }
    public void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}