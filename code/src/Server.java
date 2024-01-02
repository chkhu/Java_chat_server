import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;

public class Server {
    private static final int PORT = 3296;

    private int clientCount = 0;

    private static final LinkedList<PrintWriter> list_writers = new LinkedList<>();

    public static void main(String[] args) throws IOException {
        new Server().start();
    }

    public synchronized void start() {
        try (ServerSocket serverSocket = setupServerSocket()) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                processNewClient(clientSocket);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private ServerSocket setupServerSocket() throws IOException {
        System.out.println("Server started!!!");
        return new ServerSocket(PORT);
    }

    private void processNewClient(Socket clientSocket) {
        new Thread(new ServerClient(clientSocket, clientCount++)).start();
    }

    public void broadcast(String msg) {
        list_writers.forEach(writer -> {
            writer.println(msg);
            writer.flush();
        });
    }

    public class ServerClient implements Runnable {
        private final Socket socket;
        private BufferedReader in;
        private PrintWriter out;

        // 构造函数现在接受 Socket 和客户端计数作为参数
        public ServerClient(Socket socket, int clientCount) {
            this.socket = socket;
            initStreams(clientCount);
        }

        private void initStreams(int clientCount) {
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
                out.println(clientCount); // 发送客户端的计数作为唯一标识
                out.flush();
                list_writers.add(out);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            String msg;
            try {
                while ((msg = in.readLine()) != null) {
                    broadcast(msg);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
