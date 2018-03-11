package com.richify.goobucks;

import android.app.Activity;
import android.os.AsyncTask;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.richify.goobucks.model.User;

/**
 * Created by thomaslin on 03/03/2018.
 *
 * Handling first login user data insert
 */

public class LoginAsyncOperation extends AsyncTask<FirebaseUser, Void, Void> {

    // Declare database ref
    private DatabaseReference mDatabaseRef;
    private LoginActivity mLoginActivity;

    public LoginAsyncOperation(LoginActivity activity) {
        super();
        mLoginActivity = activity;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        mLoginActivity.launchHomeActivity();
    }

    @Override
    protected Void doInBackground(FirebaseUser... users) {
        mDatabaseRef = FirebaseDatabase.getInstance().getReference();

        final String uid = users[0].getUid();
        final String name = users[0].getDisplayName();
        final String email = users[0].getEmail();
        final Integer orderNumber = 0;
        final Boolean isBarista = false;

        mDatabaseRef.child("users").child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.exists()) {
                    writeNewUser(uid, name, email, orderNumber, isBarista);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
    }

    private void writeNewUser(String uid, String name, String email, Integer orderNumber, Boolean isBarista) {
        User user = new User(name, email, orderNumber, isBarista);
        mDatabaseRef.child("users").child(uid).setValue(user);
    }
}
