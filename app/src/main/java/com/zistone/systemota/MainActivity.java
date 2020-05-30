package com.zistone.systemota;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.downloader.Error;
import com.downloader.OnCancelListener;
import com.downloader.OnDownloadListener;
import com.downloader.OnPauseListener;
import com.downloader.OnProgressListener;
import com.downloader.OnStartOrResumeListener;
import com.downloader.PRDownloader;
import com.downloader.Progress;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    private static SharedPreferences Share(Context context) {
        return context.getSharedPreferences("DEVICEFILTER", Context.MODE_PRIVATE);
    }

    private static final String TAG = "MainActivity";
    private static final String UPDATE_URL = "http://129.204.165.206:8080/GPRS_Web/Download/OTATest";
    private static final String TXT_URL = "http://129.204.165.206:8080/GPRS_Web/Download/TxtTest";
    //    private static final String UPDATE_URL = "http://192.168.10.193:8080/GPRS_Web/Download/OTATest";
    //    private static final String TXT_URL = "http://192.168.10.193:8080/GPRS_Web/Download/TxtTest";
    private static final String SAVED_PATH = "/data/";
    //    private static final String SAVED_PATH = "/sdcard/Download/";
    private static final String TXT_FILE_NAME = "update_info.txt";
    private static final String UPDATE_FILE_NAME = "update.zip";

    private TextView _txt1;
    private Button _btn1, _btn2;
    private File _localTxtFile, _localUpdateFile;
    private ProgressBar _progressBar;
    private int _localVersion, _serverVersion;

    public int GetVersion(Context context) {
        return Share(context).getInt("Version", 111);
    }

    public boolean SetVersion(Context context, int version) {
        SharedPreferences.Editor editor = Share(context).edit();
        editor.putInt("Version", version);
        return editor.commit();
    }

    public File DownloadFile(String urlStr, String savedLocalPath, String savedFileName) {
        PRDownloader.download(urlStr, savedLocalPath, savedFileName).build().setOnStartOrResumeListener(new OnStartOrResumeListener() {
            /**
             * 开始/恢复下载
             */
            @Override
            public void onStartOrResume() {
                _progressBar.setProgress(0, true);
                _progressBar.setVisibility(View.VISIBLE);
            }
        }).setOnPauseListener(new OnPauseListener() {
            /**
             * 暂停下载
             */
            @Override
            public void onPause() {
                _progressBar.setVisibility(View.INVISIBLE);
                MyAlertDialogUtil.Dismiss();
            }
        }).setOnCancelListener(new OnCancelListener() {
            /**
             * 取消下载
             */
            @Override
            public void onCancel() {
                _progressBar.setVisibility(View.INVISIBLE);
                MyAlertDialogUtil.Dismiss();
            }
        }).setOnProgressListener(new OnProgressListener() {
            /**
             * 下载进度
             * @param progress
             */
            @Override
            public void onProgress(Progress progress) {
                double value = progress.currentBytes * 100 / progress.totalBytes;
                _progressBar.setProgress((int) value, true);
                Log.i(TAG, "当前字节数：" + progress.currentBytes + "，总字节数：" + progress.totalBytes + "，下载进度：" + value);
            }
        }).start(new OnDownloadListener() {
            /**
             * 下载完毕
             */
            @Override
            public void onDownloadComplete() {
                if (_btn1.getText().toString().equals("网络更新")) {
                    File file = new File(SAVED_PATH + TXT_FILE_NAME);
                    if (file != null && file.exists()) {
                        ShowInfo(_txt1, "版本信息下载成功");
                        String content = ReadFileByLines(SAVED_PATH + TXT_FILE_NAME);
                        _serverVersion = Integer.valueOf(content);
                        _localVersion = GetVersion(getApplicationContext());
                        if (_localVersion == _serverVersion) {
                            ShowInfo(_txt1, "已是最新版本！");
                        }
                        //                        else
                        {
                            ShowInfo(_txt1, "发现新版本，可更新！");
                            BtnInfo(_btn1, "下载升级包");
                        }
                    } else {
                        ShowInfo(_txt1, "版本信息下载失败");
                    }
                } else if (_btn1.getText().toString().equals("下载升级包")) {
                    File file = new File(SAVED_PATH + UPDATE_FILE_NAME);
                    if (file != null && file.exists()) {
                        ShowInfo(_txt1, "升级包下载成功");
                        BtnInfo(_btn1, "安装升级包");
                    } else {
                        ShowInfo(_txt1, "升级包下载失败");
                    }
                } else if (_btn1.getText().toString().equals("安装升级包")) {
                }
                _progressBar.setVisibility(View.INVISIBLE);
                MyAlertDialogUtil.Dismiss();
                MyAlertDialogUtil.ShowMessage(MainActivity.this, "提示", "下载完毕");
            }

            /**
             * 下载时发生错误
             * @param error
             */
            @Override
            public void onError(Error error) {
                _progressBar.setVisibility(View.INVISIBLE);
                MyAlertDialogUtil.Dismiss();
                MyAlertDialogUtil.ShowMessage(MainActivity.this, "错误", "下载过程中发生错误");
            }
        });
        return null;
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
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
        _txt1 = findViewById(R.id.txt1);
        _txt1.setMovementMethod(ScrollingMovementMethod.getInstance());
        _btn1 = findViewById(R.id.btn1);
        _btn2 = findViewById(R.id.btn2);
        _progressBar = findViewById(R.id.progressBar);
        _progressBar.setMin(0);
        _progressBar.setMax(100);
        String sta = SystemProperties.get("ro.crypto.state");
        Log.d(TAG, "ro.crypto.state" + sta);
        if ("encrypted".equals(sta)) {
            Log.d(TAG, "ro.crypto.state");
        }
        SystemProperties.get("ro.build.date.utc");
        PRDownloader.initialize(getApplicationContext());
    }

    private File GetFilePathSite(String filePath, String fileName) {
        File file = new File(filePath);
        File[] fileList = file.listFiles();
        if (fileList != null) {
            for (int i = 0; i < fileList.length; i++) {
                if (fileList[i].isDirectory()) {
                    String result = fileList[i].getPath();
                    File tempFile = new File(result + "/" + fileName);
                    if (tempFile.exists()) {
                        return tempFile;
                    }
                }
            }
        }
        return null;
    }

    public void ClickLocalUpdate(View v) {
        if (_btn2.getText().toString().equals("本地更新")) {
            new Thread(new Runnable() {
                public void run() {
                    _localTxtFile = GetFilePathSite("/storage/", "update_info.txt");
                    if (_localTxtFile == null || !_localTxtFile.exists())
                        _localTxtFile = GetFilePathSite("/mnt/media_rw/", "update_info.txt");
                    if (_localTxtFile != null && _localTxtFile.exists()) {
                        ShowInfo(_txt1, "版本信息读取成功");
                        String content = ReadFileByLines(_localTxtFile);
                        _serverVersion = Integer.valueOf(content);
                        _localVersion = GetVersion(getApplicationContext());
                        if (_localVersion == _serverVersion) {
                            ShowInfo(_txt1, "已是最新版本！");
                        } else {
                            ShowInfo(_txt1, "发现新版本，可更新！");
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
                    _localUpdateFile = GetFilePathSite("/storage/", "update.zip");
                    if (_localUpdateFile == null || !_localUpdateFile.exists())
                        _localUpdateFile = GetFilePathSite("/mnt/media_rw/", "update.zip");
                    if (_localUpdateFile != null && _localUpdateFile.exists()) {
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
                    File recoveryFile = new File(_localUpdateFile.getPath());
                    //验证更新包的密码签名
                    try {
                        RecoverySystem.verifyPackage(recoveryFile, recoveryVerifyListener, null);
                    } catch (Exception e) {
                        e.printStackTrace();
                        ShowInfo(_txt1, "升级包验证失败，停止安装！" + e.getMessage());
                        return;
                    }
                    ShowInfo(_txt1, "升级包验证成功，开始安装...");
                    //安装更新包
                    try {
                        SetVersion(getApplicationContext(), _serverVersion);
                        RecoverySystem.installPackage(getApplicationContext(), recoveryFile);
                    } catch (Exception e) {
                        SetVersion(getApplicationContext(), _localVersion);
                        e.printStackTrace();
                        ShowInfo(_txt1, "升级包安装失败，请与管理员联系！" + e.getMessage());
                        BtnInfo(_btn2, "本地更新");
                    }
                }
            }).start();
        }
    }

    public void ClickIntelUpdate(View v) {
        if (_btn1.getText().toString().equals("网络更新")) {
            new Thread(new Runnable() {
                public void run() {
                    DownloadFile(TXT_URL, SAVED_PATH, TXT_FILE_NAME);
                }
            }).start();
        } else if (_btn1.getText().toString().equals("下载升级包")) {
            new Thread(new Runnable() {
                public void run() {
                    DownloadFile(UPDATE_URL, SAVED_PATH, UPDATE_FILE_NAME);
                }
            }).start();
        } else if (_btn1.getText().toString().equals("安装升级包")) {
            File recoveryFile = new File(SAVED_PATH + UPDATE_FILE_NAME);
            //验证更新包的密码签名
            try {
                RecoverySystem.verifyPackage(recoveryFile, recoveryVerifyListener, null);
            } catch (Exception e) {
                e.printStackTrace();
                ShowInfo(_txt1, "升级包验证失败，停止安装！" + e.getMessage());
                return;
            }
            ShowInfo(_txt1, "升级包验证成功，开始安装...");
            //安装更新包
            try {
                SetVersion(getApplicationContext(), _serverVersion);
                RecoverySystem.installPackage(getApplicationContext(), recoveryFile);
            } catch (Exception e) {
                SetVersion(getApplicationContext(), _localVersion);
                e.printStackTrace();
                ShowInfo(_txt1, "升级包安装失败，请与管理员联系！" + e.getMessage());
                BtnInfo(_btn1, "网络更新");
            }
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
                textView.append(str + "\n");
                int scrollAmount = textView.getLayout().getLineTop(textView.getLineCount()) - textView.getHeight();
                if (scrollAmount > 0)
                    textView.scrollTo(0, scrollAmount);
            }
        });
    }

}
