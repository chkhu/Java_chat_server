import java.awt.event.ActionListener;
import java.net.*;
import java.awt.*;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.*;

import java.io.*;

public class Client {
    public static void main(String[] args) throws IOException {
        Client client = new Client();
        client.initFrame(); // 初始化窗口GUI
        client.initConnect(); // 初始化“S-C”连接
    }

    private static final int PORT = 3296; // 和server端规定的端口号一致
    private Socket socket;
    private PrintWriter out;
    private BufferedReader in;
    private JTextArea textArea;
    private JFrame frame;
    private int id;

    void initFrame() {
        frame = new JFrame();
        frame.setSize(800, 600);
        // 不可缩放
        frame.setResizable(false);
        // 关闭时，发送退出广播消息
        frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                sendExitMessage();
                closeResources();
                System.exit(0);
            }
        });

        // 配置输入部分GUI
        JPanel southPanel = getjPanel(); // 初始化放置输入框和按钮的面板
        frame.add(southPanel, BorderLayout.SOUTH);

        // 配置消息显示区域
        JScrollPane scrollPane = createMessageDisplayArea();
        frame.add(BorderLayout.CENTER, scrollPane);

        frame.setVisible(true);
    }

    private JScrollPane createMessageDisplayArea() {
        textArea = new JTextArea();
        textArea.setFocusable(false);
        return new JScrollPane(textArea);
    }

    private JPanel createInputPanel() {
        JTextField textField = new JTextField(50);
        // ... textField 和 button 的设置
        JPanel southPanel = new JPanel();
        southPanel.add(textField);

        Component button = new JButton("Send");
        southPanel.add(button);

        return southPanel;
    }

    private void sendMessage(String message) {
        out.println("Client " + id + " broadcasting: " + message);
        out.flush();
    }

    private void sendExitMessage() {
        if (out != null) {
            out.println("Client " + id + " has left the chat.");
            out.flush();
        }
    }

    private JPanel getjPanel() {
        JTextField textField = new JTextField(50);
        textField.setEditable(true); // 可编辑
        JButton button = new JButton("Send");
        ActionListener listener = (event) -> { // 设计一个监听器，当用户点击按钮或者按下回车时，将消息发送给服务器
            String message = textField.getText();
            if (message != null && !message.isEmpty()) {
                try {
                    sendMessage(message);
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
            textField.setText(""); // 清空输入框
        };
        // 两种触发监听器的方式
        textField.addActionListener(listener); // 当用户按下回车时，触发监听器
        button.addActionListener(listener); // 当用户点击按钮时，触发监听器

        JPanel south_panel = new JPanel(); // 用于放置输入框和按钮
        south_panel.add(textField);
        south_panel.add(button);
        return south_panel;
    }

    private void closeResources() {
        try {
            if (socket != null && !socket.isClosed()) {
                socket.close();
            }
            if (out != null) {
                out.close();
            }
            if (in != null) {
                in.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void initConnect() throws IOException {

        try {
            setupNetworkConnection();

            // 接收服务器发送的客户端ID
            String initialMsg = in.readLine();
            try {
                id = Integer.parseInt(initialMsg); // 将接收到的消息转换为客户端ID
                frame.setTitle("Chat Space | Client ID: " + id); // 更新窗口标题以显示客户端ID
            } catch (NumberFormatException e) {
                System.err.println("Error parsing client ID: " + initialMsg);
            }

            // 新建接收线程
            MsgReceiver receiver = new MsgReceiver();
            Thread receiverThread = new Thread(receiver);
            receiverThread.start();

            // 用消息的形式告诉用户连接成功
            out.println("Client " + id + " connection established!");

        } catch (IOException e) {
            JOptionPane.showMessageDialog(null, "Starting program failed!");
            if (socket != null)
                socket.close();
            System.exit(-1);
        }
    }

    private void setupNetworkConnection() throws IOException {
        socket = new Socket("localhost", PORT);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())), true);
    }

    public class MsgReceiver implements Runnable {
        @Override
        public void run() {
            String msg;
            try {
                while ((msg = in.readLine()) != null) {
                    textArea.append(msg + "\n");
                }
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
    }
}