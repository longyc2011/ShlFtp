
package com.setvens.shlftp;

import android.util.Log;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

import java.io.IOException;
import java.io.InputStream;
import java.net.SocketException;

public class FtpClientProxy {

    FTPClient ftpClient = new FTPClient();
    FtpConfig config;

    public FtpClientProxy(FtpConfig cfg) {
        this.config = cfg;
    }

    public FtpConfig getConfig() {
        return config;
    }

    public boolean connect() {
        try {
            FTPClientConfig ftpClientConfig = new FTPClientConfig(FTPClientConfig.SYST_UNIX);
            ftpClientConfig.setLenientFutureDates(true);
            ftpClient.configure(ftpClientConfig);
            ftpClient.connect(config.getServer(), config.getPort());
            int reply = this.ftpClient.getReplyCode();
            if (!FTPReply.isPositiveCompletion(reply)) {
                return false;
            }
            return true;
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean login() {
        if (!ftpClient.isConnected()) {
            return false;
        }
        try {
            boolean b = ftpClient.login(config.getUsername(), config.getPassword());
            if (!b) {
                return false;
            }
            ftpClient.setFileType(FTPClient.FILE_STRUCTURE);
            ftpClient.enterLocalPassiveMode(); // very important
            // ftpClient.enterLocalActiveMode();
            // ftpClient.enterRemotePassiveMode();
            // ftpClient.enterRemoteActiveMode(InetAddress.getByName(config.address),
            // config.port);
            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            return b;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public FTPFile[] getFTPFiles(String remoteDir) {
        try {
            return ftpClient.listFiles(remoteDir);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public FTPFile getFTPFile(String remotePath) {
        try {
            Log.d("", "getFTPFile.........." + remotePath);
            FTPFile f = ftpClient.mlistFile(remotePath);
            return f;
        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.d("", "getFTPFile null..........");
        return null;
    }

    public InputStream getRemoteFileStream(String remotePath) {
        InputStream ios;
        try {
            ios = ftpClient.retrieveFileStream(remotePath);
            return ios;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void close() {
        if (ftpClient.isConnected()) {
            try {
                ftpClient.logout();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            ftpClient.disconnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setRestartOffset(long len) {
        ftpClient.setRestartOffset(len);// 断点续传的position
    }

    public boolean isDone() {
        try {
            return ftpClient.completePendingCommand();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

}
