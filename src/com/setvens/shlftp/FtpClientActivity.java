
package com.setvens.shlftp;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ProgressBar;

import java.io.File;
import java.io.IOException;
import java.net.SocketException;
import java.util.List;

public class FtpClientActivity extends Activity {
    private static final String TAG = "FtpClientActivity";
    Button uploadBt, downloadBt;
    FtpConfig config = new FtpConfig();

    public static final int REFRESH_UPLOAD_PROGRESS = 1;
    public static final int REFRESH_DOWNLOAD_PROGRESS = 2;
    ProgressBar uploadBar, downloadBar;
    FtpUtils ftp = null;
    public static String sdcardPath = Environment.getExternalStorageDirectory().getAbsolutePath();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.up_load);
        uploadBar = (ProgressBar) this.findViewById(R.id.upload_progressbar);
        downloadBar = (ProgressBar) this.findViewById(R.id.download_progressbar);
        uploadBt = (Button) this.findViewById(R.id.upload);
        uploadBt.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                Thread uploadThread = new Thread("UploadThread") {
                    @Override
                    public void run() {
                        uploadFile();
                    }
                };

                uploadThread.start();

            }

        });
        downloadBt = (Button) this.findViewById(R.id.download);
        downloadBt.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                Thread downloadThread = new Thread("DownloadThread") {
                    @Override
                    public void run() {
                        downloadFile();
                    }
                };

                downloadThread.start();
            }

        });
        // µ«¬º√‹¬Î∫Õ’À∫≈
        String server = "192.168.1.170";
        String username = "ftp_test";
        String password = "ftptest";

        Log.w(TAG, "getExternalStorageDirectory = " + sdcardPath);

        config.setServer("192.168.1.170");
        config.setPort(21);
        config.setUsername(username);
        config.setPassword(password);
        config.setPath("bankafile");
        ftp = new FtpUtils();
        ftp.setHandler(handler);

    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        Thread thread = new Thread() {

            @Override
            public void run() {
                uploadFile();
            }

        };
        thread.start();

    }

    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
    }

    @Override
    protected void onStop() {
        // TODO Auto-generated method stub
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
    }

    public void downloadFile() {

        try {
            ftp.connectServer(config);
        } catch (SocketException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        // try {
        // List<String> files = ftp.getFileList("");
        // File root = new File(sdcardPath);
        // for (String file : files) {
        // Log.w(TAG, "file name = " + file);
        // }
        // } catch (IOException e) {
        // // TODO Auto-generated catch block
        // e.printStackTrace();
        // }
        // ftp.changeDirectory("bankafile");

        String remoteFile = "4.4BSD-Lite.tar.gz";
        String local = sdcardPath + "/download/4.4BSD-Lite.tar.gz";
        File localFile = new File(local);
        if (localFile.exists()) {
            localFile.delete();
            Log.w(TAG, "File " + local + " exists, delete it");
        }
        try {
            ftp.download(remoteFile, local);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private Handler handler = new Handler() {

        @Override
        public void handleMessage(Message msg) {
            int type = msg.what;

            switch (type) {
                case REFRESH_UPLOAD_PROGRESS: {
                    int progress = msg.arg1;
                    Log.w(TAG, "REFRESH_UPLOAD_PROGRESS" + progress);
                    uploadBar.setProgress(progress);
                    break;
                }
                case REFRESH_DOWNLOAD_PROGRESS: {
                    int progress = msg.arg1;
                    downloadBar.setProgress(progress);
                    Log.w(TAG, "REFRESH_DOWNLOAD_PROGRESS" + progress);
                    break;
                }
            }
        }

    };

    public void uploadFile() {

        try {
            File root = new File(sdcardPath);
            for (File file : root.listFiles()) {
                Log.w(TAG, "file name = " + file.getName());
            }
            ftp.connectServer(config);
        } catch (SocketException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        String localFile = sdcardPath + "/4.4BSD-Lite.tar.gz";
        try {
            ftp.upload(localFile, "4.4BSD-Lite.tar.gz");
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    private void showDialog(String mess) {
        Looper.prepare();
        new AlertDialog.Builder(FtpClientActivity.this).setTitle("message")
                .setMessage(mess)
                .setNegativeButton("»∑∂®", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface arg0, int arg1) {
                        // TODO Auto-generated method stub

                    }
                }).show();
        Looper.loop();
    }
}
