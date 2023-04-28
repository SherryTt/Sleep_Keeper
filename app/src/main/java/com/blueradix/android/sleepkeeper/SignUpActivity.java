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
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;



public class SignUpActivity extends AppCompatActivity {

    private static final  String TAG = "SignUpActivity";
    public FirebaseAuth mAuth;
    Button signupBtn;
    EditText signupEmailInput;
    EditText signupPassInput;
    TextView errorPassView;
    TextView errorEmailView;
    TextView linkLogin;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sisgnup);

        linkLogin = findViewById(R.id.lnkLogin);
        mAuth = FirebaseAuth.getInstance();
        signupBtn = findViewById(R.id.btnSignup);
        signupEmailInput = findViewById(R.id.txtSignupEmailInput);
        signupPassInput = findViewById(R.id.txtSignupPwdInput);
        errorPassView = findViewById(R.id.txtSignUpPassError);
        errorEmailView = findViewById(R.id.txtSignUpEmailError);

        signupBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                errorEmailView.setText("");
                errorPassView.setText("");
                //Verification of each text box
                if((signupEmailInput.getText().toString().contentEquals(""))&&(signupPassInput.getText().toString().contentEquals(""))){
                    errorPassView.setText("Email and Password cant be empty");
                }
                else if (signupEmailInput.getText().toString().contentEquals("")) {

                    errorEmailView.setText("Email cannot be empty");

                } else if (signupPassInput.getText().toString().contentEquals("")) {

                    errorPassView.setText("Password cannot be empty");

                } else {

                    errorEmailView.setText("");
                    errorPassView.setText("");
                    mAuth.createUserWithEmailAndPassword(signupEmailInput.getText().toString(), signupPassInput.getText().toString()).addOnCompleteListener(SignUpActivity.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {

                            if (task.isSuccessful()) {
                                // Sign in success, update UI with the signed-in user's information
                                Log.d(TAG, "createUserWithEmail:success");
                                FirebaseUser user = mAuth.getCurrentUser();

                                try {
                                    //Send verification email
                                    if (user != null)
                                        user.sendEmailVerification()
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {
                                                            Log.d(TAG, "Email sent.");

                                                            Intent loginIntent = new Intent(SignUpActivity.this,LoginActivity.class);
                                                            startActivity(loginIntent);
                                                            SignUpActivity.this.finish();


                                                            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                                                                    SignUpActivity.this);

                                                            // set title
                                                            alertDialogBuilder.setTitle("Please Verify Your EmailID");

                                                            // set dialog message
                                                            alertDialogBuilder
                                                                    .setMessage("A verification Email Is Sent To Your Registered EmailID, please click on the link and Sign in again!")
                                                                    .setCancelable(false)
                                                                    .setPositiveButton("Sign In", new DialogInterface.OnClickListener() {
                                                                        public void onClick(DialogInterface dialog, int id) {

                                                                            Intent loginIntent = new Intent(SignUpActivity.this,LoginActivity.class);
                                                                            startActivity(loginIntent);
                                                                            SignUpActivity.this.finish();
                                                                        }
                                                                    });

                                                            // create alert dialog
                                                            AlertDialog alertDialog = alertDialogBuilder.create();
                                                            alertDialog.show();

                                                        }
                                                    }
                                                });

                                } catch (Exception e) {
                                    errorPassView.setText(e.getMessage());
                                }
                            } else {
                                // If sign in fails, display a message to the user.
                                Log.w(TAG, "createUserWithEmail:failure", task.getException());
                                Toast.makeText(SignUpActivity.this, "Authentication failed.",
                                        Toast.LENGTH_SHORT).show();

                                if (task.getException() != null) {
                                    errorPassView.setText(task.getException().getMessage());
                                }

                            }

                        }
                    });

                }

            }
        });

        //Below code is to make text view clickable link
        String text = "Already have an account? Login here";
        SpannableString span = new SpannableString(text);
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View view) {
                Intent loginActivity = new Intent(SignUpActivity.this, LoginActivity.class);
                startActivity(loginActivity);
                SignUpActivity.this.finish();
            }
        };
        span.setSpan(clickableSpan,25,30, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        linkLogin.setText(span);
        linkLogin.setMovementMethod(LinkMovementMethod.getInstance());

    }
}
