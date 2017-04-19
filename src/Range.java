/**
 * Created by jphan on 4/17/17.
 */
public class Range {
    int min, max;

    public Range(int min, int max){
        this.min = min;
        this.max = max;
    }

    public boolean inRange(int value){
        if(value >=min && value <= max){
            return true;
        }
        return false;
    }

    public int getMin() {
        return min;
    }

    public void setMin(int min) {
        this.min = min;
    }

    public int getMax() {
        return max;
    }

    public int getRangeDifference(){
        return max - min;
    }

    public void setMax(int max) {
        this.max = max;
    }

    @Override
    public String toString(){
        return Integer.toString(min) + "_"+ Integer.toString(max) ;
    }
}
