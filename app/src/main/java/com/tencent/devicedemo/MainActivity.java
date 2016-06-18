package com.tencent.devicedemo;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcelable;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.GridView;
import android.widget.Toast;

import com.androidex.DoorLock;
import com.androidex.IDoorLockInterface;
import com.androidex.plugins.kkaexparams;
import com.tencent.device.TXBinderInfo;
import com.tencent.device.TXDeviceService;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends Activity{
  
	private GridView mGridView; 
	private BinderListAdapter mAdapter;
	private NotifyReceiver  mNotifyReceiver;
    private Toast toast;

    protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated constructor stub
		super.onCreate(savedInstanceState);
        toast = Toast.makeText(getApplicationContext(),"", Toast.LENGTH_SHORT);
        //全屏设置，隐藏窗口所有装饰
        getWindow().clearFlags(android.view.WindowManager.LayoutParams.FLAG_FORCE_NOT_FULLSCREEN);//清除FLAG
        getWindow().setFlags(android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN,android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //标题是属于View的，所以窗口所有的修饰部分被隐藏后标题依然有效
        //requestWindowFeature(getWindow().FEATURE_NO_TITLE);
        sendBroadcast(new Intent("com.android.action.hide_navigationbar"));

		setContentView(R.layout.activity_main);
        kkaexparams.runShellCommand("chmod 0666 /dev/rkey");

		Intent startIntent = new Intent(this, TXDeviceService.class); 
		startService(startIntent);
        bindService(startIntent, mConn, Context.BIND_AUTO_CREATE);

        Intent dlIntent = new Intent(this, DoorLock.class);
        startService(dlIntent);

		mGridView = (GridView) findViewById(R.id.gridView_binderlist);
		mAdapter = new BinderListAdapter(this);
		mGridView.setAdapter(mAdapter);
		
		IntentFilter filter = new IntentFilter();
		filter.addAction(TXDeviceService.BinderListChange);
		filter.addAction(TXDeviceService.OnEraseAllBinders);
        filter.addAction(DoorLock.DoorLockStatusChange);
		mNotifyReceiver = new NotifyReceiver();
		registerReceiver(mNotifyReceiver, filter);

        boolean bNetworkSetted = this.getSharedPreferences("TXDeviceSDK", 0).getBoolean("NetworkSetted", false);
        if (TXDeviceService.NetworkSettingMode == true && bNetworkSetted == false) {
			Intent intent = new Intent(MainActivity.this, WifiDecodeActivity.class);
			startActivity(intent);
        }

        AudioManager mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        //通话音量
        int max = mAudioManager.getStreamMaxVolume( AudioManager.STREAM_VOICE_CALL );
        int current = mAudioManager.getStreamVolume( AudioManager.STREAM_VOICE_CALL );
        Log.d("VIOCE_CALL","max : " + max + " current : " + current);
        //系统音量
        max = mAudioManager.getStreamMaxVolume( AudioManager.STREAM_SYSTEM );
        current = mAudioManager.getStreamVolume( AudioManager.STREAM_SYSTEM );
        Log.d("SYSTEM", "max : " + max + " current : " + current);
        //铃声音量
        max = mAudioManager.getStreamMaxVolume( AudioManager.STREAM_RING );
        current = mAudioManager.getStreamVolume( AudioManager.STREAM_RING );
        Log.d("RING", "max : " + max + " current : " + current);
        //音乐音量
        max = mAudioManager.getStreamMaxVolume( AudioManager.STREAM_MUSIC );
        current = mAudioManager.getStreamVolume( AudioManager.STREAM_MUSIC );
        Log.d("MUSIC", "max : " + max + " current : " + current);
        //提示声音音量
        max = mAudioManager.getStreamMaxVolume( AudioManager.STREAM_ALARM );
        current = mAudioManager.getStreamVolume( AudioManager.STREAM_ALARM );
        Log.d("ALARM", "max : " + max + " current : " + current);
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        AudioManager audio = (AudioManager) getSystemService(Service.AUDIO_SERVICE);
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP:
                audio .adjustStreamVolume(
                        AudioManager.STREAM_VOICE_CALL,
                        AudioManager.ADJUST_RAISE,
                        AudioManager.FLAG_PLAY_SOUND | AudioManager.FLAG_SHOW_UI);
                return true;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                audio.adjustStreamVolume(
                        AudioManager.STREAM_VOICE_CALL,
                        AudioManager.ADJUST_LOWER,
                        AudioManager.FLAG_PLAY_SOUND | AudioManager.FLAG_SHOW_UI);
                return true;
            default:
                break;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuItem m_unbind = menu.add("解除绑定");
        MenuItem m_upload_log = menu.add("上传日志");

        m_unbind.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                return true;
            }
        });

        m_upload_log.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                return true;
            }
        });

        return true;
    }

    public void eraseAllBinders(View v) {
		AlertDialog dialog = null;
		Builder builder = new Builder(this).setTitle(R.string.unbind).setMessage(R.string.q_unbind_all).setPositiveButton(R.string.cancel, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				dialog.dismiss();

			}
		}).setNegativeButton(R.string.unbind, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				dialog.dismiss();
				TXDeviceService.eraseAllBinders();
			}
		});
		dialog = builder.create();
		dialog.show();
	}
	
	public void uploadDeviceLog(View v) 
	{
		TXDeviceService.getInstance().uploadSDKLog();
	}

	protected void onResume(){
		super.onResume();
		TXBinderInfo [] arrayBinder = TXDeviceService.getBinderList();
		if (arrayBinder != null){
			List<TXBinderInfo> binderList = new ArrayList<TXBinderInfo>();
			for (int i = 0; i < arrayBinder.length; ++i){
				binderList.add(arrayBinder[i]);
			}
			if (mAdapter != null) {
				mAdapter.freshBinderList(binderList);
			}
		}
	}
	
	protected void onPause(){
		super.onPause();
	}
	
	protected void onDestroy(){
		super.onDestroy();
		unregisterReceiver(mNotifyReceiver);
	}
	
	public class NotifyReceiver extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction() == TXDeviceService.BinderListChange){
				Parcelable[] listTemp = intent.getExtras().getParcelableArray("binderlist");
				List<TXBinderInfo> binderList = new ArrayList<TXBinderInfo>();
				for (int i = 0; i < listTemp.length; ++i){
					TXBinderInfo  binder = (TXBinderInfo)(listTemp[i]);
					binderList.add(binder); 
				}
				if (mAdapter != null) {
					mAdapter.freshBinderList(binderList);
				}
			} else if (intent.getAction() == TXDeviceService.OnEraseAllBinders){
				int resultCode = intent.getExtras().getInt(TXDeviceService.OperationResult);
				if (0 != resultCode) {
					showAlert("解除绑定失败", "解除绑定失败，错误码:" + resultCode);
				} else {
					showAlert("解除绑定成功", "解除绑定成功!!!");
				}
			} else if(intent.getAction() == DoorLock.DoorLockStatusChange){
                //门禁状态改变事件
                //showAlert("门禁状态改变",intent.getStringExtra("doorsensor"));
                String doorsendor = String.format("doorsensor=%s",intent.getStringExtra("doorsensor"));
                Log.d("NotifyReceiver",doorsendor);
                toast.setText(doorsendor);
                toast.show();
            }
		}
	}
	
	private void showAlert(String strTitle, String strMsg) {
		// TODO Auto-generated method stub
		AlertDialog dialogError;
		Builder builder = new AlertDialog.Builder(this).setTitle(strTitle).setMessage(strMsg).setPositiveButton("取消", null).setNegativeButton("确定",null);
		dialogError = builder.create();
		dialogError.show();
	}

    private IDoorLockInterface doorLockService = null;
    private ServiceConnection mConn = new ServiceConnection() {
        /** 获取服务对象时的操作 */
        public void onServiceConnected(ComponentName name, IBinder service) {
            // TODO Auto-generated method stub
            doorLockService = IDoorLockInterface.Stub.asInterface(service);

        }

        /** 无法获取到服务对象时的操作 */
        public void onServiceDisconnected(ComponentName name) {
            // TODO Auto-generated method stub
            doorLockService = null;
        }

    };

}
