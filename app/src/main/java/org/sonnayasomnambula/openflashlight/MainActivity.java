package org.sonnayasomnambula.openflashlight;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.commonsware.android.lockme.AdminReceiver;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import java.util.ArrayList;


public class MainActivity extends Activity {
    static final String LOG_TAG = "OF MainActivity";
    final static int DEVICE_ADMIN_REQUEST = 2;

    boolean readyToFinish = true;
    boolean adminRequestComplete;

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
                requestDeviceAdmin();
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


    private void requestDeviceAdmin() {
        final String DO_NOT_ASK_ADMIN = "DoNotAskAdmin";

        SharedPreferences prefs = getPreferences(MODE_PRIVATE);

        if (prefs.getBoolean(DO_NOT_ASK_ADMIN, false)) {
            adminRequestComplete = true;
            return;
        }

        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(DO_NOT_ASK_ADMIN, true);
        editor.apply();

        ComponentName adminReceiverName = new ComponentName(this, AdminReceiver.class);
        DevicePolicyManager policyManager = (DevicePolicyManager) getSystemService(DEVICE_POLICY_SERVICE);

        if (policyManager.isAdminActive(adminReceiverName)) {
            adminRequestComplete = true;
            showAlertAndFinish(getString(R.string.successfully),
                               getString(R.string.on_admin_received_message));
            return;
        }

        Intent deviceAdminRequest = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN)
                .putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminReceiverName)
                .putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,
                        getString(R.string.device_admin_explanation));
        startActivityForResult(deviceAdminRequest, DEVICE_ADMIN_REQUEST);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == DEVICE_ADMIN_REQUEST) {
            adminRequestComplete = true;

            String title, text;

            if (resultCode == Activity.RESULT_OK) {
                title = getString(R.string.successfully);
                text  = getString(R.string.on_admin_received_message) +
                        "\n\n" +
                        getString(R.string.turning_off_instructions);
            } else {
                title = getString(R.string.unsuccessfully);
                text  = getString(R.string.on_admin_refused_message) +
                        "\n\n" +
                        getString(R.string.turning_off_instructions);
            }

            showAlertAndFinish(title, text);
        }
    }

    private void showAlertAndFinish(String title, String text) {
        Log.d(LOG_TAG, "Show alert dialog...");

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.JELLY_BEAN)
            showAlertAndFinish11(title, text);
        else
            showAlertAndFinish17(title, text);
    }

    private void showAlertAndFinish11 (String title, String text) {
        readyToFinish = false;

        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(text)
                .setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {
                        finish();
                    }
                })
                .setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @TargetApi(17)
    private void showAlertAndFinish17 (String title, String text) {
        readyToFinish = false;

        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(text)
                .setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialogInterface) {
                        finish();
                    }
                })
                .setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }


    @Override
    protected void onResume() {
        super.onResume();

        Log.d(LOG_TAG, "resume " + toString());

        if (adminRequestComplete)
            startService(new Intent(this, LightService.class));
    }


    @Override
    protected void onDestroy() {
        unregisterReceiver(serviceMessageReceiver);
        super.onDestroy();
    }
}
