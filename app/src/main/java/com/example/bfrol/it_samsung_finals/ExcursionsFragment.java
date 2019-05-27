package com.example.bfrol.it_samsung_finals;


import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ExcursionsFragment extends Fragment {

    private RecyclerView excursionsRecycler;
    private ExcursionsAdapter adapter;
    public ExcursionsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v =  inflater.inflate(R.layout.fragment_excursions, container, false);
        View.OnLongClickListener listener = (caller) -> {
            HashMap<String,Object> clickedItem = (HashMap<String, Object>) caller.getTag();
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle(R.string.delete_q)
                    .setNegativeButton(R.string.cancel,((dialog, which) -> {}))
                    .setPositiveButton(R.string.delete,(dialog, which) -> {
                        int index = MainActivity.currentUser.getCurrentExcursions().indexOf(clickedItem);
                        MainActivity.currentUser.getCurrentExcursions().remove(index);
                        adapter.notifyItemRemoved(index);
                        FirebaseFirestore.getInstance()
                                .collection("users")
                                .document(MainActivity.currentUser.getuID())
                                .set(MainActivity.currentUser)
                                .addOnSuccessListener(aVoid -> {})
                                .addOnFailureListener(e -> {
                                    Toast.makeText(getContext(),e.getLocalizedMessage(),Toast.LENGTH_SHORT).show();
                                });
                    })
                    .create().show();

            return false;
        };
        adapter = new ExcursionsAdapter(MainActivity.currentUser.getCurrentExcursions(),listener);
        excursionsRecycler = v.findViewById(R.id.excursions_recycler);
        excursionsRecycler.setHasFixedSize(false);
        excursionsRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        excursionsRecycler.setAdapter(adapter);
        excursionsRecycler.setRecyclerListener(viewHolder -> {
            ExcursionsAdapter.ExcursionViewHolder holder = (ExcursionsAdapter.ExcursionViewHolder) viewHolder;
            if(holder.gmap!=null)
            {
                holder.gmap.clear();
                holder.gmap.setMapType(GoogleMap.MAP_TYPE_NONE);
            }
        });
        return v;
    }
    private BitmapDescriptor bitmapDescriptorFromVector(Context context, int vectorResId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }
    class ExcursionsAdapter extends RecyclerView.Adapter<ExcursionsAdapter.ExcursionViewHolder> {
        private ArrayList<HashMap<String,Object>> excursions;
        View.OnLongClickListener onLongClickListener;
        public ExcursionsAdapter(ArrayList<HashMap<String, Object>> excursions,View.OnLongClickListener onLongClickListener) {
            this.excursions = excursions;
            this.onLongClickListener = onLongClickListener;
        }

        @NonNull
        @Override
        public ExcursionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.excursion_item,parent,false);
            return new  ExcursionViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ExcursionViewHolder excursionViewHolder, int position) {
            excursionViewHolder.bind(excursions.get(position));
        }

        @Override
        public int getItemCount() {
            return excursions.size();
        }
        class ExcursionViewHolder extends RecyclerView.ViewHolder implements OnMapReadyCallback {
            MapView excursionItemMap;
            GoogleMap gmap;
            TextView excursionItemUsername,excursionItemCity;
            CircleImageView excursionItemUserImage;
            View layout;
            ExcursionViewHolder(@NonNull View itemView) {
                super(itemView);
                layout = itemView;
                excursionItemMap = itemView.findViewById(R.id.excursion_item_map);
                excursionItemUsername = itemView.findViewById(R.id.excursion_item_username);
                excursionItemCity = itemView.findViewById(R.id.excursion_item_city);
                excursionItemUserImage = itemView.findViewById(R.id.excursion_item_user_image);
                excursionItemMap.onCreate(null);
                excursionItemMap.getMapAsync(this);
            }
            void bind(HashMap<String,Object> excursion)
            {
                excursionItemUsername.setText((String)excursion.get("name"));
                excursionItemCity.setText((String)excursion.get("location"));
                excursionItemMap.setTag(excursion.get("points"));
                layout.setTag(excursion);
                layout.setOnLongClickListener(onLongClickListener);
                StorageReference reference = FirebaseStorage.getInstance().getReference("images/" + excursion.get("uID") + ".jpg");
                GlideApp.with(excursionItemUserImage).load(reference).diskCacheStrategy(DiskCacheStrategy.NONE)
                        .skipMemoryCache(true).into(excursionItemUserImage);//TODO caching!
                setMapRoute();
            }
            void setMapRoute()
            {
                if (gmap==null) return;

                ArrayList<GeoPoint> route = (ArrayList<GeoPoint>) excursionItemMap.getTag();
                if(route==null) return;
                if(!route.isEmpty())
                {
                    for (int i = 0; i < route.size(); i++) {
                        GeoPoint point = route.get(i);
                        MarkerOptions markerOptions = new MarkerOptions();
                        markerOptions.position(new LatLng(point.getLatitude(), point.getLongitude()));
                        if (i == 0)
                            markerOptions.icon(bitmapDescriptorFromVector(getContext(), R.drawable.ic_start_marker));
                        else if (i == route.size() - 1 && route.size() != 1)
                            markerOptions.icon(bitmapDescriptorFromVector(getContext(), R.drawable.ic_finish_marker));
                        gmap.addMarker(markerOptions);
                        gmap.addMarker(markerOptions);
                    }
                    LatLng firstPoint = new LatLng(route.get(0).getLatitude(),route.get(0).getLongitude());
                    CameraPosition.Builder builder = CameraPosition.builder();
                    builder.zoom((float) 8.5);
                    builder.target(firstPoint);
                    gmap.moveCamera(CameraUpdateFactory.newCameraPosition(builder.build()));
                }
                gmap.setOnMapClickListener(latLng -> {});
                gmap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

            }
            @Override
            public void onMapReady(GoogleMap googleMap) {
                MapsInitializer.initialize(excursionItemMap.getContext());
                gmap = googleMap;
                setMapRoute();
            }
        }
    }
}
