package cn.Boy.DiskFile.dto;

public class CephFsConfigInfo {
    private String username;
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }

    private String monIp;
    public String getMonIp() {
        return monIp;
    }
    public void setMonIp(String monIp) {
        this.monIp = monIp;
    }

    private String userKey;
    public String getUserKey() {
        return userKey;
    }
    public void setUserKey(String userKey) {
        this.userKey = userKey;
    }

    private String mountPath;
    public String getMountPath() {
        return mountPath;
    }
    public void setMountPath(String mountPath) {
        this.mountPath = mountPath;
    }
}
