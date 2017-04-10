import javafx.util.Pair;

import java.util.ArrayList;

public class Tuple {
    private ArrayList<Pair<String, String>> tuple;

    public Tuple(String tupleString) {
        tuple = new ArrayList<Pair<String, String>>();
        String[] split = tupleString.split(",");
        for (String s : split) {
            tuple.add(determineType(s));
        }
    }

    public Pair<String, String> determineType(String string) {
        String type;
        if (string.contains("."))
            type = "float";
        else if (string.contains("\""))
            type = "string";
        else if (string.contains("?"))
            type = "variable";
        else
            type = "int";
        return new Pair<>(string, type);

    }
    public int getSize(){
        return tuple.size();
    }

    public Pair<String, String> get(int index){
        return tuple.get(index);
    }

    public boolean equals(Tuple tuple){
//        System.out.println("here");
        if(tuple.getSize() != this.getSize())
            return false;
        for(int i=0; i< this.getSize(); i++){
//            System.out.println("tuple.get(i).getValue() = " + tuple.get(i).getValue());
            if (tuple.get(i).getValue().equals("variable")){
                String variableRequired = tuple.get(i).getKey();
//                System.out.println("variableRequired = " + variableRequired);
                variableRequired = variableRequired.split(":")[1];
//                System.out.println("variableRequired = " + variableRequired);
                if(!this.tuple.get(i).getValue().equals(variableRequired))      {
                    return false;
                }
            }else if(!this.tuple.get(i).getKey().equals(tuple.get(i).getKey()))    // checks if Key is the same
                return false;

        }
        return true;
    }

    @Override
    public String toString(){
        String string = "";
        for(int i=0;i<tuple.size(); i++){
            string += tuple.get(i).getKey();
            if(i != tuple.size()-1){
                string +=",";
            }

        }
        return string;
    }
}
