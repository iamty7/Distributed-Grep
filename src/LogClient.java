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

public class LogClient implements Runnable {
	static int threadsCnt = 0;
	private List<String> grepRet = new LinkedList<String>();
	static int lineCnt = 0;

	public static void main(String[] args) {
		new Thread(new LogClient()).start();
	}

	@Override
	public void run() {
		logClientMain();
	}

	private void logClientMain() {
		Scanner sc = new Scanner(System.in);
		while (true) {
			// read grep command
			// while (!sc.hasNextLine()) {
			// continue;
			// }

			System.out.print("Please input your pattern:");
			String pattern = sc.nextLine();
			if (pattern.equals("exit"))
				break;
			String cmd = "grep " + pattern;

			// start multithreading
			for (int i = 0; i < 10; ++i) {
				new Thread(new MultiClient(cmd, i, grepRet)).start();
			}

			// display grep results from VMs
			while (true) {
				if (LogClient.threadsCnt == 10)
					break;
				else
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
			}
			System.out.println("While Loop Complete");
			for (int i = 0; i < grepRet.size(); ++i) {
				if (grepRet.get(i) != null && grepRet.get(i).length() != 0) {
					System.out.println(grepRet.get(i) + "\n");
				}
			}

			System.out.println(LogClient.lineCnt + " lines found!");
			//
			LogClient.threadsCnt = 0;
			grepRet.clear();
			lineCnt = 0;
		}
		sc.close();
	}

	static class MultiClient implements Runnable {

		private Socket socket;
		private String host;
		private int port;
		private String cmd;
		private int index;
		List<String> grepRet;

		public MultiClient(String cmd, int index, List<String> grepRet) {
			// calculate IP address
			StringBuffer sb = new StringBuffer();
			sb.append("172.22.146.1");
			sb.append(String.valueOf(index + 42));
			this.host = sb.toString();
			// System.out.println(sb.toString());

			this.port = 8399;
			// specify the log file name
			this.cmd = cmd + " vm" + String.valueOf(index + 1) + ".log";
			this.index = index;
			this.grepRet = grepRet;

		}

		@Override
		public void run() {
			handleSocket();
		}

		private void handleSocket() {
			try {
				// set up socket
				this.socket = new Socket(host, port);
				Writer writer = new OutputStreamWriter(socket.getOutputStream());

				// send command to the server
				writer.write(cmd);
				writer.flush();

				// read grep results line by line
				BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()),
						10 * 1024 * 1024);
				StringBuffer sb = new StringBuffer();
				String string;
				int vmLineCnt = 0;
				socket.setSoTimeout(10 * 1000);
				while ((string = reader.readLine()) != null) {
					sb.append(string + "\n");
					++vmLineCnt;
				}
				sb.append("======VM" + (index + 1) + " contains " + vmLineCnt + " lines======\n");

				grepRet.add(sb.toString());

				// close all
				writer.close();
				reader.close();
				socket.close();
				++LogClient.threadsCnt;
				LogClient.lineCnt += vmLineCnt;
			} catch (IOException e) {
				System.out.println("VM" + (index + 1) + " failed!");
				++LogClient.threadsCnt;
			} catch (Exception e) {
				++LogClient.threadsCnt;
				e.printStackTrace();
			}
		}
	}

}
