/**
 * Created by Jie on 9/12/17.
 */
import java.io.*;
import java.net.*;

public class LogServer {
    public static void main(String args[]) throws IOException {
        //throw exception information out
        int port = 8399;
        //define port number
        while (true) {
            ServerSocket server = new ServerSocket(port);
            Socket socket = server.accept();
            //waiting for the request from clients

            Reader reader = new InputStreamReader(socket.getInputStream());
            //read the command from the client
            int len;
            char chars[] = new char[64];
            StringBuilder sb = new StringBuilder();
            while ((len=reader.read(chars)) != -1) {
                sb.append(new String(chars, 0, len));
            }
            //put the received string into sb.


            Process process = null;
            process = Runtime.getRuntime().exec(sb.toString());
            //execute the command line contained in sb.

            BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()));
            //put the execution result into br.
            //StringBuilder outtemp = new StringBuilder();
           // int linecount = 0;
            String out_line ;
            Writer output = new OutputStreamWriter(socket.getOutputStream());
            while ((out_line=br.readLine()) != null) {
             //   ++linecount;
                //  sb.append(outtemp);
                output.write(out_line);
                output.flush();
            }
           // if (br.readLine() == null){
            //   outline = outputline.append()
            //}
            //put all the lines in br to outtemp.

            //Writer output = new OutputStreamWriter(socket.getOutputStream());
            //output.write(outtemp.toString());
            //output.flush();
            //transmit outtemp to the client
            //output.close();
            output.close();
            reader.close();
            socket.close();
            //close all the resources
        }
    }
}
