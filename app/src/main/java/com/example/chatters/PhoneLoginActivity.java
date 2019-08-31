package com.example.chatters;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class PhoneLoginActivity extends AppCompatActivity
{
    private Button sendVerificationCode , mVerifyCode;
    private EditText inputPhoneNumber , inputVerificationNumber;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;
    private String mVerificationID;
    private PhoneAuthProvider.ForceResendingToken mResendingToken;
    private FirebaseAuth phoneLoginFireBaseAuth;
    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_login);

        phoneLoginFireBaseAuth = FirebaseAuth.getInstance();

        initializedFields();

        sendVerificationCode.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                String phoneNumber = inputPhoneNumber.getText().toString();

                if (TextUtils.isEmpty(phoneNumber))
                {
                    Toast.makeText(PhoneLoginActivity.this, "Phone Number is required..", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    mProgressDialog.setTitle("Phone Verification");
                    mProgressDialog.setMessage("Please wait, While we are authenticating your phone number...");
                    mProgressDialog.setCanceledOnTouchOutside(false);
                    mProgressDialog.show();
                    PhoneAuthProvider.getInstance().verifyPhoneNumber(phoneNumber , 60, TimeUnit.SECONDS,PhoneLoginActivity.this , mCallbacks);
                }
            }
        });


        mVerifyCode.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                sendVerificationCode.setVisibility(View.INVISIBLE);
                inputPhoneNumber.setVisibility(View.INVISIBLE);

                String verificationCode = inputVerificationNumber.getText().toString();
                
                if (TextUtils.isEmpty(verificationCode))
                {
                    Toast.makeText(PhoneLoginActivity.this, "Please write verification code", Toast.LENGTH_SHORT).show();
                }
                else
                {

                    mProgressDialog.setTitle("Verification Code");
                    mProgressDialog.setMessage("Please wait, While we are verifying verification code...");
                    mProgressDialog.setCanceledOnTouchOutside(false);
                    mProgressDialog.show();
                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationID, verificationCode);
                    signInWithPhoneAuthCredential(credential);
                }
            }
        });

        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks()
        {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential)
            {
                signInWithPhoneAuthCredential(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e)
            {
                mProgressDialog.dismiss();
                Toast.makeText(PhoneLoginActivity.this, "Invalid phone number, Please enter correct phone number...", Toast.LENGTH_SHORT).show();

                sendVerificationCode.setVisibility(View.VISIBLE);
                inputPhoneNumber.setVisibility(View.VISIBLE);

                mVerifyCode.setVisibility(View.INVISIBLE);
                inputVerificationNumber.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onCodeSent(String verificationID, PhoneAuthProvider.ForceResendingToken forceResendingToken)
            {
                mVerificationID = verificationID;
                mResendingToken = forceResendingToken;

                mProgressDialog.dismiss();
                Toast.makeText(PhoneLoginActivity.this, "Enter the code send to your phone number...", Toast.LENGTH_SHORT).show();

                sendVerificationCode.setVisibility(View.INVISIBLE);
                inputPhoneNumber.setVisibility(View.INVISIBLE);

                mVerifyCode.setVisibility(View.VISIBLE);
                inputVerificationNumber.setVisibility(View.VISIBLE);
            }
        };
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential)
    {
        phoneLoginFireBaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(PhoneLoginActivity.this, new OnCompleteListener<AuthResult>()
                {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task)
                    {
                        if (task.isSuccessful())
                        {
                            mProgressDialog.dismiss();
                            Toast.makeText(PhoneLoginActivity.this, "Congratulation, you're logged in successfully...", Toast.LENGTH_SHORT).show();
                            sendUserToMainActivity();
                        }
                        else
                        {
                            String message = task.getException().toString();
                            Toast.makeText(PhoneLoginActivity.this, "Error : " + message, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void initializedFields()
    {
        sendVerificationCode = findViewById(R.id.send_verification_code);
        mVerifyCode = findViewById(R.id.verify_code);
        inputPhoneNumber = findViewById(R.id.phone_number);
        inputVerificationNumber = findViewById(R.id.verification_code);
        mProgressDialog = new ProgressDialog(this);
    }


    private void sendUserToMainActivity()
    {
        startActivity(new Intent(PhoneLoginActivity.this , MainActivity.class));
        finish();
    }
}
