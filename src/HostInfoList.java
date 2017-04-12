import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

/**
 * Data Structure that holds a list of HostInfo. Base uses Arraylist
 */
public class HostInfoList {
    private ArrayList<HostInfo> hostList;

    public HostInfoList() {
        hostList = new ArrayList<HostInfo>();
    }

    public void addHost(String hostInfoString, int id) {
        hostList.add(new HostInfo(hostInfoString, id));
    }

    public void addHost(String hostInfoString) {
        hostList.add(new HostInfo(hostInfoString));
    }

    /**
     * Checks whether the host list has a certain ID or not
     */
    public boolean contains(String hostInfoString, int id) {
        for (HostInfo h : hostList) {
            if (h.equals(new HostInfo(hostInfoString, id))) {
                return true;
            }
        }
        return false;
    }

    public HostInfo get(int index) {
        return hostList.get(index);
    }

    /**
     * Search HostInfo List by Host ID
     */
    public HostInfo getByID(int ID) {
        for (HostInfo h : hostList) {
            if (h.getId() == ID)
                return h;
        }
        return null;
    }

    public void remove(){
        //todo Implement me! Part 2
    }

    public int size() {
        return hostList.size();
    }

    public void clear() {
        hostList.clear();
    }

    /**
     * Prints out Host List onto Standard Out
     */
    public void print() {
        for (HostInfo h : hostList) {
            System.out.println(h.toString());
        }
    }

    @Override
    public String toString() {
        String string = "";
        for (HostInfo h : hostList) {
            string += h.toString();
            string += "/";
        }
        return string;
    }

    /**
     * Writes Host List to a file
     */
    public void save(String hostname) {
        try {
            PrintWriter writer = new PrintWriter(hostname + "-nets.txt", "UTF-8");
            for (HostInfo h : hostList) {
                writer.println(h.toString());
            }
            writer.close();
        } catch (IOException e) {
            // do something
        }
    }
}
