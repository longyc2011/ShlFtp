
package com.setvens.shlftp;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

public class FtpUtils {

    private FTPClient ftpClient;

    private static final String TAG = "FtpUtils";

    private Handler handler = null;

    /**
     * ��·��Ϊ"/",�����Ҫ���ӷ�����֮����ת��·��������path�ж���
     * 
     * @param ftpConfig
     * @throws SocketException
     * @throws IOException
     */

    public boolean connectServer(FtpConfig ftpConfig) throws SocketException,

            IOException {

        String server = ftpConfig.getServer();

        int port = ftpConfig.getPort();

        String user = ftpConfig.getUsername();

        String password = ftpConfig.getPassword();

        String path = ftpConfig.getPath();

        return connectServer(server, port, user, password, path);

    }

    /**
     * ����ftp������
     * 
     * @param server ������ip
     * @param port �˿ڣ�ͨ��Ϊ21
     * @param user �û���
     * @param password ����
     * @param path ���������֮���Ĭ��·��
     * @return ���ӳɹ�����true�����򷵻�false
     * @throws SocketException
     * @throws IOException
     */

    public boolean connectServer(String server, int port, String user,

            String password, String path) throws SocketException, IOException {

        ftpClient = new FTPClient();

        ftpClient.connect(server, port);

        ftpClient.setControlEncoding("GBK");

        Log.w(TAG, "Connected to " + server + ".");

        Log.w(TAG, "FTP server reply code:" + ftpClient.getReplyCode());

        if (FTPReply.isPositiveCompletion(ftpClient.getReplyCode())) {

            if (ftpClient.login(user, password)) {

                // Path is the sub-path of the FTP path

                if (path.length() != 0) {

                    ftpClient.changeWorkingDirectory(path);

                }

                return true;

            }

        }

        disconnect();

        return false;

    }

    /**
     * �Ͽ���Զ�̷�����������
     * 
     * @throws IOException
     */

    public void disconnect() throws IOException {

        if (ftpClient.isConnected()) {

            ftpClient.disconnect();

        }

    }

    /**
     * ��FTP�������������ļ�,֧�ֶϵ����������ذٷֱȻ㱨
     * 
     * @param remote Զ���ļ�·��������
     * @param local �����ļ���������·��
     * @return ���ص�״̬
     * @throws IOException
     */

    public DownloadStatus download(String remote, String local)

            throws IOException {

        // ���ñ���ģʽ

        ftpClient.enterLocalPassiveMode();

        // �����Զ����Ʒ�ʽ����

        ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

        DownloadStatus result;

        // ���Զ���ļ��Ƿ����

        FTPFile[] files = ftpClient.listFiles(new String(

                remote.getBytes("GBK"), "iso-8859-1"));

        if (files.length != 1) {

            Log.w(TAG, "Զ���ļ�������");

            return DownloadStatus.RemoteFileNotExist;

        }

        long lRemoteSize = files[0].getSize();

        File f = new File(local);

        // ���ش����ļ������жϵ�����

        if (f.exists()) {

            long localSize = f.length();

            // �жϱ����ļ���С�Ƿ����Զ���ļ���С

            if (localSize >= lRemoteSize) {

                Log.w(TAG, "�����ļ�����Զ���ļ���������ֹ");

                return DownloadStatus.LocalFileBiggerThanRemoteFile;

            }

            // ���жϵ�����������¼״̬

            FileOutputStream out = new FileOutputStream(f, true);

            ftpClient.setRestartOffset(localSize);

            InputStream in = ftpClient.retrieveFileStream(new String(remote

                    .getBytes("GBK"), "iso-8859-1"));

            byte[] bytes = new byte[1024];

            long step = lRemoteSize / 100;

            step = step == 0 ? 1 : step;// �ļ���С��step����Ϊ0

            int process = (int) (localSize / step);

            int c;

            while ((c = in.read(bytes)) != -1) {

                out.write(bytes, 0, c);

                localSize += c;

                int nowProcess = (int) (localSize / step);

                if (nowProcess > process) {

                    process = nowProcess;

                    if (process % 10 == 0) {
                        Message msg = handler.obtainMessage();
                        msg.what = FtpClientActivity.REFRESH_DOWNLOAD_PROGRESS;
                        msg.arg1 = process;
                        handler.sendMessage(msg);
                        Log.w(TAG, "���ؽ��ȣ�" + process);

                    }

                }

            }

            in.close();

            out.close();

            boolean isDo = ftpClient.completePendingCommand();

            if (isDo) {

                result = DownloadStatus.DownloadFromBreakSuccess;

            } else {

                result = DownloadStatus.DownloadFromBreakFailed;

            }

        } else {

            OutputStream out = new FileOutputStream(f);

            InputStream in = ftpClient.retrieveFileStream(new String(remote

                    .getBytes("GBK"), "iso-8859-1"));

            byte[] bytes = new byte[1024];

            long step = lRemoteSize / 100;

            step = step == 0 ? 1 : step;// �ļ���С��step����Ϊ0

            int process = 0;

            long localSize = 0L;

            int c;

            while ((c = in.read(bytes)) != -1) {

                out.write(bytes, 0, c);

                localSize += c;

                int nowProcess = (int) (localSize / step);

                if (nowProcess > process) {

                    process = nowProcess;

                    if (process % 10 == 0) {

                        Message msg = handler.obtainMessage();
                        msg.what = FtpClientActivity.REFRESH_DOWNLOAD_PROGRESS;
                        msg.arg1 = process;
                        handler.sendMessage(msg);
                        Log.w(TAG, "���ؽ��ȣ�" + process);

                    }

                }

            }

            in.close();

            out.close();

            boolean upNewStatus = ftpClient.completePendingCommand();

            if (upNewStatus) {

                result = DownloadStatus.DownloadNewSuccess;

            } else {

                result = DownloadStatus.DownloadNewFailed;

            }

        }

        return result;

    }

    public boolean changeDirectory(String path) throws IOException {

        return ftpClient.changeWorkingDirectory(path);

    }

    public boolean createDirectory(String pathName) throws IOException {

        return ftpClient.makeDirectory(pathName);

    }

    public boolean removeDirectory(String path) throws IOException {

        return ftpClient.removeDirectory(path);

    }

    public boolean removeDirectory(String path, boolean isAll)

            throws IOException {

        if (!isAll) {

            return removeDirectory(path);

        }

        FTPFile[] ftpFileArr = ftpClient.listFiles(path);

        if (ftpFileArr == null || ftpFileArr.length == 0) {

            return removeDirectory(path);

        }

        //

        for (FTPFile ftpFile : ftpFileArr) {

            String name = ftpFile.getName();

            if (ftpFile.isDirectory()) {

                Log.w(TAG, "* [sD]Delete subPath [" + path + "/" + name + "]");

                if (!ftpFile.getName().equals(".")

                        && (!ftpFile.getName().equals(".."))) {

                    removeDirectory(path + "/" + name, true);

                }

            } else if (ftpFile.isFile()) {

                Log.w(TAG, "* [sF]Delete file [" + path + "/" + name + "]");

                deleteFile(path + "/" + name);

            } else if (ftpFile.isSymbolicLink()) {

            } else if (ftpFile.isUnknown()) {

            }

        }

        return ftpClient.removeDirectory(path);

    }

    /**
     * �鿴Ŀ¼�Ƿ����
     * 
     * @param path
     * @return
     * @throws IOException
     */

    public boolean isDirectoryExists(String path) throws IOException {

        boolean flag = false;

        FTPFile[] ftpFileArr = ftpClient.listFiles(path);

        for (FTPFile ftpFile : ftpFileArr) {

            if (ftpFile.isDirectory()

                    && ftpFile.getName().equalsIgnoreCase(path)) {

                flag = true;

                break;

            }

        }

        return flag;

    }

    /**
     * �õ�ĳ��Ŀ¼�µ��ļ����б�
     * 
     * @param path
     * @return
     * @throws IOException
     */

    public List<String> getFileList(String path) throws IOException {

        // listFiles return contains directory and file, it's FTPFile instance

        // listNames() contains directory, so using following to filer

        // directory.

        // String[] fileNameArr = ftpClient.listNames(path);

        FTPFile[] ftpFiles = ftpClient.listFiles(path);

        List<String> retList = new ArrayList<String>();

        if (ftpFiles == null || ftpFiles.length == 0) {

            return retList;

        }

        for (FTPFile ftpFile : ftpFiles) {

            if (ftpFile.isFile()) {

                retList.add(ftpFile.getName());

            }

        }

        return retList;

    }

    public boolean deleteFile(String pathName) throws IOException {

        return ftpClient.deleteFile(pathName);

    }

    /**
     * �ϴ��ļ���FTP��������֧�ֶϵ�����
     * 
     * @param local �����ļ����ƣ�����·��
     * @param remote Զ���ļ�·��������Linux�ϵ�·��ָ����ʽ��֧�ֶ༶Ŀ¼Ƕ�ף�֧�ֵݹ鴴�������ڵ�Ŀ¼�ṹ
     * @return �ϴ����
     * @throws IOException
     */

    public UploadStatus upload(String local, String remote) throws IOException {

        // ����PassiveMode����

        ftpClient.enterLocalPassiveMode();

        // �����Զ��������ķ�ʽ����

        ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

        ftpClient.setControlEncoding("GBK");

        UploadStatus result;

        // ��Զ��Ŀ¼�Ĵ���

        String remoteFileName = remote;

        if (remote.contains("/")) {

            remoteFileName = remote.substring(remote.lastIndexOf("/") + 1);

            // ����������Զ��Ŀ¼�ṹ������ʧ��ֱ�ӷ���

            if (createDirecroty(remote, ftpClient) == UploadStatus.CreateDirectoryFail) {

                return UploadStatus.CreateDirectoryFail;

            }

        }

        // ���Զ���Ƿ�����ļ�

        FTPFile[] files = ftpClient.listFiles(new String(remoteFileName

                .getBytes("GBK"), "iso-8859-1"));

        if (files.length == 1) {

            long remoteSize = files[0].getSize();

            File f = new File(local);

            long localSize = f.length();

            if (remoteSize == localSize) { // �ļ�����

                return UploadStatus.FileExits;

            } else if (remoteSize > localSize) {

                return UploadStatus.RemoteFileBiggerThanLocalFile;

            }

            // �����ƶ��ļ��ڶ�ȡָ��,ʵ�ֶϵ�����

            result = uploadFile(remoteFileName, f, ftpClient, remoteSize);

            // ����ϵ�����û�гɹ�����ɾ�����������ļ��������ϴ�

            if (result == UploadStatus.UploadFromBreakFailed) {

                if (!ftpClient.deleteFile(remoteFileName)) {

                    return UploadStatus.DeleteRemoteFaild;

                }

                result = uploadFile(remoteFileName, f, ftpClient, 0);

            }

        } else {

            result = uploadFile(remoteFileName, new File(local), ftpClient, 0);

        }

        return result;

    }

    /**
     * �ݹ鴴��Զ�̷�����Ŀ¼
     * 
     * @param remote Զ�̷������ļ�����·��
     * @param ftpClient FTPClient����
     * @return Ŀ¼�����Ƿ�ɹ�
     * @throws IOException
     */

    public UploadStatus createDirecroty(String remote, FTPClient ftpClient)

            throws IOException {

        UploadStatus status = UploadStatus.CreateDirectorySuccess;

        String directory = remote.substring(0, remote.lastIndexOf("/") + 1);

        if (!directory.equalsIgnoreCase("/")

                && !ftpClient.changeWorkingDirectory(new String(directory

                        .getBytes("GBK"), "iso-8859-1"))) {

            // ���Զ��Ŀ¼�����ڣ���ݹ鴴��Զ�̷�����Ŀ¼

            int start = 0;

            int end = 0;

            if (directory.startsWith("/")) {

                start = 1;

            } else {

                start = 0;

            }

            end = directory.indexOf("/", start);

            while (true) {

                String subDirectory = new String(remote.substring(start, end)

                        .getBytes("GBK"), "iso-8859-1");

                if (!ftpClient.changeWorkingDirectory(subDirectory)) {

                    if (ftpClient.makeDirectory(subDirectory)) {

                        ftpClient.changeWorkingDirectory(subDirectory);

                    } else {

                        Log.w(TAG, "����Ŀ¼ʧ��");

                        return UploadStatus.CreateDirectoryFail;

                    }

                }

                start = end + 1;

                end = directory.indexOf("/", start);

                // �������Ŀ¼�Ƿ񴴽����

                if (end <= start) {

                    break;

                }

            }

        }

        return status;

    }

    /**
     * �ϴ��ļ���������,���ϴ��Ͷϵ�����
     * 
     * @param remoteFile Զ���ļ��������ϴ�֮ǰ�Ѿ�������������Ŀ¼���˸ı�
     * @param localFile �����ļ�File���������·��
     * @param processStep ��Ҫ��ʾ�Ĵ�����Ȳ���ֵ
     * @param ftpClient FTPClient����
     * @return
     * @throws IOException
     */

    public UploadStatus uploadFile(String remoteFile, File localFile,

            FTPClient ftpClient, long remoteSize) throws IOException {

        UploadStatus status;

        // ��ʾ���ȵ��ϴ�

        System.out.println("localFile.length():" + localFile.length());

        long step = localFile.length() / 100;

        step = step == 0 ? 1 : step;// �ļ���С��step����Ϊ0

        int process = 0;

        long localreadbytes = 0L;

        RandomAccessFile raf = new RandomAccessFile(localFile, "r");

        OutputStream out = ftpClient.appendFileStream(new String(remoteFile

                .getBytes("GBK"), "iso-8859-1"));

        // �ϵ�����

        if (remoteSize > 0) {

            ftpClient.setRestartOffset(remoteSize);

            process = (int) (remoteSize / step);

            raf.seek(remoteSize);

            localreadbytes = remoteSize;

        }

        byte[] bytes = new byte[1024];

        int c;

        while ((c = raf.read(bytes)) != -1) {

            out.write(bytes, 0, c);

            localreadbytes += c;

            if (localreadbytes / step != process) {

                process = (int) (localreadbytes / step);

                if (process % 10 == 0) {
                    Message msg = handler.obtainMessage();
                    msg.what = FtpClientActivity.REFRESH_UPLOAD_PROGRESS;
                    msg.arg1 = process;
                    handler.sendMessage(msg);
                    Log.w(TAG, "�ϴ����ȣ�" + process);

                }

            }

        }

        out.flush();

        raf.close();

        out.close();

        boolean result = ftpClient.completePendingCommand();

        if (remoteSize > 0) {

            status = result ? UploadStatus.UploadFromBreakSuccess

                    : UploadStatus.UploadFromBreakFailed;

        } else {

            status = result ? UploadStatus.UploadNewFileSuccess

                    : UploadStatus.UploadNewFileFailed;

        }

        return status;

    }

    public InputStream downFile(String sourceFileName) throws IOException {

        return ftpClient.retrieveFileStream(sourceFileName);

    }

    public enum UploadStatus {

        CreateDirectoryFail, // Զ�̷�������ӦĿ¼����ʧ��

        CreateDirectorySuccess, // Զ�̷���������Ŀ¼�ɹ�

        UploadNewFileSuccess, // �ϴ����ļ��ɹ�

        UploadNewFileFailed, // �ϴ����ļ�ʧ��

        FileExits, // �ļ��Ѿ�����

        RemoteFileBiggerThanLocalFile, // Զ���ļ����ڱ����ļ�

        UploadFromBreakSuccess, // �ϵ������ɹ�

        UploadFromBreakFailed, // �ϵ�����ʧ��

        DeleteRemoteFaild; // ɾ��Զ���ļ�ʧ��

    }

    public enum DownloadStatus {

        RemoteFileNotExist, // Զ���ļ�������

        DownloadNewSuccess, // �����ļ��ɹ�

        DownloadNewFailed, // �����ļ�ʧ��

        LocalFileBiggerThanRemoteFile, // �����ļ�����Զ���ļ�

        DownloadFromBreakSuccess, // �ϵ������ɹ�

        DownloadFromBreakFailed; // �ϵ�����ʧ��

    }

    public Handler getHandler() {
        return handler;
    }

    public void setHandler(Handler handler) {
        this.handler = handler;
    }

}
