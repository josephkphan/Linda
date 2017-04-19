import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.util.ArrayList;

public class LookUpTable {
    ArrayList<Pair<String, ArrayList<Range>>> lookUpTable;
    int min, max;

    public LookUpTable(int min, int max) {
        lookUpTable = new ArrayList<Pair<String, ArrayList<Range>>>();
        this.min = min;
        this.max = max;
    }

    public LookUpTable(int min, int max, String hostName) {
        lookUpTable = new ArrayList<Pair<String, ArrayList<Range>>>();
        this.min = min;
        this.max = max;
        addNewHost(hostName);
    }

    public void addNewHost(String Hostname) {
        int oldMax,oldRange, newMax;
        ArrayList<Range> host = new ArrayList<Range>();
        if (lookUpTable.size() == 0) {
//            host.add(new Range(0, (int)Math.pow(2, 32)-1));
            host.add(new Range(min,max));
        } else {
            for (Pair<String, ArrayList<Range>> p : lookUpTable) {
                for(Range r : p.getValue()){
                    oldMax = r.getMax();
                    oldRange = r.getRangeDifference();
                    newMax = (oldMax - (oldRange / (lookUpTable.size()+1)));
                    r.setMax(newMax-1);
                    host.add(new Range(newMax,oldMax));
                }
            }
        }
        lookUpTable.add(new Pair<>(Hostname, host));
    }

    public void deleteHost(String Hostname){
        int index = searchForHost(Hostname);
        int rangeDiff;
        int split;
        int max;
        if(index!= -1){
            //found
            for(Range r: lookUpTable.get(index).getValue()){
                rangeDiff = r.getRangeDifference();
                split = rangeDiff/(lookUpTable.size()-1);
                for (int i=0; i<lookUpTable.size(); i++) {
                    if(i==index){
                        continue;
                    }
                    max = r.getMin()+split;
                    if( max >=r.getMax()){
                        max = r.getMax();
                    }
                    lookUpTable.get(i).getValue().add(new Range(r.getMin(), max));
                    r.setMin(r.getMin()+split+1);
                    if( r.getMin()>r.getMax()){
                        break;
                    }
                }
            }
        }
        lookUpTable.remove(index);
    }

    public String getHostFromID(int id) {
        for (Pair<String, ArrayList<Range>> p : lookUpTable) {
            for (Range r : p.getValue()) {
                if(r.inRange(id)){
                    return p.getKey();
                }
            }
        }
        return "not found";
    }

    public int searchForHost(String Hostname){
        for (int i=0; i<lookUpTable.size(); i++) {
            if(lookUpTable.get(i).getKey().equals(Hostname)){
                return i;
            }
        }
        return -1;
    }

    @Override
    public String toString() {
        String string = "";
        for (Pair<String, ArrayList<Range>> p : lookUpTable) {
            string += p.getKey();
            string += ":";
            for (Range r : p.getValue()) {
                string += r.toString();
                string += ",";
            }
            string = string.substring(0, string.length() - 1);
            string += '/';
        }
        return string;
    }

    public void update(String string){
        lookUpTable.clear();
        String[] split = string.split("/");
        for(String s : split){
            System.out.println(s);
            ArrayList<Range> arrayList = new ArrayList<Range>();
            String[] split2 = s.split(":");
            String[] split3 = split2[1].split(",");

            for(String s2 : split3){
                String[] split4 = s2.split("_");
                arrayList.add(new Range(Integer.parseInt(split4[0]),Integer.parseInt(split4[1])));
            }

            Pair<String, ArrayList<Range>> p= new Pair<>(split2[0],arrayList);
            lookUpTable.add(p);
        }

    }
    public void save(String filePath) {
        String string = "";
        for (Pair<String, ArrayList<Range>> p : lookUpTable) {
            string += p.getKey();
            string += ":";
            for (Range r : p.getValue()) {
                string += r.toString();
                string += ",";
            }
            string = string.substring(0, string.length() - 1);
            string += '\n';
        }
        try {
            PrintWriter writer = new PrintWriter(filePath, "UTF-8");

            writer.print(string);
            writer.close();
        } catch (IOException e) {
            // do something
        }
    }

    public static void main(String[] args) {
        LookUpTable lookUpTable = new LookUpTable(0,99);
        lookUpTable.addNewHost("h0");
        lookUpTable.addNewHost("h1");
        lookUpTable.addNewHost("h2");
        lookUpTable.addNewHost("h3");
//        System.out.println(lookUpTable.toString());
        lookUpTable.deleteHost("h3");
        System.out.println(lookUpTable.toString());

        System.out.println("---------------------------------");
        LookUpTable t2 = new LookUpTable(0,99);
        t2.update(lookUpTable.toString());
        System.out.println(t2.toString());

    }
}
