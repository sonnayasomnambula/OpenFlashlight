package org.sonnayasomnambula.openflashlightmini;


import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;

class NotSupportedException extends Exception {
    public NotSupportedException(String message) {
        super(message);
    }
}

abstract public class Torch {

    public static Torch create(Context applicationContext) {
        return new DeprecatedTorch();
    }

    public static boolean isCameraExists(Context applicationContext) {
        return applicationContext.getPackageManager().hasSystemFeature(
                PackageManager.FEATURE_CAMERA_FLASH);
    }


    public static boolean isCameraPermissionGranted(Context applicationContext) {
        int state = applicationContext.checkCallingOrSelfPermission(Manifest.permission.CAMERA);
        return state == PackageManager.PERMISSION_GRANTED;
    }


    abstract public void turnOn() throws Exception;
    abstract public void turnOff();
}
