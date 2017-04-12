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
import java.util.concurrent.TimeUnit;

public class Host {
    private final static String salt = "DGE$5SGr@3VsHYUMas2323E4d57vfBfFSTRU@!DSH(*%FDSdfg13sgfsg";
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
        requests = new ArrayList<>();
        isBlocking = false;
        setUp();
        startLindaCommandPrompt();
    }

    /**
     * Set up servers. Determines which port is unused
     */
    private void setUp() {
        //getting manual port #
        Scanner scanner = new Scanner(System.in);
        System.out.print("Enter your host name: ");
        yourName = scanner.nextLine();
        System.out.print("Enter Port Number to Listen to: ");
        String strPortNum = scanner.nextLine();
        portNumber = Integer.parseInt(strPortNum);
        System.out.println();

        //Creating Server Socket
        try {
            listener = new ServerSocket(portNumber);
        } catch (IOException e) {
            e.printStackTrace();
        }
        getIPAddress();
        displayIPAddress();
        createSocketListenerThread();
        createHostList();
        clearTupleSpace();
    }

    /**
     * Adds yourself to a new empty host List.
     */
    private void createHostList() {
        System.out.println("Creating socket to yourself");
        hostInfoList.addHost(yourName + "," + ip.getHostAddress() + "," + Integer.toString(portNumber), 0);
        hostInfoList.writeToFile(yourName);
    }

    /**
     * Clears out tuple tuple space and creates an empty tuple file.
     */
    private void clearTupleSpace() {
        tupleSpace.getTupleList().clear();
        tupleSpace.writeToFile(yourName);
    }

    /**
     * Gets and saves the IPAddress of the current host
     */
    private void getIPAddress() {
        try {
            ip = InetAddress.getLocalHost();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void displayIPAddress() {
        System.out.println(ip.getHostAddress() + " at port number: " + Integer.toString(portNumber));
    }

    /**
     * Another will change blocking code boolean to false (no longer blocking)
     */
    private void endBlockingCode() {
        isBlocking = false;
        System.out.println("NOT BLOCKED ANYMORE!");
    }

    /**
     * will close the socket.
     */
    private void closeSocket(Socket socket) {
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();

        }
    }


    ////////////////////////////////////////////  SERVER CODE  ////////////////////////////////////////////////////


    /**
     * Keeps a thread running to check whether other hosts want to add this one. On accept(), it will
     * create a new socket channel and save the socket information
     */
    private void createSocketListenerThread() {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        Socket socket = listener.accept();
                        createSocketInputStreamHandlerThread(socket);
                    } catch (Exception e) {
                        e.printStackTrace();
                        break;
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
    private void createSocketInputStreamHandlerThread(Socket s) {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
                        String streamString = in.readLine();
                        readServerInputStream(streamString, s);
//                        System.out.println(streamString);
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
    private void readServerInputStream(String s, Socket socket) {
        String[] split = s.split("-");
        if (split[0].equals("in") || split[0].equals("read")) {
            requests.add(new Pair<>(s, socket));
            handlerServerInOrReadRequest(requests.get(requests.size() - 1));
        } else if (split[0].equals("out")) {
            handleServerOutRequest(split[1], socket);
        } else if (split[0].equals("unblock")) {
            System.out.println("Tuple Found: " + split[1]);
            endBlockingCode();
            closeSocket(socket);
        } else if (split[0].equals("add")) {
            handleServerAddRequest(split[1], socket);
        } else {
            System.out.println(s);
        }
    }

    /**
     * Will add the given host data into the host net file.
     */
    private void handleServerAddRequest(String hostInfoString, Socket socket) {
        hostInfoList.clear();
        String[] split = hostInfoString.split("/");
        for (String s : split)
            hostInfoList.addHost(s);
        hostInfoList.writeToFile(yourName);
        closeSocket(socket);
    }

    /**
     * Will save the data in the tuple file. It will then Check whether or not there is a pending In Request that
     * can be fullfilled with the new tuple. If it is successful, then it will send a message back to the blocked
     * User with the tuple just written to out
     */
    private void handleServerOutRequest(String input, Socket socket) {
        tupleSpace.add(input);
        System.out.println("Received Tuple: " + input);
        tupleSpace.writeToFile(yourName);

        // Checks if any requests were filled
        for (Pair<String, Socket> r : requests)
            handlerServerInOrReadRequest(r);

        closeSocket(socket);
    }

    /**
     * Checks whether if the tuple is found. If it is found, it will send a message back to the receiver with the
     * tuple. If it is not found. It will not send a message back. It will then save the request and if an out
     * happens that matches the requirements, it will then send back the request
     */
    private void handlerServerInOrReadRequest(Pair<String, Socket> request) {
        boolean isRead = true;
        String[] split = request.getKey().split("-");
        if (split[0].equals("in"))
            isRead = false;
        String input = split[1];
        Socket socket = request.getValue();
        int searchIndex = tupleSpace.search(input);
        if (searchIndex != -1) {
            System.out.println("Found!");
            handleServerInOrReadReply(tupleSpace.get(searchIndex).toString(), socket);
            if (!isRead) {
                System.out.println("Deleting!");
                tupleSpace.remove(searchIndex);
                tupleSpace.writeToFile(yourName);
            }
            requests.remove(request);
        }

    }

    /**
     * Will Send a message contain and "unblock" and the found tuple back to the user
     */
    private void handleServerInOrReadReply(String message, Socket socket) {
        try {
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            out.println("unblock-" + message);  //WRITING TO SOCKET
            System.out.println("Unblock Message Sent");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    //////////////////////////////////////////////  CLIENT CODE  ////////////////////////////////////////////////////

    /**
     * Creates the Command line prompt "Linda>" for user. It takes in input and processes their request
     */
    private void startLindaCommandPrompt() {
        // starting Linda
        Scanner scanner = new Scanner(System.in);   //todo This was the last thing i changed
        while (true) {
            //Checking for other Hosts
            System.out.print("Linda>");
            String s = scanner.nextLine();
            try {
                if (!s.contains("(") || !s.contains(")"))
                    throw new Exception();
                s = s.replaceAll("\\s+", "");
                String[] split;
                split = s.split("\\(");
                String command = split[0];
                String[] split2 = split[1].split("\\)");
                String input = split2[0];
                if (command.equals("in")) {
                    handleClientInOrReadRequest(input, "in");
                } else if (command.equals("read")) {
                    handleClientInOrReadRequest(input, "read");
                } else if (command.equals("out")) {
                    handleClientOutRequest(input);
                } else if (command.equals("add")) {
                    for (int i = 1; i < split.length; i++) {
                        split2 = split[i].split("\\)");
                        input = split2[0];
                        handleClientAddRequest(input);
                    }
                    // Dont adding - notify others of host configurations
//                    hostInfoList.print();
                    hostInfoList.writeToFile(yourName);
                    sendAllHostsCurrentHostInfoList();
                } else {
                    throw new Exception();
                }
            } catch (Exception e) {
                System.out.println("Invalid Input. Please Try again");
            }
        }
    }

    /**
     * User typed a command of type "add( <ip address> , <port number>)" This will connect the current host
     * to the input host.
     */
    private void handleClientAddRequest(String input) {
        String[] inputList = input.split(",");
        String hostName = inputList[0];
        String ipAddress = inputList[1];
        String portNumStr = inputList[2];
        int portNum = Integer.parseInt(portNumStr);

        try {   //checks each add individually this way
            Socket socket = new Socket(ipAddress, portNum);
            createSocketInputStreamHandlerThread(socket);
            hostInfoList.addHost(hostName + "," + ip.getHostAddress() + "," +
                    Integer.toString(portNum), hostInfoList.size()); //todo change later

        } catch (Exception e) {
            System.out.println("Add Failed");
        }
    }

    /**
     * Will send Host net file contents to all other connected hosts
     */
    private void sendAllHostsCurrentHostInfoList() {
        for (int i = 1; i < hostInfoList.size(); i++) {
            try {
                Socket socket = new Socket(hostInfoList.get(i).getiPAddress(), hostInfoList.get(i).getPortNumber());
                createSocketInputStreamHandlerThread(socket);
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                out.println("add-" + hostInfoList.toString());
            } catch (IOException e) {
                System.out.println("Error in Notify Others");
            }
        }

    }

    /**
     * User typed a comamnd of type "out( <tuple> )" in the terminal. This request hashes the input tuple and sends
     * the type to the correct host over the distributed system (where the tuple will be saved)
     */
    private void handleClientOutRequest(String input) {
        int sendToHost = getHostIDFromMD5Hash(input, hostInfoList.size());
        String message = "out-" + input;
        try {
            Socket socket = new Socket(hostInfoList.getByID(sendToHost).getiPAddress(), hostInfoList.getByID(sendToHost).getPortNumber());
            createSocketInputStreamHandlerThread(socket);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            out.println(message);//WRITING TO SOCKET
        } catch (IOException e) {
            System.out.println("Error in lindaOut");
        }

    }

    /**
     * User typed a command of type "read( <tuple> )" "in( <tuple> )" in the terminal.
     * Will receive back a tuple from a host, or will wait(block) until a tuple is received.
     * Tuple requests can include variables i.e.   read(i?:string, i?:float, 3.0)
     */
    private void handleClientInOrReadRequest(String input, String inOrRead) {
        // Hashing
        String message = inOrRead + "-" + input;
        if (input.contains("?")) {
            //Broadcast Message to everyone
            for (int i = 0; i < hostInfoList.size(); i++) {
                try {
                    Socket socket = new Socket(hostInfoList.get(i).getiPAddress(), hostInfoList.get(i).getPortNumber());
                    createSocketInputStreamHandlerThread(socket);
                    PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                    out.println(message);//WRITING TO SOCKET
                    isBlocking = true;
                    System.out.println("BLOCKED!");
                    while (true) {                  //todo STUCK HERE!
                        try {
                            TimeUnit.SECONDS.sleep(1);
                        } catch (Exception e) {

                        }
                        if (!isBlocking)
                            break;
                    }
                    socket.close();
                } catch (IOException e) {
                    System.out.println("Error in linda in");
                }
            }

        } else {
            // Specific host to request from
            int sendToHost = getHostIDFromMD5Hash(input, hostInfoList.size());
            try {
                Socket socket = new Socket(hostInfoList.getByID(sendToHost).getiPAddress(), hostInfoList.getByID(sendToHost).getPortNumber());
                createSocketInputStreamHandlerThread(socket);
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                out.println(message);//WRITING TO SOCKET
                isBlocking = true;
                System.out.println("BLOCKED!");
                while (true) {
                    try {
                        TimeUnit.SECONDS.sleep(1);
                    } catch (Exception e) {

                    }
                    System.out.println("still blocked");
                    if (!isBlocking)
                        break;
                }
                socket.close();
            } catch (IOException e) {
                System.out.println("Error in linda in");
            }
        }
    }


    ////////////////////////////////////////////  HASHING METHODS  ///////////////////////////////////////////////////

    /**
     * Will MD5 hash the message given and mod the hex result by numHosts.
     */
    private static int getHostIDFromMD5Hash(String message, int numHosts) {
        String hashedString = MD5Hash(message);
        return hex2decimal(hashedString) % numHosts;
    }


    /**
     * Takes a string, and converts it to md5 hashed string.
     */
    private static String MD5Hash(String message) {
        String md5 = "";
        if (null == message)
            return null;

        message = message + salt;       //adding a salt to the string before it gets hashed.
        try {
            MessageDigest digest = MessageDigest.getInstance("MD5");            //Create MessageDigest object for MD5
            digest.update(message.getBytes(), 0, message.length());      //Update input string in message digest
            md5 = new BigInteger(1, digest.digest()).toString(16);//Converts message digest value in base 16 (hex)
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return md5;
    }

    /**
     * Converts a Hex String to an integer
     */
    private static int hex2decimal(String s) {
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
