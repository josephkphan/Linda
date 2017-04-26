# Linda

# Project Description:
Linda is a distributed model that provides a conceptually global tuple space. Remote processes can access the
tuple space by atomic operations(in, rd, inp, rdp, out, eval) For my model, I have only implemented in, rd, out, and add
This program supports redundancy and fault-tolerant. There replication factor of 2 Any host and any process can
fail but the network configurations (i.e., host IP addresses and port numbers) should always available, and the
tuples space should consistent. To create this redundancy, consistency, high availability, and fault tolerance,
I integrated consistent hashing to effeciently adda nd remove hosts.

# Linda Commands:
 - in(tuple) - will Request a tuple from the tuple space and delete it. It is also a blocking call
 - rd(tuple) - will Request a tuple from the tuple space and read it. It is also a blocking call
 - outtuple) - will insert a tuple into the tuple space
 - add{(host name, ip address, port number)}  - adds the following host
 - delete{(host name)} - will delete a host from the system

# Note
 - This program only handles edge cases for at most one host is crashed at a time
 - If a host is crashed, all the requests unfulfilled by that host will be dropped


# Run the Program
Compile the Files  <br />
 - run "make"

Run the Program
 - "java P1 <host name>" <br />

Clean up files
 - "rm *.class *.txt <br />


# Examples:
$ P1 host_1  <br />
129.210.16.80 at port number: 9998  <br />
linda>  <br />

$ P1 host_2
129.210.16.81 at port number: 3571  <br />
linda> add (host_1, 129.210.16.80, 9998)  <br />
linda> out(“abc”, 3)  <br />
put tuple (“abc”, 3) on 129.210.16.81  <br />
linda>  <br />

linda> in(“abc”, ?i:int)  <br />
get tuple (“abc”, 3) on 129.210.16.81  <br />
linda>  <br />

# More Information
The add hosts will run the remote execute program to setup client/server program on the hosts and report available
port numbers. All those information should save on the hosts’ /tmp/<login>/linda/<name>/nets and all tuples should
store in /tmp/<login>/linda/<name>/tuples. Make the /tmp/<login>, /tmp/<login>/linda and
/tmp/<login>/linda/<name>mode 777, and make nets and tuples mode 666. Which host to store the tuples is decided by
hashing, e.g., “md5sum - <<< “<string>”, or echo “<string>” | md5sum.