import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.util.ArrayList;

public class HostInfoList {
    ArrayList<HostInfo> hostList;

    public HostInfoList() {
        hostList = new ArrayList<HostInfo>();
    }

    public void addHost(String hostInfoString, int id) {
        hostList.add(new HostInfo(hostInfoString, id));
    }

    public void addHost(String hostInfoString) {
        hostList.add(new HostInfo(hostInfoString));
    }

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

    public HostInfo getByID(int ID) {
        for (HostInfo h : hostList) {
            if (h.getId() == ID)
                return h;
        }
        return null;
    }

    public int size() {
        return hostList.size();
    }

    public void clear() {
        hostList.clear();
    }

    public void print(){
        for (HostInfo h: hostList){
            System.out.println(h.toString());
        }
    }

    @Override
    public String toString(){
        String string = "";
        for (HostInfo h: hostList){
            string += h.toString();
            string +="/";
        }
        return string;
    }

    public void writeToFile(String hostname){
        try{
            PrintWriter writer = new PrintWriter(hostname + "-nets.txt", "UTF-8");

            for (HostInfo h: hostList){
                writer.println(h.toString());
            }
            writer.close();
        } catch (IOException e) {
            // do something
        }
    }
}
