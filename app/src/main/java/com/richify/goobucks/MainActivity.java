package com.richify.goobucks;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.Profile;
import com.facebook.ProfileTracker;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mikepenz.fontawesome_typeface_library.FontAwesome;
import com.mikepenz.materialdrawer.AccountHeader;
import com.mikepenz.materialdrawer.AccountHeaderBuilder;
import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.ProfileDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IProfile;
import com.mikepenz.materialdrawer.util.AbstractDrawerImageLoader;
import com.mikepenz.materialdrawer.util.DrawerImageLoader;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private DatabaseReference mDatabaseRef;
    private AccountHeader headerResult;
    private Drawer result;
    private IProfile mProfile;

    ProfileTracker profileTracker;
    CallbackManager callbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerImageLoader.init(new AbstractDrawerImageLoader() {
            @Override
            public void set(ImageView imageView, Uri uri, Drawable placeholder) {
                Picasso.with(imageView.getContext()).load(uri).placeholder(placeholder).into(imageView);
            }

            @Override
            public void cancel(ImageView imageView) {
                Picasso.with(imageView.getContext()).cancelRequest(imageView);
            }
        });

        mDatabaseRef = FirebaseDatabase.getInstance().getReference();
        profileTracker = new ProfileTracker() {
            @Override
            protected void onCurrentProfileChanged(Profile oldProfile, Profile currentProfile) {
                if (currentProfile != null) {
                    populateProfileInfo(currentProfile);
                }
            }
        };

        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        if (accessToken != null) {
            Profile currentProfile = Profile.getCurrentProfile();
            if (currentProfile != null) {
                populateProfileInfo(currentProfile);
                headerResult = new AccountHeaderBuilder()
                        .withActivity(this)
                        .withHeaderBackground(R.drawable.header)
                        .addProfiles(mProfile)
                        .withSavedInstance(savedInstanceState)
                        .build();
            } else {
                Profile.fetchProfileForCurrentAccessToken();
            }

        }

        result = new DrawerBuilder()
                .withActivity(this)
                .withToolbar(toolbar)
                .withAccountHeader(headerResult)
                .addDrawerItems(
                        new PrimaryDrawerItem().withName("Home")
                                .withIcon(FontAwesome.Icon.faw_home)
                                .withIdentifier(0),
                        new PrimaryDrawerItem().withName(R.string.history)
                                .withIcon(FontAwesome.Icon.faw_history)
                                .withIdentifier(1),
                        new PrimaryDrawerItem().withName(R.string.donate)
                                .withIcon(FontAwesome.Icon.faw_credit_card)
                                .withIdentifier(2)
                )
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {

                        Log.i(TAG, "identifier: " + drawerItem.getIdentifier());

                        switch ((int) drawerItem.getIdentifier()) {
                            case 0:
                                result.closeDrawer();
                                return true;
                            case 1:
                                return true;
                            case 2:
                                return true;
                            case 100:
                                Log.i(TAG, "OnDrawerItemClickListener:register as barista");
                                new MaterialDialog.Builder(MainActivity.this)
                                        .title(R.string.app_name)
                                        .theme(Theme.LIGHT)
                                        .content(R.string.become_barista_confirmation)
                                        .positiveText(R.string.confirm_positive)
                                        .positiveColor(
                                                ContextCompat.getColor(
                                                        MainActivity.this,
                                                        R.color.buttonText)
                                        )
                                        .negativeText(R.string.confirm_negative)
                                        .negativeColor(
                                                ContextCompat.getColor(
                                                        MainActivity.this,
                                                        R.color.buttonText)
                                        )
                                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                                            @Override
                                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                                Log.i(TAG, "MaterialDialog.SingleButtonCallback:positive");
                                            }
                                        })
                                        .onNegative(new MaterialDialog.SingleButtonCallback() {
                                            @Override
                                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                                Log.i(TAG, "MaterialDialog.SingleButtonCallback:negative");
                                            }
                                        })
                                        .show();
                                return true;
                            case -1:
                                Log.i(TAG, "OnDrawerItemClickListener:Logging user out");
                                onLogout();
                                return true;
                            default:
                                return false;
                        }
                    }
                })
                .addStickyDrawerItems(
                        new SecondaryDrawerItem().withName(R.string.become_barista)
                                .withIcon(FontAwesome.Icon.faw_hand_paper)
                                .withIdentifier(100),
                        new SecondaryDrawerItem().withName(R.string.log_out)
                                .withIcon(FontAwesome.Icon.faw_sign_out_alt)
                                .withIdentifier(-1)
                )
                .withSavedInstance(savedInstanceState)
                .build();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Read from the database
        mDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        profileTracker.startTracking();
    }


    @Override
    public void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        super.onSaveInstanceState(outState, outPersistentState);
    }

    @Override
    public void onBackPressed() {
        //handle the back press :D close the drawer first and if the drawer is closed close the activity
        if (result != null && result.isDrawerOpen()) {
            result.closeDrawer();
        } else {
            super.onBackPressed();
        }
    }

    private void populateProfileInfo(Profile profile) {
        callbackManager = CallbackManager.Factory.create();
        Set permissions = AccessToken.getCurrentAccessToken().getPermissions();

        if (permissions.contains("user_location")) {
            fetchLocation();
        } else {
            LoginManager loginManager = LoginManager.getInstance();
            loginManager.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
                @Override
                public void onSuccess(LoginResult loginResult) {
                    fetchLocation();
                }

                @Override
                public void onCancel() {
                    String permissionMessage = getResources()
                            .getString(R.string.location_permission_message);
                    Toast.makeText(MainActivity.this, permissionMessage, Toast.LENGTH_LONG)
                            .show();
                }

                @Override
                public void onError(FacebookException error) {

                }
            });
            loginManager.logInWithReadPermissions(this, Arrays.asList("user_location"));
        }

        mProfile = new ProfileDrawerItem()
                .withName(profile.getName())
                .withIcon(profile.getProfilePictureUri(200, 200));

    }

    private void fetchLocation() {
        Bundle parameters = new Bundle();
        parameters.putString("fields", "location");

        new GraphRequest(
                AccessToken.getCurrentAccessToken(),
                "/me",
                parameters,
                HttpMethod.GET,
                new GraphRequest.Callback() {
                    @Override
                    public void onCompleted(GraphResponse response) {
                        if (response.getError() != null) {
                            Toast.makeText(MainActivity.this,
                                    response.getError().getErrorMessage(),
                                    Toast.LENGTH_LONG).show();
                            return;
                        }

                        JSONObject jsonResponse = response.getJSONObject();
                        try {
                            JSONObject locationObj = jsonResponse.getJSONObject("location");
                            String locationString = locationObj.getString("name");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
        ).executeAsync();
    }

    public void onLogout() {
        LoginManager.getInstance().logOut();
        launchLoginActivity();

        setResult(RESULT_OK);
        finish();
    }

    private void launchLoginActivity() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}
