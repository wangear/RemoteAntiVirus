package com.ahn.remoteantivirus;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

/**
 * Target Device와 Socket통신하는 Class
 */
public class ConnectionService extends Service {

    private static final String TAG = "ConnectionService";
    private String mIpAddr = null;
    private Socket mSocket = null;
    private PrintWriter out = null;
    private String mDir = null;
    private ICallback mCallback = null;

    /**
     * ip 주소를 가져와서 socket 연결 수행
     * @param dir 바이러스 검사할 directory
     */
    public void checkVirus(String dir, String ipAddr) {
        mDir = dir;
        if(ipAddr == null || ipAddr.length() ==0)
            mIpAddr = Util.getLocalIpAddress(Constants.TYPE_INET4ADDRESS);
        else
            mIpAddr = ipAddr;
        Log.d(TAG, "ip = " + mIpAddr);
        connect();
    }
    //서비스 바인더 내부 클래스 선언

    public class MainServiceBinder extends Binder {
        ConnectionService getService() {
            return ConnectionService.this; //현재 서비스를 반환.
        }
    }

    private final IBinder mBinder = new MainServiceBinder();

    //액티비티에서 콜백 함수를 등록하기 위함.
    public void registerCallback(ICallback cb) {
        mCallback = cb;
    }

    //콜백 인터페이스 선언
    public interface ICallback {
        void updateDisplay(String text);
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    private void connect() {
        Log.d(TAG, "connect");

        Thread thread = new Thread() {
            String data = null;
            BufferedReader in = null;

            public void run() {
                Log.d(TAG, "start mSocket");
                try {
                    mSocket = new Socket(mIpAddr, Constants.PORT);
                    out = new PrintWriter(mSocket.getOutputStream(), true);
                    in = new BufferedReader(new InputStreamReader(mSocket.getInputStream()));
                    out.println(mDir);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                try {
                    while (mSocket != null && mSocket.isConnected()) {
                        data = in.readLine();

                        if (data != null) {
                            Log.d(TAG, "data = " + data);

                            mCallback.updateDisplay(data);
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        thread.start();
        sendMsg(mDir);
    }

    public void sendMsg(String text) {
        mDir = text;
        Log.d(TAG, "mDir = " + mDir);
        if (mSocket != null && mSocket.isConnected() && out != null) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    out.println(mDir);
                }
            }).start();
        } else {
            Log.d(TAG, "not connected");
            mCallback.updateDisplay(getString(R.string.connect_fail));
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind");
        disconnect();
        return mBinder;
    }

    public void disconnect() {
        try {
            if (mSocket != null) {
                mSocket.close();
                mSocket = null;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}