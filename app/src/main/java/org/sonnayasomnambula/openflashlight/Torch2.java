package org.sonnayasomnambula.openflashlight;


import android.annotation.TargetApi;
import android.content.Context;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.support.annotation.NonNull;
import android.util.Log;

@TargetApi(21)
public class Torch2 extends Torch {

    static final String LOG_TAG = "OF Torch2";

    Context context;
    CameraManager manager;
    String cameraId;

    Torch2(Context applicationContext) {
        context = applicationContext;
    }

    CameraTorchCallback cameraTorchCallback = new CameraTorchCallback();

    @Override
    public void turnOn() throws Exception /* CameraAccessException, NotSupportedException */ {
        manager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
        for (String id : manager.getCameraIdList()) {
            if (turnCameraTorchOn(id))
                return;
        }

        throw new NotSupportedException("No camera with torch mode found");
    }


    private boolean turnCameraTorchOn(String cameraId) {
        Log.d(LOG_TAG, "try camera " + cameraId);

        try {
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
            Boolean isFlashAvailable = characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);
            if (isFlashAvailable == null) {
                Log.e(LOG_TAG, "camera " + cameraId + " has no characteristics!");
                return false;
            }
            if (! isFlashAvailable) {
                Log.d(LOG_TAG, "camera " + cameraId + " has no flash");
                return false;
            }
            Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);
            if (facing != null && facing == CameraCharacteristics.LENS_FACING_FRONT) {
                Log.d(LOG_TAG, "camera " + cameraId + " is selfie camera");
                return false;
            }
            manager.setTorchMode(cameraId, true);
            cameraTorchCallback.register();
            this.cameraId = cameraId;
            return true;
        } catch (CameraAccessException e) {
            Log.e(LOG_TAG, "cannot manage camera " + cameraId, e);
        }

        return false;
    }

    @Override
    public void turnOff() {
        cameraTorchCallback.unregister();

        if (! cameraId.isEmpty()) {
            try {
                manager.setTorchMode(cameraId, false);
            } catch (CameraAccessException e) {
                Log.e(LOG_TAG, "setTorchMode with camera id = " + cameraId + " and enabled = false falls", e);
            } finally {
                cameraId = "";
            }
        }
    }


    private class CameraTorchCallback extends CameraManager.TorchCallback {

        private boolean isRegistered = false;

        public void register() {
            if (isRegistered) return;
            manager.registerTorchCallback(CameraTorchCallback.this, null);
            isRegistered = true;
        }

        public void unregister() {
            if (! isRegistered) return;
            manager.unregisterTorchCallback(CameraTorchCallback.this);
            isRegistered = false;
        }

        @Override
        public void onTorchModeUnavailable(@NonNull String cameraId) {
            if (Torch2.this.cameraId.equals(cameraId))
                Torch2.this.cameraId = "";
        }
    }
}
