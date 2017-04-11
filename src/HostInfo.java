
public class HostInfo {
    private String hostName, iPAddress;
    private int portNumber, id;

    public HostInfo(String hostInfo, int id) {
        String[] split = hostInfo.split(",");
        hostName = split[0];
        iPAddress = split[1];
        this.id = id;
        portNumber = Integer.parseInt(split[2]);
    }

    public HostInfo(String hostInfo) {
        String[] split = hostInfo.split(",");
        hostName = split[0];
        iPAddress = split[1];
        this.id = Integer.parseInt(split[3]);
        portNumber = Integer.parseInt(split[2]);
    }

    public String getHostName() {
        return hostName;
    }

    public String getiPAddress() {
        return iPAddress;
    }

    public int getPortNumber() {
        return portNumber;
    }

    public int getId() {
        return id;
    }

    @Override
    public String toString() {
        return getHostName() + "," + getiPAddress() + "," + Integer.toString(getPortNumber()) +
                "," + Integer.toString(getId());
    }

    public boolean equals(HostInfo other) {
        if (other.getId() == this.getId())
            return true;
        return false;
    }

}
