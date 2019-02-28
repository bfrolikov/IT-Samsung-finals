package com.example.bfrol.it_samsung_finals;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

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
                    //the user profile menu was opened
                    UserProfileFragment fragment = new UserProfileFragment();
                    FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                    fragmentTransaction.replace(R.id.fragment_area,fragment);
                    fragmentTransaction.commit();
                    updateMenu(MENU_WITHOUT_SEARCH);
                    return true;
                case R.id.navigation_messages:
                    updateMenu(MENU_WITHOUT_SEARCH);
                    return true;
                case R.id.navigation_search:
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
        toolbar.setTitle("Test");
        setSupportActionBar(toolbar);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.toolbar_menu_without_search,menu);
        this.menu = menu;
        return true;
    }
    private void updateMenu(int type)
    {
        //the app is meant to have dynamically change
        if(menu == null) return;
        MenuInflater menuInflater = getMenuInflater();
        menu.clear();
        if(type == MENU_WITH_SEARCH)
        {
            menuInflater.inflate(R.menu.toolbar_menu_with_search,menu);
        }
        else if(type == MENU_WITHOUT_SEARCH)
        {
            menuInflater.inflate(R.menu.toolbar_menu_without_search,menu);
        }
    }
}
