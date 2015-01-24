
package com.setvens.shlftp;

public class FtpConfig {
    private int port;
    private int bufferSize = 1024 * 4;
    private String server;
    private String username;
    private String password;
    private String path;

    private static int defaultBufferSize = 1024 * 4;

    public FtpConfig() {
    }

    public FtpConfig(String server, String username, String password, String path, int port,
            int bufferSize) {

        this.port = port;
        this.bufferSize = bufferSize;
        this.password = password;
        this.server = server;
        this.username = username;
        this.path = path;
    }

    public FtpConfig(String server, String username, String password) {
        this(server, username, password, "/", 21, defaultBufferSize);

    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getBufferSize() {
        return bufferSize;
    }

    public void setBufferSize(int bufferSize) {
        this.bufferSize = bufferSize;
    }

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

}
