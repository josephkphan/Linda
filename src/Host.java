import javafx.util.Pair;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Scanner;

public class Host {
    private final static String salt = "DGE$5SGr@3VsHYUMas2323E4d57vfBfFSTRU@!DSH(*%FDSdfg13sgfsg";
    private Scanner scanner;
    private InetAddress ip;
    private ServerSocket listener;
    private ArrayList<Socket> socketList;
    private int portNumber;
    private boolean isBlocking;
    private TupleSpace tupleSpace;
    private ArrayList<Pair<String, Socket>> requests;

    public Host() throws IOException {
        //Setting up Server Stuff
        tupleSpace = new TupleSpace();
        requests = new ArrayList<Pair<String, Socket>>();
        scanner = new Scanner(System.in);
        socketList = new ArrayList<Socket>();
        isBlocking = false;
        setupServer();
        findOtherHosts();
        addSelf();
        runLinda();
    }

    /**
     * Set up servers. Determines which port is unused
     */
    private void setupServer() throws IOException {
        //getting manual port #
        System.out.print("Enter Port Number to Listen to: ");   //todo change so this is automated
        String strPortNum = scanner.nextLine();
        portNumber = Integer.parseInt(strPortNum);
        System.out.println();

        //Creating Server Socket
        listener = new ServerSocket(portNumber);
        printIPAddress();
    }

    /**
     * Print out the IPAddress of the current host
     */
    private void printIPAddress() {
        // gettings ip address
        try {
            ip = InetAddress.getLocalHost();
            System.out.println(ip.getHostAddress() +
                    " at port number: " + Integer.toString(portNumber));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Keeps a thread running to check whether other hosts want to add this one. On accept(), it will
     * create a new socket channel and save the socket information
     */
    private void findOtherHosts() {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Socket socket = listener.accept();
                        System.out.println("Listener Accepted New Host");
                        socketList.add(socket);
                        makeNewSocketListener(socket);
                        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                        out.println("You added me successfully");//WRITING TO SOCKET
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        t.start();
    }

    /**
     * This is called whenever a new socket channel is created. A new thread will constantly wait to see if new
     * input comes from that socket stream.
     */
    private void makeNewSocketListener(Socket s) {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
                        String streamString = in.readLine();
                        socketCommand(streamString, s);
                        System.out.println(streamString);
                        if (streamString.equals("null")) {
                            System.out.println("FOUND STRING NULL. THAT SOCKET GOT MESSED UP"); //todo MAKE FAUlT TOLERANT HERE
                            System.exit(0);
                        }

                    } catch (Exception e) {
                        System.out.println("Error in socketListener");
                        System.exit(0);
                    }
                }
            }
        });
        t.start();
    }

    /**
     * This parses messages recived by the socket streams. It will then call the corresponding method to
     * respond to the request. These messages are requests from other users
     */
    private void socketCommand(String s, Socket socket) {
        String[] split = s.split("-");
        if (split[0].equals("in") || split[0].equals("read")) {
            requests.add(new Pair<>(s, socket));    //add to request //todo Check this later!!!! should be completed
            inAndRead(requests.get(requests.size() - 1));
        } else if (split[0].equals("out")) {
            out(split[1]);
        } else if (split[0].equals("unblock")) {
            System.out.println("split[1] = " + split[1]);
            inResponse();
        } else if(split[0].equals("add")) {
            lindaAdd(split[1]);
        }else{
            System.out.println(s);
        }
    }

    ////////////////////////////Handle Other User's Requests/////////////////////////////////////////////

    /**
     * Checks whether if the tuple is found. If it is found, it will send a message back to the receiver with the
     * tuple. If it is not found. It will not send a message back. It will then save the request and if an out
     * happens that matches the requirements, it will then send back the request
     */
    private void inAndRead(Pair<String, Socket> request) {
        String[] split = request.getKey().split("-");
        boolean isRead;
        if (split[0].equals("in"))
            isRead = false;
        else
            isRead = true;
        String input = split[1];
        Socket socket = request.getValue();
        System.out.println("Inside - in");
        //todo Check if you have the given input. If you do send it back to the socket. Otherwise ignore

        int searchIndex = tupleSpace.search(input);
        if (searchIndex != -1) {
            System.out.println("tuple Found!! = " + tupleSpace.get(searchIndex));
            respondToRequest(tupleSpace.get(searchIndex).toString(), socket);
            if (!isRead)
                tupleSpace.remove(searchIndex);
            requests.remove(request);
        }
    }

    /**
     * Will save the data in the tuple file. It will then Check whether or not there is a pending In Request that
     * can be fullfilled with the new tuple. If it is successful, then it will send a message back to the blocked
     * User with the tuple just written to out
     */
    private void out(String input) {
        System.out.println("Here!!");
        System.out.println("Received: " + input);
        tupleSpace.add(input);

        // Checks if any requests were filled
        for (int i = 0; i < requests.size(); i++) {
            inAndRead(requests.get(i));
        }
        //todo Write to file here
    }


    private void respondToRequest(String message, Socket socket) {
        System.out.println("Responding to Request");
        try {
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            out.println("unblock-"+message+"SUCCESS BITCH");  //WRITING TO SOCKET
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Another Host has responded to your request (read or in). It prints out to terminal and stops the
     * Linda blocking code
     */
    private void inResponse() {
        //write outout back to terminal. End linda blocking loop
        if(isBlocking)
            isBlocking = false;
    }

    /**
     * Creates a socket channel to yourself. This should only be called once. It is simply used for consistency so
     * all forms of communication is over the socket channels
     */
    private void addSelf() {
        System.out.println("Creating socket to yourself");
        try {
            Socket socket = new Socket(ip.getHostAddress(), portNumber);
            makeNewSocketListener(socket);
        } catch (Exception e) {
            System.out.println("Add Failed");
        }

    }

    /**
     * Creates the Command line prompt "Linda>" for user. It takes in input and processes their request
     */
    private void runLinda() {
        // starting Linda
        while (true) {
            //Checking for other Hosts
            System.out.print("Linda>");
            String s = scanner.nextLine();
            try {
                if (!s.contains("(") || !s.contains(")"))
                    throw new Exception();
                s = s.replaceAll("\\s+", "");
                String[] split;
                split = s.split("\\(");  //todo try catch block if invalid index?
                String command = split[0];  //todo Check if multiple (( or ))???
                String[] split2 = split[1].split("\\)");
                String input = split2[0];
//                System.out.println("command = " + command);
//                System.out.println("input = " + input);

                if (command.equals("in")) {
                    lindaIn(input);
                } else if (command.equals("read")) {
                    lindaRead(input);
                } else if (command.equals("out")) {
                    lindaOut(input);
                } else if (command.equals("add")) {
                    for(int i=1; i<split.length; i++){
                        split2 = split[i].split("\\)");
                        input = split2[0];
                        lindaAdd(input);
                    }
                    // Dont adding - notify others of host configurations
                } else {
                    throw new Exception();
                }
            } catch (Exception e) {
                System.out.println("Invalid Input. Please Try again");
            }
        }
    }


    /////////////////////////////Handles User's Input from Linda//////////////////////////////////////////

    /**
     * User typed a command of type "in( <tuple> )" in the terminal. This requests a tuple to be found over the
     * distributed system.
     */
    private void lindaIn(String input) {
        // Hashing
        System.out.println("input = " + input);
        String message = "in-" + input;
        if (input.contains("?")) {
            //Broadcast Message to everyone
            for (int i = 0; i < socketList.size(); i++) {
                try {
                    PrintWriter out = new PrintWriter(socketList.get(i).getOutputStream(), true);
                    out.println(message);//WRITING TO SOCKET
                } catch (IOException e) {
                    System.out.println("Error in linda in");
                }
            }

        } else {
            // Specific host to request from
            int sendToHost = getHashHost(input, socketList.size());
            try {
                PrintWriter out = new PrintWriter(socketList.get(sendToHost).getOutputStream(), true);
                out.println(message);//WRITING TO SOCKET
            } catch (IOException e) {
                System.out.println("Error in linda in");
            }
        }
//        isBlocking = true;
//        while(isBlocking){
//            //Blocking code to wait for input
//        }

    }

    /**
     * User typed a command of type "read( <tuple> )" in the terminal. This requests a tuple to be found over the
     * distributed system.
     */
    private void lindaRead(String input) {
        // Hashing
        System.out.println("input = " + input);
        String message = "read-" + input;
        if (input.contains("?")) {
            //Broadcast Message to everyone
            for (int i = 0; i < socketList.size(); i++) {
                try {
                    PrintWriter out = new PrintWriter(socketList.get(i).getOutputStream(), true);
                    out.println(message);//WRITING TO SOCKET
                } catch (IOException e) {
                    System.out.println("Error in linda read");
                }
            }

        } else {
            // Specific host to request from
            int sendToHost = getHashHost(input, socketList.size());
            try {
                PrintWriter out = new PrintWriter(socketList.get(sendToHost).getOutputStream(), true);
                out.println(message);//WRITING TO SOCKET
            } catch (IOException e) {
                System.out.println("Error in linda read");
            }
        }
//        isBlocking = true;
//        while(isBlocking){
//            //Blocking code to wait for input
//        }

    }

    /**
     * User typed a comamnd of type "out( <tuple> )" in the terminal. This request hashes the input tuple and sends
     * the type to the correct host over the distributed system (where the tuple will be saved)
     */
    private void lindaOut(String input) {
        System.out.println("input = " + input);
        String message = "out-" + input;

        //todo Finish me : this should choose a specific host to store it in. It's storing it in itself rn
        try {
            PrintWriter out = new PrintWriter(socketList.get(0).getOutputStream(), true);
            out.println(message);//WRITING TO SOCKET
        } catch (IOException e) {
            System.out.println("Error in lindaOut");
        }

    }

    /**
     * User typed a command of type "add( <ip address> , <port number>)" This will connect the current host
     * to the input host.
     */
    private void lindaAdd(String input) {
        String[] inputList = input.split(",");
        String ipAddress = inputList[0];
        String portNumStr = inputList[1];
        int portNum = Integer.parseInt(portNumStr);
//        System.out.println("ipAddress = " + ipAddress);
//        System.out.println("portNum = " + portNum);

        try {
            Socket socket = new Socket(ipAddress, portNum);
            makeNewSocketListener(socket);
        } catch (Exception e) {
            System.out.println("Add Failed");
        }
    }

    private void notifyOthers(){
        //todo Communicate with other users to tell them what hosts are in the system.


    }


    public static int getHashHost(String message, int numHosts) {
        String hashedString = md5Hash(message);
        return hex2decimal(hashedString) % numHosts;
    }


    /**
     * Takes a string, and converts it to md5 hashed string.
     */
    public static String md5Hash(String message) {
        String md5 = "";
        if (null == message)
            return null;

        message = message + salt;//adding a salt to the string before it gets hashed.
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
            val = 16 * val + d;
        }
        if (val < 0) val *= -1;
        return val;
    }

}





