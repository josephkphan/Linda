import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

public class Host {
    private Scanner scanner;
    private InetAddress ip;
    private ServerSocket listener;
    private ArrayList<Socket> socketList;
    private int portNumber;

    public Host() throws IOException {
        //Setting up Server Stuff
        scanner = new Scanner(System.in);
        socketList = new ArrayList<Socket>();
        setupServer();
        findOtherHosts();
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

    private void makeNewSocketListener(Socket s) {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    try {
                        BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
                        String streamString = in.readLine();
                        System.out.println(streamString);
                        if(streamString.equals("null")){
                            System.out.println("FOUND STRING NULL. THAT SOCKET GOT MESSED UP"); //todo MAKE FAUlT TOLERANT HERE
                            System.exit(0);
                        }

                    } catch (Exception e) {
                        System.out.println("Error in socketListener");
                    }
                }
            }
        });
        t.start();

    }

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
                split = split[1].split("\\)");
                String input = split[0];
//                System.out.println("command = " + command);
//                System.out.println("input = " + input);

                if (command.equals("in")) {
                    in();
                } else if (command.equals("out")) {
                    out();
                } else if (command.equals("add")) {
                    add(input);
                } else {
                    throw new Exception();
                }
            } catch (Exception e) {
                System.out.println("Invalid Input. Please Try again");
            }
        }
    }

    private void in() {

    }


    private void out() {

    }

    private void add(String input) {
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

    public void nextLindaLine() {


    }

    public static void main(String[] args) {
        try {
            new Host();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}





