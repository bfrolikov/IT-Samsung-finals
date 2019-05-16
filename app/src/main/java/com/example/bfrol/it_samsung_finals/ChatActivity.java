package com.example.bfrol.it_samsung_finals;

import android.app.DownloadManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.Query;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {
    private RecyclerView chatReycler;
    private ChatRecyclerViewAdapter adapter;
    private EditText editTextChatBox;
    private ImageButton buttonChatBoxSend;
    private ImageButton routeButton;
    private CollectionReference activeCollection;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        chatReycler = findViewById(R.id.chat_recycler_view);
        editTextChatBox = findViewById(R.id.edittext_chatbox);
        buttonChatBoxSend = findViewById(R.id.button_chatbox_send);
        routeButton = findViewById(R.id.route_button);
        activeCollection = null;
        Bundle bundle = getIntent().getExtras();
        User user = (User) bundle.getSerializable(SelectedUserProfileActivity.USER_KEY);
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        adapter = new ChatRecyclerViewAdapter(new ArrayList<>(), firebaseUser);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setStackFromEnd(true);
        chatReycler.setLayoutManager(manager);
        chatReycler.setAdapter(adapter);
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
                            Message firstMessage = new Message(firebaseUser.getUid(),user.getuID(),"Hello, i'm interested",new Date(), "", new ArrayList<>());//generate automatic message
                            documentReference.collection("messages").add(firstMessage).addOnSuccessListener(documentReference1 -> {
                                documentReference1.get().addOnSuccessListener(documentSnapshot -> {
                                    adapter.addMessage(documentSnapshot);
                                    adapter.notifyDataSetChanged();
                                    activeCollection = documentReference.collection("messages");
                                });

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
            }
        });
        buttonChatBoxSend.setOnClickListener(sender->{
            if(activeCollection!=null && !editTextChatBox.getText().toString().isEmpty())
            {
                Message newMessage = new Message(firebaseUser.getUid(),user.getuID(),editTextChatBox.getText().toString(),new Date(), "", new ArrayList<>());
                editTextChatBox.setText("");
                activeCollection.add(newMessage).addOnSuccessListener(documentReference -> { //add message to the database
                    documentReference.get().addOnSuccessListener(documentSnapshot -> {
                        //loaded message from the database
                        adapter.addMessage(documentSnapshot);
                        adapter.notifyDataSetChanged();
                        chatReycler.smoothScrollToPosition(adapter.getItemCount());
                    });
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

            });
            builder.create().show();
        });
    }
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
        else{
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_received,parent,false);
            return new ReceivedMessageViewHolder(v);
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
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");
            routeViewHolder.routeSentTime.setText(simpleDateFormat.format(message.getTime()));
            routeViewHolder.sentMap.getMapAsync(googleMap -> {
                UiSettings uiSettings = googleMap.getUiSettings();
                uiSettings.setAllGesturesEnabled(false);
                uiSettings.setIndoorLevelPickerEnabled(false);
                uiSettings.setMyLocationButtonEnabled(false);
                uiSettings.setMapToolbarEnabled(false);
                uiSettings.setZoomControlsEnabled(false);
                ArrayList<GeoPoint> routePoints = message.getRoutePoints();
                if(!routePoints.isEmpty())
                {
                    LatLng firstPoint = new LatLng(routePoints.get(0).getLatitude(),routePoints.get(0).getLongitude());
                    CameraPosition.Builder builder = CameraPosition.builder();
                    builder.zoom(10);
                    builder.target(firstPoint);
                    googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(builder.build()));
                }
            });
        }
        else if(viewHolder.getItemViewType()==VIEW_TYPE_ROUTE_RECEIVED)
        {
            ReceivedRouteViewHolder routeViewHolder = (ReceivedRouteViewHolder) viewHolder;
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm");
            routeViewHolder.routeReceivedTime.setText(simpleDateFormat.format(message.getTime()));
            routeViewHolder.receivedMap.getMapAsync(googleMap -> {
                UiSettings uiSettings = googleMap.getUiSettings();
                uiSettings.setAllGesturesEnabled(false);
                uiSettings.setIndoorLevelPickerEnabled(false);
                uiSettings.setMyLocationButtonEnabled(false);
                uiSettings.setMapToolbarEnabled(false);
                uiSettings.setZoomControlsEnabled(false);
                ArrayList<GeoPoint> routePoints = message.getRoutePoints();
                if(!routePoints.isEmpty())
                {
                    LatLng firstPoint = new LatLng(routePoints.get(0).getLatitude(),routePoints.get(0).getLongitude());
                    CameraPosition.Builder builder = CameraPosition.builder();
                    builder.zoom(10);
                    builder.target(firstPoint);
                    googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(builder.build()));
                }
            });
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
    private class ReceivedRouteViewHolder extends RecyclerView.ViewHolder
    {
        MapView receivedMap;
        TextView routeReceivedTime;
        public ReceivedRouteViewHolder(@NonNull View itemView) {
            super(itemView);
            receivedMap = itemView.findViewById(R.id.received_map);
            routeReceivedTime = itemView.findViewById(R.id.route_received_time);
        }
    }
    private class SentRouteViewHolder extends RecyclerView.ViewHolder
    {
        MapView sentMap;
        TextView routeSentTime;
        public SentRouteViewHolder(@NonNull View itemView) {
            super(itemView);
            sentMap = itemView.findViewById(R.id.sent_map);
            routeSentTime = itemView.findViewById(R.id.route_sent_time);
        }

    }
    public void setMessages(ArrayList<DocumentSnapshot> messages) {
        this.messages = messages;
    }
    public void addMessage(DocumentSnapshot message)
    {
        messages.add(message);
    }
}