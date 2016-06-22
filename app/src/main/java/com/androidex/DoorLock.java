package com.androidex;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.androidex.plugins.OnBackCall;
import com.androidex.plugins.kkfile;

import java.util.HashMap;

/**
 * Created by yangjun on 16/6/6.
 * 锁相开源门禁机软件的主要服务类,DoorLock主要提供开门,关门指令以及上报门开和关闭的事件.
 */
public class DoorLock extends Service implements OnBackCall {

    public static final String TAG = "DoorLock";
    public static final String mDoorSensorAction = "com.android.action.doorsensor";

    private DoorLockServiceBinder mDoorLock;

    /**
     * 当门的状态改变时的事件定义
     */
    public static final String DoorLockStatusChange 	 = "DoorLockStatusChange";
    /**
     * DoorLock通过DoorLockOpenDoor广播获得开门指令并发送给门禁控制器
     */
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
        if(r == 9)
            Toast.makeText(DoorLock.this, String.format("Open %d,delay %ds close.",1,16*150/1000), Toast.LENGTH_SHORT).show();
        else
            Toast.makeText(DoorLock.this, String.format("Open door 1 fail return %d.",r), Toast.LENGTH_SHORT).show();

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

        /**
         * 开门指令
         * @param index     门的序号,主门=0,副门=1
         * @param delay     延迟关门的时间,0表示不启用延迟关门,大于0表示延迟时间,延迟时间为delay*150ms
         * @return          大于0表示成功,实际上等于9表示真正的成功,因为返回值表示写入的数据,开门指令长度为9.
         */
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
