
import java.io.*;
import java.net.*;

/*
 * Listens on 8399 socket and connects with a client
 * Receives grep command from client and executes the command
 * Sends results back to the client
 * 
 * @author Jie Yin
 */

public class LogServer {
	public static void main(String args[]) throws IOException {
		// define port number
		int port = 8399;
	
		ServerSocket server = new ServerSocket(port);
		while (true) {
			// waits for the request from clients
			Socket socket = server.accept();
			
			// reads the command from the client
			Reader reader = new InputStreamReader(socket.getInputStream());
			int len;
			char chars[] = new char[64];
			StringBuilder sb = new StringBuilder();
			len = reader.read(chars);
			sb.append(chars, 0, len);

			// executes the command
			Process process = null;
			process = Runtime.getRuntime().exec(sb.toString());
			
			// reads results form inputStream and sends back to the client
			BufferedReader br = new BufferedReader(new InputStreamReader(process.getInputStream()), 10 * 1024 * 1024);
			String out_line;
			Writer output = new OutputStreamWriter(socket.getOutputStream());
			while ((out_line = br.readLine()) != null) {
				output.write(out_line + "\n");
				output.flush();
			}
			
			// close all the resources
			output.close();
			reader.close();
			socket.close();
			
		}
	}
}
