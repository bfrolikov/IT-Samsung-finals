package com.example.bfrol.it_samsung_finals;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

public class SearchFragment extends Fragment {
    RecyclerView srchRecyclerView;
    //this fragment will be handling search for users
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v =  inflater.inflate(R.layout.fragment_search,container,false);
        srchRecyclerView = v.findViewById(R.id.srch_recycler_view);
        srchRecyclerView.setAdapter(MainActivity.adapter);
        srchRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        return v;
    }
}
