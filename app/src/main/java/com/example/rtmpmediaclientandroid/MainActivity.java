package com.example.rtmpmediaclientandroid;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Surface;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;

import cn.nodemedia.NodePublisher;

public class MainActivity extends AppCompatActivity {
    private static final String[] PERMISSIONS = new String[]{
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO};
    private static final int REQUEST_PERMISSION_CODE = 0XFF00;
    private NodePublisher np;
    private SharedPreferences sp;
    private Boolean isFrontCamera = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requestPermission();

        sp = getSharedPreferences("rtmp_record", Context.MODE_PRIVATE);

        FrameLayout fl = findViewById(R.id.camera_view);
        np = new NodePublisher(this, "");
        np.setAudioCodecParam(NodePublisher.NMC_CODEC_ID_AAC, NodePublisher.NMC_PROFILE_AUTO, 48000, 1, 64_000);
        np.setVideoOrientation(NodePublisher.VIDEO_ORIENTATION_PORTRAIT);
        np.setVideoCodecParam(NodePublisher.NMC_CODEC_ID_H264, NodePublisher.NMC_PROFILE_AUTO, 720, 1280, 17, 2_000_000);
        np.attachView(fl);
        np.openCamera(isFrontCamera);
        np.setVideoOrientation(Surface.ROTATION_0);
        Button publishBtn = findViewById(R.id.publish_btn);
        Button stopBtn = findViewById(R.id.stop_btn);
        Button switchCameraBtn = findViewById(R.id.switch_camera_btn);
        EditText rtmpInput = findViewById(R.id.rtmp_input);

        np.setOnNodePublisherEventListener((NodePublisher publisher, int event, String msg) -> {

        });


        // 取上次设置的rtmp地址
        String recordRtmpUrl = sp.getString("rtmp_url", "");
        rtmpInput.setText(recordRtmpUrl);

        // 自动运行推流
        autoStartRtmp(recordRtmpUrl);

        // 按钮点击事件
        publishBtn.setOnClickListener((v) -> {
            String rtmpTxt = rtmpInput.getText().toString();
            if (!rtmpTxt.isEmpty() || rtmpTxt == "") {
                // 存储记录
                sp.edit().putString("rtmp_url", rtmpTxt).apply();
                np.start(rtmpTxt);
            }
        });
        stopBtn.setOnClickListener((v) -> {
            np.stop();
        });
        switchCameraBtn.setOnClickListener((v) -> {
            switchCamera();
        });
    }

    private Boolean autoStartRtmp(String rtmpUrl) {
        if (rtmpUrl.isEmpty() || rtmpUrl == "") {
            return false;
        }
        Boolean ret = true;
        for (int i = 0; i < PERMISSIONS.length; i++) {
            if(ContextCompat.checkSelfPermission(this,PERMISSIONS[i])!= PackageManager.PERMISSION_GRANTED)
            {
                ret = false;
                break;
            }
        }
        if (ret) {
            np.start(rtmpUrl);
        }
        return ret;
    }

    private void switchCamera() {
        np.switchCamera();
    }

    private void requestPermission() {
        ActivityCompat.requestPermissions(MainActivity.this, PERMISSIONS, REQUEST_PERMISSION_CODE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        np.detachView();
        np.closeCamera();
        np.stop();
    }
}