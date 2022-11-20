package com.chuanshanjia.sdk.model;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.chuanshanjia.sdk.R;

public class ConfirmDialog  extends Dialog {

    public ConfirmDialog(Context context) {
        super(context, R.style.MyDialog);
        View view = LayoutInflater.from(context).inflate(R.layout.csj_progress_dialog, null);
        setContentView(view);
    }

    public void showLoading() {
        show();
    }

    public void hideLoading() {
        if (isShowing()) {
            dismiss();
        }
    }
}
