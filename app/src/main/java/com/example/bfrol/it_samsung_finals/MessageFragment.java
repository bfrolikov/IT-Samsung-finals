package com.example.bfrol.it_samsung_finals;

import android.content.Intent;
import android.os.Bundle;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageFragment extends Fragment {
    public static final String USER_TAG = "usertag";
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_message,container,false);
        RecyclerView messageRecycler = v.findViewById(R.id.message_recycler);
        messageRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        ArrayList<User> dummyList = new ArrayList<>();
        dummyList.add(new User("Борис","Иванов","ahsh","agah","agah","agag","T3nXMpwJESUtg8lTjr5xVnzpCZk1",0));
        messageRecycler.setAdapter(new MessageUserRecyclerViewAdapter(dummyList));
        return v;
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
            bundle.putSerializable(MessageFragment.USER_TAG,currentUser);
            Intent openChatActivity = new Intent(viewHolder.itemView.getContext(),ChatActivity.class);
            openChatActivity.putExtras(bundle);
            viewHolder.itemView.getContext().startActivity(openChatActivity);
        });
        viewHolder.msgItemName.setText(currentUser.getFirstName()+" "+currentUser.getLastName());
        viewHolder.msgItemLastMessage.setText("TODO");
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
