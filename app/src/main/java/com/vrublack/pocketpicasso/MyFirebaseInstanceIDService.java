package com.vrublack.pocketpicasso;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;


public class MyFirebaseInstanceIDService extends FirebaseInstanceIdService
{
    @Override
    public void onTokenRefresh()
    {
        // Get updated InstanceID token.
        String refreshedToken = FirebaseInstanceId.getInstance().getToken();
        Debug.d("Refreshed token: " + refreshedToken);

        Util.setPref(this, Const.FCM_TOKEN, refreshedToken);
    }
}
