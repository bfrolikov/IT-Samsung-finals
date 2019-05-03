package com.example.bfrol.it_samsung_finals;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {
    RecyclerView chatRecyclerView;
    ChatRecyclerViewAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        chatRecyclerView = findViewById(R.id.chat_recycler_view);
        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setStackFromEnd(true);
        chatRecyclerView.setLayoutManager(manager);
        Bundle bundle = getIntent().getExtras();
        User user = (User) bundle.getSerializable(SelectedUserProfileActivity.USER_KEY);
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        adapter = new ChatRecyclerViewAdapter(new ArrayList<DocumentSnapshot>(),firebaseUser);
        chatRecyclerView.setAdapter(adapter);
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
                            Message firstMessage = new Message(firebaseUser.getUid(),user.getuID(),"Hello, i'm interested","00:00");//generate automatic message
                            documentReference.collection("messages").add(firstMessage);
                        });
                    }
                    else
                    {
                        //found a room
                        DocumentSnapshot documentSnapshot= querySnapshot1.getDocuments().get(0);
                        documentSnapshot.getReference().collection("messages").get().addOnSuccessListener(querySnapshot2 ->
                        {
                            ArrayList<DocumentSnapshot> messages = (ArrayList<DocumentSnapshot>) querySnapshot2.getDocuments();
                            adapter.setMessages(messages);
                            adapter.notifyDataSetChanged();
                        });
                    }
                });
            }
            else
            {
                //found a room
                DocumentSnapshot documentSnapshot= querySnapshot.getDocuments().get(0);
                documentSnapshot.getReference().collection("messages").get().addOnSuccessListener(querySnapshot2 ->
                {
                    ArrayList<DocumentSnapshot> messages = (ArrayList<DocumentSnapshot>) querySnapshot2.getDocuments();
                    adapter.setMessages(messages);
                    adapter.notifyDataSetChanged();
                });
            }
        });
    }
}

class ChatRecyclerViewAdapter extends RecyclerView.Adapter
{
    private static final int VIEW_TYPE_MESSAGE_SENT = 1;
    private static final int VIEW_TYPE_MESSAGE_RECEIVED = 2;

    private ArrayList<DocumentSnapshot> messages;
    private FirebaseUser firebaseUser;

    public ChatRecyclerViewAdapter(ArrayList<DocumentSnapshot> messages, FirebaseUser firebaseUser) {
        this.messages = messages;
        this.firebaseUser = firebaseUser;
    }

    @Override
    public int getItemViewType(int position) {
        Message currMessage = messages.get(position).toObject(Message.class);
        if(currMessage.getSender().equals(firebaseUser.getUid()))
            return VIEW_TYPE_MESSAGE_SENT;
        else
            return VIEW_TYPE_MESSAGE_RECEIVED;
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
            ((SentMessageViewHolder)viewHolder).sentTime.setText(message.getTime());
        }
        else if (viewHolder.getItemViewType() == VIEW_TYPE_MESSAGE_RECEIVED)
        {
            ((ReceivedMessageViewHolder)viewHolder).receivedMessage.setText(message.getText());
            ((ReceivedMessageViewHolder)viewHolder).receivedTime.setText(message.getTime());
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public void setMessages(ArrayList<DocumentSnapshot> messages) {
        this.messages = messages;
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
}
