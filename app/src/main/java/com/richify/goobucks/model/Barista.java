package com.richify.goobucks.model;

import android.support.annotation.Nullable;

import java.security.PublicKey;
import java.util.ArrayList;

/**
 * Created by thomaslin on 04/03/2018.
 *
 */

public class Barista {

    private String displayName, email, uid, profilePictureUri;
    private Double rating;
    private ArrayList<String> menu;
    private Boolean isVerified;

    public Barista(String displayName, String email, String uid,
                   String profilePictureUri, Boolean isVerified,
                   Double rating, @Nullable ArrayList<String> menu) {
        this.displayName = displayName;
        this.email = email;
        this.uid = uid;
        this.profilePictureUri = profilePictureUri;
        this.isVerified = isVerified;
        this.rating = rating;
        this.menu = menu;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getEmail() {
        return email;
    }

    public String getUid() {
        return uid;
    }

    public Double getRating() {
        return rating;
    }

    public ArrayList<String> getMenu() {
        return menu;
    }

    public String getProfilePictureUri() {
        return profilePictureUri;
    }

    public Boolean getIsVerified() {
        return isVerified;
    }
}
