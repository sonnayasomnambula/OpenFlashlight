package org.sonnayasomnambula.openflashlight;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import java.util.ArrayList;


public class MainActivity extends Activity {
    static final String LOG_TAG = "OF MainActivity";

    boolean readyToFinish = true;

    BroadcastReceiver serviceMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (! Actions.MESSAGE.equals(intent.getAction())) return;

            final int id = intent.getIntExtra(Actions.Message.EXTRA_ID, 0);
            switch (id) {
                case Actions.Message.ID_FAILED_TO_START:
                    showToastAndFinish(intent.getStringExtra(Actions.Message.EXTRA_TEXT));
                    break;
                case Actions.Message.ID_STARTED_SUCCESSFULLY:
                    showToastAndFinish(getString(R.string.service_running));
                    break;
                case Actions.Message.ID_ABOUT_TO_STOP:
                    showToastAndFinish(getString(R.string.about_to_stop));
                    break;
            }
        }
    };


    private void showToastAndFinish(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        Log.d(LOG_TAG, "Message is \"" + message + "\" ready to finish is " + readyToFinish);
        if (readyToFinish) finish();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(LOG_TAG, "create " + toString());

        IntentFilter messagesOnly = new IntentFilter(Actions.MESSAGE);
        registerReceiver(serviceMessageReceiver, messagesOnly);

        PermissionListener permissionListener = new PermissionListener() {
            @Override
            public void onPermissionGranted() {
                finish();
            }

            @Override
            public void onPermissionDenied(ArrayList<String> deniedPermissions) {
                Toast.makeText(MainActivity.this,
                               getString(R.string.camera_permission_missing),
                               Toast.LENGTH_LONG)
                        .show();
                finish();
            }
        };

        new TedPermission(this)
                .setPermissionListener(permissionListener)
                .setDeniedMessage(R.string.on_permission_denied_message)
                .setPermissions(Manifest.permission.CAMERA)
                .setGotoSettingButtonText(R.string.setting)
                .setDeniedCloseButtonText(R.string.close)
                .check();
    }


    @Override
    protected void onResume() {
        super.onResume();

        Log.d(LOG_TAG, "resume " + toString());

        startService(new Intent(this, LightService.class));
    }


    @Override
    protected void onDestroy() {
        unregisterReceiver(serviceMessageReceiver);
        super.onDestroy();
    }
}
