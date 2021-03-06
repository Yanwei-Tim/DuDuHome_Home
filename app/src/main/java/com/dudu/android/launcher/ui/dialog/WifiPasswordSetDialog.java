package com.dudu.android.launcher.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.dudu.android.launcher.R;

import com.dudu.android.launcher.utils.WifiApAdmin;

public class WifiPasswordSetDialog extends Dialog {

    private Context mContext;

    private EditText password1ET, password2ET;

    private Button wifiPasswordSetBT;

    private WifiApAdmin.WifiSettingStateCallback mCallBack;

    private WifiPasswordSetDialog(Context context, int theme) {
        super(context, theme);
        this.mContext = context;
    }

    private WifiPasswordSetDialog(Context context) {
        super(context, R.style.WifiSettingDialogStyle);
        this.mContext = context;
    }

    public static WifiPasswordSetDialog createDialog(Context context, WifiApAdmin.WifiSettingStateCallback callback) {
        WifiPasswordSetDialog dialog = new WifiPasswordSetDialog(context);
        dialog.setCallback(callback);
        return dialog;
    }

    public void setCallback(WifiApAdmin.WifiSettingStateCallback callback) {
        this.mCallBack = callback;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wifi_passwordset_dialog_windows);

        password1ET = (EditText) findViewById(R.id.password1ET);
        password2ET = (EditText) findViewById(R.id.password2ET);


        wifiPasswordSetBT = (Button) findViewById(R.id.wifiPasswordSetBT);
        wifiPasswordSetBT.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                setPassword();
            }
        });
    }

    private void setPassword() {
        if (TextUtils.isEmpty(password1ET.getText().toString())) {
            //密码不能为空
            return;
        }

        if (TextUtils.isEmpty(password2ET.getText().toString())) {
            //请确认密码

            return;
        }

        if (password1ET.getText().toString().length() < 8 || password2ET.getText().toString().length() < 8) {
            //密码最少为8位

            return;
        }

        if (!password1ET.getText().toString().equals(password2ET.getText().toString())) {
            //2次输入的密码不一样
            return;
        }

        if (WifiApAdmin.setWifiApPassword(mContext, password2ET.getText().toString())) {
            //设置密码成功

            this.dismiss();
        } else {
            //设置密码失败

        }
    }
}
