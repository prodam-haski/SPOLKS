package server.UDP;

import java.net.InetAddress;

public class UDPClient {
    private InetAddress address;
    private int port;

    private boolean wantUpload;
    private boolean wantDownload;

    private String fileName;

    UDPClient(InetAddress address, int port) {
        this.address = address;
        this.port = port;
        wantDownload = false;
        wantUpload = false;
    }

    public boolean isWantUpload() {
        return wantUpload;
    }

    public void setWantUpload(boolean wantUpload) {
        this.wantUpload = wantUpload;
    }

    public boolean isWantDownload() {
        return wantDownload;
    }

    public void setWantDownload(boolean wantDownload) {
        this.wantDownload = wantDownload;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public InetAddress getAddress() {
        return address;
    }

    public void setAddress(InetAddress address) {
        this.address = address;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
