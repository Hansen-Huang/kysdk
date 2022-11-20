package com.chuanshanjia.sdk.model;

/**
 * Created by chuanshanjia on 2018/04/03.
 */

import android.os.Handler;
import android.os.Message;
import android.widget.Button;

import java.util.Timer;
//定时器
public class MyCountTimer {
    public int TIME_COUNT = 60;

    private Button btn;

    Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            // 要做的事情
            TIME_COUNT = TIME_COUNT - 1;
            if (TIME_COUNT >= 0) {
                onTick(TIME_COUNT);
            } else {
                onFinish();
            }

        }
    };

    public MyCountTimer(long millisInFuture, long countDownInterval, Button btn) {
        this.btn = btn;
    }

    public MyCountTimer(Button btn) {
        this.btn = btn;
    }

    Timer task;

    public class MyThread implements Runnable {
        @Override
        public void run() {
            // TODO Auto-generated method stub
            while (TIME_COUNT >= 0) {
                try {

                    Message message = new Message();
                    handler.sendMessage(message);// 发送消息
                    Thread.sleep(1000);// 线程暂停1秒，单位毫秒
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
    }

    public void start() {
        TIME_COUNT = 60;
        new Thread(new MyThread()).start();
    }

    public void onFinish() {
        btn.setText("重新获取");
        btn.setEnabled(true);
    }

    public void onTick(long millisUntilFinished) {
        btn.setEnabled(false);
        btn.setText(millisUntilFinished + "秒后重发");
    }
}