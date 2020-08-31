package org.sonnayasomnambula.openflashlight;


import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.IBinder;
import androidx.core.app.NotificationCompat;
import android.util.Log;

import com.commonsware.android.lockme.AdminReceiver;

public class LightService extends Service {
    static final String LOG_TAG = "OF LightService";

    private Torch torch;
    private ActivityInformer activityInformer = new ActivityInformer();
    private BroadcastReceiver screenWatcher;

    private DevicePolicyManager policyManager;
    private ComponentName adminReceiverName;

    private enum ReasonForStopping {Undefined, NotificationTap, IconTap, ScreenOn}
    private ReasonForStopping reasonForStopping = ReasonForStopping.Undefined;

    @Override
    public void onCreate() {
        super.onCreate();

        Log.d(LOG_TAG, "service started");

        try {
            Context appContext = getApplicationContext();
            if (! Torch.isCameraExists(appContext)) {
                stopWithMessage(getResources().getString(R.string.no_camera_found));
                return;
            }
            if (! Torch.isCameraPermissionGranted(appContext)) {
                stopWithMessage(getResources().getString(R.string.camera_permission_missing));
                return;
            }
            torch = Torch.create(appContext);
            torch.turnOn();
            getPolicyManager();
            registerScreenWatcher();
            showNotification();
            activityInformer.successMessage();
        } catch (NotSupportedException e) {
            stopWithMessage(getResources().getString(R.string.torch_mode_not_supported));
        } catch (Exception e) {
            stopWithMessage(e.getMessage());
        }
    }

    private void getPolicyManager() {
        adminReceiverName = new ComponentName(getApplicationContext(), AdminReceiver.class);
        policyManager = (DevicePolicyManager) getSystemService(DEVICE_POLICY_SERVICE);
    }

    private void registerScreenWatcher() {
        screenWatcher = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (Intent.ACTION_SCREEN_ON.equals(intent.getAction())) {
                    Log.d(LOG_TAG, "Screen on");
                    reasonForStopping = ReasonForStopping.ScreenOn;
                    stopSelf();
                }
            }
        };

        registerReceiver(screenWatcher, new IntentFilter(Intent.ACTION_SCREEN_ON));
    }

    private void showNotification() {
        final int ID = (int)System.currentTimeMillis();
        final String CHANNEL_ID = "channel_main";

        Intent intentStop = new Intent(getApplicationContext(), LightService.class)
                .setAction(Actions.STOP);

        PendingIntent pendingIntentStop = PendingIntent.getService(
                getApplicationContext(), 0, intentStop, PendingIntent.FLAG_CANCEL_CURRENT);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            NotificationChannel notificationChannel = notificationManager.getNotificationChannel(CHANNEL_ID);
            if (notificationChannel == null) {
                NotificationChannel channel = new NotificationChannel(CHANNEL_ID, getString(R.string.channel_name), NotificationManager.IMPORTANCE_DEFAULT);
                notificationManager.createNotificationChannel(channel);
            }
        }

        Notification notification = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_stat_notify)
                .setContentText(getResources().getString(R.string.press_to_turn_off))
                .setContentIntent(pendingIntentStop)
                .build();

        startForeground(ID, notification);
    }

    private void stopWithMessage(String message) {
        Log.e(LOG_TAG, message);
        activityInformer.failureMessage(message);
        stopSelf();
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(LOG_TAG, "start " + startId + " action " + intent.getAction());

        if (Actions.STOP.equals(intent.getAction())) {
            reasonForStopping = ReasonForStopping.NotificationTap;
            stopSelf();
        }

        if (intent.getAction() == null && startId > 1) {
            activityInformer.aboutToStopMessage();
            reasonForStopping = ReasonForStopping.IconTap;
            stopSelf();
        }

        return START_STICKY;
    }


    @Override
    public void onDestroy() {
        Log.d(LOG_TAG, "onDestroy");
        if (torch != null)
            torch.turnOff();
        unregisterScreenWatcher();
        if (reasonForStopping == ReasonForStopping.ScreenOn)
            sendDeviceToSleep();
        super.onDestroy();
    }

    private void unregisterScreenWatcher() {
        if (screenWatcher == null) return;

        unregisterReceiver(screenWatcher);
        screenWatcher = null;
    }


    private void sendDeviceToSleep() {
        if (policyManager.isAdminActive(adminReceiverName))
            policyManager.lockNow();
    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private class ActivityInformer {
        void failureMessage(String text) {
            Intent message = new Intent(Actions.MESSAGE)
                    .putExtra(Actions.Message.EXTRA_ID, Actions.Message.ID_FAILED_TO_START)
                    .putExtra(Actions.Message.EXTRA_TEXT, text);
            sendBroadcast(message);
        }

        void successMessage() {
            Intent message = new Intent(Actions.MESSAGE)
                    .putExtra(Actions.Message.EXTRA_ID, Actions.Message.ID_STARTED_SUCCESSFULLY);
            sendBroadcast(message);
        }

        void aboutToStopMessage() {
            Intent message = new Intent(Actions.MESSAGE)
                    .putExtra(Actions.Message.EXTRA_ID, Actions.Message.ID_ABOUT_TO_STOP);
            sendBroadcast(message);
        }
    }
}
