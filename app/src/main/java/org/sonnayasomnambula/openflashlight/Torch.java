package org.sonnayasomnambula.openflashlight;


import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

class NotSupportedException extends Exception {
    public NotSupportedException(String message) {
        super(message);
    }
}

abstract public class Torch {

    public static Torch create(Context applicationContext) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
            return new DeprecatedTorch();
        else
            return new Torch2(applicationContext);
    }

    public static boolean isCameraExists(Context applicationContext) {
        return applicationContext.getPackageManager().hasSystemFeature(
                PackageManager.FEATURE_CAMERA_FLASH);
    }


    public static boolean isCameraPermissionGranted(Context applicationContext) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
            return true;
        else
            return isCamPermissionGranted(applicationContext);
    }

    @TargetApi(23)
    private static boolean isCamPermissionGranted(Context applicationContext) {
        int state = applicationContext.checkSelfPermission(Manifest.permission.CAMERA);
        return state == PackageManager.PERMISSION_GRANTED;
    }

    abstract public void turnOn() throws Exception;
    abstract public void turnOff();
}
