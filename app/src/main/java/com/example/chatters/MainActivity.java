package com.example.chatters;

import android.content.DialogInterface;
import android.content.Intent;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.tabs.TabLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity
{
    private Toolbar mToolbar;
    private ViewPager mViewPager;
    private TabLayout mTabLayout;
    private ChatterAdapter mChatterAdapter;
    private FirebaseUser mCurrentUser;
    private FirebaseAuth mFireBaseAuth;
    private DatabaseReference mDatabaseReference;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mFireBaseAuth = FirebaseAuth.getInstance();
        mCurrentUser = mFireBaseAuth.getCurrentUser();
        mDatabaseReference = FirebaseDatabase.getInstance().getReference();

        mToolbar = findViewById(R.id.toolBar);
        setSupportActionBar(mToolbar);
        mToolbar.setTitle("Chatters");

        mViewPager = findViewById(R.id.viewPager);
        mChatterAdapter = new ChatterAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mChatterAdapter);

        mTabLayout = findViewById(R.id.tabLayout);
        mTabLayout.setupWithViewPager(mViewPager);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.option_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if (item.getItemId() == R.id.find_friends)
        {
            sendUserToFindFriendsActivity();
        }
        if (item.getItemId() == R.id.create_group)
        {
            requestNewGroup();
        }

        if (item.getItemId() == R.id.log_out)
        {
            mFireBaseAuth.signOut();
            sendUserToLoginActivity();
        }

        if (item.getItemId() == R.id.settings)
        {
            sendUserToSettingsActivity();
        }
        return true;
    }

    private void requestNewGroup()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this , R.style.AlertDialog);
        builder.setTitle("Enter Group Name :");

        final EditText groupName = new EditText(MainActivity.this);
        groupName.setHint("eg: Developers");
        builder.setView(groupName);

        builder.setPositiveButton("Create", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                String mGroupName = groupName.getText().toString();

                if (TextUtils.isEmpty(mGroupName))
                {
                    Toast.makeText(MainActivity.this, "Please Enter Group Name", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    createNewGroup(mGroupName);
                }
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                dialog.cancel();
            }
        });

        builder.show();
    }

    private void createNewGroup(final String mGroupName)
    {
        mDatabaseReference.child("Groups").child(mGroupName).setValue("")
                .addOnCompleteListener(new OnCompleteListener<Void>()
                {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                        Toast.makeText(MainActivity.this, mGroupName +"  Group is created successfully", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    @Override
    protected void onStart()
    {
        super.onStart();

        if (mCurrentUser == null)
        {
            sendUserToLoginActivity();
        }
        else
        {
            verifyUserExistence();
        }
    }

    private void verifyUserExistence()
    {
        String currentUserNameID = mFireBaseAuth.getCurrentUser().getUid();
        mDatabaseReference.child("Users").child(currentUserNameID).addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot)
            {
                if ((dataSnapshot.child("name").exists()))
                {
                    Toast.makeText(MainActivity.this, "Welcome", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    sendUserToSettingsActivity();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError)
            {

            }
        });
    }

    private void sendUserToLoginActivity()
    {
        Intent loginIntent = new Intent(MainActivity.this , LogInActivity.class);
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
    }

    private void sendUserToSettingsActivity()
    {
        Intent settingIntent = new Intent(MainActivity.this , SettingsActivity.class);
        settingIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(settingIntent);
        finish();
    }

    private void sendUserToFindFriendsActivity()
    {
        startActivity(new Intent(MainActivity.this, FindFriendsActivity.class));
    }

}
