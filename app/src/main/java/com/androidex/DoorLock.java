package com.androidex;

import android.app.AlertDialog;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.androidex.plugins.OnBackCall;
import com.androidex.plugins.kkfile;

import java.util.HashMap;

/**
 * Created by yangjun on 16/6/6.
 */
public class DoorLock extends Service implements OnBackCall {

    public static final String TAG = "DoorLock";
    public static final String mDoorSensorAction = "com.android.action.doorsensor";

    private DoorLockServiceBinder mDoorLock;

    //当门的状态改变时的事件定义
    public static final String DoorLockStatusChange 	 = "DoorLockStatusChange";
    public static final String DoorLockOpenDoor          = "DoorLockOpenDoor";
    private NotifyReceiver mReceiver;
    private static DoorLock mServiceInstance = null;

    public static DoorLock getInstance()
    {
        return mServiceInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mDoorLock = new DoorLockServiceBinder();
        mReceiver = new NotifyReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(mDoorSensorAction);
        filter.addAction(DoorLockOpenDoor);
        registerReceiver(mReceiver, filter);
        int r = mDoorLock.openDoor(1,16);
        Log.d(TAG,String.format("open door %d",r));
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(mReceiver);
        super.onDestroy();
        mServiceInstance = null;
        mDoorLock = null;
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onStart(Intent intent, int startId) {

        super.onStart(intent, startId);
        mServiceInstance = this;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mDoorLock;
    }

    @Override
    public void onBackCallEvent(int code, String args) {
        Log.v("onBackCallEvent",args);
    }

    public class DoorLockServiceBinder extends IDoorLockInterface.Stub{
        String rkeyDev = "/dev/rkey";
        int ident = 0;

        public int openDoor(int index, int delay){
            kkfile rkey = new kkfile();

            if(index < 0 || index > 0xFE) index = 0;
            if(ident < 0 || ident > 0xFE) ident = 0;
            if(delay < 0 || delay > 0xFE) delay = 0;
            String cmd = String.format("FB%02X2503%02X01%02X00FE",ident,index,delay);
            int r = rkey.writeHex(rkeyDev,cmd);
            return r > 0?1:0;
        }
        public int closeDoor(int index){
            kkfile rkey = new kkfile();

            if(index < 0 || index > 0xFE) index = 0;
            if(ident < 0 || ident > 0xFE) ident = 0;
            String cmd = String.format("FB%02X2503%02X000000FE",ident,index);
            int r = rkey.writeHex(rkeyDev,cmd);
            return r > 0 ? 1:0;
        }
    }

    public class NotifyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(mDoorSensorAction)){
                String doorsensor = intent.getStringExtra("doorsensor");
                UEventMap mds = new UEventMap(doorsensor);

                Log.d(TAG, String.format("%s\t Door sensor=%s\n",mds.get("doorsensor"), mds.toString()));

                Intent ds_intent = new Intent();
                ds_intent.setAction(DoorLock.DoorLockStatusChange);
                ds_intent.putExtra("doorsensor",mds.get("doorsensor"));
                sendBroadcast(ds_intent);
            } else if(intent.getAction().equals(DoorLockOpenDoor)) {
                int index = intent.getIntExtra("index", 0);
                int status = intent.getIntExtra("status", 0);

                if (status != 0){
                    mDoorLock.openDoor(index, 0x20);
                }else {
                    mDoorLock.closeDoor(index);
                }
            }
        }
    }

    public static final class UEventMap {
        // collection of key=value pairs parsed from the uevent message
        private final HashMap<String,String> mMap = new HashMap<String,String>();

        public UEventMap(String message) {
            int offset = 0;
            int length = message.length();

            if(length == 0)return;
            if(message.substring(0,1).equals("{")){
                message = message.substring(1);
            }
            if(message.substring(message.length() - 1,message.length()).equals("}")){
                message = message.substring(0,message.length() - 1);
            }
            length = message.length();
            while (offset < length) {
                int equals = message.indexOf('=', offset);
                int at = message.indexOf(',', offset);
                if (at < 0) break;

                if (equals > offset && equals < at) {
                    // key is before the equals sign, and value is after
                    mMap.put(message.substring(offset, equals).trim(),
                            message.substring(equals + 1, at).trim());
                }

                offset = at + 1;
            }
        }

        public String get(String key) {
            return mMap.get(key);
        }

        public String get(String key, String defaultValue) {
            String result = mMap.get(key);
            return (result == null ? defaultValue : result);
        }

        public String toString() {
            return mMap.toString();
        }
    }

}
