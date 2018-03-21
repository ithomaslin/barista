package com.richify.goobucks.util;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.google.firebase.messaging.FirebaseMessaging;

/**
 * Created by thomaslin on 18/03/2018.
 *
 */

public class InstanceIdService extends FirebaseInstanceIdService {

    private static final String TAG = "InstanceIdService";

    @Override
    public void onTokenRefresh() {
        String token = FirebaseInstanceId.getInstance().getToken();
        FirebaseMessaging.getInstance()
                .subscribeToTopic("order_status");
    }
}
