package com.example.bfrol.it_samsung_finals;

import android.app.DownloadManager;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class MessageFragment extends Fragment {
    public static final String USER_ARRAY_TAG = "userarraytag";
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;
    private MessageUserRecyclerViewAdapter adapter;
    private AdapterHandler handler;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_message,container,false);
        RecyclerView messageRecycler = v.findViewById(R.id.message_recycler);
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        messageRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new MessageUserRecyclerViewAdapter(new ArrayList<User>());
        messageRecycler.setAdapter(adapter);
        handler = new AdapterHandler(adapter);
        HttpHelper httpHelper = new HttpHelper(handler);
        try {
            httpHelper.run(firebaseAuth.getCurrentUser().getUid());
        } catch (Exception e) {
            Log.e("Error:",e.getLocalizedMessage());
        }
        return v;
    }
    class HttpHelper //this helps to execute the cloud function for finding users in the database
    {
        private final OkHttpClient client = new OkHttpClient();
        private AdapterHandler handler;

        public HttpHelper(AdapterHandler handler) {
            this.handler = handler;
        }

        public void run(String s) throws Exception
        {

            String url = "https://us-central1-xsharing-c7dd4.cloudfunctions.net/loadUsers?text="+s;
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
                    ArrayList<User> newUserDialogues = gson.fromJson(response.body().string(),listType);
                    Bundle bundle = new Bundle();
                    bundle.putSerializable(USER_ARRAY_TAG,newUserDialogues);
                    Message msg = new Message();
                    msg.setData(bundle);
                    handler.sendMessage(msg);
                }
            });
        }
    }
    static class AdapterHandler extends Handler
    {
        private WeakReference<MessageUserRecyclerViewAdapter> recyclerWeakReference;
        AdapterHandler(MessageUserRecyclerViewAdapter adapter)
        {
            recyclerWeakReference = new WeakReference<MessageUserRecyclerViewAdapter>(adapter);
        }

        @Override
        public void handleMessage(Message msg) {

            MessageUserRecyclerViewAdapter adapter = recyclerWeakReference.get();
            if(adapter!=null)
            {
                Bundle bundle = msg.getData();
                ArrayList<User> loadedUserDialogues = (ArrayList<User>) bundle.getSerializable(USER_ARRAY_TAG);
                adapter.setUserDialogues(loadedUserDialogues);
                adapter.notifyDataSetChanged();
            }
        }
    }

}
class MessageUserRecyclerViewAdapter extends RecyclerView.Adapter<MessageUserRecyclerViewAdapter.MessageUserViewHolder>
{
    private ArrayList<User> userDialogues;

    MessageUserRecyclerViewAdapter(ArrayList<User> userDialogues) {
        this.userDialogues = userDialogues;
    }

    @NonNull
    @Override
    public MessageUserRecyclerViewAdapter.MessageUserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_item,parent,false);
        return new MessageUserViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageUserRecyclerViewAdapter.MessageUserViewHolder viewHolder, int position) {
        User currentUser = userDialogues.get(position);
        viewHolder.itemView.setOnClickListener(caller->{
            Bundle bundle = new Bundle();
            bundle.putSerializable(SelectedUserProfileActivity.USER_KEY,currentUser);
            Intent openChatActivity = new Intent(viewHolder.itemView.getContext(),ChatActivity.class);
            openChatActivity.putExtras(bundle);
            viewHolder.itemView.getContext().startActivity(openChatActivity);
        });
        viewHolder.msgItemName.setText(currentUser.getFirstName()+" "+currentUser.getLastName());
        viewHolder.msgItemLastMessage.setText("");
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference imageReference = storage.getReference("images/"+currentUser.getuID()+".jpg");
        GlideApp.with(viewHolder.msgItemUsrImage).load(imageReference).diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true).into(viewHolder.msgItemUsrImage);//TODO caching!
    }

    @Override
    public int getItemCount() {
        return userDialogues.size();
    }


    class MessageUserViewHolder extends RecyclerView.ViewHolder
    {
        TextView msgItemName,msgItemLastMessage;
        CircleImageView msgItemUsrImage;
        public MessageUserViewHolder(@NonNull View itemView) {
            super(itemView);
            msgItemName = itemView.findViewById(R.id.msg_item_name);
            msgItemLastMessage = itemView.findViewById(R.id.msg_item_last_message);
            msgItemUsrImage = itemView.findViewById(R.id.msg_item_usr_image);
        }
    }

    public void setUserDialogues(ArrayList<User> userDialogues) {
        this.userDialogues = userDialogues;
    }
}
