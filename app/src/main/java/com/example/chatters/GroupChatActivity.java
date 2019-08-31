package com.example.chatters;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;

public class GroupChatActivity extends AppCompatActivity
{
    private Toolbar mToolbar;
    private ImageButton sendButton;
    private EditText userMessageInput;
    private ScrollView mScrollView;
    private TextView displayTextMessage;
    private FirebaseAuth groupChatFireBaseAuth;
    private DatabaseReference groupChatDatabaseReference , groupNameDatabase , groupMessageKeyDatabase;

    private String currentGroupName , currentUserID , currentUserName , currentDate , currentTime;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);

        currentGroupName = getIntent().getExtras().get("Group Name").toString();
        Toast.makeText(GroupChatActivity.this, currentGroupName, Toast.LENGTH_SHORT).show();


        groupChatFireBaseAuth = FirebaseAuth.getInstance();
        currentUserID = groupChatFireBaseAuth.getCurrentUser().getUid();
        groupChatDatabaseReference = FirebaseDatabase.getInstance().getReference().child("Users");
        groupNameDatabase = FirebaseDatabase.getInstance().getReference().child("Groups").child(currentGroupName);


        initializedFields();

        getUserInfo();

        sendButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                saveMessageInfoToDatabase();

                userMessageInput.setText("");

                mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });
    }


    @Override
    protected void onStart()
    {
        super.onStart();

        groupNameDatabase.addChildEventListener(new ChildEventListener()
        {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s)
            {
                if (dataSnapshot.exists())
                {
                    displayMessage(dataSnapshot);
                }
            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s)
            {
                if (dataSnapshot.exists())
                {
                    displayMessage(dataSnapshot);
                }
            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot)
            {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s)
            {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });
    }


    private void initializedFields()
    {
        mToolbar = findViewById(R.id.group_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle(currentGroupName);

        sendButton = findViewById(R.id.send_button);
        userMessageInput = findViewById(R.id.input_text);
        mScrollView = findViewById(R.id.group_chat_scroll_view);
        displayTextMessage = findViewById(R.id.scroll_view_text);
    }

    private void getUserInfo()
    {
        groupChatDatabaseReference.child(currentUserID).addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if (dataSnapshot.exists())
                {
                    currentUserName = dataSnapshot.child("name").getValue().toString();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {


            }
        });
    }

    private void saveMessageInfoToDatabase()
    {
        String message = userMessageInput.getText().toString();
        String messageKey = groupNameDatabase.push().getKey();
        if (TextUtils.isEmpty(message))
        {
            Toast.makeText(this, "Please Write Some Thing", Toast.LENGTH_SHORT).show();
        }
        else
        {
            Calendar callForDate = Calendar.getInstance();
            SimpleDateFormat currentDateFormat = new SimpleDateFormat("dd MMM, yyyy");
            currentDate = currentDateFormat.format(callForDate.getTime());

            Calendar callForTime = Calendar.getInstance();
            SimpleDateFormat currentTimeFormat = new SimpleDateFormat("hh:mm a");
            currentTime = currentTimeFormat.format(callForTime.getTime());

            HashMap<String , Object> groupMessageKey = new HashMap<>();
            groupNameDatabase.updateChildren(groupMessageKey);
            groupMessageKeyDatabase = groupNameDatabase.child(messageKey);

            HashMap<String,Object> messageInfoMap = new HashMap<>();
                messageInfoMap.put("name" , currentUserName);
                messageInfoMap.put("message" , message);
                messageInfoMap.put("date" , currentDate);
                messageInfoMap.put("time" , currentTime);

            groupMessageKeyDatabase.updateChildren(messageInfoMap);
        }
    }

    private void displayMessage(DataSnapshot dataSnapshot)
    {
        Iterator iterator = dataSnapshot.getChildren().iterator();

        while (iterator.hasNext())
        {
            String chatDate = (String) ((DataSnapshot)iterator.next()).getValue();
            String chatMessage = (String) ((DataSnapshot)iterator.next()).getValue();
            String chatName = (String) ((DataSnapshot)iterator.next()).getValue();
            String chatTime = (String) ((DataSnapshot)iterator.next()).getValue();

            displayTextMessage.append(chatName + ":\n" + chatMessage + "\n" + chatTime + "     " + chatDate + "\n\n");

            mScrollView.fullScroll(ScrollView.FOCUS_DOWN);
        }
    }

}
