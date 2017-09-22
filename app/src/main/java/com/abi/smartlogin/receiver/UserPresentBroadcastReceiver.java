package com.abi.smartlogin.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.abi.smartlogin.view.LockActivity;

public class UserPresentBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        // Start the login activity after screen unlock
        if (intent.getAction().equals(Intent.ACTION_USER_PRESENT)) {
            Intent activityIntent = new Intent(context, LockActivity.class);
            activityIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(activityIntent);
        }
    }

}