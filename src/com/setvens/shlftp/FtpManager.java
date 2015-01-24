
package com.setvens.shlftp;

import android.util.Log;

import org.apache.commons.net.ftp.FTPFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class FtpManager {

    FtpClientProxy ftpProxy;
    IRetrieveListener listener;
    volatile boolean isLogin = false;
    volatile boolean stopDownload = false;

    public FtpManager() {

    }

    public FtpManager(FtpConfig cfg) {
        ftpProxy = new FtpClientProxy(cfg);
    }

    /**
     * track listener for FTP downloading
     * 
     * @param listener
     */
    public void setListener(IRetrieveListener listener) {
        this.listener = listener;
    }

    /**
     * stop download task if you set true
     * 
     * @param stopDownload
     */
    public void setStopDownload(boolean stopDownload) {
        this.stopDownload = stopDownload;
    }

    public FTPFile[] showListFile(String remoteDir) {
        return ftpProxy.getFTPFiles(remoteDir);
    }

    public boolean connectLogin() {
        boolean ok = false;
        if (ftpProxy.connect()) {
            ok = ftpProxy.login();
        }
        isLogin = ok;
        return ok;
    }

    /**
     * @param remoteDir of FTP
     * @param name of file name under FTP Server's remote DIR.
     * @return FTPFile
     */
    public FTPFile getFileByName(String remoteDir, String name) {
        FTPFile[] files = showListFile(remoteDir);
        if (files != null) {
            for (FTPFile f : files) {
                if (name.equalsIgnoreCase(f.getName())) {
                    return f;
                }
            }
        }
        return null;
    }

    public void download(String remotePath, String localPath, long offset) {
        listener.onStart();
        File f = new File(localPath);
        byte[] buffer = new byte[ftpProxy.getConfig().getBufferSize()];
        int len = -1;
        long now = -1;
        boolean append = false;
        InputStream ins = null;
        OutputStream ous = null;
        try {
            if (offset > 0) { // 用于续传
                ftpProxy.setRestartOffset(offset);
                now = offset;
                append = true;
            }
            Log.d("", "downloadFile:" + now + ";" + remotePath);
            ins = ftpProxy.getRemoteFileStream(remotePath);
            ous = new FileOutputStream(f, append);
            Log.d("", "downloadFileRenew:" + ins);
            while ((len = ins.read(buffer)) != -1) {
                if (stopDownload) {
                    break;
                }
                ous.write(buffer, 0, len);
                now = now + len;
                listener.onTrack(now);// 监控当前下载了多少字节，可用于显示到UI进度条中
            }
            if (stopDownload) {
                listener.onCancel("");
            } else {
                if (ftpProxy.isDone()) {
                    listener.onDone();
                } else {
                    listener.onError("File Download Error", ERROR.FILE_DOWNLOAD_ERROR);
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
            listener.onError("File Download Error:" + e, ERROR.FILE_DOWNLOAD_ERROR);
        } finally {
            try {
                ous.close();
                ins.close();
            } catch (Exception e2) {
            }
        }
    }

    public void download(String remotePath, String localPath) {
        download(remotePath, localPath, -1);
    }

    public void close() {
        ftpProxy.close();
    }

    public static class ERROR { // 自己定义的一些错误代码
        public static final int FILE_NO_FOUNT = 9001;
        public static final int FILE_DOWNLOAD_ERROR = 9002;
        public static final int LOGIN_ERROR = 9003;
        public static final int CONNECT_ERROR = 9004;
    }
}
