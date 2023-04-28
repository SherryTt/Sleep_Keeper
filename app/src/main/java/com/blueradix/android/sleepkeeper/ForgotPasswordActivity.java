package com.blueradix.android.sleepkeeper;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ForgotPasswordActivity extends AppCompatActivity {

    private static final String TAG = "ForgotPasswordActivity";
    public FirebaseAuth mAuth;
    Button resetPassBtn;
    EditText registeredEmailInput;
    TextView linkRelogin;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);


        registeredEmailInput = findViewById(R.id.txtResetEmailInput);
        resetPassBtn = findViewById(R.id.ResetBtn);
        linkRelogin = findViewById(R.id.lnkRelogin);
        mAuth = FirebaseAuth.getInstance();


        resetPassBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mAuth.sendPasswordResetEmail(registeredEmailInput.getText().toString())
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) Log.d(TAG, "Email sent.");


                                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                                        ForgotPasswordActivity.this);

                                // set title
                                alertDialogBuilder.setTitle("Reset Password");

                                // set dialog message
                                alertDialogBuilder
                                        .setMessage("A Reset Password Link Is Sent To Your Registered EmailID")
                                        .setCancelable(false)
                                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                Intent loginActivity = new Intent(ForgotPasswordActivity.this, LoginActivity.class);
                                                startActivity(loginActivity);
                                                ForgotPasswordActivity.this.finish();
                                            }
                                        });

                                AlertDialog alertDialog = alertDialogBuilder.create();
                                alertDialog.show();

                            }
                        });
            }
        });


        //Below code is to make text view clickable link
        String text = "Back to Login";
        SpannableString span = new SpannableString(text);
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View view) {
                Intent loginActivity = new Intent(ForgotPasswordActivity.this, LoginActivity.class);
                startActivity(loginActivity);
                ForgotPasswordActivity.this.finish();
            }
        };
        span.setSpan(clickableSpan,8,13, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        linkRelogin.setText(span);
        linkRelogin.setMovementMethod(LinkMovementMethod.getInstance());


    }
}
