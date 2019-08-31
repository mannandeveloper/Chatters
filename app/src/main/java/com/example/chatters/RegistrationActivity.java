package com.example.chatters;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.mbms.MbmsErrors;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegistrationActivity extends AppCompatActivity
{
    private FirebaseAuth mRegistrationFireBaseAuth;
    private TextView mHavingAnAccount;
    private EditText mRegistrationEmail , mRegistrationPassword;
    private Button mCreatingNewAccountButton;
    private ProgressDialog loadingBar;
    private DatabaseReference mDatabaseReference;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        mRegistrationFireBaseAuth = FirebaseAuth.getInstance();
        mDatabaseReference = FirebaseDatabase.getInstance().getReference();

        initializedFields();

        mHavingAnAccount.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                sendUserToLoginActivity();
            }
        });

        mCreatingNewAccountButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                createNewAccount();
            }
        });
    }

    private void createNewAccount()
    {
        loadingBar.setTitle("Creating new account");
        loadingBar.setMessage("Please wait , while we are creating your new account...");
        loadingBar.setCanceledOnTouchOutside(true);
        loadingBar.show();
        String registerEmail = mRegistrationEmail.getText().toString();
        String registerPassword = mRegistrationPassword.getText().toString();

        if (TextUtils.isEmpty(registerEmail))
        {
            Toast.makeText(this, "Please enter the email address", Toast.LENGTH_SHORT).show();
        }
        if (TextUtils.isEmpty(registerPassword))
        {
            Toast.makeText(this, "Please enter the password", Toast.LENGTH_SHORT).show();
        }
        else
        {
            mRegistrationFireBaseAuth.createUserWithEmailAndPassword(registerEmail , registerPassword)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>()
                    {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task)
                        {
                            if (task.isSuccessful())
                            {
                                String currentUser = mRegistrationFireBaseAuth.getCurrentUser().getUid();
                                sendUserToMainActivity();
                                Toast.makeText(RegistrationActivity.this, "Account created successfully...", Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                            }
                            else
                            {
                                String message = task.getException().toString();
                                Toast.makeText(RegistrationActivity.this, "Error :" + message, Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                            }
                        }
                    });
        }
    }

    private void initializedFields()
    {
        mHavingAnAccount = findViewById(R.id.alreadyHaveAccount);
        mRegistrationEmail = findViewById(R.id.registerUserName);
        mRegistrationPassword = findViewById(R.id.registerPassword);
        mCreatingNewAccountButton = findViewById(R.id.createAccount);
        loadingBar = new ProgressDialog(this);
    }

    private void sendUserToLoginActivity()
    {
        startActivity(new Intent(RegistrationActivity.this , LogInActivity.class));
    }

    private void sendUserToMainActivity()
    {
        Intent mainIntent = new Intent(RegistrationActivity.this , MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }

}
