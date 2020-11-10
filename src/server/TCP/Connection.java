package server.TCP;

import java.net.InetAddress;

public class Connection {
    private InetAddress clientAddress;
    private boolean wasBroken;

    private boolean downloading;
    private String downloadFileName;

    private boolean uploading;
    private String uploadingFileName;

    Connection(InetAddress clientAddress) {
        this.clientAddress = clientAddress;
        downloading = false;
        uploading = false;
        wasBroken = false;
    }

    public boolean isDownloading() {
        return downloading;
    }

    public void setDownloading(boolean downloading) {
        this.downloading = downloading;
    }

    public InetAddress getClientAddress() {
        return clientAddress;
    }

    public boolean isWasBroken() {
        return wasBroken;
    }

    public boolean isUploading() {
        return uploading;
    }

    public void setUploading(boolean uploading) {
        this.uploading = uploading;
    }

    public void setWasBroken(boolean wasBroken) {
        this.wasBroken = wasBroken;
    }

    public String getDownloadFileName() {
        return downloadFileName;
    }

    public void setDownloadFileName(String downloadFileName) {
        this.downloadFileName = downloadFileName;
    }

    public String getUploadingFileName() {
        return uploadingFileName;
    }

    public void setUploadingFileName(String uploadingFileName) {
        this.uploadingFileName = uploadingFileName;
    }
}
