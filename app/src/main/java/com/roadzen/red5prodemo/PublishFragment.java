package com.roadzen.red5prodemo;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.red5pro.streaming.R5Connection;
import com.red5pro.streaming.R5Stream;
import com.red5pro.streaming.R5StreamProtocol;
import com.red5pro.streaming.config.R5Configuration;
import com.red5pro.streaming.source.R5Camera;
import com.red5pro.streaming.source.R5Microphone;

import java.util.List;

public class PublishFragment extends Fragment implements SurfaceHolder.Callback {

    private static final int CAMERA = 100;
    private R5Configuration configuration;

    public static PublishFragment newInstance() {
     PublishFragment fragment = new PublishFragment();
     Bundle args = new Bundle();
     fragment.setArguments(args);
     return fragment;
   }

    protected Camera camera;
    protected boolean isPublishing = false;
    protected R5Stream stream;

    public PublishFragment() {
     // Required empty public constructor
   }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Button publishButton = (Button) getActivity().findViewById(R.id.publishButton);
        publishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onPublishToggle();
            }
        });
    }


    public static void setCameraDisplayOrientation(Activity activity, int cameraId, android.hardware.Camera camera) {
        android.hardware.Camera.CameraInfo info =
                new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(cameraId, info);
        int rotation = activity.getWindowManager().getDefaultDisplay()
                .getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0: degrees = 0; break;
            case Surface.ROTATION_90: degrees = 90; break;
            case Surface.ROTATION_180: degrees = 180; break;
            case Surface.ROTATION_270: degrees = 270; break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        camera.setDisplayOrientation(result);
    }

    private void onPublishToggle() {
        Button publishButton = (Button) getActivity().findViewById(R.id.publishButton);
        if(isPublishing) {
            stop();
        }
        else {
            start();
        }
        isPublishing = !isPublishing;
        publishButton.setText(isPublishing ? "stop" : "start");
    }

    public void start() {
        camera.stopPreview();

        stream = new R5Stream(new R5Connection(configuration));
        stream.setView((SurfaceView) getActivity().findViewById(R.id.surfaceView));

        R5Camera r5Camera = new R5Camera(camera, 320, 240);
        R5Microphone r5Microphone = new R5Microphone();

        stream.attachCamera(r5Camera);
        stream.attachMic(r5Microphone);

        stream.publish("roadzen_test", R5Stream.RecordType.Live);
        camera.startPreview();
    }

    public void stop() {
        if(stream != null) {
            stream.stop();
            camera.startPreview();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if(isPublishing) {
            onPublishToggle();
        }
    }

   @Override
   public void onCreate(Bundle savedInstanceState) {
     super.onCreate(savedInstanceState);
     setUpConfig();
   }

   @Override
   public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
     View v = inflater.inflate(R.layout.fragment_publish, container, false);
     return v;
   }

    private void preview() {
        camera = Camera.open(Camera.CameraInfo.CAMERA_FACING_FRONT);
        SurfaceView surface = (SurfaceView) getActivity().findViewById(R.id.surfaceView);
        surface.getHolder().addCallback(this);
    }

    private void setUpConfig() {
        configuration = new R5Configuration(R5StreamProtocol.RTSP, "http://192.168.3.57"/*TODO localhost url*/ ,  5080, "live", 1.0f);
        configuration.setLicenseKey("PT7P-3RQ4-CUIA-XEB3"); //TODO add sdk key
        configuration.setBundleID(getActivity().getPackageName());
    }

    @Override
    public void onResume() {
        super.onResume();
        preview();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            camera.setPreviewDisplay(holder);
            setCameraDisplayOrientation(getActivity(),Camera.CameraInfo.CAMERA_FACING_FRONT,camera);
            if(camera != null){
                //Setting the camera's aspect ratio
                Camera.Parameters parameters = camera.getParameters();
                List<Camera.Size> sizes = parameters.getSupportedPreviewSizes();
                Camera.Size optimalSize = getOptimalPreviewSize(sizes, getResources().getDisplayMetrics().widthPixels, getResources().getDisplayMetrics().heightPixels);
                parameters.setPreviewSize(optimalSize.width, optimalSize.height);
                camera.setParameters(parameters);
            }
            camera.startPreview();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    private Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int w, int h) {
        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio=(double)h / w;

        if (sizes == null) return null;

        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;
        int targetHeight = h;

        for (Camera.Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        return optimalSize;
    }
}