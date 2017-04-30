# Linda

# Project Description:
Linda is a distributed model (Peer to Peer System) for a tuple storage system. Remote processes can access the
tuple space by atomic operations(in, rd, inp, rdp, out, eval). For my model, I have only implemented in, rd, out, add, and delete
This program supports redundancy and fault-tolerance. There replication factor of 2 Any host and any process can
fail but the network configurations (i.e., host IP addresses and port numbers) should always available, and the
tuples space should consistent. To create this redundancy, consistency, high availability, and fault tolerance,
I integrated consistent hashing to efficiently add and remove hosts while redistributing tuples.

# Linda Commands:
 - in(tuple) - will Request a tuple from the tuple space and delete it. It is also a blocking call
 - rd(tuple) - will Request a tuple from the tuple space and read it. It is also a blocking call
 - out(tuple) - will insert a tuple into the tuple space
 - add{(host name, ip address, port number)}  - adds the following host
 - delete(host name{,hostname}) - will delete a list of hosts from the system

# Note
 - This program only handles edge cases for at most one host is crashed at a time
 - If a host is crashed, all the requests will get redirected to that host's back up
 - Hosts within the system are the only ones that can add in new hosts. An outside host cannot add a member of the system
 - While a host is crashed - only in, rd, out are taken care of. CANNOT ADD OR DELETE when a host is crashed


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

