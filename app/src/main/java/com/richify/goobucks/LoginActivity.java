package com.richify.goobucks;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Arrays;
import dmax.dialog.SpotsDialog;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    private static final String EMAIL = "email";
    private FirebaseAuth mAuth;
    private AlertDialog dialog;
    private SharedPreferences mSharedPreference;

    AppEventsLogger logger;
    LoginButton fbLoginButton;
    CallbackManager callbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        dialog = new SpotsDialog(LoginActivity.this, R.style.CustomLoading);

        final com.facebook.AccessToken loginToken = com.facebook.AccessToken.getCurrentAccessToken();
        if (loginToken != null) {
            launchHomeActivity();
        }

        mAuth = FirebaseAuth.getInstance();
        mSharedPreference = this.getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);

        logger = AppEventsLogger.newLogger(this);
        fbLoginButton = findViewById(R.id.facebook_login_button);
        fbLoginButton.setReadPermissions(Arrays.asList(EMAIL));

        // Login button callback registration
        callbackManager = CallbackManager.Factory.create();
        fbLoginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                Log.d(TAG, "facebook:onSuccess:" + loginResult);
                dialog.show();
                handleFacebookAccessToken(loginResult.getAccessToken());
            }

            @Override
            public void onCancel() {
                Log.d(TAG, "facebook:onCancel");
            }

            @Override
            public void onError(FacebookException error) {
                Log.d(TAG, "facebook:onError", error);
                logger.logEvent(TAG + ":facebook:onError: " + error);
                String toastMessage = error.getMessage();
                Toast.makeText(LoginActivity.this, toastMessage, Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    public void launchHomeActivity() {
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
        finish();
    }

    private void handleFacebookAccessToken(AccessToken token) {
        Log.d(TAG, "handleFacebookAccessToken:" + token);

        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential).addOnSuccessListener(
                this, new OnSuccessListener<AuthResult>() {
            @Override
            public void onSuccess(AuthResult authResult) {
                FirebaseUser user = authResult.getUser();
                LoginAsyncOperation asyncOperation = new LoginAsyncOperation(LoginActivity.this);
                asyncOperation.onPreExecute();
                asyncOperation.execute(user);

                SharedPreferences.Editor editor = mSharedPreference.edit();
                editor.putString("userEmail", user.getEmail());
                editor.putString("userName", user.getDisplayName());
                editor.apply();

                dialog.dismiss();
            }
        });
    }

}
