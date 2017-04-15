# Linda

# Project Description:
Linda is a distributed model that provides a conceptually global tuple space. Remote processes can access the
tuple space by atomic operations(in, rd, inp, rdp, out, eval) For my model, I have only implemented in, rd, out, and add


# Linda Commands:
 - in(tuple) - will Request a tuple from the tuple space and delete it. It is also a blocking call
 - rd(tuple) - will Request a tuple from the tuple space and read it. It is also a blocking call
 - outtuple) - will insert a tuple into the tuple space
 - add{(host name, ip address, port number)}  - adds the following host

# Note
The add command has to be executed before all other subcommands.

# Run the Program
Compile the Files
 - "javac Host.java HostInfo.java HostInfoList.java P1.java Tuple.java TupleSpace.java Pair.java"
Run the Program
 - "java P1 <host name>"
 Clean up files
 - "rm *.class *.txt


# Examples:
$ P1 host_1
129.210.16.80 at port number: 9998
linda>

$ P1 host_2
129.210.16.81 at port number: 3571
linda> add (host_1, 129.210.16.80, 9998)
linda> out(“abc”, 3)
put tuple (“abc”, 3) on 129.210.16.81
linda>

linda> in(“abc”, ?i:int)
get tuple (“abc”, 3) on 129.210.16.81
linda>

#More Information
The add hosts will run the remote execute program to setup client/server program on the hosts and report available
port numbers. All those information should save on the hosts’ /tmp/<login>/linda/<name>/nets and all tuples should
store in /tmp/<login>/linda/<name>/tuples. Make the /tmp/<login>, /tmp/<login>/linda and
/tmp/<login>/linda/<name>mode 777, and make nets and tuples mode 666. Which host to store the tuples is decided by
hashing, e.g., “md5sum - <<< “<string>”, or echo “<string>” | md5sum.