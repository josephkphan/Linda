import java.io.IOException;
import java.io.PrintWriter;
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
    public void save(String hostname) {
        try {
            PrintWriter writer = new PrintWriter(hostname + "-tuples.txt", "UTF-8");

            for (Tuple t : tupleList) {
                writer.println(t.toString());
            }
            writer.close();
        } catch (IOException e) {
            // do something
        }
    }

}
