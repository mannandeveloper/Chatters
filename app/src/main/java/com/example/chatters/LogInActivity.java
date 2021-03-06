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

public class LogInActivity extends AppCompatActivity
{
    private FirebaseAuth mLoginFireBaseAuth;
    private TextView mCreateNewAccount , mForgotPassword;
    private EditText mLoginEmail , mLoginPassword;
    private Button mSignInButton , mPhoneLogin;
    private ProgressDialog mLoadingBar;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        mLoginFireBaseAuth = FirebaseAuth.getInstance();

        initializedFields();

        mCreateNewAccount.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                sendUserToRegistrationActivity();
            }
        });

        mSignInButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                allowUserToLogIn();
            }
        });

        mPhoneLogin.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                startActivity(new Intent(LogInActivity.this , PhoneLoginActivity.class));
            }
        });

    }

    private void allowUserToLogIn()
    {
        String loginEmail = mLoginEmail.getText().toString();
        String loginPassword = mLoginPassword.getText().toString();

        if (TextUtils.isEmpty(loginEmail))
        {
            Toast.makeText(this, "Please enter the email address", Toast.LENGTH_SHORT).show();
        }
        if (TextUtils.isEmpty(loginPassword))
        {
            Toast.makeText(this, "Please enter the password", Toast.LENGTH_SHORT).show();
        }
        else
        {
            mLoadingBar.setTitle("Signing In");
            mLoadingBar.setMessage("Please wait...");
            mLoadingBar.setCanceledOnTouchOutside(true);
            mLoadingBar.show();
            mLoginFireBaseAuth.signInWithEmailAndPassword(loginEmail , loginPassword)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>()
                    {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task)
                        {
                            if (task.isSuccessful())
                            {
                                sendUserToMainActivity();
                                Toast.makeText(LogInActivity.this, "Login successfully...", Toast.LENGTH_SHORT).show();
                                mLoadingBar.dismiss();
                            }
                            else
                            {
                                String message = task.getException().toString();
                                Toast.makeText(LogInActivity.this, "Error :" + message, Toast.LENGTH_SHORT).show();
                                mLoadingBar.dismiss();
                            }
                        }
                    });
        }
    }

    private void initializedFields()
    {
        mCreateNewAccount = findViewById(R.id.createNewAccount);
        mForgotPassword = findViewById(R.id.forgotPassword);
        mLoginEmail = findViewById(R.id.loginEmail);
        mLoginPassword = findViewById(R.id.loginPassword);
        mSignInButton = findViewById(R.id.signIn);
        mLoadingBar = new ProgressDialog(this);
        mPhoneLogin = findViewById(R.id.phone_login);
    }

    private void sendUserToMainActivity()
    {
        Intent mainIntent = new Intent(LogInActivity.this , MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
    private void sendUserToRegistrationActivity()
    {
        startActivity(new Intent(LogInActivity.this , RegistrationActivity.class));
    }
}
