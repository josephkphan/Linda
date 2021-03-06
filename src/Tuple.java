import java.util.ArrayList;

/**
 * Java Implementation of a Tuple. Used ArrayList of Pairs.
 * Each Pair contains a String as the key (which is the tuple value), and the tuple type as the Pair-value
 */
public class Tuple {
    private ArrayList<Pair<String, String>> tuple;
    int ID;

    public Tuple(String tupleString) {
        String[] split = tupleString.split(":");
        ID = Integer.parseInt(split[0]);
        tupleString= split[1];
        tuple = new ArrayList<Pair<String, String>>();
        split = tupleString.split(",");
        for (String s : split) {
            tuple.add(determineType(s));
        }
    }
    public Tuple(String tupleString, int ID) {
        tuple = new ArrayList<Pair<String, String>>();
        String[] split = tupleString.split(",");
        for (String s : split) {
            tuple.add(determineType(s));
        }
        this.ID = ID;
    }

    /**
     * This Tuple Class only has 3 types: String, Variable, or Integer
     * "Variable" are used for the Tuple search so it does not need to put a specific value - just the type
     */
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

    public int getSize() {
        return tuple.size();
    }

    public ArrayList<Pair<String, String>> getTuple() {
        return tuple;
    }

    public void setTuple(ArrayList<Pair<String, String>> tuple) {
        this.tuple = tuple;
    }

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public Pair<String, String> get(int index) {
        return tuple.get(index);
    }

    /**
     * Checks whether Two Tuples are the same or not.
     * if is it a variable parameter, it will check the type rather than the actual value
     */
    public boolean equals(Tuple tuple) {
        if (tuple.getSize() != this.getSize()) {          // Checks if the Tuple has the same number of elements
            return false;
        }
        for (int i = 0; i < this.getSize(); i++) {      // Checks whether it is a variable parameter or not
            if (tuple.get(i).getValue().equals("variable")) {
                String variableRequired = tuple.get(i).getKey();
                variableRequired = variableRequired.split(":")[1];
                if (!this.tuple.get(i).getValue().equals(variableRequired)) {
                    return false;
                }
            }else if(this.get(i).getValue().equals("variable")){
                String variableRequired = this.get(i).getKey();
                variableRequired = variableRequired.split(":")[1];
                if (!tuple.tuple.get(i).getValue().equals(variableRequired)) {
                    return false;
                }

            } else if (!this.tuple.get(i).getKey().equals(tuple.get(i).getKey()))    // Checks if Key is the same
                return false;

        }
        return true;
    }

    @Override
    public String toString() {
        String string = Integer.toString(ID)+ ":";
        for (int i = 0; i < tuple.size(); i++) {
            string += tuple.get(i).getKey();
            if (i != tuple.size() - 1) {
                string += ",";
            }

        }
        return string;
    }

    public String toString2() {
        String string = "";
        for (int i = 0; i < tuple.size(); i++) {
            string += tuple.get(i).getKey();
            if (i != tuple.size() - 1) {
                string += ",";
            }

        }
        return string;
    }

}
