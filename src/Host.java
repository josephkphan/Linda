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
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

public class Host {
    private final static String salt = "DGE$5SGr@3VsHYUMas2323E4d57vfBfFSTRU@!DSH(*%FDSdfg13sgfsg";     // md5 salt
    private final static int START = 1025;
    private final static int END = 65525;
    private ServerSocket listener;                      // Used to accept other socket connections
    private String yourName;                            // Your host name
    private InetAddress ip;                             // Your IP Address
    private int portNumber;                             // Your Port Number
    private boolean isBlocking;                         // Used for Blocking code (read, in)
    private TupleSpace tupleSpace;                      // Used to store Tuples
    private ArrayList<Pair<String, Socket>> requests;   // Saves all in and read requests
    private HostInfoList hostInfoList;                  // Contains all Host Information
    private String myRequest;                           // saves your last request

    public Host(String hostName) {
        // Initializing Variables
        tupleSpace = new TupleSpace();
        hostInfoList = new HostInfoList();
        requests = new ArrayList<>();
        isBlocking = false;

        setUp(hostName);
        startLindaCommandPrompt();
    }

    /**
     * Set up servers. Determines which port is unused
     */
    private void setUp(String hostName) {
        getHostName(hostName);
//        findAvailablePort();
        createServerSocket();
        getIPAddress();
        displayIPAddress();
        createSocketListenerThread();
        createHostList();
        clearTupleSpace();
    }

    /**
     * gets User defined host name
     */
    private void getHostName(String yourName) {
//        Scanner scanner = new Scanner(System.in);
//        System.out.print("Enter your host name: ");
//        yourName = scanner.nextLine();
        this.yourName = yourName;
    }

    /**
     * Creates a server socket
     */
    private void createServerSocket() {
        //Creating Server Socket
        for(int port = START; port<= END; port++){
            try{
                port = ThreadLocalRandom.current().nextInt(START,END);
                listener = new ServerSocket(port);
                portNumber = port;
                break;
            }catch (IOException e){
                continue;
            }
        }
    }

    /**
     * Adds yourself to a new empty host List.
     */
    private void createHostList() {
//        System.out.println("Creating socket to yourself");
        hostInfoList.addHost(yourName + "," + ip.getHostAddress() + "," + Integer.toString(portNumber), 0);
        hostInfoList.save(yourName);
    }

    /**
     * Clears out tuple tuple space and creates an empty tuple file.
     */
    private void clearTupleSpace() {
        tupleSpace.getTupleList().clear();
        tupleSpace.save(yourName);
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
//        System.out.println("NOT BLOCKED ANYMORE!");
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
                        Socket socket = listener.accept();      //blocking code - will wait until another host wants to
                        createSocketInputStreamHandlerThread(socket); // connect with this host's server socket
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
    private void createSocketInputStreamHandlerThread(Socket socket) {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        // Reads the input Stream from the Socket
                        BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                        String streamString = in.readLine();
                        readServerInputStream(streamString, socket);
//                        System.out.println(streamString);
                        if (streamString.equals("null")) {
                            // Will Check whether the input Stream is Null - If it is null then socket connection failed
                            // todo If this happens, You should remove this socket from the host info list
                            //todo and tell all other hosts(for fault tolerance purposes)
                            break;
                        }
                    } catch (Exception e) {
//                        System.out.println("Socket Closed");
                        break;
                    }
                }
                closeSocket(socket);
            }
        });
        t.start();

    }

    /**
     * This parses messages recived by the socket streams. It will then call the corresponding method to
     * respond to the request. These messages are requests from other users
     */
    private void readServerInputStream(String s, Socket socket) {
//        System.out.println("----------------------" + s);
        String[] split = s.split("-");
        if (split[0].equals("in") || split[0].equals("read")) {         // Read "in" or "read" from input stream
            requests.add(new Pair<>(s, socket));
            handlerServerInOrReadRequest(requests.get(requests.size() - 1));

        } else if (split[0].equals("out")) {                            // Read "out" from input Stream
            handleServerOutRequest(split[1], socket);

        } else if (split[0].equals("unblock")) {                        // Read "unblock" from input Stream
            handleInOrReadReplyResponse(socket, split[1]);

        } else if (split[0].equals("add")) {                            // Read "add" from input Stream
            handleServerAddRequest(split[1], socket);

        } else if (split[0].equals("delete")) {
            deleteTuple(split[1], socket);
        } else {                                        // Got some other garbage - could be null or something else
            System.out.println(s);
        }
    }

    private void handleInOrReadReplyResponse(Socket socket, String tupleString) {
        String[] split = myRequest.split("-");
        Tuple myTuple = new Tuple(split[1]);
        Tuple receivedTuple = new Tuple(tupleString);
        if (myTuple.equals(receivedTuple)) {
            // Received the correct Tuple
            System.out.println("Received: " + receivedTuple.toString());
            endBlockingCode();
            myRequest = "no current request pending";
            if (split[0].equals("in")) {
                try {
                    PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                    out.println("delete-" + tupleString);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }else{
                closeSocket(socket);
            }
        }else {
            closeSocket(socket);
        }

    }

    /**
     * Will add the given host data into the host net file.
     * Input should be given in in one string where each hostInfo is separated by /
     * i.e.    host0,127.0.1.1,9000,0/host1,127.0.1.1,9001,1
     * All host should be sent in one String. This will replace the current Host net information
     */
    private void handleServerAddRequest(String hostInfoString, Socket socket) {
        hostInfoList.clear();
        String[] split = hostInfoString.split("/");
        for (String s : split)          // Going through each Host and adding them to Host List
            hostInfoList.addHost(s);
        hostInfoList.save(yourName);
        closeSocket(socket);            // Ending Socket Connection
    }

    /**
     * Delete a tuple
     */
    private void deleteTuple(String tupleString, Socket socket) {
//        System.out.println("Deleting!");
        tupleSpace.remove(tupleSpace.search(tupleString));
        closeSocket(socket);            // Ending Socket Connection
    }

    /**
     * Will save the data in the tuple file. It will then Check whether or not there is a pending In Request that
     * can be fulfilled with the new tuple. If it is successful, then it will send a message back to the blocked
     * User with the tuple just written to out
     */
    private void handleServerOutRequest(String input, Socket socket) {
        tupleSpace.add(input);                      // Adds tuple to Tuple space and will print what tuple was received
        System.out.println("Received Tuple: " + input);
        tupleSpace.save(yourName);
        for (Pair<String, Socket> r : requests)     // Checks if any in or read requests were filled
            handlerServerInOrReadRequest(r);
        closeSocket(socket);                        //Close socket connection - no need to reply back to user.
    }

    /**
     * Checks whether if the tuple is found. If it is found, it will send a message back to the receiver with the
     * tuple. If it is not found. It will not send a message back. It will then save the request and if an out
     * happens that matches the requirements, it will then send back the request
     */
    private void handlerServerInOrReadRequest(Pair<String, Socket> request) {
        // Checks whether Request was and In or Read Command
        String[] split = request.getKey().split("-");

        // Extract out the tuple requested and will search tuple space for it
        String input = split[1];
        Socket socket = request.getValue();
        int searchIndex = tupleSpace.search(input);
        if (searchIndex != -1) {
            // Reply back to blocked host that the tuple was found - returns back the tuple
//            System.out.println("Found!");
            handleServerInOrReadReply(tupleSpace.get(searchIndex).toString(), socket);
            // FulFilled Request, so it should removed off the Request List
            requests.remove(request);
        }

    }

    /**
     * Will Send a message contain and "unblock" and the found tuple back to the user
     */
    private void handleServerInOrReadReply(String tuple, Socket socket) {
        // Signal Host to Wake up
        try {
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            out.println("unblock-" + tuple);
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
        Scanner scanner = new Scanner(System.in);
        while (true) {                                              // Run Linda Command Line
            System.out.print("Linda>");
            String s = scanner.nextLine();                          // Read User's Input
            try {
                if (!s.contains("(") || !s.contains(")"))           // Simple Check to see if User's input is valid
                    throw new Exception();
                s = s.replaceAll("\\s+", "");                       // Remove input from User's Request
                String[] split;
                split = s.split("\\(");                       // Split by (
                String command = split[0];
                String[] split2 = split[1].split("\\)");
                String input = split2[0];
                if (command.equals("in")) {                                     // User "in" Request
                    handleClientInOrReadRequest(input, "in");
                } else if (command.equals("rd")) {
                    handleClientInOrReadRequest(input, "read");        // User "rd" Request
                } else if (command.equals("out")) {
                    handleClientOutRequest(input);
                } else if (command.equals("add")) {                             // User "add" Request
                    for (int i = 1; i < split.length; i++) {        // *note user can add more than one host at a time
                        split2 = split[i].split("\\)");
                        input = split2[0];
                        handleClientAddRequest(input);
                    }
//                    hostInfoList.print();
                    hostInfoList.save(yourName);                    // Save Successfully added hosts onto net file
                    sendAllHostsCurrentHostInfoList();              // Notify Other Hosts of this net file
                } else {
                    throw new Exception();
                }
            } catch (Exception e) {
                System.out.println("Invalid Input. Please Try again");
            }
        }
    }

    /**
     * User typed a command of type "add( <host name>, <ip address> , <port number>)" This will connect the current host
     * to the input host.
     */
    private void handleClientAddRequest(String input) {
        // Parse Through Host info
        String[] inputList = input.split(",");
        String hostName = inputList[0];
        String ipAddress = inputList[1];
        String portNumStr = inputList[2];
        int portNum = Integer.parseInt(portNumStr);

        // Attempts to create a connection with given host information
        try {
            Socket socket = new Socket(ipAddress, portNum);
            createSocketInputStreamHandlerThread(socket);
            // Connection Successful! Add host to List
            hostInfoList.addHost(hostName + "," + ip.getHostAddress() + "," +
                    Integer.toString(portNum), hostInfoList.size());
            closeSocket(socket);
        } catch (Exception e) {
            System.out.println("Failed to Add: " + input);
        }
    }

    /**
     * Will send Host net file contents to all other connected hosts
     */
    private void sendAllHostsCurrentHostInfoList() {
        for (int i = 1; i < hostInfoList.size(); i++) {
            try {
                // Create Socket Connection
                Socket socket = new Socket(hostInfoList.get(i).getiPAddress(), hostInfoList.get(i).getPortNumber());
                createSocketInputStreamHandlerThread(socket);

                // Send out Host File through socket Stream
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                out.println("add-" + hostInfoList.toString());
            } catch (IOException e) {
                System.out.println("Error in Host Info Transfer");
            }
        }

    }

    /**
     * User typed a command of type "out( <tuple> )" in the terminal. This request hashes the input tuple and sends
     * the type to the correct host over the distributed system (where the tuple will be saved)
     */
    private void handleClientOutRequest(String input) {
        int sendToHost = getHostIDFromMD5Hash(input, hostInfoList.size());
        String message = "out-" + input;
        try {
            // Create Socket Connection
            Socket socket = new Socket(hostInfoList.getByID(sendToHost).getiPAddress(), hostInfoList.getByID(sendToHost).getPortNumber());
            createSocketInputStreamHandlerThread(socket);

            //Send out Tuple info for host to save
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            out.println(message);
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
        String message = inOrRead + "-" + input;
        myRequest = message;
        // Checks whether or not the Tuple Request has a variable parameter or not
        if (input.contains("?")) {
            //Broadcast Message to everyone
            System.out.println("Broadcast!");
            broadcastInOrReadRequest(message);
        } else {
            // Specific host to request from
            specificInOrReadRequest(message, input);
        }
    }

    /**
     * Will broadcast the In or Read Request to all hosts
     */
    private void broadcastInOrReadRequest(String message) {
        System.out.println(hostInfoList);
        for (int i = 0; i < hostInfoList.size(); i++) {
            try {
                Socket socket = new Socket(hostInfoList.get(i).getiPAddress(), hostInfoList.get(i).getPortNumber());
                createSocketInputStreamHandlerThread(socket);
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                out.println(message);
                isBlocking = true;
//                System.out.println("BLOCKED!");
            } catch (IOException e) {
                System.out.println("Error in linda in");
            }
        }
        while (true) {                  //todo STUCK HERE!
            try {
                TimeUnit.SECONDS.sleep(2);
            } catch (Exception e) {

            }
            if (!isBlocking)
                break;
        }
    }

    /**
     * Will hash the message and based on that hash, choose a specific host to send the In or Read Request to
     */
    private void specificInOrReadRequest(String message, String input) {
        int sendToHost = getHostIDFromMD5Hash(input, hostInfoList.size());
        try {
            Socket socket = new Socket(hostInfoList.getByID(sendToHost).getiPAddress(), hostInfoList.getByID(sendToHost).getPortNumber());
            createSocketInputStreamHandlerThread(socket);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            out.println(message);//WRITING TO SOCKET
            isBlocking = true;
//            System.out.println("BLOCKED!");
        } catch (IOException e) {
            System.out.println("Error in linda in");
        }
        while (true) {
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (Exception e) {
            }
            if (!isBlocking)
                break;
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
