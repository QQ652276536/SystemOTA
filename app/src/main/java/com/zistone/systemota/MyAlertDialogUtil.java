package com.zistone.systemota;

import android.app.Activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

public class MyAlertDialogUtil {

    private static AlertDialog _alertDialog;
    private static Listener _listener;

    public interface Listener {
        void OnDismiss();

        void OnConfirm();

        void OnCancel();
    }

    /**
     * 确认窗体
     * @param context
     * @param title
     * @param content
     */
    public static void ShowConfirm(Context context, String title, String content) {
        //确保创建Dialog的Activity没有finish才显示
        if (context instanceof Activity && !((Activity) context).isFinishing()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(title);
            builder.setMessage(content);
            builder.setNegativeButton("好的", (dialog, which) -> {
                _listener.OnConfirm();
            });
            builder.setPositiveButton("不了", (dialog, which) -> {
                _listener.OnCancel();
            });
            builder.show();
        }
    }

    /**
     * 提示窗体
     * @param context
     * @param title
     * @param content
     */
    public static void ShowMessage(Context context, String title, String content) {
        //确保创建Dialog的Activity没有finish才显示
        if (context instanceof Activity && !((Activity) context).isFinishing()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(title);
            builder.setMessage(content);
            builder.setPositiveButton("知道了", (dialog, which) -> {
            });
            builder.show();
        }
    }

    /**
     * 等待窗体
     * @param context
     * @param touchOutSide
     * @param listener
     * @param str
     */
    public static void ShowWait(Context context, boolean touchOutSide, Listener listener, String str) {
        //确保创建Dialog的Activity没有finish才显示
        if (context instanceof Activity && !((Activity) context).isFinishing()) {
            if (_alertDialog == null) {
                _alertDialog = new AlertDialog.Builder(context, R.style.CustomProgressDialog).create();
                _listener = listener;
            }
            View loadView = LayoutInflater.from(context).inflate(R.layout.progress_dialog, null);
            _alertDialog.setView(loadView, 0, 0, 0, 0);
            _alertDialog.setCanceledOnTouchOutside(touchOutSide);
            TextView textView = loadView.findViewById(R.id.txt_dialog);
            textView.setText(str);
            _alertDialog.show();
            _alertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    if (_listener != null)
                        _listener.OnDismiss();
                }
            });
        }
    }

    /**
     * 等待窗体
     * @param context
     * @param touchOutSide
     * @param str
     */
    public static void ShowWait(Context context, boolean touchOutSide, String str) {
        //确保创建Dialog的Activity没有finish才显示
        if (context instanceof Activity && !((Activity) context).isFinishing()) {
            if (_alertDialog == null) {
                _alertDialog = new AlertDialog.Builder(context, R.style.CustomProgressDialog).create();
            }
            View loadView = LayoutInflater.from(context).inflate(R.layout.progress_dialog, null);
            TextView textView = loadView.findViewById(R.id.txt_dialog);
            textView.setText(str);
            _alertDialog.setCanceledOnTouchOutside(touchOutSide);
            _alertDialog.setView(loadView, 0, 0, 0, 0);
            _alertDialog.show();
        }
    }

    public static void Dismiss() {
        if (_alertDialog != null) {
            _alertDialog.dismiss();
        }
    }

}