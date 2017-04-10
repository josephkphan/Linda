//import java.io.BufferedReader;
//import java.io.IOException;
//import java.io.InputStreamReader;
//import java.net.Socket;
//import java.util.Scanner;
//
///**
// * Created by jphan on 4/5/17.
// */
//public class LindaInputHandler {
//
//    public static void main(String args[]) throws IOException {
//        LindaInputHandler L = new LindaInputHandler();
//        L.nextLindaLine();
//    }
//
//    private void in() {
//        System.out.println("IN COMMAND");
//    }
//
//
//    private void out() {
//        System.out.println("OUT COMMAND");
//
//    }
//
//    private void add(String input) {
//        String[] inputList = input.split(",");
//        String ipAddress = inputList[0];
//        String portNumStr = inputList[1];
//        int portNum = Integer.parseInt(portNumStr);
//        System.out.println("ipAddress = " + ipAddress);
//        System.out.println("portNum = " + portNum);
//
//        try {
//            Socket s = new Socket(ipAddress, portNum);
//            BufferedReader in =
//                    new BufferedReader(new InputStreamReader(s.getInputStream()));
//            String answer = in.readLine();  //todo what is this?
//        } catch (Exception e) {
//            System.out.println("Add Failed");
//        }
//    }
//
//    public void nextLindaLine() {
//        if (!isLindaPrinted) {
//            System.out.print("Linda>");
//            isLindaPrinted = true;
//            //Linda checks input
//            String s = scanner.nextLine();
//            System.out.println("s = " + s);
//            try {
//                if (!s.contains("(") || !s.contains(")"))
//                    throw new Exception();
//                s = s.replaceAll("\\s+", "");
//                System.out.println("Your Input = " + s);
//                String[] split;
//                split = s.split("\\(");  //todo try catch block if invalid index?
//                String command = split[0];  //todo Check if multiple (( or ))???
//                split = split[1].split("\\)");
//                String input = split[0];
//                System.out.println("command = " + command);
//                System.out.println("input = " + input);
//
//                if (command.equals("in")) {
//                    in();
//                } else if (command.equals("out")) {
//                    out();
//                } else if (command.equals("add")) {
//                    add(input);
//                } else {
//                    throw new Exception();
//                }
//            } catch (Exception e) {
//                System.out.println("Invalid Input. Please Try again");
//
//            }
//
//        }
//    }
//    // do parse stuff here
//}
//
