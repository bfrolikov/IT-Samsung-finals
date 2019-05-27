package com.example.bfrol.it_samsung_finals;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.provider.CalendarContract;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v7.widget.Toolbar;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

public class ChatActivity extends AppCompatActivity{
    private RecyclerView chatReycler;
    private ChatRecyclerViewAdapter adapter;
    private EditText editTextChatBox;
    private ImageButton buttonChatBoxSend;
    private ImageButton routeButton;
    private Toolbar chatToolbar;
    private CollectionReference activeCollection;
    private User user;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        chatToolbar = findViewById(R.id.chat_toolbar);

        chatReycler = findViewById(R.id.chat_recycler_view);
        editTextChatBox = findViewById(R.id.edittext_chatbox);
        buttonChatBoxSend = findViewById(R.id.button_chatbox_send);
        routeButton = findViewById(R.id.route_button);
        activeCollection = null;
        Bundle bundle = getIntent().getExtras();
        user = (User) bundle.getSerializable(SelectedUserProfileActivity.USER_KEY);
        chatToolbar.setTitle(user.getFirstName()+" "+user.getLastName());
        setSupportActionBar(chatToolbar);
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        adapter = new ChatRecyclerViewAdapter(new ArrayList<>(), firebaseUser);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setStackFromEnd(true);
        chatReycler.setLayoutManager(manager);
        chatReycler.setAdapter(adapter);
        chatReycler.setHasFixedSize(false);
        chatReycler.setRecyclerListener(viewHolder -> {
            if(viewHolder.getItemViewType()==ChatRecyclerViewAdapter.VIEW_TYPE_ROUTE_RECEIVED)
            {
                ChatRecyclerViewAdapter.ReceivedRouteViewHolder holder = (ChatRecyclerViewAdapter.ReceivedRouteViewHolder) viewHolder;
                if(holder.map!=null)
                {

                    holder.map.clear();
                    holder.map.setMapType(GoogleMap.MAP_TYPE_NONE);
                  //  holder.receivedMap.onPause();
                }
            }
            else if (viewHolder.getItemViewType()==ChatRecyclerViewAdapter.VIEW_TYPE_ROUTE_SENT)
            {
                ChatRecyclerViewAdapter.SentRouteViewHolder holder = (ChatRecyclerViewAdapter.SentRouteViewHolder) viewHolder;
                if(holder.map!=null)
                {
                    holder.map.clear();
                    holder.map.setMapType(GoogleMap.MAP_TYPE_NONE);
                    //holder.sentMap.onPause();
                }

            }
        });
        editTextChatBox.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus && adapter.getItemCount()!=0) {
                Log.v("testing","yes");
                chatReycler.scrollToPosition(adapter.getItemCount() - 1);
            }
        });
        FirebaseFirestore firestore = FirebaseFirestore.getInstance();
        Query getRoom1 = firestore.collection("rooms").whereEqualTo("user1",firebaseUser.getUid()).whereEqualTo("user2",user.getuID());
        Query getRoom2 = firestore.collection("rooms").whereEqualTo("user2",firebaseUser.getUid()).whereEqualTo("user1",user.getuID());
        getRoom1.get().addOnSuccessListener(querySnapshot -> {
            if (querySnapshot.getDocuments().size()==0)
            {
                getRoom2.get().addOnSuccessListener(querySnapshot1 -> {
                    if (querySnapshot1.getDocuments().size()==0)
                    {
                        //no chatrooms found, create new
                        Map<String,Object> newChatRoom = new HashMap<>();
                        newChatRoom.put("user1",firebaseUser.getUid());
                        newChatRoom.put("user2",user.getuID());
                        firestore.collection("rooms").add(newChatRoom).addOnSuccessListener(documentReference -> {
                            activeCollection = documentReference.collection("messages");
                            activeCollection.
                                    orderBy("time", Query.Direction.DESCENDING).
                                    limit(1).
                                    addSnapshotListener((querySnapshot3, e) ->{
                                        if(querySnapshot3!=null)
                                        {
                                            DocumentSnapshot newMessage = querySnapshot3.getDocuments().get(0);
                                            adapter.addMessage(newMessage);
                                            adapter.notifyItemInserted(adapter.getItemCount());
                                            chatReycler.scrollToPosition(adapter.getItemCount()-1);
                                        }
                                    });
                            Message firstMessage = new Message(firebaseUser.getUid(),user.getuID(),"Hello, i'm interested",new Date(), "", new ArrayList<>());//generate automatic message
                            activeCollection.add(firstMessage).addOnSuccessListener(documentReference1 -> {
                                //add new message to the collection
                            });
                        });
                    }
                    else
                    {
                        //found a room
                        DocumentSnapshot documentSnapshot= querySnapshot1.getDocuments().get(0);
                        activeCollection = documentSnapshot.getReference().collection("messages");
                        activeCollection.orderBy("time", Query.Direction.ASCENDING).get().addOnSuccessListener(querySnapshot2 ->
                        {
                            ArrayList<DocumentSnapshot> messages = (ArrayList<DocumentSnapshot>) querySnapshot2.getDocuments();
                            adapter.setMessages(messages);
                            adapter.notifyDataSetChanged();
                            chatReycler.scrollToPosition(adapter.getItemCount()-1);
                        });
                        activeCollection.
                                orderBy("time", Query.Direction.DESCENDING).
                                limit(1).
                                addSnapshotListener((querySnapshot3, e) ->{
                                    if(querySnapshot3!=null)
                                    {
                                        DocumentSnapshot newMessage = querySnapshot3.getDocuments().get(0);
                                        adapter.addMessage(newMessage);
                                        adapter.notifyItemInserted(adapter.getItemCount());
                                        chatReycler.scrollToPosition(adapter.getItemCount()-1);
                                    }
                                });
                    }
                });
            }
            else
            {
                //found a room
                DocumentSnapshot documentSnapshot= querySnapshot.getDocuments().get(0);
                activeCollection =  documentSnapshot.getReference().collection("messages");
                activeCollection.orderBy("time", Query.Direction.ASCENDING).get().addOnSuccessListener(querySnapshot2 ->
                {
                    ArrayList<DocumentSnapshot> messages = (ArrayList<DocumentSnapshot>) querySnapshot2.getDocuments();
                    adapter.setMessages(messages);
                    adapter.notifyDataSetChanged();
                    chatReycler.scrollToPosition(adapter.getItemCount()-1);
                });
                activeCollection.
                        orderBy("time", Query.Direction.DESCENDING).
                        limit(1).
                        addSnapshotListener((querySnapshot3, e) ->{
                            if(querySnapshot3!=null)
                            {
                                DocumentSnapshot newMessage = querySnapshot3.getDocuments().get(0);
                                adapter.addMessage(newMessage);
                                adapter.notifyItemInserted(adapter.getItemCount());
                                chatReycler.scrollToPosition(adapter.getItemCount()-1);
                            }
                        });
            }
        });
        buttonChatBoxSend.setOnClickListener(caller->{
            if(activeCollection!=null && !editTextChatBox.getText().toString().isEmpty())
            {
                Message newMessage = new Message(firebaseUser.getUid(),user.getuID(),editTextChatBox.getText().toString(),new Date(), "", new ArrayList<>());
                editTextChatBox.setText("");
                activeCollection.add(newMessage).addOnSuccessListener(documentReference -> { //add message to the database
                });
            }
        });
        routeButton.setOnClickListener(sender->{
            //TODO open available routes dialog
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            ArrayList<String> routeNames = new ArrayList<>(MainActivity.currentUser.getRoutes().keySet());
            final String[] options = routeNames.toArray(new String[0]);
            builder.setTitle(R.string.my_routes);
            builder.setItems(options,(dialog, which) -> {
                if(activeCollection!=null) {
                    Message newMessage = new Message(firebaseUser.getUid(), user.getuID(), "", new Date(), options[which], MainActivity.currentUser.getRoutes().get(options[which]));
                    activeCollection.add(newMessage).addOnSuccessListener(documentReference -> {//add message to the database
                    });
                }
            });
            builder.create().show();
        });
      /*  addToCalendarButton.setOnClickListener(caller->{
            Intent openCalendarIntent = new Intent(Intent.ACTION_INSERT)
                    .setData(CalendarContract.Events.CONTENT_URI)
                    .putExtra(CalendarContract.Events.TITLE,getResources().getString(R.string.excursion_with_user)+" "+user.getFirstName()+" "+user.getLastName())
                    .putExtra(CalendarContract.Events.EVENT_LOCATION,user.getCountry()+", "+user.getCity());
            startActivity(openCalendarIntent);
        });*/
    }
    class ChatRecyclerViewAdapter extends RecyclerView.Adapter
    {
        private static final int VIEW_TYPE_MESSAGE_SENT = 1;
        private static final int VIEW_TYPE_MESSAGE_RECEIVED = 2;
        private static final int VIEW_TYPE_ROUTE_SENT = 3;
        private static final int VIEW_TYPE_ROUTE_RECEIVED = 4;
        private ArrayList<DocumentSnapshot> messages;
        private FirebaseUser firebaseUser;
        public ChatRecyclerViewAdapter(ArrayList<DocumentSnapshot> messages, FirebaseUser firebaseUser) {
            this.messages = messages;
            this.firebaseUser = firebaseUser;
        }
        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            if (viewType == VIEW_TYPE_MESSAGE_SENT)
            {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_sent,parent,false);
                return new SentMessageViewHolder(v);
            }
            else if (viewType==VIEW_TYPE_MESSAGE_RECEIVED){
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_received,parent,false);
                return new ReceivedMessageViewHolder(v);
            }
            else if (viewType==VIEW_TYPE_ROUTE_SENT)
            {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.route_sent,parent,false);
                Log.v("measure",String.valueOf(v.getMeasuredHeight()));
                return new SentRouteViewHolder(v);
            }
            else  //(viewType==VIEW_TYPE_ROUTE_RECEIVED)
            {
                View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.route_received,parent,false);
                Log.v("measure",String.valueOf(v.getMeasuredHeight()));
                return new ReceivedRouteViewHolder(v);
            }
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int position) {
            Message message = messages.get(position).toObject(Message.class);
            if(viewHolder.getItemViewType() == VIEW_TYPE_MESSAGE_SENT)
            {
                ((SentMessageViewHolder)viewHolder).sentMessage.setText(message.getText());
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");
                ((SentMessageViewHolder)viewHolder).sentTime.setText(simpleDateFormat.format(message.getTime()));
            }
            else if (viewHolder.getItemViewType() == VIEW_TYPE_MESSAGE_RECEIVED)
            {
                ((ReceivedMessageViewHolder)viewHolder).receivedMessage.setText(message.getText());
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");
                ((ReceivedMessageViewHolder)viewHolder).receivedTime.setText(simpleDateFormat.format(message.getTime()));
            }
            else if (viewHolder.getItemViewType() == VIEW_TYPE_ROUTE_SENT)
            {
                SentRouteViewHolder routeViewHolder = (SentRouteViewHolder) viewHolder;
                routeViewHolder.bind(message);
            }
            else if(viewHolder.getItemViewType()==VIEW_TYPE_ROUTE_RECEIVED)
            {
                ReceivedRouteViewHolder routeViewHolder = (ReceivedRouteViewHolder) viewHolder;
                routeViewHolder.bind(message);

            }
        }


        @Override
        public int getItemCount() {
            return messages.size();
        }

        @Override
        public int getItemViewType(int position) {
            Message currMessage = messages.get(position).toObject(Message.class);
            if(!currMessage.getRouteName().isEmpty())
            {
                if(currMessage.getSender().equals(firebaseUser.getUid()))
                    return VIEW_TYPE_ROUTE_SENT;
                else
                    return VIEW_TYPE_ROUTE_RECEIVED;
            }
            else
            {
                if(currMessage.getSender().equals(firebaseUser.getUid()))
                    return VIEW_TYPE_MESSAGE_SENT;
                else
                    return VIEW_TYPE_MESSAGE_RECEIVED;
            }

        }

        private class ReceivedMessageViewHolder extends RecyclerView.ViewHolder
        {
            TextView receivedMessage, receivedTime;
            ReceivedMessageViewHolder(@NonNull View itemView) {
                super(itemView);
                receivedMessage = itemView.findViewById(R.id.received_message);
                receivedTime = itemView.findViewById(R.id.received_time);
            }
        }
        private class SentMessageViewHolder extends RecyclerView.ViewHolder
        {
            TextView sentMessage, sentTime;
            SentMessageViewHolder(@NonNull View itemView) {
                super(itemView);
                sentMessage = itemView.findViewById(R.id.sent_message);
                sentTime = itemView.findViewById(R.id.sent_time);
            }
        }
        private class ReceivedRouteViewHolder extends RecyclerView.ViewHolder implements OnMapReadyCallback
        {
            MapView receivedMap;
            TextView routeReceivedTime;
            GoogleMap map;
            public ReceivedRouteViewHolder(@NonNull View itemView) {
                super(itemView);
                receivedMap = itemView.findViewById(R.id.received_map);
                routeReceivedTime = itemView.findViewById(R.id.route_received_time);
                receivedMap.onCreate(null);
                receivedMap.getMapAsync(this);
            }

            @Override
            public void onMapReady(GoogleMap googleMap) {
                MapsInitializer.initialize(getApplicationContext());
                map = googleMap;
                setMapRoute();

            }
            void bind(Message message)
            {
                receivedMap.setTag(message);
                setMapRoute();

                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");
                routeReceivedTime.setText(simpleDateFormat.format(message.getTime()));
            }
            void setMapRoute()
            {
                if(map == null) return;
                Message message = (Message) receivedMap.getTag();
                if(message==null) return;
                //TODO set route and move camera
                ArrayList<GeoPoint> routePoints = message.getRoutePoints();
                if(!routePoints.isEmpty())
                {
                    for (int i = 0; i < routePoints.size(); i++) {
                        GeoPoint point = routePoints.get(i);
                        MarkerOptions markerOptions = new MarkerOptions();
                        markerOptions.position(new LatLng(point.getLatitude(), point.getLongitude()));
                        if (i == 0)
                            markerOptions.icon(bitmapDescriptorFromVector(routeReceivedTime.getContext(), R.drawable.ic_start_marker));
                        else if (i == routePoints.size() - 1 && routePoints.size() != 1)
                            markerOptions.icon(bitmapDescriptorFromVector(routeReceivedTime.getContext(), R.drawable.ic_finish_marker));
                        map.addMarker(markerOptions);
                        map.addMarker(markerOptions);
                    }
                    LatLng firstPoint = new LatLng(routePoints.get(0).getLatitude(),routePoints.get(0).getLongitude());
                    CameraPosition.Builder builder = CameraPosition.builder();
                    builder.zoom(10);
                    builder.target(firstPoint);
                    map.moveCamera(CameraUpdateFactory.newCameraPosition(builder.build()));
                }
                map.setOnMapClickListener(latLng -> {
                    Intent openMapActivity = new Intent(routeReceivedTime.getContext(),MapActivity.class);
                    openMapActivity.putExtra(UserProfileFragment.MODE_KEY,MapActivity.MODE_DISPLAY);
                    openMapActivity.putExtra(UserProfileFragment.ROUTE_NAME_KEY,message.getRouteName());
                    ArrayList<LatLngSerializablePair> serRoute = new ArrayList<>();
                    for(GeoPoint point:routePoints)
                    {
                        serRoute.add(new LatLngSerializablePair(point.getLatitude(),point.getLongitude()));
                    }
                    openMapActivity.putExtra(MapActivity.ROUTE_POINTS_KEY,serRoute);
                    openMapActivity.putExtra(MapActivity.UID_KEY,user.getuID());
                    openMapActivity.putExtra(MapActivity.NAME_KEY,user.getFirstName()+" "+user.getLastName());
                    openMapActivity.putExtra(MapActivity.LOCATION_KEY,user.getCountry()+", "+user.getCity());
                    startActivity(openMapActivity);
                });
                map.setMapType(GoogleMap.MAP_TYPE_NORMAL);

            }
        }
        private class SentRouteViewHolder extends RecyclerView.ViewHolder implements OnMapReadyCallback
        {
            MapView sentMap;
            TextView routeSentTime;
            GoogleMap map;
            public SentRouteViewHolder(@NonNull View itemView) {
                super(itemView);
                sentMap = itemView.findViewById(R.id.sent_map);
                routeSentTime = itemView.findViewById(R.id.route_sent_time);
                sentMap.onCreate(null);
                sentMap.getMapAsync(this);
            }
            @Override
            public void onMapReady(GoogleMap googleMap) {
                MapsInitializer.initialize(getApplicationContext());
                map = googleMap;
                setMapRoute();
            }
            void bind(Message message)
            {
                sentMap.setTag(message);
                setMapRoute();
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");
                routeSentTime.setText(simpleDateFormat.format(message.getTime()));
            }
            void setMapRoute()
            {
                if(map == null) return;
                Message message = (Message) sentMap.getTag();

                if(message==null) return;
                //TODO set route and move camera

                ArrayList<GeoPoint> routePoints = message.getRoutePoints();
                if(!routePoints.isEmpty())
                {
                    for (int i = 0; i < routePoints.size(); i++) {
                        GeoPoint point = routePoints.get(i);
                        MarkerOptions markerOptions = new MarkerOptions();
                        markerOptions.position(new LatLng(point.getLatitude(), point.getLongitude()));
                        if (i == 0)
                            markerOptions.icon(bitmapDescriptorFromVector(routeSentTime.getContext(), R.drawable.ic_start_marker));
                        else if (i == routePoints.size() - 1 && routePoints.size() != 1)
                            markerOptions.icon(bitmapDescriptorFromVector(routeSentTime.getContext(), R.drawable.ic_finish_marker));
                        map.addMarker(markerOptions);
                    }
                    LatLng firstPoint = new LatLng(routePoints.get(0).getLatitude(),routePoints.get(0).getLongitude());
                    CameraPosition.Builder builder = CameraPosition.builder();
                    builder.zoom(10);
                    builder.target(firstPoint);
                    map.moveCamera(CameraUpdateFactory.newCameraPosition(builder.build()));
                }
                map.setOnMapClickListener(latLng -> {
                    Intent openMapActivity = new Intent(routeSentTime.getContext(),MapActivity.class);
                    openMapActivity.putExtra(UserProfileFragment.MODE_KEY,MapActivity.MODE_DISPLAY);
                    openMapActivity.putExtra(UserProfileFragment.ROUTE_NAME_KEY,message.getRouteName());
                    ArrayList<LatLngSerializablePair> serRoute = new ArrayList<>();
                    for(GeoPoint point:routePoints)
                    {
                        serRoute.add(new LatLngSerializablePair(point.getLatitude(),point.getLongitude()));
                    }
                    openMapActivity.putExtra(MapActivity.ROUTE_POINTS_KEY,serRoute);
                    openMapActivity.putExtra(MapActivity.UID_KEY,user.getuID());
                    openMapActivity.putExtra(MapActivity.NAME_KEY,user.getFirstName()+" "+user.getLastName());
                    openMapActivity.putExtra(MapActivity.LOCATION_KEY,user.getCountry()+", "+user.getCity());
                    startActivity(openMapActivity);
                });
                map.setMapType(GoogleMap.MAP_TYPE_NORMAL);

            }

        }
        public void setMessages(ArrayList<DocumentSnapshot> messages) {
            this.messages = messages;
        }
        public void addMessage(DocumentSnapshot message)
        {
            messages.add(message);
        }
        private BitmapDescriptor bitmapDescriptorFromVector(Context context, int vectorResId) {
            Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
            vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
            Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            vectorDrawable.draw(canvas);
            return BitmapDescriptorFactory.fromBitmap(bitmap);
        }
    }
}
