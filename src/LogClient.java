import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilterWriter;
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
public class LogClient implements Runnable {

	// represent for numbers of child threads that finish tasks
	static int threadsCnt = 0;

	// count line number of the results
	static int lineCnt = 0;

	public static void main(String[] args) {
		// start the main thread
		new Thread(new LogClient()).start();
	}

	@Override
	public void run() {
		logClientMain();
	}

	/*
	 * Implements functionalities of LogClient
	 * 
	 */
	private void logClientMain() {

		Scanner sc = new Scanner(System.in);
		while (true) {

			// users input options and patterns for grep command
			System.out.print("Your option & pattern:");
			String pattern = sc.nextLine();

			if (pattern.equals("exit"))
				break;

			String cmd = "grep " + pattern;

			// start multithreading, one thread connecting one VM
			for (int i = 0; i < 10; ++i) {

				new Thread(new MultiClient(cmd, i)).start();
			}

			// main thread waiting until all child threads finish their tasks
			while (true) {
				if (LogClient.threadsCnt == 10)
					break;
				else
					try {
						Thread.sleep(20);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
			}

			// print out grep results from files
			for (int i = 0; i < 10; i++) {
				printFile(i);
			}

			System.out.println(LogClient.lineCnt + " lines found!");

			// initialize global variants
			LogClient.threadsCnt = 0;

			LogClient.lineCnt = 0;
		}
		sc.close();
	}

	/*
	 * Prints the file line by line
	 * 
	 * @param index the index of the child thread, from 0~9
	 * 
	 */
	private void printFile(int index) {

		File fileName = new File("grepLog" + (index + 1) + ".log");
		String readStr;
		// check whether the file exists
		if (!fileName.exists()) {
			try {
				fileName.createNewFile();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}

		try {
			FileReader fileReader = new FileReader(fileName);
			BufferedReader buffread = new BufferedReader(fileReader);
			try {
				while ((readStr = buffread.readLine()) != null)
					System.out.println(readStr);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

	}

	/*
	 * Child thread used to connects with server and sends a grep command
	 * 
	 */
	static class MultiClient implements Runnable {

		private Socket socket;
		private String host;
		private int port;
		private String cmd;
		// used to indicates the number of the VM
		private int index;
		List<String> grepRet;

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
		public MultiClient(String cmd, int index) {
			// calculate IP address
			StringBuffer sb = new StringBuffer();
			sb.append("172.22.146.1");
			sb.append(String.valueOf(index + 42));
			this.host = sb.toString();
			this.port = 8399;
			this.cmd = cmd + " vm" + String.valueOf(index + 1) + ".log";
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
				writer.write(cmd);
				writer.flush();

				// open log file
				File fileName = new File("grepLog" + (index + 1) + ".log");
				FileWriter fileWriter = new FileWriter(fileName);

				// read grep results line by line and stores into the file
				BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()),
						10 * 1024 * 1024);
				String string;
				// count line number for one VM
				int vmLineCnt = 0;
				socket.setSoTimeout(10 * 1000);

				while ((string = reader.readLine()) != null) {
					fileWriter.write(string + "\n");
					fileWriter.flush();
					++vmLineCnt;
				}
				fileWriter.write("======VM" + (index + 1) + " contains " + vmLineCnt + " lines======");
				fileWriter.flush();

				// close sockets and I/O streams
				writer.close();
				reader.close();
				socket.close();
				fileWriter.close();
				++LogClient.threadsCnt;
				LogClient.lineCnt += vmLineCnt;
			} catch (IOException e) {
				System.out.println("VM" + (index + 1) + " failed!");
				++LogClient.threadsCnt;
				File fileName = new File("grepLog" + (index + 1) + ".log");
				try {
					FileWriter fileWriter = new FileWriter(fileName);
					fileWriter.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			} catch (Exception e) {
				++LogClient.threadsCnt;
				e.printStackTrace();
			}
		}
	}

}
