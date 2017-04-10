import javafx.util.Pair;

import java.util.ArrayList;
import java.util.Scanner;

/**
 * Created by jphan on 4/9/17.
 */
public class Tuple {
    private ArrayList<ArrayList<Pair<String,String>>> tupleList;

    public Tuple(){
        tupleList = new ArrayList<ArrayList<Pair<String, String> > >();
    }

    public Tuple(String tupleString){
        tupleList = new ArrayList<ArrayList<Pair<String, String> > >();
        add(tupleString);

    }

    public void add(String tupleString){
        ArrayList<Pair<String, String>> list = new ArrayList<Pair<String, String> >();
        String[] split = tupleString.split(",");
        for(String s: split){
            list.add(determineType(s));
        }
        tupleList.add(list);
    }

    public Pair<String,String> determineType(String string){
        String type;
        if(string.contains("."))
            type = "Double";
        else if(string.contains("\""))
            type = "String";
        else
            type = "Integer";
        return new Pair<>(string,type);

    }

    public void search(){
        //todo Implement me
    }

    public void print(){
        System.out.println(tupleList.toString());
    }

    public static void main(String[] args){
        String s;
        Scanner scan = new Scanner(System.in);
        System.out.print("Enter Tuple:");
        s = scan.nextLine();
        Tuple t = new Tuple(s);
        t.print();
    }
}
