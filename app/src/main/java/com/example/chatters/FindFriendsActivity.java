package com.example.chatters;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class FindFriendsActivity extends AppCompatActivity
{
    private Toolbar mToolbar;
    private RecyclerView findFriendsRecyclerView;
    private DatabaseReference mDatabaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_friends);

        mDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users");

        findFriendsRecyclerView = findViewById(R.id.find_friends_recycler_list);
        findFriendsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        mToolbar = findViewById(R.id.tool_bar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Find Friends");
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        FirebaseRecyclerOptions<Contacts> options = new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(mDatabaseReference , Contacts.class)
                .build();

        FirebaseRecyclerAdapter<Contacts, FindFriendsViewHolder> contactsAdapter = new
                FirebaseRecyclerAdapter<Contacts, FindFriendsViewHolder>(options)
                {
                    @Override
                    protected void onBindViewHolder(@NonNull FindFriendsViewHolder holder, final int position, @NonNull Contacts model)
                    {
                        holder.userName.setText(model.getName());
                        holder.userStatus.setText(model.getStatus());
                        Picasso.get().load(model.getImages()).placeholder(R.drawable.profile_image).into(holder.profileImage);

                        holder.itemView.setOnClickListener(new View.OnClickListener()
                        {
                            @Override
                            public void onClick(View v)
                            {
                                String visitUserID = getRef(position).getKey();
                                Intent profileIntent = new Intent(FindFriendsActivity.this , ProfileActivity.class);
                                profileIntent.putExtra("visitUserID" , visitUserID);
                                startActivity(profileIntent);
                            }
                        });
                    }

                    @NonNull
                    @Override
                    public FindFriendsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType)
                    {
                        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.user_display , viewGroup , false);
                        FindFriendsViewHolder friendsViewHolder = new FindFriendsViewHolder(view);
                        return friendsViewHolder;
                    }
                };

        findFriendsRecyclerView.setAdapter(contactsAdapter);
        contactsAdapter.startListening();
    }

    public static class FindFriendsViewHolder extends RecyclerView.ViewHolder
    {
        private TextView userName, userStatus;
        private CircleImageView profileImage;

        public FindFriendsViewHolder(@NonNull View itemView)
        {
            super(itemView);

            userName = itemView.findViewById(R.id.user_profile_name);
            userStatus = itemView.findViewById(R.id.user_status);
            profileImage = itemView.findViewById(R.id.user_profile_image);
        }
    }
}
