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

    private JFrame frame = new JFrame("�����j��: ���r�C��");
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
     * �z�L�s������A���B�G�m GUI �M���U GUI listeners�ӫإߥΤ�ݡC
     */
    public TicTacToeClient(String serverAddress) throws Exception {

        // �]�mnetworking
        socket = new Socket(serverAddress, PORT);
        in = new BufferedReader(new InputStreamReader(
            socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);

        // GUI����
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
     * �Ȥ�ݪ��D�u�{�N��ť�Ӧۦ��A���������C
     * �Ĥ@�������N�O�ڭ̦���аO����WELCOME�������C
     * �M��ڭ̶i�J�@�Ӵ`����ť��VALID_MOVE���B��OPPONENT_MOVED���B��VICTORY���B
     * ��DEFEAT���B��TIE���B��OPPONENT_QUIT���Ρ�MESSAGE�������A�þA��a�B�z�C�������C
     * ��VICTORY���B��DEFEAT���M��TIE���߰ݥΤ�O�_�n���t�@�ӹC���C
     * �p�G�OFalse�A�h�h�X�j��æV���A���o�e��QUIT�������C
     * �p�G���� OPPONENT_QUIT �����A�h�h�X�`���A�B���A���]�|�o�e�@����QUIT�������C
     */
    public void play() throws Exception {
        String response;
        try {
            response = in.readLine();
            if (response.startsWith("WELCOME")) {
                char mark = response.charAt(8);
                icon = new ImageIcon(mark == 'X' ? "x.png" : "o.png");
                opponentIcon  = new ImageIcon(mark == 'X' ? "o.png" : "x.png");
                frame.setTitle("���r�C�� - ���a " + mark);
            }
            while (true) {
                response = in.readLine();
                if (response.startsWith("VALID_MOVE")) {
                    messageLabel.setText("�U�o�̨S���D, �е���");
                    currentSquare.setIcon(icon);
                    currentSquare.repaint();
                } else if (response.startsWith("OPPONENT_MOVED")) {
                    int loc = Integer.parseInt(response.substring(15));
                    board[loc].setIcon(opponentIcon);
                    board[loc].repaint();
                    messageLabel.setText("���w�U, �ӧA�F");
                } else if (response.startsWith("VICTORY")) {
                    messageLabel.setText("�AĹ�F");
                    break;
                } else if (response.startsWith("DEFEAT")) {
                    messageLabel.setText("�A��F");
                    break;
                } else if (response.startsWith("TIE")) {
                    messageLabel.setText("�����");
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
            "�ٷQ�b���@����?",
            "�����j���w��A�D�Ԥ��r�C��",
            JOptionPane.YES_NO_OPTION);
        frame.dispose();
        return response == JOptionPane.YES_OPTION;
    }

    /**
     * �Ȥ�ݵ������ϧΤE�c��C�C�Ӥ�泣�O�]�t�զ⭱�O�C
     * �Ȥ�ݩI�s setIcons() ��icon��R���Aicon�O'X'�άO'O'
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
     * �N�Ȥ�ݧ@�����ε{�ǹB��
     */
    public static void main(String[] args) throws Exception {
        while (true) {
            String serverAddress = (args.length == 0) ? "localhost" : args[0];
            TicTacToeClient client = new TicTacToeClient(serverAddress);
            client.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            client.frame.setSize(540, 700);
            client.frame.setVisible(true);
            client.frame.setResizable(false);
            //���p�Ʊ�i�H�Y������A�W���o��i�ۦ���ѱ�
            client.play();
            if (!client.wantsToPlayAgain()) {
                break;
            }        }
    }
}
