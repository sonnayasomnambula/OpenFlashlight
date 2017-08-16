package org.sonnayasomnambula.openflashlightmini;

import android.hardware.Camera;

import java.util.List;

public class DeprecatedTorch extends Torch {

    private Camera cameraInstance;
    private String cameraLastMode;

    @Override
    public void turnOn() throws Exception {
        if (cameraInstance != null) return;

        cameraInstance = Camera.open();

        Camera.Parameters parameters = cameraInstance.getParameters();
        List<String> flashModes = parameters.getSupportedFlashModes();
        boolean hasCameraFlashTorchMode = flashModes.contains(Camera.Parameters.FLASH_MODE_TORCH);

        if (! hasCameraFlashTorchMode)
            throw new NotSupportedException("Camera does not have torch mode");

        cameraLastMode = parameters.getFlashMode();
        parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
        cameraInstance.setParameters(parameters);
        cameraInstance.startPreview();
    }

    @Override
    public void turnOff() {
        if (cameraInstance == null) return;

        cameraInstance.stopPreview();
        Camera.Parameters parameters = cameraInstance.getParameters();
        parameters.setFlashMode(cameraLastMode);
        cameraInstance.setParameters(parameters);
        cameraInstance.release();
        cameraInstance = null;
    }
}
