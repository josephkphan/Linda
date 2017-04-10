import javafx.util.Pair;

import java.util.ArrayList;
import java.util.Scanner;

/**
 * Created by jphan on 4/9/17.
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


    public void search(String input) {
        Tuple findThis = new Tuple(input);
        for (int i = 0; i < tupleList.size(); i++) {
            if (tupleList.get(i).equals(findThis)) {
                System.out.println("Found!");
                System.out.println(i);
                System.out.println("tupleList.get(i) = " + tupleList.get(i).toString());
                break;
            }

        }

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
}
