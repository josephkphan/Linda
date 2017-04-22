import java.io.*;
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

import static java.lang.System.exit;

public class Host {
    private final static String salt = "DGE$5SGr@3VsHYUMas2323E4d57vfBfFSTRU@!DSH(*%FDSdfg13sgfsg";     // md5 salt
    private final static int START = 1025;              // Constant - Minimum port number allowed
    private final static int END = 65525;               // Constant - Maximum Port Number allowed
    private final String LOGIN = "jphan1";              // Santa Clara DC Login Name
    private String dir;                                 // Used to set up FilePath
    private ServerSocket listener;                      // Used to accept other socket connections
    private String yourName;                            // Your host name
    private InetAddress ip;                             // Your IP Address
    private int portNumber;                             // Your Port Number
    private boolean isBlocking;                         // Used for Blocking code (read, in)
    private TupleSpace tupleSpace;                      // Used to store Tuples
    private TupleSpace backUpTupleSpace;                // Used as a back up for another host
    private ArrayList<Pair<String, Socket>> requests;   // Saves all in and read requests
    private ArrayList<Pair<String, Socket>> backUpRequests;     // Saves all in and read requests
    private HostInfoList hostInfoList;                  // Contains all Host Information
    private String myRequest;                           // saves your last request
    private String tupleFilePath, hostInfoFilePath;     // saves data to these files
    private String backUpTupleFilePath, lookUpTableFilePath;    // FilePaths used for backup
    private LookUpTable lookUpTable;                    // Used to Map Tuples from the consistent Hashing
    private final int MIN = 0;                          // Minimum Value allowed from Consistent Hashing
    private final int MAX = 127;                        // Maximum Value allowed from Consistent Hashing
//    private final int MAX = ((int) Math.pow(2, 16) - 1);      //todo change to this later

    /**
     * Constructor, Pass in hostname as argument (typically argv[0]
     */
    public Host(String hostName) {
        // Initializing Variables
        tupleSpace = new TupleSpace();
        backUpTupleSpace = new TupleSpace();
        lookUpTable = new LookUpTable(MIN, MAX);
        hostInfoList = new HostInfoList();
        requests = new ArrayList<>();
        backUpRequests = new ArrayList<>();
        isBlocking = false;

        // Setting Up Environment
        setUp(hostName);
        startLindaCommandPrompt();
    }

    /**
     * Set up servers. Determines which port is unused
     */
    private void setUp(String hostName) {
        getHostName(hostName);          // Sets hostname locally
        createFilePath();               // Creates all the file path strings to directories
        createServerSocket();           // Creates Server Socket
        getIPAddress();                 // Saves IP Address Locally
        displayIPAddress();             // Prints out IP Address in a certain format
        createSocketListenerThread();   // Waits for socket connections on separate Thread

        // Checks if directory exists to determine whether the server crahsed or not
        File f = new File(dir);
        if (f.exists() && f.isDirectory()) {
            // Data already exists, This means that you are recovering from a crash
            System.out.println("Came Back From Crash- Recovering Data");

            // Recreate data from files
            tupleSpace.fromFile(tupleFilePath);
            hostInfoList.fromFile(hostInfoFilePath);
            lookUpTable.fromFile(lookUpTableFilePath);

            // Request Data from your backup
            justCameBackFromCrash();

        } else {
            // You were not part of a system.
            System.out.println("No data to recover from- Starting Fresh");
            boolean success = f.mkdir();                    // Create the directory
//            System.out.println(success);                    // Check is creating directories was successful
            createHostList();                               // Create the Host List and add yourself
            clearTupleSpace();                              // Create an empty Tuple Space
            lookUpTable.addNewHost(yourName);               // Create a new lookup table and add yourself

            // Save data to files
            lookUpTable.save(lookUpTableFilePath);
            backUpTupleSpace.save(backUpTupleFilePath);
            hostInfoList.save(hostInfoFilePath);
        }
    }

    ///////////////////////////////////// Methods used on Start Up //////////////////////////////////////////////////

    /**
     * Create File paths
     */
    private void createFilePath() {
        // Path for My Computer
        dir = "/home/jphan/IdeaProjects/Coen241CloudComputing/" + yourName + "/";

        // Path for DC Machine
        // String dir = "/tmp/" + LOGIN + "/linda/"+ yourName+"/";

        tupleFilePath = dir + "tuples.txt";
        hostInfoFilePath = dir + "nets.txt";
        backUpTupleFilePath = dir + "backUpTuples.txt";
        lookUpTableFilePath = dir + "lookUpTable.txt";

    }

    /**
     * gets User defined host name
     */
    private void getHostName(String yourName) {
        this.yourName = yourName;
    }

    /**
     * Clears out tuple tuple space and creates an empty tuple file.
     */
    private void clearTupleSpace() {
        tupleSpace.getTupleList().clear();
        tupleSpace.save(tupleFilePath);
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
     * Adds yourself to a new empty host List.
     */
    private void createHostList() {
        hostInfoList.addHost(yourName + "," + ip.getHostAddress() + "," + Integer.toString(portNumber), 0);
    }

    /**
     * Creates a server socket. This will keep attempting random ports until it finds a successful one
     */
    private void createServerSocket() {
        for (int port = START; port <= END; port++) {
            try {
                port = ThreadLocalRandom.current().nextInt(START, END);
                listener = new ServerSocket(port);
                portNumber = port;                  // will only reach here on a successful Port
                break;
            } catch (IOException e) {
                continue;
            }
        }
    }

    ////////////////////////////////////////// Commonly Used Methods /////////////////////////////////////////////////

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
        System.out.println("--------" + s + "--------");
        String[] split = s.split("-");
        if (split[0].equals("in") || split[0].equals("read")) {         // Read "in" or "read" from input stream
            requests.add(new Pair<>(s, socket));
            handlerServerInOrReadRequest(requests.get(requests.size() - 1));

        } else if (split[0].equals("out")) {                            // Read "out" from input Stream
            handleServerOutRequest(split[1], socket);

        } else if (split[0].equals("backupin") || split[0].equals("backupread")) {          // Read "backupin"
            backUpRequests.add(new Pair<>(s, socket));
            handlerServerBackUpInOrReadRequest(backUpRequests.get(requests.size() - 1));

        } else if (split[0].equals("backupout")) {                      // Read "backupout"
            handleServerBackUpOutRequest(split[1], socket);

        } else if (split[0].equals("unblock")) {                        // Read "unblock" from input Stream
            handleInOrReadReplyResponse(socket, split[1]);

        } else if (split[0].equals("add")) {                            // Read "add" from input Stream
            handleServerAddRequest(split[1], socket);

        } else if (split[0].equals("delete")) {                         // Used to Delete Tuples
            deleteTuple(split[1], socket);

        } else if (split[0].equals("backupdelete")) {                   // Used to delete tuples from back up
            deleteBackUpTuple(split[1], socket);

        } else if (split[0].equals("requestRecoverData")) {             // crashed host asking for their data back
            sendRecoverData(socket);

        } else if (split[0].equals("receivedRecoverData")) {            // crashed host receiving their back up data
            restoreStateFromRecoverData(split[1], socket);

        } else if (split[0].equals("updateLookUpTable")) {              // get the new look up table
            handleServerUpdateLookUpTableRequest(socket, split[1]);

        } else if (split[0].equals("deleteYoSelf")) {                   // you are getting removed from the network
            handleServerDeleteRequest(socket);

        } else if (split[0].equals("backUpTupleSpace")) {               // You are given somebody else's backup
            handleServerUpdateBackUpRequest(split[1], socket);

        } else if (split[0].equals("requestYouToBackUpTupleSpace")) {   // You need to back up your data in somebody else
            saveBackup();

        } else {                                        // Got some other garbage - could be null or something else
            System.out.println(s);
        }
    }

    /**
     * Will see if you can fulfill the given in or read request
     */
    private void handleInOrReadReplyResponse(Socket socket, String tupleString) {
        String[] split = myRequest.split("-");
        Tuple myTuple = new Tuple(split[1]);
        Tuple receivedTuple = new Tuple(tupleString);
        if (myTuple.equals(receivedTuple)) {
            // Received the correct Tuple
            System.out.println("get tuple(" + receivedTuple.toString() + ") on " + socket.getInetAddress().getHostAddress());
            endBlockingCode();
            myRequest = "no current request pending";
            if (split[0].equals("in")) {
                try {
                    PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                    out.println("delete-" + tupleString);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                closeSocket(socket);
            }
        } else {
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
        System.out.println("Adding Hosts: " + hostInfoString);
        hostInfoList.clear();
        String[] split = hostInfoString.split("/");
        for (String s : split) {          // Going through each Host and adding them to Host List
            hostInfoList.addHost(s);
        }
        hostInfoList.save(hostInfoFilePath);
        closeSocket(socket);            // Ending Socket Connection
    }

    /**
     * Delete a tuple from your tuple space
     */
    private void deleteTuple(String tupleString, Socket socket) {
//      System.out.println("Deleting!");
        tupleSpace.remove(tupleSpace.search(tupleString));
        closeSocket(socket);            // Ending Socket Connection
        saveBackup();
    }

    /**
     * Will save the data in the tuple file. It will then Check whether or not there is a pending In Request that
     * can be fulfilled with the new tuple. If it is successful, then it will send a message back to the blocked
     * User with the tuple just written to out
     */
    private void handleServerOutRequest(String input, Socket socket) {
        tupleSpace.add(input);                      // Adds tuple to Tuple space and will print what tuple was received
//        System.out.println("Received Tuple: " + input);
        tupleSpace.save(tupleFilePath);
        for (Pair<String, Socket> r : requests) {     // Checks if any in or read requests were filled
            handlerServerInOrReadRequest(r);
        }
        closeSocket(socket);                        //Close socket connection - no need to reply back to user.
        System.out.println("Saving:" + input);
        saveBackup();
    }


    /**
     * This method should be called whenever an out happens to you. This will send your tuplespace to be backed up
     */
    private void saveBackup() {
        //back up Tuple space in your back up.
        try {
            singleMessageWithoutBackUpCatch("backUpTupleSpace-" + tupleSpace.toString(), findBackupHostIndex(yourName));
        } catch (Exception e) {
            System.out.println("Save to back up failed.");
        }
        System.out.println("sending out to backup:" + tupleSpace.toString() + " in host " + findBackupHostIndex(yourName));
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
            socketReplyMessage("unblock-" + tupleSpace.get(searchIndex).toString(), socket);
            // FulFilled Request, so it should removed off the Request List
            requests.remove(request);
        }

    }

    /**
     * Will Send a message contain and "unblock" and the found tuple back to the user
     */
    private void socketReplyMessage(String message, Socket socket) {
        // Signal Host to Wake up
        try {
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            out.println("unblock-" + message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * You were told that you are going to be deleted. Redistribute your tuples back out to everyone before you GTFO
     * remove yourself from the host Info List.
     * Broadcast that.
     * You're a ghost now..
     */
    private void handleServerDeleteRequest(Socket socket) {
        System.out.println("I have to delete Myself");
        if(hostInfoList.size()==1){
            //You're the last one
            deleteDir(new File(dir));
            exit(0); // Everything about you was pretty much erased from existence. Might as well die
        }
        lookUpTable.deleteHost(yourName);           // delete that user from the current Look up table
        broadcastMessage("updateLookUpTable-" + lookUpTable.toString()); //Broadcast the new look up table to everyone
        redistributeTuples();                       //redistribute your tuples  //technically this line is redundant
        timeout(1);

        hostInfoList.remove(yourName);              // remove yourself from hostList.
        broadcastMessage("add-" + hostInfoList.toString()); // send out the new broadcast list
        timeout(1);
        //delete the directory

        //tell somebody else to backup their data
        broadcastMessage("requestYouToBackUpTupleSpace-somebodyPeacedOut");
        closeSocket(socket);
        timeout(1);
        deleteDir(new File(dir));
        exit(0); // Everything about you was pretty much erased from existence. Might as well die
    }

    /**
     * program sleeps for the given duration of time
     */
    private void timeout(int seconds) {
        try {
            TimeUnit.SECONDS.sleep(seconds);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Was Given a new lookup table. Need to Check if you have any tuples you should no longer have.
     */
    private void handleServerUpdateLookUpTableRequest(Socket socket, String input) {
        // Saves Lookup Table
        System.out.println("Received lookuptable: " + input);
        lookUpTable.update(input);
        lookUpTable.save(lookUpTableFilePath);
        // Checks for misplaced Tuples
        redistributeTuples();
        closeSocket(socket);
    }

    /**
     * Sends and erases any tuples you should no longer have. Backs up your tuple space
     */
    private void redistributeTuples() {
        System.out.println("Redistributing Tuples");
        // Going through Tuple Space
        for (int i = 0; i < tupleSpace.size(); i++) {
            String hostToHoldTuple = lookUpTable.getHostFromID(tupleSpace.get(i).getID());
            if (!hostToHoldTuple.equals(yourName)) {
                //You have a tuple that you aren't supposed to have
                singleMessage("out-" + tupleSpace.get(i).toString(), hostInfoList.getIndex(hostToHoldTuple));
                tupleSpace.remove(i);
                i--;
            }
        }
        tupleSpace.save(tupleFilePath);
        saveBackup();
    }


    ////////////////////////////////////////Back up Stuff/////////////////////////////////////////

    /**
     * Delete a tuple from the back up Tuple Space
     */
    private void deleteBackUpTuple(String tupleString, Socket socket) {
//      System.out.println("Deleting!");
        backUpTupleSpace.remove(backUpTupleSpace.search(tupleString));
        closeSocket(socket);

    }

    /**
     * Will save the data in the tuple file. It will then Check whether or not there is a pending In Request that
     * can be fulfilled with the new tuple. If it is successful, then it will send a message back to the blocked
     * User with the tuple just written to out
     */
    private void handleServerBackUpOutRequest(String input, Socket socket) {
        backUpTupleSpace.add(input);                      // Adds tuple to Tuple space and will print what tuple was received
//        System.out.println("Received Tuple: " + input);
        backUpTupleSpace.save(backUpTupleFilePath);
        for (Pair<String, Socket> r : backUpRequests) {     // Checks if any in or read requests were filled
            handlerServerBackUpInOrReadRequest(r);
        }
        closeSocket(socket);                        //Close socket connection - no need to reply back to user.
    }


    /**
     * Checks whether if the tuple is found. If it is found, it will send a message back to the receiver with the
     * tuple. If it is not found. It will not send a message back. It will then save the request and if an out
     * happens that matches the requirements, it will then send back the request
     */
    private void handlerServerBackUpInOrReadRequest(Pair<String, Socket> request) {
        // Checks whether Request was and In or Read Command
        String[] split = request.getKey().split("-");

        // Extract out the tuple requested and will search tuple space for it
        String input = split[1];
        Socket socket = request.getValue();
        int searchIndex = backUpTupleSpace.search(input);
        if (searchIndex != -1) {
            // Reply back to blocked host that the tuple was found - returns back the tuple
//            System.out.println("Found!");
            socketReplyMessage("unblock-" + backUpTupleSpace.get(searchIndex).toString(), socket);
            // FulFilled Request, so it should removed off the Request List
            backUpRequests.remove(request);
        }

    }

    /**
     * Saves the new Back up Tuple Space for another host
     */
    private void handleServerUpdateBackUpRequest(String input, Socket socket) {
        backUpTupleSpace.update(input);
        System.out.println("Saving Backup: " + input);
        backUpTupleSpace.save(backUpTupleFilePath);
        closeSocket(socket);
    }

    ////////////////////////////////////// Server Recover Data Methods //////////////////////////////////////////////

    /**
     * Sends out the back ups you were holding. Also send out your current Look Up Table and Host Info List
     */
    private void sendRecoverData(Socket socket) {
        //send net file, back up tuple space, look up table
        try {
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            out.println("receivedRecoverData-" +
                    hostInfoList.toString() + "~" +
                    backUpTupleSpace.toString() + "~" +
                    lookUpTable.toString());
            //todo ADD BACK UP REQUEST QUEUE
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void restoreStateFromRecoverData(String recoverData, Socket socket) {
        // remove net file, tuplespace, look up table  from recover data
        String[] split = recoverData.split("~");
        hostInfoList.update(split[0]);
        tupleSpace.update(split[1]);
        lookUpTable.update(split[2]);

        //todo ADD BACK UP REQUEST QUEUE

        //todo ALSO SAVE REQUEST QUEUE

        hostInfoList.save(hostInfoFilePath);
        tupleSpace.save(tupleFilePath);
        lookUpTable.save(lookUpTableFilePath);

        // Fix your port number and tell everyone your new number
        try {
            HostInfo hostInfo = hostInfoList.getByHostName(yourName);
            hostInfo.setPortNumber(portNumber);
            broadcastMessage("add-" + hostInfoList.toString());
        } catch (Exception e) {
            System.out.println("Incorrect name given");
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
                if (!s.contains("(") || !s.contains(")")) {        // Simple Check to see if User's input is valid
                    throw new Exception();
                }
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
                    broadcastMessage("add-" + hostInfoList.toString());              // Notify Other Hosts of this net file
                    hostInfoList.save(hostInfoFilePath);                    // Save Successfully added hosts onto net file
                    lookUpTable.save(lookUpTableFilePath);
                    try {
                        TimeUnit.SECONDS.sleep(2);
                    } catch (Exception e) {

                    }
                    broadcastMessage("updateLookUpTable-" + lookUpTable.toString());

                } else if (command.equals("delete")) {              // Want to delete a host from network
                    for (int i = 1; i < split.length; i++) {        // *note user can add more than one host at a time
                        split2 = split[i].split("\\)");
                        input = split2[0];
                        handleClientDeleteRequest(input);
                    }

                } else {
                    throw new Exception();                          // Invalid Input
                }
            } catch (Exception e) {
                System.out.println("Invalid Input. Please Try again");
                e.printStackTrace();
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
            hostInfoList.addHost(hostName + "," + ipAddress + "," +
                    Integer.toString(portNum), hostInfoList.size());    //todo THIS NEEDS TO CHANGE ID HAS TO BE UNIQUE -- HASH?
            lookUpTable.addNewHost(hostName);
            closeSocket(socket);

        } catch (Exception e) {
            System.out.println("Failed to Add: " + input);
        }

    }

    /**
     * User typed a command of type "out( <tuple> )" in the terminal. This request hashes the input tuple and sends
     * the type to the correct host over the distributed system (where the tuple will be saved)
     */
    private void handleClientOutRequest(String input) {
        //todo should also add in ID into the out message to save with the tuple. !!!!!!!!!!
        int sendToHost = getHostIndexFromTupleID(getTupleID(input));
        System.out.println(sendToHost);
        String message = "out-" + Integer.toString(getTupleID(input)) + ":" + input;
        try {
            // Create Socket Connection
            Socket socket = new Socket(hostInfoList.getByID(sendToHost).getiPAddress(), hostInfoList.getByID(sendToHost).getPortNumber());
            createSocketInputStreamHandlerThread(socket);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            out.println(message);
            System.out.println("put tuple (" + input + ") on " + socket.getInetAddress().getHostAddress());
        } catch (IOException e) {
            System.out.println("out to backup");
            singleMessageWithoutBackUpCatch("backup" + message, findBackupHostIndex(sendToHost));
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
            broadcastMessage(message);
        } else {
            // Specific host to request from
            int sendToHost = getHostIndexFromTupleID(getTupleID(input));
            singleMessage(message, sendToHost);
        }
        startBlockingCode();
    }

    /**
     * This should notify that Host to be deleted. You handle the lookup table, and broadcast it to everyone
     */
    private void handleClientDeleteRequest(String input) {
        //tell the new client to go kill himself
        try {
            singleMessageWithoutBackUpCatch("deleteYoSelf", hostInfoList.getIndex(input));
        } catch (Exception e) {
            System.out.println("Incorrect Name");
        }

    }

    /////////////////////////////////////////// Helper Methods ///////////////////////////////////////////////////////

    /**
     * Will broadcast the In or Read Request to all hosts
     */
    private void broadcastMessage(String message) {
        System.out.println("Broadcasting Message:" + message);
        for (int i = 0; i < hostInfoList.size(); i++) {
            try {
                Socket socket = new Socket(hostInfoList.get(i).getiPAddress(), hostInfoList.get(i).getPortNumber());
                createSocketInputStreamHandlerThread(socket);
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                out.println(message);
            } catch (IOException e) {
                singleMessageWithoutBackUpCatch("backup" + message, findBackupHostIndex(i));
                System.out.println("send message to backup!");
            }
        }

    }

    /**
     * A Read or Write Request was just made. This imitates a blocking request
     */
    private void startBlockingCode() {
        isBlocking = true;
        while (true) {
            timeout(2);
            if (!isBlocking)
                break;
        }
    }

    /**
     * Will hash the message and based on that hash, choose a specific host to send the In or Read Request to
     * NOTE this wont work if more than one client is down at once. Otherwise it opens the possibility of a hang
     * if both the backup and the actual guy is down then.. this isn't going to work.
     */
    private void singleMessage(String message, int hostIndex) {
        try {
            System.out.println("Sending out message:" + message);
            Socket socket = new Socket(hostInfoList.getByID(hostIndex).getiPAddress(), hostInfoList.getByID(hostIndex).getPortNumber());
            createSocketInputStreamHandlerThread(socket);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            out.println(message);
        } catch (IOException e) {
            singleMessageWithoutBackUpCatch("backup+" + message, findBackupHostIndex(hostIndex));
            System.out.println("send message to backup!");
        }
    }

    /**
     * This is is one time message.If the message doesn't go through- too bad.
     */
    private void singleMessageWithoutBackUpCatch(String message, int hostIndex) {
        try {
            System.out.println("Sending out message(nobackup): " + message);
            Socket socket = new Socket(hostInfoList.getByID(hostIndex).getiPAddress(), hostInfoList.getByID(hostIndex).getPortNumber());
            createSocketInputStreamHandlerThread(socket);
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            out.println(message);
        } catch (IOException e) {
            System.out.println("ERROR in single message w/o backup");
        }
    }



    /**
     * Request Back up data from your back up host
     */
    private void justCameBackFromCrash() {
        // Try to connect to your backup
        try {
            singleMessageWithoutBackUpCatch("requestRecoverData-saveMePlease", findBackupHostIndex(yourName));
            singleMessageWithoutBackUpCatch("pleaseUpdateYourBackUp-prettyPlease", findHostForBackUpYouHave(yourName));
        } catch (Exception e) {
            System.out.println("communication with backup failed");
            e.printStackTrace();
        }

    }

    /////////////////////////////////////////// Back Up Indexing Methods /////////////////////////////////////////////

    /**
     * Will find the backup host name for the given host name
     * returns back the index in the HostInfo of which backup it is
     */
    private int findBackupHostIndex(String name) {
        return (hostInfoList.getIndex(name) + 1) % hostInfoList.size();
    }

    /**
     * Will find the backup host name for the given host index
     * returns back the index in the HostInfo of which backup it is
     */
    private int findBackupHostIndex(int index) {
        return (index + 1) % hostInfoList.size();
    }

    /**
     * Reversing the above methods. This will find whose backup you have
     */
    private int findHostForBackUpYouHave(String name) {
        return (hostInfoList.getIndex(name) - 1) % hostInfoList.size();
    }

    ////////////////////////////////////////////  HASHING METHODS  ///////////////////////////////////////////////////

    /**
     * Gets tuple ID after hashing
     */
    private int getTupleID(String message) {
        String hashedString = MD5Hash(message);
//        return hex2decimal(hashedString) % ((int) Math.pow(2, 32) - 1);        //todo should now be 2^32, not numhosts
        return hex2decimal(hashedString) % MAX;        //todo should now be 2^32, not numhosts
    }

    /**
     * Looks at Lookup table to see who should store the tuple
     */
    private int getHostIndexFromTupleID(int tupleID) {
        return hostInfoList.getIndex(lookUpTable.getHostFromID(tupleID));
    }

    /**
     * Takes a string, and converts it to md5 hashed string.
     */
    private static String MD5Hash(String message) {
        String md5 = "";
        if (null == message) {
            return null;
        }
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
        if (val < 0) {
            val *= -1;
        }
        return val;
    }

    /**
     * Deletes a Directory and all of its contents. This is used when a host is deleted from the network.
     */
    public static boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));

                if (!success) {
                    return false;
                }
            }
        }
        return dir.delete();
    }

}
