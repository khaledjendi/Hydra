package service;

/**
 * Created by kal on 3/1/17.
 */
public class Server {
    private String srvName;
    private String srvIP;
    private String port;

    public String getSrvName() {
        return srvName;
    }

    public void setSrvName(String srvName) {
        this.srvName = srvName;
    }

    public String getSrvIP() {
        return srvIP;
    }

    public void setSrvIP(String srvIP) {
        this.srvIP = srvIP;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public Server() {
    }

    public Server(String srvName, String srvIP, String port) {
        this.srvName = srvName;
        this.srvIP = srvIP;
        this.port = port;
    }
}
