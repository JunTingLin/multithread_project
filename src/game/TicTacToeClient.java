package game;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;


public class TicTacToeClient {

    private JFrame frame = new JFrame("中央大學: 井字遊戲");
    private JLabel messageLabel = new JLabel("");
    private ImageIcon icon;
    private ImageIcon opponentIcon;
    private ImageIcon img = new ImageIcon("./NCULogo.png");

    private Square[] board = new Square[9];
    private Square currentSquare;

    private static int PORT = 1111;
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;

    /**
     * 透過連接到伺服器、佈置 GUI 和註冊 GUI listeners來建立用戶端。
     */
    public TicTacToeClient(String serverAddress) throws Exception {

        // 設置networking
        socket = new Socket(serverAddress, PORT);
        in = new BufferedReader(new InputStreamReader(
            socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);

        // GUI介面
        messageLabel.setBackground(Color.lightGray);
        messageLabel.setFont(new Font("Helvetica", Font.PLAIN, 20));
        frame.getContentPane().add(messageLabel, "South");
        frame.setIconImage(img.getImage());

        JPanel boardPanel = new JPanel();
        boardPanel.setBackground(Color.black);
        boardPanel.setLayout(new GridLayout(3, 3, 2, 2));
        for (int i = 0; i < board.length; i++) {
            final int j = i;
            board[i] = new Square();
            board[i].addMouseListener(new MouseAdapter() {
                public void mousePressed(MouseEvent e) {
                    currentSquare = board[j];
                    out.println("MOVE " + j);}});
            boardPanel.add(board[i]);
        }
        frame.getContentPane().add(boardPanel, "Center");
    }

    /**
     * 客戶端的主線程將監聽來自伺服器的消息。
     * 第一條消息將是我們收到標記的“WELCOME”消息。
     * 然後我們進入一個循環監聽“VALID_MOVE”、“OPPONENT_MOVED”、“VICTORY”、
     * “DEFEAT”、“TIE”、“OPPONENT_QUIT”或“MESSAGE”消息，並適當地處理每條消息。
     * “VICTORY”、“DEFEAT”和“TIE”詢問用戶是否要玩另一個遊戲。
     * 如果是False，則退出迴圈並向伺服器發送“QUIT”消息。
     * 如果收到 OPPONENT_QUIT 消息，則退出循環，且伺服器也會發送一條“QUIT”消息。
     */
    public void play() throws Exception {
        String response;
        try {
            response = in.readLine();
            if (response.startsWith("WELCOME")) {
                char mark = response.charAt(8);
                icon = new ImageIcon(mark == 'X' ? "x.png" : "o.png");
                opponentIcon  = new ImageIcon(mark == 'X' ? "o.png" : "x.png");
                frame.setTitle("井字遊戲 - 玩家 " + mark);
            }
            while (true) {
                response = in.readLine();
                if (response.startsWith("VALID_MOVE")) {
                    messageLabel.setText("下這裡沒問題, 請等待");
                    currentSquare.setIcon(icon);
                    currentSquare.repaint();
                } else if (response.startsWith("OPPONENT_MOVED")) {
                    int loc = Integer.parseInt(response.substring(15));
                    board[loc].setIcon(opponentIcon);
                    board[loc].repaint();
                    messageLabel.setText("對手已下, 該你了");
                } else if (response.startsWith("VICTORY")) {
                    messageLabel.setText("你贏了");
                    break;
                } else if (response.startsWith("DEFEAT")) {
                    messageLabel.setText("你輸了");
                    break;
                } else if (response.startsWith("TIE")) {
                    messageLabel.setText("平手喔");
                    break;
                } else if (response.startsWith("MESSAGE")) {
                    messageLabel.setText(response.substring(8));
                }
            }
            out.println("QUIT");
        }
        finally {
            socket.close();
        }
    }

    private boolean wantsToPlayAgain() {
        int response = JOptionPane.showConfirmDialog(frame,
            "還想在玩一次嗎?",
            "中央大學歡迎你挑戰井字遊戲",
            JOptionPane.YES_NO_OPTION);
        frame.dispose();
        return response == JOptionPane.YES_OPTION;
    }

    /**
     * 客戶端視窗的圖形九宮格。每個方格都是包含白色面板。
     * 客戶端呼叫 setIcons() 用icon填充它，icon是'X'或是'O'
     */
    static class Square extends JPanel {
        JLabel label = new JLabel((Icon)null);

        public Square() {
            setBackground(Color.white);
            add(label);
        }

        public void setIcon(Icon icon) {
            label.setIcon(icon);
        }
    }

    /**
     * 將客戶端作為應用程序運行
     */
    public static void main(String[] args) throws Exception {
        while (true) {
            String serverAddress = (args.length == 0) ? "localhost" : args[0];
            TicTacToeClient client = new TicTacToeClient(serverAddress);
            client.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            client.frame.setSize(540, 700);
            client.frame.setVisible(true);
            client.frame.setResizable(false);
            //假如希望可以縮放視窗，上面這行可自行註解掉
            client.play();
            if (!client.wantsToPlayAgain()) {
                break;
            }        }
    }
}
