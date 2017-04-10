import java.io.IOException;

public class P1 {
    public static void main(String[] args) {
        try {
            new Host();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
