import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
public class Tester {

    private final static String salt="DGE$5SGr@3VsHYUMas2323E4d57vfBfFSTRU@!DSH(*%FDSdfg13sgfsg";

    public static void main(String[] args) {
        String password = "thisismypasswordss";
        String empty =  null;
        String msg = "This is a text message.";
        System.out.println(password+" MD5 hashed to>>>>>>> " + md5Hash(password));
        System.out.println(empty+" MD5 hashed to>>>>>>> " + md5Hash(null));
        System.out.println(msg+" MD5 hashed to>>>>>>> " + md5Hash(msg));
    }

    public static int hashMessage(String message, int numHosts){
        String hashedString = md5Hash(message);
        return hex2decimal(hashedString)%numHosts;
    }

    //Takes a string, and converts it to md5 hashed string.
    public static String md5Hash(String message) {
        String md5 = "";
        if(null == message)
            return null;

        message = message+salt;//adding a salt to the string before it gets hashed.
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");//Create MessageDigest object for MD5
            digest.update(message.getBytes(), 0, message.length());//Update input string in message digest
            md5 = new BigInteger(1, digest.digest()).toString(16);//Converts message digest value in base 16 (hex)
//            System.out.println(hex2decimal(md5)%3);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return md5;
    }

    public static int hex2decimal(String s) {
        String digits = "0123456789ABCDEF";
        s = s.toUpperCase();
        int val = 0;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            int d = digits.indexOf(c);
            val = 16*val + d;
        }
        if( val < 0 ) val *=-1;
        return val;
    }
}