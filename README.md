# ECE-428-MP1

Group Member: yutao2, jiey3

#### Instructions to run the distributed_grep program:

1. Place log files and java program in the same directory
2. Compile the LogServer.java and LogClient.java: javac LogServer.java; javac LogClient.java
3. Run LogServer on all the 10 VMs, and pick one VM to run LogClient
4. Input your grep options and patterns in the terminal according to the prompt. For example, if you want to get lines with pattern "com", just input "com"; if you want to get lines with pattern "nasa"  and "NASA" (case insensitive), just input "-i nasa" or "-i NASA" (exclude quotation marks)
5. The terminal will show the results from all the VMs, and the results will be automatically stored in files named grepLogi.log (where i is the index of VM, from 1~10). Next grep command will overwrite the previous results
6. The client  can identify certain fail-stop VMs, and give the VM indices at the very beginning

#### Instructions to run the unit test:

1. The log files for unit test are placed on VM2 and VM3 (which are named vm2Test.log and vm3Test.log). VM1 has the both two unit test log files
2. Compile and run the LogClientTest in VM1
3. The client will compare the results from the servers  (VM2 and VM3) to the ones obtained locally.

