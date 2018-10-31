package com.roadzen.red5prodemo;

import android.app.Activity;
import android.hardware.Camera;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.red5pro.streaming.R5Connection;
import com.red5pro.streaming.R5Stream;
import com.red5pro.streaming.R5StreamProtocol;
import com.red5pro.streaming.config.R5Configuration;
import com.red5pro.streaming.event.R5FrameListener;
import com.red5pro.streaming.view.R5VideoView;

public class SubscribeFragment extends Fragment {

    private R5Configuration configuration;
    private R5Stream stream;
    private boolean isSubscribing;

    public static SubscribeFragment newInstance() {
        SubscribeFragment fragment = new SubscribeFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public SubscribeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        configuration = new R5Configuration(R5StreamProtocol.RTSP, "http://192.168.3.57:5080"/*TODO localhost url*/, 5080, "live", 1.0f);
        configuration.setLicenseKey("PT7P-3RQ4-CUIA-XEB3"); //TODO add sdk key
        configuration.setBundleID(getActivity().getPackageName());
    }

    private void onSubscribeToggle() {
        Button subscribeButton = (Button) mView.findViewById(R.id.subscribeButton);
        if (isSubscribing) {
            stop();
        } else {
            start();
        }
        isSubscribing = !isSubscribing;
        subscribeButton.setText(isSubscribing ? "stop" : "start");

    }

    public void start() {
        Log.d("frames received", "start");
        R5VideoView videoView = mView.findViewById(R.id.subscribeView);
        stream = new R5Stream(new R5Connection(configuration));
        videoView.attachStream(stream);
        stream.play("roadzen_test");
        stream.setFrameListener(new R5FrameListener() {
            @Override
            public void onBytesReceived(byte[] bytes, int i, int i1) {
                Log.d("Frames", "Frame rcvd >> " + bytes.length + "");
            }
        });
    }

    public void stop() {
        if (stream != null) {
            stream.stop();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (isSubscribing) {
            onSubscribeToggle();
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Button publishButton = (Button) mView.findViewById(R.id.subscribeButton);
        publishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onSubscribeToggle();
            }
        });
    }


    View mView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_subscribe, container, false);
        onSubscribeToggle();
        return mView;
    }

}