package org.sonnayasomnambula.openflashlightmini;

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


public class MainActivity extends Activity {
    static final String LOG_TAG = "OFM MainActivity";
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
                    showLongToastAndFinish(intent.getStringExtra(Actions.Message.EXTRA_TEXT));
                    break;
                case Actions.Message.ID_STARTED_SUCCESSFULLY:
                    showShortToastAndFinish(getString(R.string.service_running));
                    break;
                case Actions.Message.ID_ABOUT_TO_STOP:
                    showShortToastAndFinish(getString(R.string.about_to_stop));
                    break;
            }
        }
    };

    private void showShortToastAndFinish(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
        if (readyToFinish) finish();
    }

    private void showLongToastAndFinish(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        if (readyToFinish) finish();
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(LOG_TAG, "create " + toString());

        IntentFilter messagesOnly = new IntentFilter(Actions.MESSAGE);
        registerReceiver(serviceMessageReceiver, messagesOnly);

        requestDeviceAdmin();
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
                text  = getString(R.string.on_admin_received_message);
            } else {
                title = getString(R.string.unsuccessfully);
                text  = getString(R.string.on_admin_refused_message);

            }

            text += "\n\n" +
                    getString(R.string.turning_off_instructions);

            if (Build.VERSION.SDK_INT >= 21) {
                text += "\n\n" + getString(R.string.try_openflashlight);
            }

            showAlertAndFinish(title, text);
        }
    }

    private void showAlertAndFinish(String title, String text) {
        Log.d(LOG_TAG, "Show alert dialog...");

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
