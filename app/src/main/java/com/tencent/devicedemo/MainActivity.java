package com.tencent.devicedemo;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.nfc.NfcAdapter;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.SystemClock;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.GridView;
import android.widget.Toast;

import com.androidex.DoorLock;
import com.androidex.LoyaltyCardReader;
import com.androidex.SoundPoolUtil;
import com.androidex.plugins.kkaexparams;
import com.tencent.device.TXBinderInfo;
import com.tencent.device.TXDeviceService;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends Activity implements LoyaltyCardReader.AccountCallback{
  
	private GridView mGridView; 
	private BinderListAdapter mAdapter;
	private NotifyReceiver  mNotifyReceiver;
    private Toast toast;
    // Recommend NfcAdapter flags for reading from other Android devices. Indicates that this
    // activity is interested in NFC-A devices (including other Android devices), and that the
    // system should not check for the presence of NDEF-formatted data (e.g. Android Beam).
    public static int READER_FLAGS =
            NfcAdapter.FLAG_READER_NFC_A | NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK;
    public LoyaltyCardReader mLoyaltyCardReader;

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
        //if(intent.getBooleanExtra("back", false))
        {
            ActionBar ab = getActionBar();
            if(ab != null)
                ab.setDisplayHomeAsUpEnabled(true);
        }

		setContentView(R.layout.activity_main);
        kkaexparams.runShellCommand("chmod 0666 /dev/rkey");

		Intent startIntent = new Intent(this, TXDeviceService.class); 
		startService(startIntent);

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
        if(Build.VERSION.SDK_INT >= 19) {
            mLoyaltyCardReader = new LoyaltyCardReader(this);
        }

        // Disable Android Beam and register our card reader callback
        enableReaderMode();

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

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        switch(keyCode){
            case KeyEvent.KEYCODE_0:
                SoundPoolUtil.getSoundPoolUtil().loadVoice(this,0);
                return true;
            case KeyEvent.KEYCODE_1:
                SoundPoolUtil.getSoundPoolUtil().loadVoice(this,1);
                return true;
            case KeyEvent.KEYCODE_2:
                SoundPoolUtil.getSoundPoolUtil().loadVoice(this,2);
                return true;
            case KeyEvent.KEYCODE_3:
                SoundPoolUtil.getSoundPoolUtil().loadVoice(this,3);
                return true;
            case KeyEvent.KEYCODE_4:
                SoundPoolUtil.getSoundPoolUtil().loadVoice(this,4);
                return true;
            case KeyEvent.KEYCODE_5:
                SoundPoolUtil.getSoundPoolUtil().loadVoice(this,5);
                return true;
            case KeyEvent.KEYCODE_6:
                SoundPoolUtil.getSoundPoolUtil().loadVoice(this,6);
                return true;
            case KeyEvent.KEYCODE_7:
                SoundPoolUtil.getSoundPoolUtil().loadVoice(this,7);
                return true;
            case KeyEvent.KEYCODE_8:
                SoundPoolUtil.getSoundPoolUtil().loadVoice(this,8);
                return true;
            case KeyEvent.KEYCODE_9:
                SoundPoolUtil.getSoundPoolUtil().loadVoice(this,9);
                return true;
            case KeyEvent.KEYCODE_STAR:
                SoundPoolUtil.getSoundPoolUtil().loadVoice(this,10);
                return true;
            case KeyEvent.KEYCODE_ENTER:
            case KeyEvent.KEYCODE_POUND:
                SoundPoolUtil.getSoundPoolUtil().loadVoice(this,11);
                return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    @Override
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
        MenuItem m_opendoor_log = menu.add("打开主门");
        MenuItem m_opendoor1_log = menu.add("打开副门");
        MenuItem m_setAlarm = menu.add("设置定时开机");
        MenuItem m_runReboot = menu.add("重启");
        MenuItem m_runShutdown = menu.add("关机");
        MenuItem m_setPlugedShutdown = menu.add("设置拔电关机");

        MenuItem m_exit_log = menu.add("退出");

        m_unbind.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {

                return true;
            }
        });

        m_upload_log.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                uploadDeviceLog(null);
                return true;
            }
        });

        m_opendoor_log.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                int status = 2;
                Intent ds_intent = new Intent();
                ds_intent.setAction(DoorLock.DoorLockOpenDoor);
                ds_intent.putExtra("index",0);
                ds_intent.putExtra("status",status);
                sendBroadcast(ds_intent);
                return true;
            }
        });

        m_opendoor1_log.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                int status = 2;
                Intent ds_intent = new Intent();
                ds_intent.setAction(DoorLock.DoorLockOpenDoor);
                ds_intent.putExtra("index",1);
                ds_intent.putExtra("status",status);
                sendBroadcast(ds_intent);
                return true;
            }
        });
        m_exit_log.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                setResult(RESULT_OK);
                finish();
                sendBroadcast(new Intent("com.android.action.display_navigationbar"));
                return true;
            }
        });

        m_setAlarm.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                long wakeupTime = SystemClock.elapsedRealtime() + 240000;       //唤醒时间,如果是关机唤醒时间不能低于3分钟,否则无法实现关机定时重启

                DoorLock.getInstance().runSetAlarm(wakeupTime);
                return true;
            }
        });

        m_runReboot.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                DoorLock.getInstance().runReboot();
                return true;
            }
        });

        m_runShutdown.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                DoorLock.getInstance().runShutdown();
                return true;
            }
        });

        m_setPlugedShutdown.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                DoorLock.getInstance().setPlugedShutdown();
                return true;
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                setResult(RESULT_OK);
                finish();
                return true;
        }
        return false;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        return super.dispatchKeyEvent(event);
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
        enableReaderMode();
	}
	
	protected void onPause(){
		super.onPause();
        disableReaderMode();
        //unbindService(mConn);
    }
	
	protected void onDestroy(){
		super.onDestroy();
        //unbindService(mConn);
        unregisterReceiver(mNotifyReceiver);
        sendBroadcast(new Intent("com.android.action.display_navigationbar"));
	}

    private void enableReaderMode() {
        Log.i("", "启用读卡模式");
        if(Build.VERSION.SDK_INT >= 19)
        {
            Activity activity = this;
            NfcAdapter nfc = NfcAdapter.getDefaultAdapter(activity);
            if (nfc != null) {
                nfc.enableReaderMode(activity, mLoyaltyCardReader, READER_FLAGS, null);
            }
        }
    }

    private void disableReaderMode() {
        Log.i("", "禁用读卡模式");
        if(Build.VERSION.SDK_INT >= 19) {
            Activity activity = this;
            NfcAdapter nfc = NfcAdapter.getDefaultAdapter(activity);
            if (nfc != null) {
                nfc.disableReaderMode(activity);
            }
        }
    }

    @Override
    public void onAccountReceived(String account) {
        // This callback is run on a background thread, but updates to UI elements must be performed
        // on the UI thread.
        toast.setText(account);
        toast.show();
        /*getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mAccountField.setText(account);
            }
        });*/
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

}
