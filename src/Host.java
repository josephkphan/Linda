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
    private int portNumber;
    private boolean isBlocking;
    private TupleSpace tupleSpace;
    private ArrayList<Pair<String, Socket>> requests;
    private HostInfoList hostInfoList;
    private String yourName;

    public Host() {
        tupleSpace = new TupleSpace();
        hostInfoList = new HostInfoList();
        requests = new ArrayList<Pair<String, Socket>>();
        scanner = new Scanner(System.in);
        isBlocking = false;
        setupServer();
        findOtherHosts();
        addSelf();
        runLinda();
    }

    /**
     * Set up servers. Determines which port is unused
     */
    private void setupServer() {
        //getting manual port #
        System.out.print("Enter your host name: ");
        yourName = scanner.nextLine();
        System.out.print("Enter Port Number to Listen to: ");   //todo change so this is automated
        String strPortNum = scanner.nextLine();
        portNumber = Integer.parseInt(strPortNum);
        System.out.println();

        //Creating Server Socket
        try {
            listener = new ServerSocket(portNumber);
        } catch (IOException e) {
            e.printStackTrace();
        }
        printIPAddress();
    }

    /**
     * Print out the IPAddress of the current host
     */
    private void printIPAddress() {
        try {
            ip = InetAddress.getLocalHost();
            System.out.println(ip.getHostAddress() + " at port number: " + Integer.toString(portNumber));
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
                        makeNewSocketListener(socket);
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
                            break;
                        }
                    } catch (Exception e) {
//                        System.out.println("Socket Closed");
                        break;
                    }
                }
                try {
                    s.close();
                } catch (IOException e) {
                    System.out.print("");
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

//        System.out.println("-------------------------");
        System.out.println("s = " + s);
        String[] split = s.split("-");
        if (split[0].equals("in") || split[0].equals("read")) {
            requests.add(new Pair<>(s, socket));    //add to request //todo Check this later!!!! should be completed
            inAndRead(requests.get(requests.size() - 1));
        } else if (split[0].equals("out")) {
            out(split[1], socket);
        } else if (split[0].equals("unblock")) {
            System.out.println("split[1] = " + split[1]);
            inResponse();
            closeSocket(socket);
        } else if (split[0].equals("add")) {
            add(split[1], socket);
        } else {
            System.out.println(s);
        }
    }

    /**
     * Checks whether if the tuple is found. If it is found, it will send a message back to the receiver with the
     * tuple. If it is not found. It will not send a message back. It will then save the request and if an out
     * happens that matches the requirements, it will then send back the request
     */
    private void inAndRead(Pair<String, Socket> request) {
        System.out.println("Checking In/Read");
        String[] split = request.getKey().split("-");
        boolean isRead;
        if (split[0].equals("in"))
            isRead = false;
        else
            isRead = true;
        String input = split[1];
        Socket socket = request.getValue();
//        System.out.println("Inside - in");

        int searchIndex = tupleSpace.search(input);
        if (searchIndex != -1) {
            System.out.println("Found!");
//            System.out.println("tuple Found!! = " + tupleSpace.get(searchIndex));
            respondToRequest(tupleSpace.get(searchIndex).toString(), socket);
            if (!isRead){
                System.out.println("Deleting!");
                tupleSpace.remove(searchIndex);
                tupleSpace.writeToFile(yourName);
            }
            requests.remove(request);
        }

    }

    /**
     * Will save the data in the tuple file. It will then Check whether or not there is a pending In Request that
     * can be fullfilled with the new tuple. If it is successful, then it will send a message back to the blocked
     * User with the tuple just written to out
     */
    private void out(String input, Socket socket) {
//        System.out.println("Here!!");
//        System.out.println("Received: " + input);
        tupleSpace.add(input);
        System.out.println("Added Tuple to File");
        tupleSpace.writeToFile(yourName);
        // Checks if any requests were filled
        for (int i = 0; i < requests.size(); i++) {
            inAndRead(requests.get(i));
        }
        closeSocket(socket);
    }


    private void respondToRequest(String message, Socket socket) {
        System.out.println("Responding to Request");
        System.out.println("socket.getInetAddress().getHostAddress() = " + socket.getInetAddress().getHostAddress());
        System.out.println("socket.getPort() = " + Integer.toString(socket.getPort()));
        try {
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            out.println("unblock-" + message);  //WRITING TO SOCKET
            System.out.println("Unblock Message Sent");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void closeSocket(Socket socket) {
        try {
            socket.close();
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
        if (isBlocking)
            isBlocking = false;
        System.out.println("NOT BLOCKED ANYMORE!");
    }

    private void add(String hostInfoString, Socket socket) {
        hostInfoList.clear();
        String[] split = hostInfoString.split("/");
        for (String s : split)
            hostInfoList.addHost(s);
//        hostInfoList.print();
        hostInfoList.writeToFile(yourName);
    }

    /**
     * Creates a socket channel to yourself. This should only be called once. It is simply used for consistency so
     * all forms of communication is over the socket channels
     */
    private void addSelf() {
        System.out.println("Creating socket to yourself");
        hostInfoList.addHost(yourName+ "," + ip.getHostAddress() + "," + Integer.toString(portNumber), 0);
        hostInfoList.writeToFile(yourName);
        tupleSpace.writeToFile(yourName);
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
                    lindaInOrRead(input, "in");
                } else if (command.equals("read")) {
                    lindaInOrRead(input,"read");
                } else if (command.equals("out")) {
                    lindaOut(input);
                } else if (command.equals("add")) {
                    for (int i = 1; i < split.length; i++) {
                        split2 = split[i].split("\\)");
                        input = split2[0];
                        lindaAdd(input);
                    }
                    // Dont adding - notify others of host configurations
                    hostInfoList.print();
                    hostInfoList.writeToFile(yourName);
                    notifyOthers();
                } else {
                    throw new Exception();
                }
            } catch (Exception e) {
                System.out.println("Invalid Input. Please Try again");
            }
        }
    }

    /**
     * User typed a command of type "in( <tuple> )" in the terminal. This requests a tuple to be found over the
     * distributed system.
     */
    /**
     * User typed a command of type "read( <tuple> )" in the terminal. This requests a tuple to be found over the
     * distributed system.
     */
    private void lindaInOrRead(String input, String inOrRead) {
        // Hashing
//        System.out.println("input = " + input);
        String message = inOrRead + "-" + input;
        if (input.contains("?")) {
            //Broadcast Message to everyone
            for (int i = 0; i < hostInfoList.size(); i++) {
                try {
                    Socket socket = new Socket(hostInfoList.get(i).getiPAddress(), hostInfoList.get(i).getPortNumber());
                    makeNewSocketListener(socket);
                    PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                    out.println(message);//WRITING TO SOCKET
                    isBlocking = true;
                    System.out.println("BLOCKED!");
                    while (true) {
                        if (!isBlocking) {
                            break;
                        }
                    }
                    socket.close();
                } catch (IOException e) {
                    System.out.println("Error in linda in");
                }
            }

        } else {
            // Specific host to request from
            int sendToHost = getHashHost(input, hostInfoList.size());
            try {
                Socket socket = new Socket(hostInfoList.getByID(sendToHost).getiPAddress(), hostInfoList.getByID(sendToHost).getPortNumber());
                makeNewSocketListener(socket);
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                out.println(message);//WRITING TO SOCKET
                isBlocking = true;
                System.out.println("BLOCKED!");
                while (true) {
                    if(!isBlocking){
                        break;
                    }
                    //Blocking code to wait for input
                }
                socket.close();
            } catch (IOException e) {
                System.out.println("Error in linda in");
            }
        }
    }




    /**
     * User typed a comamnd of type "out( <tuple> )" in the terminal. This request hashes the input tuple and sends
     * the type to the correct host over the distributed system (where the tuple will be saved)
     */
    private void lindaOut(String input) {
//        System.out.println("input = " + input);
        int sendToHost = getHashHost(input, hostInfoList.size());
        String message = "out-" + input;

        //todo Finish me : this should choose a specific host to store it in. It's storing it in itself rn
        try {
            Socket socket = new Socket(hostInfoList.getByID(sendToHost).getiPAddress(), hostInfoList.getByID(sendToHost).getPortNumber());
            makeNewSocketListener(socket);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
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
        String hostName = inputList[0];
        String ipAddress = inputList[1];
        String portNumStr = inputList[2];
        int portNum = Integer.parseInt(portNumStr);
//        System.out.println("ipAddress = " + ipAddress);
//        System.out.println("portNum = " + portNum);

        try {
            Socket socket = new Socket(ipAddress, portNum);
            makeNewSocketListener(socket);
            //todo Was successful. Go make the ID creator here. and then add to hostList
            hostInfoList.addHost(hostName + "," + ip.getHostAddress() + "," +
                    Integer.toString(portNum), hostInfoList.size()); //todo change later

        } catch (Exception e) {
            System.out.println("Add Failed");
        }
    }

    private void notifyOthers() {
        for (int i = 1; i < hostInfoList.size(); i++) {
            try {
                Socket socket = new Socket(hostInfoList.get(i).getiPAddress(), hostInfoList.get(i).getPortNumber());
                makeNewSocketListener(socket);
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                out.println("add-" + hostInfoList.toString());
            } catch (IOException e) {
                System.out.println("Error in Notify Others");
            }
        }

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
