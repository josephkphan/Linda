import java.io.*;
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

    public int getIndex(int id) throws Exception {
        {
            for (int i = 0; i < hostList.size(); i++)
                if (hostList.get(i).getId() == id) {
                    return i;
                }
        }
        throw new Exception();
    }

    public int getIndex(String hostName) {
        for (int i = 0; i < hostList.size(); i++)
            if (hostList.get(i).getHostName().equals(hostName)) {
                return i;
            }
        return -1;

    }

    public HostInfo get(int index) {
        return hostList.get(index);
    }

    public void remove(String hostName){
        try {
            hostList.remove(getIndex(hostName));
        }catch (Exception e){
            System.out.println("incorrect host name");
        }
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

    public HostInfo getByHostName(String hostName) throws Exception {
        for (HostInfo h : hostList) {
            if (h.getHostName().equals(hostName))
                return h;
        }
        throw new Exception();
    }

    public void remove() {
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
    public void save(String filePath) {
        try {
            PrintWriter writer = new PrintWriter(filePath, "UTF-8");
            for (HostInfo h : hostList) {
                writer.println(h.toString());
            }
            writer.close();
        } catch (IOException e) {
            // do something
        }
    }

    public void update(String string){
        hostList.clear();
        String split[] = string.split("/");
        for(String s: split){
            this.addHost(s);
        }
    }

    public void fromFile(String filePath){
        try {
            File file = new File(filePath);
            FileReader fileReader = new FileReader(file);
            BufferedReader bufferedReader = new BufferedReader(fileReader);
            String line;
            String string = "";
            while ((line = bufferedReader.readLine()) != null) {
                string +=line;
                string +='/';
            }
            string = string.substring(0,string.length()-1);
            update(string);
            fileReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void main(String[] args) {
        String dir = "/home/jphan/IdeaProjects/Coen241CloudComputing/h0/nets.txt";
        HostInfoList hostInfoList = new HostInfoList();
        hostInfoList.fromFile(dir);
        System.out.println("hostInfoList.toString() = " + hostInfoList.toString());
        int index = hostInfoList.getIndex("h0");
        System.out.println("index = " + index);
    }

}
