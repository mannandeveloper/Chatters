package com.example.chatters;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Gallery;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity
{
    private CircleImageView mProfileImage;
    private EditText userName , status;
    private Button updateStatus;
    private FirebaseAuth mFirebaseAuth;
    private String currentUserID;
    private DatabaseReference mDatabaseReference;
    private StorageReference userProfileImageReference;
    private static final int galleryPictures = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);


        mFirebaseAuth = FirebaseAuth.getInstance();
        currentUserID = mFirebaseAuth.getCurrentUser().getUid();
        mDatabaseReference = FirebaseDatabase.getInstance().getReference();
        userProfileImageReference = FirebaseStorage.getInstance().getReference().child("Profile Images");

        initializedFields();

        updateStatus.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                updateSettings();
            }
        });


        retrieveUserInfo();


        mProfileImage.setOnClickListener(  new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent gallery = new Intent();
                gallery.setAction(Intent.ACTION_GET_CONTENT);
                gallery.setType("image/*");
                startActivityForResult(gallery , galleryPictures);
            }
        });
    }



    private void initializedFields()
    {
        mProfileImage = findViewById(R.id.profile_image);
        userName = findViewById(R.id.user_name);
        status = findViewById(R.id.status);
        updateStatus = findViewById(R.id.update_status_button);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == galleryPictures && resultCode == RESULT_OK && data != null)
        {
            Uri imageUri = data.getData();

            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start(this);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE)
        {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK)
            {
                Uri resultUri = result.getUri();

                StorageReference filePath = userProfileImageReference.child(currentUserID + ".jpg");
                filePath.putFile(resultUri).addOnCompleteListener( new OnCompleteListener<UploadTask.TaskSnapshot>()
                {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task)
                    {
                        if (task.isSuccessful())
                        {
                            Toast.makeText(SettingsActivity.this, "Profile image uploaded successfully", Toast.LENGTH_SHORT).show();

                            final String downloadUri = task.getResult().getUploadSessionUri().toString();

                            mDatabaseReference.child("Users").child(currentUserID).child("images")
                                    .setValue(downloadUri)
                                    .addOnCompleteListener(new OnCompleteListener<Void>()
                                    {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task)
                                        {
                                            if (task.isSuccessful())
                                            {
                                                Toast.makeText(SettingsActivity.this, "Image save in database successfully", Toast.LENGTH_SHORT).show();
                                            }
                                            else
                                            {
                                                String message = task.getException().toString();
                                                Toast.makeText(SettingsActivity.this, "Error : " +message, Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                        }
                        else
                        {
                            String message = task.getException().toString();
                            Toast.makeText(SettingsActivity.this, "Error :" + message, Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            }
        }
    }

    private void updateSettings()
    {
        String setUserName = userName.getText().toString();
        String setStatus = status.getText().toString();

        if (TextUtils.isEmpty(setUserName))
        {
            Toast.makeText(this, "Please enter your name...", Toast.LENGTH_SHORT).show();
        }
        if (TextUtils.isEmpty(setStatus))
        {
            Toast.makeText(this, "Please enter your status", Toast.LENGTH_SHORT).show();
        }
        else
        {
            HashMap<String , String> profileMap = new HashMap<>();
                profileMap.put("uid" , currentUserID);
                profileMap.put("name" , setUserName);
                profileMap.put("status" , setStatus);

            mDatabaseReference.child("Users").child(currentUserID).setValue(profileMap)
                    .addOnCompleteListener(new OnCompleteListener<Void>()
                    {
                        @Override
                        public void onComplete(@NonNull Task<Void> task)
                        {
                               if (task.isSuccessful())
                               {
                                   sendUserToMainActivity();
                                   Toast.makeText(SettingsActivity.this, "Profile Updated Successfully", Toast.LENGTH_SHORT).show();
                               }
                               else
                               {
                                   String message = task.getException().toString();
                                   Toast.makeText(SettingsActivity.this, "Error : " + message, Toast.LENGTH_SHORT).show();
                               }
                        }
                    });
        }
    }



    private void retrieveUserInfo()
    {
        mDatabaseReference.child("Users").child(currentUserID)
                .addValueEventListener(new ValueEventListener()
                {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                    {
                        if ((dataSnapshot.exists()) && (dataSnapshot.hasChild("name") && (dataSnapshot.hasChild("images"))))
                        {
                            String retrieveUserName = dataSnapshot.child("name").getValue().toString();
                            String retrieveStatus = dataSnapshot.child("status").getValue().toString();
                            String retrieveProfilePicture = dataSnapshot.child("images").getValue().toString();

                            userName.setText(retrieveUserName);
                            status.setText(retrieveStatus);

                            Picasso.get().load(retrieveProfilePicture).into(mProfileImage);
                        }
                        else if ((dataSnapshot.exists()) && (dataSnapshot.hasChild("name")))
                        {
                            String retrieveUserName = dataSnapshot.child("name").getValue().toString();
                            String retrieveStatus = dataSnapshot.child("status").getValue().toString();

                            userName.setText(retrieveUserName);
                            status.setText(retrieveStatus);
                        }
                        else 
                        {
                            Toast.makeText(SettingsActivity.this, "Please Update your profile", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError)
                    {

                    }
                });
    }


    private void sendUserToMainActivity()
    {
        Intent mainIntent = new Intent(SettingsActivity.this , MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
}
