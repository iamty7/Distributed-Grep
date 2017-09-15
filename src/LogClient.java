import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

public class LogClient {

	public static void main(String[] args) {

		List<String> grepRet = new LinkedList<String>();

		System.out.print("Please input your cmd:");
		while (true) {
			// read grep command
			Scanner sc = new Scanner(System.in);
			String cmd = sc.nextLine();

			// check command
			if (!cmd.startsWith("grepLog ")) {
				System.out.println("Command invalid!\n\nPlease input your cmd:");
				continue;
			}

			// start multithreading
			for (int i = 0; i < 10; ++i) {
				new Thread(new MultiClient(cmd, i, grepRet)).start();
			}

			// display grep results from VMs
			for (int i = 0; i < 10; ++i) {
				if (grepRet.get(i) != null && grepRet.get(i).length() != 0) {
					System.out.println(grepRet.get(i) + "\n");
				}
			}

			sc.close();

		}
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

			this.port = 8399;
			// specify the log file name
			this.cmd = cmd + "vm" + String.valueOf(index + 1) + ".log";
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
				Reader reader = new InputStreamReader(socket.getInputStream());
				StringBuffer sb = new StringBuffer();
				char[] chars = new char[1024];
				int lineCnt = 0;
				socket.setSoTimeout(10 * 1000);
				while (reader.read(chars) != -1) {
					sb.append(chars);
					++lineCnt;
				}
				sb.append("======VM " + index + "contains " + lineCnt + "lines======\n");

				grepRet.add(this.index, sb.toString());

				// close all
				writer.close();
				reader.close();
				socket.close();

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
