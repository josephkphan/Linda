import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;

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

    public int search(String input) {
        Tuple findThis = new Tuple(input);
        for (int i = 0; i < tupleList.size(); i++) {
            if (tupleList.get(i).equals(findThis)) {
                System.out.println("Found!");
                System.out.println(i);
                System.out.println("tupleList.get(i) = " + tupleList.get(i).toString());
                return i;
            }

        }
        return -1;
    }

    public void print() {
        System.out.println(tupleList.toString());
    }

    public static void main(String[] args) {
        String s;
        Scanner scan = new Scanner(System.in);
        System.out.print("Enter Tuple:");
        s = scan.nextLine();
        TupleSpace ts = new TupleSpace();
        ts.add(s);
        ts.print();
        System.out.print("Enter Search Tuple");
        s = scan.nextLine();
        ts.search(s);
    }

    public void writeToFile(String hostname){
        try{
            PrintWriter writer = new PrintWriter(hostname + "-tuples.txt", "UTF-8");

            for (Tuple t: tupleList){
                writer.println(t.toString());
            }
            writer.close();
        } catch (IOException e) {
            // do something
        }
    }
}
