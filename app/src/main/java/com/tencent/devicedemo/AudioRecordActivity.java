package com.tencent.devicedemo;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Environment;
import android.view.Gravity;
import android.view.KeyEvent;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.audiorecoder.AudioRecoderDialog;
import com.audiorecoder.AudioRecoderUtils;
import com.tencent.device.TXDeviceService;

import java.io.File;

/**
 * Created by xinshuhao on 16/7/17.
 */
public class AudioRecordActivity extends Activity implements AudioRecoderUtils.OnAudioStatusUpdateListener {
    private AudioRecoderDialog recoderDialog;
    private AudioRecoderUtils recoderUtils;
    private CheckBox cb_record;
    private TextView tt_tv;
    private long downT;
    private String audioFile;
    private Long peerTinyId;
    private long[] aa=new long[1];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_audiorecord);
        recoderDialog = new AudioRecoderDialog(this);
        recoderDialog.setShowAlpha(0.98f);
        recoderDialog.setFocusable(false);

        Intent intent = getIntent();
        peerTinyId = intent.getLongExtra("tinyid", 0);
        aa[0]=peerTinyId;

         audioFile = this.getCacheDir().getAbsolutePath() + "/recoder.amr";
        recoderUtils = new AudioRecoderUtils(new File(audioFile));
        recoderUtils.setOnAudioStatusUpdateListener(this);
        cb_record=(CheckBox)findViewById(R.id.cb_audiorecord);
        tt_tv=(TextView)findViewById(R.id.tv_recordname);

      TextView  tt_tv1=(TextView)findViewById(R.id.tv_recordname);
        TextView  tt_tv2=(TextView)findViewById(R.id.btn_send_audio);
        TextView  tt_tv3=(TextView)findViewById(R.id.tv_back);
        Typeface typeFace = Typeface.createFromAsset(getAssets(), "fonts/GBK.TTF");
       tt_tv1.setTypeface(typeFace);
        tt_tv2.setTypeface(typeFace);
        tt_tv3.setTypeface(typeFace);


        cb_record.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    recoderUtils.startRecord();
                    downT = System.currentTimeMillis();
                   recoderDialog.showAtLocation(cb_record, Gravity.CENTER, 0, 0);
                    tt_tv.setText("录音中(2键停止)");
                   }else if(!isChecked){
                    recoderUtils.stopRecord();
                    tt_tv.setText("录音(1键)");
                    recoderDialog.dismiss();
                }
            }
        });
    }

    @Override
    public void onUpdate(double db) {
        if(null != recoderDialog) {
            int level = (int) db;
            recoderDialog.setLevel((int) db);
            recoderDialog.setTime(System.currentTimeMillis() - downT);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch(keyCode){
            case KeyEvent.KEYCODE_1:
                cb_record.setChecked(true);
                return true;
            case KeyEvent.KEYCODE_2:
                recoderDialog.dismiss();
             cb_record.setChecked(false);
                return true;
            case KeyEvent.KEYCODE_3:
                {
                TXDeviceService.sendAudioMsg(audioFile, (int)(System.currentTimeMillis() - downT),0 , aa);
                }
                return true;
            case KeyEvent.KEYCODE_DEL:
                finish();
                break;
        }
        return super.onKeyUp(keyCode, event);
    }
}
