package com.zistone.systemota;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.os.PowerManager;
import android.os.RecoverySystem;
import android.os.SystemProperties;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

public class MainActivity extends AppCompatActivity {

    private static SharedPreferences Share(Context context) {
        return context.getSharedPreferences("DEVICEFILTER", Context.MODE_PRIVATE);
    }

    private static final String TAG = "MainActivity";
    private static final String UPDATE_URL = "http://129.204.165.206:8080/GPRS_Web/Download/OTATest";
    private static final String TXT_URL = "http://129.204.165.206:8080/GPRS_Web/Download/TxtTest";
    //    private static final String UPDATE_URL = "http://192.168.43.164:8080/GPRS_Web/Download/OTATest";
    //    private static final String TXT_URL = "http://192.168.43.164:8080/GPRS_Web/Download/TxtTest";
    private static final String UPDATE_PATH = "/data/update.zip";
    private static final String TXT_PATH = "/data/update_info.txt";

    private TextView _txt1;
    private Button _btn1, _btn2;
    private PowerManager.WakeLock _wakeLock;
    private File localTxtFile, localUpdateFile;

    public int GetVersion(Context context) {
        return Share(context).getInt("Version", 111);
    }

    public boolean SetVersion(Context context, int version) {
        SharedPreferences.Editor editor = Share(context).edit();
        editor.putInt("Version", version);
        return editor.commit();
    }

    public File DownloadFile(String urlStr, String localPath) {
        File file = null;
        try {
            //统一资源
            URL url = new URL(urlStr);
            //连接类的父类，抽象类
            URLConnection urlConnection = url.openConnection();
            //http的连接类
            HttpURLConnection httpURLConnection = (HttpURLConnection) urlConnection;
            //设定请求的方法，默认是GET
            httpURLConnection.setRequestMethod("GET");
            //设置字符编码
            httpURLConnection.setRequestProperty("Charset", "UTF-8");
            //打开到此 URL 引用的资源的通信链接（如果尚未建立这样的连接）。
            httpURLConnection.connect();
            httpURLConnection.getResponseCode();
            InputStream inputStream = httpURLConnection.getInputStream();
            file = new File(localPath);
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            if (inputStream != null) {
                FileOutputStream fileOutputStream = new FileOutputStream(localPath);
                byte[] buf = new byte[1024];
                int ch;
                while ((ch = inputStream.read(buf)) != -1) {
                    //将获取到的流写入文件中
                    fileOutputStream.write(buf, 0, ch);
                }
                if (fileOutputStream != null) {
                    fileOutputStream.flush();
                    fileOutputStream.close();
                }
                httpURLConnection.disconnect();
            }
        } catch (Exception e) {
            e.printStackTrace();
            ShowInfo(_txt1, e.getMessage());
            file = null;
            DismissWaitWindow();
        }
        return file;
    }

    public String ReadFileByLines(File file) {
        String result = "";
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            String tempString = null;
            while ((tempString = reader.readLine()) != null) {
                result += tempString;
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }

    public String ReadFileByLines(String path) {
        String result = "";
        File file = new File(path);
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            String tempString = null;
            while ((tempString = reader.readLine()) != null) {
                result += tempString;
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 0:
                //可以遍历每个权限设置情况
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //这里写你需要相关权限的操作
                } else {
                    Toast.makeText(this, "权限没有开启", Toast.LENGTH_SHORT).show();
                }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @SuppressLint("InvalidWakeLockTag")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        PowerManager pm = (PowerManager) this.getSystemService(Context.POWER_SERVICE);
        _wakeLock = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "OTA Wakelock");
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
        _txt1 = findViewById(R.id.txt1);
        _txt1.setMovementMethod(ScrollingMovementMethod.getInstance());
        _btn1 = findViewById(R.id.btn1);
        _btn2 = findViewById(R.id.btn2);
        String sta = SystemProperties.get("ro.crypto.state");
        Log.d(TAG, "ro.crypto.state" + sta);
        if ("encrypted".equals(sta)) {
            Log.d(TAG, "ro.crypto.state");
        }
        SystemProperties.get("ro.build.date.utc");
    }

    private void ShowWaitWindow() {
        ProgressDialogUtil.ShowProgressDialog(MainActivity.this, false, "正在下载...");
    }

    private void DismissWaitWindow() {
        ProgressDialogUtil.Dismiss();
    }

    private File GetFilePathSite(String filePath, String fileName) {
        File file = new File(filePath);
        File[] fileList = file.listFiles();
        for (int i = 0; i < fileList.length; i++) {
            if (fileList[i].isDirectory()) {
                String result = fileList[i].getPath();
                File tempFile = new File(result + "/" + fileName);
                if (tempFile.exists()) {
                    return tempFile;
                }
            }
        }
        return null;
    }

    public void LocalUpdate(View v) {
        if (_btn2.getText().toString().equals("本地更新")) {
            new Thread(new Runnable() {
                public void run() {
                    localTxtFile = GetFilePathSite("/storage/", "update_info.txt");
                    if (localTxtFile == null || !localTxtFile.exists())
                        localTxtFile = GetFilePathSite("/mnt/media_rw/", "update_info.txt");
                    if (localTxtFile != null && localTxtFile.exists()) {
                        ShowInfo(_txt1, "版本信息读取成功");
                        String content = ReadFileByLines(localTxtFile);
                        int version = Integer.valueOf(content);
                        int shareValue = GetVersion(getApplicationContext());
                        if (version == shareValue) {
                            ShowInfo(_txt1, "已是最新版本！");
                        } else {
                            ShowInfo(_txt1, "发现新版本，可更新！");
                            SetVersion(getApplicationContext(), version);
                            BtnInfo(_btn2, "读取升级包");
                        }
                    } else {
                        ShowInfo(_txt1, "版本信息读取失败");
                    }
                }
            }).start();
        } else if (_btn2.getText().toString().equals("读取升级包")) {
            new Thread(new Runnable() {
                public void run() {
                    localUpdateFile = GetFilePathSite("/storage/", "update.zip");
                    if (localUpdateFile == null || !localUpdateFile.exists())
                        localUpdateFile = GetFilePathSite("/mnt/media_rw/", "update.zip");
                    if (localUpdateFile != null && localUpdateFile.exists()) {
                        ShowInfo(_txt1, "升级包读取成功");
                        BtnInfo(_btn2, "安装升级包");
                    } else {
                        ShowInfo(_txt1, "升级包读取失败");
                    }
                }
            }).start();
        } else if (_btn2.getText().toString().equals("安装升级包")) {
            new Thread(new Runnable() {
                public void run() {
                    File recoveryFile = new File(localUpdateFile.getPath());
                    _wakeLock.acquire();
                    //验证更新包的密码签名
                    try {
                        RecoverySystem.verifyPackage(recoveryFile, recoveryVerifyListener, null);
                    } catch (Exception e) {
                        _wakeLock.release();
                        e.printStackTrace();
                        ShowInfo(_txt1, "升级包验证失败，停止安装！" + e.getMessage());
                        return;
                    }
                    ShowInfo(_txt1, "升级包验证成功，开始安装...");
                    //安装更新包
                    try {
                        RecoverySystem.installPackage(getApplicationContext(), recoveryFile);
                    } catch (Exception e) {
                        _wakeLock.release();
                        e.printStackTrace();
                        ShowInfo(_txt1, "升级包安装失败，请与管理员联系！" + e.getMessage());
                        BtnInfo(_btn2, "本地更新");
                    }
                }
            }).start();
        }
    }

    public void IntelUpdate(View v) {
        ShowWaitWindow();
        if (_btn1.getText().toString().equals("网络更新")) {
            new Thread(new Runnable() {
                public void run() {
                    File file = DownloadFile(TXT_URL, TXT_PATH);
                    if (file != null && file.exists()) {
                        ShowInfo(_txt1, "版本信息下载成功");
                        String content = ReadFileByLines(TXT_PATH);
                        int version = Integer.valueOf(content);
                        int shareValue = GetVersion(getApplicationContext());
                        if (version == shareValue) {
                            ShowInfo(_txt1, "已是最新版本！");
                        } else {
                            ShowInfo(_txt1, "发现新版本，可更新！");
                            SetVersion(getApplicationContext(), version);
                            BtnInfo(_btn1, "下载升级包");
                        }
                    } else {
                        ShowInfo(_txt1, "版本信息下载失败");
                    }
                    DismissWaitWindow();
                }
            }).start();
        } else if (_btn1.getText().toString().equals("下载升级包")) {
            new Thread(new Runnable() {
                public void run() {
                    File file = DownloadFile(UPDATE_URL, UPDATE_PATH);
                    if (file != null && file.exists()) {
                        ShowInfo(_txt1, "升级包下载成功");
                        BtnInfo(_btn1, "安装升级包");
                    } else {
                        ShowInfo(_txt1, "升级包下载失败");
                    }
                    DismissWaitWindow();
                }
            }).start();
        } else if (_btn1.getText().toString().equals("安装升级包")) {
            new Thread(new Runnable() {
                public void run() {
                    File recoveryFile = new File(UPDATE_PATH);
                    _wakeLock.acquire();
                    //验证更新包的密码签名
                    try {
                        RecoverySystem.verifyPackage(recoveryFile, recoveryVerifyListener, null);
                    } catch (Exception e) {
                        _wakeLock.release();
                        e.printStackTrace();
                        ShowInfo(_txt1, "升级包验证失败，停止安装！" + e.getMessage());
                        DismissWaitWindow();
                        return;
                    }
                    ShowInfo(_txt1, "升级包验证成功，开始安装...");
                    //安装更新包
                    try {
                        RecoverySystem.installPackage(getApplicationContext(), recoveryFile);
                    } catch (Exception e) {
                        _wakeLock.release();
                        e.printStackTrace();
                        ShowInfo(_txt1, "升级包安装失败，请与管理员联系！" + e.getMessage());
                        BtnInfo(_btn1, "网络更新");
                    }
                    DismissWaitWindow();
                }
            }).start();
        }
    }

    RecoverySystem.ProgressListener recoveryVerifyListener = new RecoverySystem.ProgressListener() {
        public void onProgress(int progress) {
            Log.d(TAG, "升级包验证：" + progress + "%");
            final int progress1 = progress;
            ShowInfo(_txt1, "升级包验证：" + progress1 + "%");

        }
    };

    private void BtnInfo(final Button button, final String str) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                button.setText(str);
            }
        });
    }

    private void ShowInfo(final TextView textView, final String str) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textView.append(str+"\n");
            }
        });
    }

}
