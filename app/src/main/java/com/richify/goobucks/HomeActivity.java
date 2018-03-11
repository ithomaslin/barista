package com.richify.goobucks;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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
import com.richify.goobucks.Fragments.HistoryFragment;
import com.richify.goobucks.Fragments.HomeActivityFragment;
import com.richify.goobucks.Fragments.dummy.DummyContent;
import com.richify.goobucks.model.Barista;
import com.richify.goobucks.model.User;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class HomeActivity extends AppCompatActivity
        implements HistoryFragment.OnListFragmentInteractionListener {

    private static final String TAG = "HomeActivity";
    private static final int becomeBaristaIdentifier = 100;
    private static final int logoutIdentifier = -1;
    private FirebaseUser fUser;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabaseRef;
    private AccountHeader headerResult;
    private SharedPreferences mSharedPreference;
    private Drawer result;
    private IProfile mProfile;
    private Profile fbProfile;
    private List<IDrawerItem> stickyDrawerItems;
    private SharedPreferences.Editor editor;
    private Toolbar toolbar;

    ProfileTracker profileTracker;
    CallbackManager callbackManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mAuth = FirebaseAuth.getInstance();
        stickyDrawerItems = new ArrayList<IDrawerItem>();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference();
        mSharedPreference = this.getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE);

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

        fUser = mAuth.getCurrentUser();
        if (fUser != null) {
            mDatabaseRef.child("users").child(fUser.getUid()).child("isBarista").addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Boolean isBarista = (Boolean) dataSnapshot.getValue();
                    if (isBarista != null && !isBarista) {
                        result.addStickyFooterItemAtPosition(
                                new SecondaryDrawerItem().withName(R.string.become_barista)
                                        .withIcon(FontAwesome.Icon.faw_hand_paper)
                                        .withIdentifier(becomeBaristaIdentifier),
                                0
                        );
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        if (savedInstanceState == null) {
            Fragment fragment = null;
            Class fragmentClass = HomeActivityFragment.class;
            try {
                fragment = (Fragment) fragmentClass.newInstance();
            } catch (Exception e) {
                e.printStackTrace();
            }
            FragmentManager fragmentManager = getSupportFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.Content, fragment).commit();
        }

        profileTracker = new ProfileTracker() {
            @Override
            protected void onCurrentProfileChanged(Profile oldProfile, Profile currentProfile) {
                if (currentProfile != null) {
                    fbProfile = currentProfile;
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

        initDrawer(savedInstanceState);

    }

    private void initDrawer(Bundle savedInstanceState) {

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
                        Class fragmentClass;

                        switch ((int) drawerItem.getIdentifier()) {
                            case 0:
                                fragmentClass = HomeActivityFragment.class;
                                switchFragment(fragmentClass);
                                return true;
                            case 1:
                                fragmentClass = HistoryFragment.class;
                                switchFragment(fragmentClass);
                                return true;
                            case 2:
                                return true;
                            case becomeBaristaIdentifier:
                                Log.i(TAG, "OnDrawerItemClickListener:register as barista");
                                new MaterialDialog.Builder(HomeActivity.this)
                                        .title(R.string.app_name)
                                        .theme(Theme.LIGHT)
                                        .content(R.string.become_barista_confirmation)
                                        .positiveText(R.string.confirm_positive)
                                        .positiveColor(
                                                ContextCompat.getColor(
                                                        HomeActivity.this,
                                                        R.color.primary)
                                        )
                                        .negativeText(R.string.confirm_negative)
                                        .negativeColor(
                                                ContextCompat.getColor(
                                                        HomeActivity.this,
                                                        R.color.primary)
                                        )
                                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                                            @Override
                                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                                Log.i(TAG, "MaterialDialog.SingleButtonCallback:positive");
                                                final FirebaseUser user = mAuth.getCurrentUser();
                                                final String userId;
                                                final String displayName;
                                                final String email;
                                                final Double rating = 0.0;
                                                final String profilePictureUri = mProfile.getIcon().getUri().toString();
                                                final Boolean isVerified = false;

                                                if (user != null) {
                                                    userId = user.getUid();
                                                    displayName = user.getDisplayName();
                                                    email = user.getEmail();

                                                    mDatabaseRef.child("barista").child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                                                        @Override
                                                        public void onDataChange(DataSnapshot dataSnapshot) {
                                                            if (!dataSnapshot.exists()) {
                                                                writeNewBarista(displayName, email,
                                                                        userId, profilePictureUri,
                                                                        isVerified, rating);
                                                                editor.putBoolean("isBarista", Boolean.FALSE);

                                                                mDatabaseRef.child("users")
                                                                        .child(userId)
                                                                        .child("isBarista")
                                                                        .setValue(true);

                                                                result.removeStickyFooterItemAtPosition(0);
                                                            }
                                                        }

                                                        @Override
                                                        public void onCancelled(DatabaseError databaseError) {

                                                        }
                                                    });
                                                }
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
                            case logoutIdentifier:
                                Log.i(TAG, "OnDrawerItemClickListener:Logging user out");
                                onLogout();
                                return true;
                            default:
                                return false;
                        }
                    }
                })
                .addStickyDrawerItems(
                        new SecondaryDrawerItem().withName(R.string.log_out)
                                .withIcon(FontAwesome.Icon.faw_sign_out_alt)
                                .withIdentifier(logoutIdentifier)
                )
                .withSavedInstance(savedInstanceState)
                .build();
    }

    private void switchFragment(Class fragmentClass) {
        Fragment fragment = null;
        try {
            fragment = (Fragment) fragmentClass.newInstance();
        } catch (Exception e) {
            e.printStackTrace();
        }
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.Content, fragment).commit();
        result.closeDrawer();
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
        super.onBackPressed();
    }

    private void populateProfileInfo(final Profile profile) {
        mProfile = new ProfileDrawerItem()
                .withName(mSharedPreference.getString("userName", "Full name"))
                .withEmail(mSharedPreference.getString("userEmail", "Email"))
                .withIcon(profile.getProfilePictureUri(200, 200));
    }

    public void onLogout() {
        LoginManager.getInstance().logOut();
        SharedPreferences.Editor editor = mSharedPreference.edit();
        editor.clear().apply();
        launchLoginActivity();

        setResult(RESULT_OK);
        finish();
    }

    private void launchLoginActivity() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onListFragmentInteraction(DummyContent.DummyItem item) {

    }

    private void writeNewBarista(String displayName, String email,
                                 String uid,
                                 String profilePictureUri,
                                 Boolean isVerified,
                                 Double rating) {
        Barista barista = new Barista(displayName, email, uid, profilePictureUri, isVerified, rating, null);
        mDatabaseRef.child("barista").child(uid).setValue(barista);
    }

}
