package com.ahn.remoteantivirus;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends Activity {

    private static final String TAG = "MainActivity";
    private EditText mEtProgress = null;
    private EditText mEtIpAddr = null;
    private TextView mTvResult = null;
    private ConnectionService mService = null;
    private String mDir = null;

    private ServiceConnection mConnection = new ServiceConnection() {
        private ComponentName name;
        private IBinder service;

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            this.name = name;
            this.service = service;
            ConnectionService.MainServiceBinder binder = (ConnectionService.MainServiceBinder) service;
            mService = binder.getService(); //서비스 받아옴
            mService.registerCallback(mCallback); //콜백 등록
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
        }
    };

    //서비스에서 아래의 콜백 함수를 호출하며, 콜백 함수에서는 액티비티에서 처리할 내용 입력
    private ConnectionService.ICallback mCallback = new ConnectionService.ICallback() {

        public void updateDisplay(final String text) {

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "text = " + text);
                    mTvResult.setText(text);
                }
            });
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnStart = (Button) findViewById(R.id.btn_start);
        mEtProgress = (EditText) findViewById(R.id.et_dir);
        mTvResult = (TextView) findViewById(R.id.tv_result);
        mEtIpAddr = (EditText) findViewById(R.id.et_ip);

        btnStart.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            mDir = mEtProgress.getText().toString();
                                            String ipAddr = mEtIpAddr.getText().toString();

                                            if (mDir != null && mDir.length() > 0) {
                                                mTvResult.setText(getString(R.string.progress));
                                                mService.checkVirus(mDir, ipAddr);
                                            } else
                                                mTvResult.setText(getString(R.string.no_dir));

                                        }
                                    }
        );

        Intent intent = new Intent(this, ConnectionService.class);

        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
        if (mService != null) {
            mService.disconnect();
            unbindService(mConnection);
        }
    }
}
