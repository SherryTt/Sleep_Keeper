package com.blueradix.android.sleepkeeper;

import static android.content.ContentValues.TAG;

import android.content.Intent;
import android.os.Bundle;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    EditText emailTxtInput;
    EditText passwordTxtInput;
    Button Loginbtn;
    Button sendAgainBtn;
    TextView textViewError;
    TextView textViewForgotpass;
    TextView linkSignup;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        linkSignup = findViewById(R.id.lnkSignup);
        textViewForgotpass = findViewById(R.id.textViewForgot);
        emailTxtInput = findViewById(R.id.txtEmailInput);
        passwordTxtInput = findViewById(R.id.txtPwdInput);
        Loginbtn = findViewById(R.id.LoginBtn);
        textViewError = findViewById(R.id.textViewError);
        sendAgainBtn = findViewById(R.id.sendAgainBtn);
        mAuth = FirebaseAuth.getInstance();
        sendAgainBtn.setVisibility(View.INVISIBLE);


        Loginbtn.setOnClickListener((view) -> {

            if((emailTxtInput.getText().toString().contentEquals(""))&&(passwordTxtInput.getText().toString().contentEquals(""))){

                textViewError.setText("Email and Password cant be empty");

            } else if (emailTxtInput.getText().toString().contentEquals("")) {

                textViewError.setText("Email cant be empty");

            } else if (passwordTxtInput.getText().toString().contentEquals("")) {

                textViewError.setText("Password cant be empty");

            }else {
                mAuth.signInWithEmailAndPassword(emailTxtInput.getText().toString(), passwordTxtInput.getText().toString())
                            .addOnCompleteListener(LoginActivity.this,(task) -> {

                                if (task.isSuccessful()) {
                                    // Sign in success, update UI with the signed-in user's information
                                    Log.d(TAG, "signInWithEmail:success");

                                    FirebaseUser user = mAuth.getCurrentUser();

                                    if (user != null) {
                                        if (user.isEmailVerified()) {

                                            System.out.println("Email Verified : " + user.isEmailVerified());
                                            Intent StartMainActivity = new Intent(LoginActivity.this, MainActivity.class);
                                            setResult(RESULT_OK, null);
                                            startActivity(StartMainActivity);
                                            LoginActivity.this.finish();
                                        }
                                        else {

                                            sendAgainBtn.setVisibility(View.VISIBLE);
                                            textViewError.setText("Please Verify your EmailID and Login");

                                            sendAgainBtn.setOnClickListener(new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    FirebaseUser user = mAuth.getCurrentUser();
                                                    user.sendEmailVerification()
                                                            .addOnCompleteListener(task -> {
                                                                if (task.isSuccessful()) {
                                                                    Toast.makeText(LoginActivity.this, "Verification email sent", Toast.LENGTH_SHORT).show();
                                                                } else {
                                                                    Toast.makeText(LoginActivity.this, "Failed to send verification email", Toast.LENGTH_SHORT).show();
                                                                }
                                                            });
                                                }
                                            });
                                        }
                                    }

                                } else {
                                    // If sign in fails, display a message to the user.
                                    Log.w(TAG, "signInWithEmail:failure", task.getException());
                                    Toast.makeText(LoginActivity.this, "Authentication failed.",
                                            Toast.LENGTH_SHORT).show();
                                    if (task.getException() != null) {
                                        textViewError.setText(task.getException().getMessage());
                                    }

                                }
                });

        }
    });


    //Below code is to make text view clickable link
        String text1 = "Forgot password?";
        SpannableString span1 = new SpannableString(text1);

        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View view) {
                Intent forgotPasswordActivity = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
                startActivity(forgotPasswordActivity);
                LoginActivity.this.finish();
            }
        };
        span1.setSpan(clickableSpan,0,15, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        textViewForgotpass.setText(span1);
        textViewForgotpass.setMovementMethod(LinkMovementMethod.getInstance());



        String text2 = "Don't have an account? Sign up here";
        SpannableString span2 = new SpannableString(text2);

        ClickableSpan clickableSpan1 = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View view) {
                Intent signupActivity = new Intent(LoginActivity.this, SignUpActivity.class);
                startActivity(signupActivity);
                LoginActivity.this.finish();
            }
        };

        span2.setSpan(clickableSpan1,23,30, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        linkSignup.setText(span2);
        linkSignup.setMovementMethod(LinkMovementMethod.getInstance());
    }
}
