package com.richify.goobucks.model;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.security.PublicKey;
import java.util.ArrayList;

/**
 * Created by thomaslin on 04/03/2018.
 *
 */

public class Barista {

    private String displayName, email, uid, profilePictureUri, description, location;
    private Double rating;
    private Boolean isVerified;

    public Barista() {}

    public Barista(String displayName,
                   String email,
                   String uid,
                   String profilePictureUri,
                   String description,
                   String location,
                   Double rating,
                   Boolean isVerified) {

        this.displayName = displayName;
        this.email = email;
        this.uid = uid;
        this.profilePictureUri = profilePictureUri;
        this.description = description;
        this.location = location;
        this.rating = rating;
        this.isVerified = isVerified;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String setDisplayName(String _name) {
        this.displayName = _name;
        return displayName;
    }

    public String getEmail() {
        return email;
    }

    public String setEmail(String _email) {
        this.email = _email;
        return email;
    }

    public String getUid() {
        return uid;
    }

    public String setUid(String _uid) {
        this.uid = _uid;
        return uid;
    }

    public String getProfilePictureUri() {
        return profilePictureUri;
    }

    public String getDescription() {
        return description;
    }

    public String getLocation() {
        return location;
    }

    public Double getRating() {
        return rating;
    }

    public Boolean getIsVerified() {
        return isVerified;
    }
}
