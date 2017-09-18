import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

/*
 * Interacts with users. 
 * Scans their inputs of options and patterns for the grep command.
 * Sends the command to the server.
 * Displays the data from server.
 * 
 * 
 * @author Yu Tao
 * 
 */
public class LogClientTest implements Runnable {

	// represents for numbers of child threads that finish tasks
	static int threadsCnt = 0;
	static String cmd;

	public static void main(String[] args) {

		// Pattern 1: frequent pattern
		// Pattern 2: infrequent pattern
		// Pattern 3: somewhat frequent pattern
		// Pattern 4: pattern only in one vm log
		// Pattern 5: regexps pattern
		String[] cmds = { "grep 404", "grep AArcada", "grep arcada", "grep edu", "grep 'edu.*HTTP'" };

		for (int i = 0; i < 5; ++i) {
			cmd = cmds[i];
			Thread thread = new Thread(new LogClientTest());
			thread.start();
			while (true) {
				if (LogClientTest.threadsCnt == 10)
					break;
				else
					try {
						thread.sleep(10);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
			}
			LogClientTest.threadsCnt = 0;
		}
	}

	@Override
	public void run() {
		logClientTestMain(LogClientTest.cmd);
	}

	/*
	 * Implements functionalities of LogClientTest
	 * 
	 */
	private void logClientTestMain(String cmd) {

		// start multithreading, one thread connecting one VM
		for (int i = 0; i < 10; ++i) {
			new Thread(new MultiClientTest(cmd, i)).start();
		}

	}

	/*
	 * Child thread used to connects with server and sends a grep command
	 * 
	 */
	static class MultiClientTest implements Runnable {

		private Socket socket;
		private String host;
		private int port;
		private String clientCmd;
		// used to indicates the number of the VM
		private int index;

		/*
		 * 
		 * The constructor creates a thread to connect to a server
		 * 
		 * @param cmd the options and pattern of the grep command
		 * 
		 * @param index the index of the child thread, from 0~9
		 * 
		 * @param grepRet the grep results for the main thread to display and
		 * store
		 */
		public MultiClientTest(String cmd, int index) {
			// calculate IP address
			StringBuffer sb = new StringBuffer();
			sb.append("172.22.146.1");
			sb.append(String.valueOf(index + 42));
			this.host = sb.toString();
			this.port = 8399;
			this.clientCmd = cmd + " vm" + String.valueOf(index + 1) + "Test.log";
			this.index = index;

		}

		@Override
		public void run() {
			handleSocket();
		}

		/*
		 * Sets up a socket to a server and grabs the results
		 * 
		 */
		private void handleSocket() {
			try {
				// set up socket
				this.socket = new Socket(host, port);
				Writer writer = new OutputStreamWriter(socket.getOutputStream());

				// send command to the server
				writer.write(clientCmd);
				writer.flush();

				// read grep results from remote
				BufferedReader serverReader = new BufferedReader(new InputStreamReader(socket.getInputStream()),
						10 * 1024 * 1024);
				// read grep results from local
				Process process = null;
				process = Runtime.getRuntime().exec(clientCmd);
				BufferedReader localReader = new BufferedReader(new InputStreamReader(process.getInputStream()));

				String serverStr = null, localStr = null;
				// compare results from remote and local line by line
				socket.setSoTimeout(5 * 1000);
				boolean is_Match = true;
				while ((serverStr = serverReader.readLine()) != null && (localStr = localReader.readLine()) != null) {
					if (!serverStr.equals(localStr)) {
						System.out.println(
								"===Results do not match!!===" + "serverStr: " + serverStr + " localStr: " + localStr);
						is_Match = false;
						break;
					}

				}
				if (is_Match)
					System.out.println("===Server and Local Results from vm" + (index + 1) + " match!!===\n");

				// close sockets and I/O streams
				writer.close();
				serverReader.close();
				localReader.close();
				socket.close();
				++LogClientTest.threadsCnt;
			} catch (IOException e) {
				++LogClientTest.threadsCnt;
			} catch (Exception e) {
				++LogClientTest.threadsCnt;
				e.printStackTrace();
			}
		}
	}

}
