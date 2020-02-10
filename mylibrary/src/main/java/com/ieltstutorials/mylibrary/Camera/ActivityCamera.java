package com.ieltstutorials.mylibrary.Camera;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.Group;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.ieltstutorials.mylibrary.Gallery.ActivityGallery;
import com.ieltstutorials.mylibrary.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ActivityCamera extends AppCompatActivity {

    Button button;
    TextureView textureView;
    ImageView ivFlash, ivCameraSwitch;
    Group group;
    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();

    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    private String cameraID;
    CameraDevice cameraDevice;
    CameraCaptureSession cameraCaptureSession;
    CaptureRequest.Builder captureRequestBuilder;

    private Size imageDimensions;
    private File file;
    Handler mBackgroundHandler;
    HandlerThread mBackgroundThread;
    private Bitmap bmp;
    int isFlash = 0;
    CameraCharacteristics characteristics;

    private boolean mManualFocusEngaged = false;
    private String TAG = "Krunal";
    boolean isAuto = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);


        try {
            cameraID = ActivityGallery.cameraID;

            ivFlash = findViewById(R.id.ivFlash);
            group = findViewById(R.id.group);
            ivCameraSwitch = findViewById(R.id.ivCameraSwitch);
            textureView = findViewById(R.id.tv);
            button = findViewById(R.id.btn);
            textureView.setSurfaceTextureListener(textureListener);

            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    takePicture();
                }
            });

            if (cameraID.equalsIgnoreCase("0")) {
                ivFlash.setVisibility(View.GONE);
            }

            ivFlash.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    switch (isFlash) {
                        case 0:
                            isFlash = 1;
                            isAuto = false;
                            ivFlash.setImageDrawable(ContextCompat.getDrawable(ActivityCamera.this, R.drawable.ic_flash_off));
                            captureRequestBuilder.set(CaptureRequest.FLASH_MODE, CameraMetadata.FLASH_MODE_OFF);
                            break;
                        case 1:
                            isFlash = 2;
                            isAuto = false;
                            ivFlash.setImageDrawable(ContextCompat.getDrawable(ActivityCamera.this, R.drawable.ic_flash_on));
                            captureRequestBuilder.set(CaptureRequest.FLASH_MODE, CameraMetadata.FLASH_MODE_TORCH);
                            break;
                        case 2:
                            isFlash = 0;
                            isAuto = true;
                            ivFlash.setImageDrawable(ContextCompat.getDrawable(ActivityCamera.this, R.drawable.ic_flash_auto));
                            captureRequestBuilder.set(CaptureRequest.FLASH_MODE, CameraMetadata.FLASH_STATE_FIRED);
                            break;
                    }

                    try {
                        cameraCaptureSession.setRepeatingRequest(captureRequestBuilder.build(), null, mBackgroundHandler);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }

                }
            });

            ivCameraSwitch.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    try {
                        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
                        String tempCameraID = manager.getCameraIdList()[0];
                        if (cameraID.equals(tempCameraID)) {
                            cameraID = manager.getCameraIdList()[1];
                        } else {
                            cameraID = manager.getCameraIdList()[0];
                        }

                        if (textureView.isAvailable()) {
                            openCamera();
                        } else {
                            textureView.setSurfaceTextureListener(textureListener);
                        }
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }
            });
            group.setVisibility(View.VISIBLE);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    TextureView.SurfaceTextureListener textureListener = new TextureView.SurfaceTextureListener() {

        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {
            openCamera();
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {

        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {

        }
    };

    private final CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {

        @Override
        public void onOpened(@NonNull CameraDevice camera) {
            cameraDevice = camera;
            createCameraPreview();
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice cameraDevice) {
            cameraDevice.close();
        }

        @Override
        public void onError(@NonNull CameraDevice cameraDevice, int i) {
            cameraDevice.close();
            cameraDevice = null;

        }
    };

    private void createCameraPreview() {
        try {
            SurfaceTexture texture = textureView.getSurfaceTexture();
            texture.setDefaultBufferSize(imageDimensions.getWidth(), imageDimensions.getHeight());
            Surface surface = new Surface(texture);
            captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            captureRequestBuilder.addTarget(surface);
            captureRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CameraMetadata.CONTROL_AF_MODE_AUTO);
            switch (isFlash) {
                case 0:
                    isAuto = false;
                    ivFlash.setImageDrawable(ContextCompat.getDrawable(ActivityCamera.this, R.drawable.ic_flash_off));
                    captureRequestBuilder.set(CaptureRequest.FLASH_MODE, CameraMetadata.FLASH_MODE_OFF);
                    break;
                case 1:
                    isAuto = false;
                    ivFlash.setImageDrawable(ContextCompat.getDrawable(ActivityCamera.this, R.drawable.ic_flash_on));
                    captureRequestBuilder.set(CaptureRequest.FLASH_MODE, CameraMetadata.FLASH_MODE_TORCH);
                    break;
                case 2:
                    isAuto = true;
                    ivFlash.setImageDrawable(ContextCompat.getDrawable(ActivityCamera.this, R.drawable.ic_flash_auto));
                    captureRequestBuilder.set(CaptureRequest.FLASH_MODE, CameraMetadata.FLASH_STATE_FIRED);
                    break;
            }

            cameraDevice.createCaptureSession(Collections.singletonList(surface), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession Session) {

                    if (cameraDevice == null) {
                        return;
                    }
                    cameraCaptureSession = Session;

                    updatePreview();
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                    Toast.makeText(ActivityCamera.this, "Error ", Toast.LENGTH_SHORT).show();
                }
            }, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void updatePreview() {
        try {
            if (cameraDevice == null)
                return;
            captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
            cameraCaptureSession.setRepeatingRequest(captureRequestBuilder.build(), null, mBackgroundHandler);
            textureView.setOnTouchListener(new CameraFocusOnTouchHandler(characteristics, captureRequestBuilder, cameraCaptureSession, mBackgroundHandler));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void openCamera() {
        try {
            CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
            assert manager != null;

            characteristics = manager.getCameraCharacteristics(cameraID);
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);

            assert map != null;
            imageDimensions = map.getOutputSizes(SurfaceTexture.class)[0];
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(ActivityCamera.this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
                return;
            }

            manager.openCamera(cameraID, stateCallback, null);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void takePicture() {
        try {
            if (cameraDevice == null) {
                return;
            }
            if (isAuto) {
                cameraCaptureSession.setRepeatingRequest(captureRequestBuilder.build(), null, mBackgroundHandler);
            }


            CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
            characteristics = manager.getCameraCharacteristics(cameraDevice.getId());

            Size[] jpegSizes = null;
            jpegSizes = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP).getOutputSizes(ImageFormat.JPEG);
            int width = 640;
            int height = 480;
            if (jpegSizes != null && jpegSizes.length > 0) {
                width = jpegSizes[0].getWidth();
                height = jpegSizes[0].getHeight();
            }


            ImageReader reader = ImageReader.newInstance(width, height, ImageFormat.JPEG, 1);
            List<Surface> outputSurface = new ArrayList<>(2);
            outputSurface.add(reader.getSurface());
            outputSurface.add(new Surface(textureView.getSurfaceTexture()));

            final CaptureRequest.Builder captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureRequestBuilder.addTarget(reader.getSurface());
            captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);

            int roatation = getWindowManager().getDefaultDisplay().getRotation();
            captureRequestBuilder.set(CaptureRequest.JPEG_ORIENTATION, ORIENTATIONS.get(roatation));

            Long tsLong = System.currentTimeMillis() / 1000;
            String ts = tsLong.toString();
            file = new File(Environment.getExternalStorageDirectory() + "/" + ts + ".jpg");

            ImageReader.OnImageAvailableListener onImageAvailableListener = new ImageReader.OnImageAvailableListener() {
                @Override
                public void onImageAvailable(ImageReader imageReader) {

                    Image image = null;
                    image = imageReader.acquireLatestImage();
                    ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                    byte[] bytes = new byte[buffer.capacity()];
                    buffer.get(bytes);
                    bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);

                    try {
                        saveImage(bytes);
//                        setImage();
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        if (image != null) {
                            image.close();
                        }
                    }

                }
            };

            reader.setOnImageAvailableListener(onImageAvailableListener, mBackgroundHandler);

            final CameraCaptureSession.CaptureCallback captureCallback = new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
                    super.onCaptureCompleted(session, request, result);
                    Toast.makeText(ActivityCamera.this, "Save", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(ActivityCamera.this, ImagePreviewActivity.class);
                    intent.putExtra("Image", file.toString());
                    startActivity(intent);

//                    Glide.with(ActivityCamera.this)
//                            .load(file) // Uri of the picture
//                            .into(iv);

//                    iv.setVisibility(View.VISIBLE);
//                    group.setVisibility(View.GONE);
                    try {
                        createCameraPreview();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            };

            cameraDevice.createCaptureSession(outputSurface, new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                    try {
                        cameraCaptureSession.capture(captureRequestBuilder.build(), captureCallback, mBackgroundHandler);

                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {

                }
            }, mBackgroundHandler);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void saveImage(byte[] bytes) {
        OutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(file);
            outputStream.write(bytes);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();

        startBackgroundThread();

        if (textureView.isAvailable()) {
            openCamera();
        } else {
            textureView.setSurfaceTextureListener(textureListener);
        }
    }

    private void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("Camera Background");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());

    }

    @Override
    protected void onPause() {
        stopBackgroundThread();
        super.onPause();

    }

    private void stopBackgroundThread() {
        try {
            mBackgroundThread.quitSafely();
            mBackgroundThread.join();
            mBackgroundThread = null;
            mBackgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 101) {
            if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                Toast.makeText(this, "Please Grant permission", Toast.LENGTH_SHORT).show();
            }

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cameraDevice = null;
        cameraCaptureSession = null;
        captureRequestBuilder = null;
        mBackgroundHandler = null;
        mBackgroundThread = null;
        characteristics = null;
        textureView.setSurfaceTextureListener(null);
    }
}
