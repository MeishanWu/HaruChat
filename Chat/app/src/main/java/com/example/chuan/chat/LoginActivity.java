package com.example.chuan.chat;

import android.content.ComponentName;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

/**
 * see tutorial https://www.youtube.com/watch?v=9ARoMRd1kXo
 */
public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    Button sign_in, sign_up;
    EditText email, password;
    LinearLayout activity_login;
    private FirebaseAuth auth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        // Set up the login form.
        sign_in = (Button)findViewById(R.id.email_sign_in_button);
        sign_up = (Button)findViewById(R.id.email_sign_up_button);
        email = (EditText)findViewById(R.id.email);
        password = (EditText)findViewById(R.id.password);
        activity_login = (LinearLayout)findViewById(R.id.activity_login);

        sign_up.setOnClickListener(this);
        sign_in.setOnClickListener(this);

        //firebase auth
        auth = FirebaseAuth.getInstance();

        //sign out
        if(auth.getCurrentUser()!= null){
            auth.signOut();
        }
    }

    /**
     * override onclick method for the two buttons
     * @param view
     */
    @Override
    public void onClick(View view){
        if(view.getId() == R.id.email_sign_up_button){
            try{
                signupUser(email.getText().toString(), password.getText().toString());
            }catch (Exception e){
                Log.e("sign up", "error");
            }
        }
        else if(view.getId() == R.id.email_sign_in_button){
            try {
                loginUser(email.getText().toString(), password.getText().toString());
            }catch (Exception e){
                Log.e("sign in", "error");
            }
        }
    }


    /**
     * helper function for signing up
     * @param email
     * @param password
     */
    private void signupUser(final String email, final String password) {
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(!task.isSuccessful()){
                            Toast.makeText(getApplicationContext(),
                                    task.getException().getMessage(), Toast.LENGTH_LONG).show();
                        }else{
                            //present error message
                            Toast.makeText(getApplicationContext(),
                                    "Register success", Toast.LENGTH_LONG).show();
                            Intent i = new Intent();
                            i.setComponent(new ComponentName("jp.live2d.sample", "jp.live2d.sample.MainHaruActivity"));
                            i.putExtra("email", email);
                            i.putExtra("password", password);
                            startActivity(i);
                        }
                    }
                });

    }

    /**
     * helper function for signing in
     * @param email
     * @param password
     */
    private void loginUser(final String email, final String password) {
        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(!task.isSuccessful()){
                            //present error message
                            Toast.makeText(getApplicationContext(),
                                    task.getException().getMessage(), Toast.LENGTH_LONG).show();

                        }else{
                            Toast.makeText(getApplicationContext(), "Login success", Toast.LENGTH_LONG).show();
                            Intent i = new Intent();
                            i.setComponent(new ComponentName("jp.live2d.sample", "jp.live2d.sample.MainHaruActivity"));
                            i.putExtra("email", email);
                            i.putExtra("password", password);
                            startActivity(i);
                        }
                    }
                });
    }
}

