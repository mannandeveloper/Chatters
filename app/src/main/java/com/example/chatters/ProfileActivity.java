package com.example.chatters;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfileActivity extends AppCompatActivity
{
    private String receiverUserID , senderUserID, currentState;
    private CircleImageView profileImage;
    private TextView userProfileName, userProfileStatus;
    private Button sendMessage, cancelButton;
    private DatabaseReference profileDatabaseReference, chatRequestRef, contactRef;
    private FirebaseAuth mFireBaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mFireBaseAuth = FirebaseAuth.getInstance();
        profileDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users");
        chatRequestRef = FirebaseDatabase.getInstance().getReference().child("Chat Requests");
        contactRef = FirebaseDatabase.getInstance().getReference().child("Contacts");

        receiverUserID = getIntent().getExtras().get("visitUserID").toString();
        senderUserID = mFireBaseAuth.getCurrentUser().getUid();


        initializedFields();

        RetrieveUserInfo();
    }

    private void initializedFields()
    {
        profileImage = findViewById(R.id.image_profile);
        userProfileName = findViewById(R.id.profile_user_name);
        userProfileStatus = findViewById(R.id.profile_user_status);
        sendMessage = findViewById(R.id.send_message);
        cancelButton = findViewById(R.id.cancel_button);
        currentState = "New";
    }


    private void RetrieveUserInfo()
    {
        profileDatabaseReference.child(receiverUserID).addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if ((dataSnapshot.exists())  && (dataSnapshot.hasChild("images")))
                {
                    String userImage = dataSnapshot.child("images").getValue().toString();
                    String userName = dataSnapshot.child("name").getValue().toString();
                    String userStatus = dataSnapshot.child("status").getValue().toString();

                    Picasso.get().load(userImage).placeholder(R.drawable.profile_image).into(profileImage);
                    userProfileName.setText(userName);
                    userProfileStatus.setText(userStatus);


                    manageChatRequests();
                }
                else
                {
                    String userName = dataSnapshot.child("name").getValue().toString();
                    String userStatus = dataSnapshot.child("status").getValue().toString();

                    userProfileName.setText(userName);
                    userProfileStatus.setText(userStatus);

                    manageChatRequests();

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });
    }

    private void manageChatRequests()
    {
        chatRequestRef.child(senderUserID)
                .addValueEventListener(new ValueEventListener()
                {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                    {
                        if (dataSnapshot.hasChild(receiverUserID))
                        {
                            String requestType = dataSnapshot.child(receiverUserID).child("Request Type").getValue().toString();

                            if (requestType.equals("Send"))
                            {
                                currentState = "Request Sent";
                                sendMessage.setText("Cancel Chat Request");
                            }
                            else if (requestType.equals("Received"))
                            {
                                currentState = "Request Accepted";
                                sendMessage.setText("Accepted Chat Request");
                                cancelButton.setVisibility(View.VISIBLE);
                                cancelButton.setEnabled(true);

                                cancelButton.setOnClickListener(new View.OnClickListener()
                                {
                                    @Override
                                    public void onClick(View v)
                                    {
                                        cancelChatRequest();
                                    }
                                });
                            }
                        }
                        else
                        {
                            contactRef.child(senderUserID)
                                    .addListenerForSingleValueEvent(new ValueEventListener()
                                    {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                                        {
                                            if (dataSnapshot.hasChild(receiverUserID))
                                            {
                                                currentState = "Friends";
                                                sendMessage.setText("Unfriend");
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError)
                                        {

                                        }
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError)
                    {

                    }
                });
        if (!senderUserID.equals(receiverUserID))
        {
            sendMessage.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    sendMessage.setEnabled(false);
                    if (currentState.equals("New"))
                    {
                        sendChatRequest();
                    }
                    if (currentState.equals("Request Sent"))
                    {
                        cancelChatRequest();
                    }
                    if (currentState.equals("Request Received"))
                    {
                        acceptedChatRequest();
                    }
                }
            });
        }
        else
        {
            sendMessage.setVisibility(View.INVISIBLE);
        }
    }

    private void sendChatRequest()
    {
        chatRequestRef.child(senderUserID).child(receiverUserID)
                .child("Request Type").setValue("send")
                .addOnCompleteListener(new OnCompleteListener<Void>()
                {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        if (task.isSuccessful())
                        {
                            chatRequestRef.child(receiverUserID).child(senderUserID)
                                    .child("Request Type").setValue("Received")
                                    .addOnCompleteListener(new OnCompleteListener<Void>()
                                    {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {
                                            if (task.isSuccessful())
                                            {
                                                sendMessage.setEnabled(true);
                                                currentState = "Request Sent";
                                                sendMessage.setText("Cancel Chat Request");
                                            }
                                        }
                                    });
                        }
                    }
                });
    }


    private void cancelChatRequest()
    {
        chatRequestRef.child(senderUserID).child(receiverUserID)
                .removeValue()
                .addOnCompleteListener(new OnCompleteListener<Void>()
                {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        if (task.isSuccessful())
                        {
                            chatRequestRef.child(receiverUserID).child(senderUserID)
                                    .removeValue()
                                    .addOnCompleteListener(new OnCompleteListener<Void>()
                                    {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {
                                            sendMessage.setEnabled(true);
                                            currentState = "New";
                                            sendMessage.setText("Chat Request");

                                            cancelButton.setVisibility(View.INVISIBLE);
                                            cancelButton.setEnabled(false);
                                        }
                                    });
                        }
                    }
                });
    }

    private void acceptedChatRequest()
    {
        contactRef.child(senderUserID).child(receiverUserID)
                .child("Contacts").setValue("Saved")
                .addOnCompleteListener(new OnCompleteListener<Void>()
                {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        if (task.isSuccessful())
                        {
                            contactRef.child(receiverUserID).child(senderUserID)
                                    .child("Contacts").setValue("Saved")
                                    .addOnCompleteListener(new OnCompleteListener<Void>()
                                    {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {
                                            if (task.isSuccessful())
                                            {
                                                chatRequestRef.child(senderUserID).child(receiverUserID)
                                                        .removeValue()
                                                        .addOnCompleteListener(new OnCompleteListener<Void>()
                                                        {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task)
                                                            {
                                                                if (task.isSuccessful())
                                                                {
                                                                    chatRequestRef.child(receiverUserID).child(senderUserID)
                                                                            .removeValue()
                                                                            .addOnCompleteListener(new OnCompleteListener<Void>()
                                                                            {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<Void> task)
                                                                                {
                                                                                    sendMessage.setEnabled(true);
                                                                                    currentState = "Friends";
                                                                                    sendMessage.setText("Unfriend");
                                                                                    cancelButton.setVisibility(View.INVISIBLE);
                                                                                    cancelButton.setEnabled(false);

                                                                                }
                                                                            });
                                                                }
                                                            }
                                                        });
                                            }
                                        }
                                    });
                        }
                    }
                });
    }
}
