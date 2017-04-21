import java.io.*;
import java.util.ArrayList;

/**
 * Data Structure that Contains Tuples. Uses ArrayList as Base.
 */
public class TupleSpace {
    private ArrayList<Tuple> tupleList;

    public TupleSpace() {
        tupleList = new ArrayList<Tuple>();
    }

    public TupleSpace(String tupleString) {
        tupleList = new ArrayList<Tuple>();
        add(tupleString);

    }

    public void add(String tupleString) {
        tupleList.add(new Tuple(tupleString));
    }

    public void remove(int index) {
        tupleList.remove(index);
    }

    public Tuple get(int index) {
        return tupleList.get(index);
    }

    /**
     * Returns the tuple that matches the input
     */
    public int search(String input) {
        Tuple findThis = new Tuple(input);
        for (int i = 0; i < tupleList.size(); i++) {
            if (tupleList.get(i).equals(findThis)) {
                return i;
            }

        }
        return -1;
    }

    public void print() {
        System.out.println(tupleList.toString());
    }


    public ArrayList<Tuple> getTupleList() {
        return tupleList;
    }

    public int size() {
        return tupleList.size();
    }

    /**
     * Writes Tuple Space to a file
     */
    public void save(String filePath) {
        try {
            PrintWriter writer = new PrintWriter(filePath, "UTF-8");

            for (Tuple t : tupleList) {
                writer.println(t.toString());
            }
            writer.close();
        } catch (IOException e) {
            // do something
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

    public void update(String string){
        tupleList.clear();
        String[] split = string.split("/");
        for(String s: split){
            this.add(s);
        }
    }
    @Override
    public String toString(){
        String string = "";
        for(Tuple t: tupleList){
            string+=t.toString();
            string +="/";
        }
        return string;
    }

    public static void main(String[] args) {
        TupleSpace t1 = new TupleSpace();
        t1.add("100:\"abc\",1,3.0");
        t1.add("99:2.0");
        System.out.println(t1.toString());
        TupleSpace t2 = new TupleSpace();
        t2.update(t1.toString());
        System.out.println("-------------------");
        System.out.println(t2.toString());
    }

}
