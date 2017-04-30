/**
 * Contains Main Method to Run P1
 */
public class P2 {
    public static void main(String[] args) {
        if(args.length==1){
            new Host(args[0]);
        }else{
            System.out.println("Invalid Numer of Arguments. Try Again");
        }
    }
}
