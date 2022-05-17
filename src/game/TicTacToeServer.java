package game;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * �h�H���r�C�����A��
 *
 * ���\�L���ƶq�����a�缾
 */
public class TicTacToeServer {

    /**
     * �������ε{���C�t��s�����Τ�ݡC
     */
    public static void main(String[] args) throws Exception {
        ServerSocket socketeer = new ServerSocket(1111);
        System.out.println("���r�C��-���A���B�@��...");
        try {
            while (true) {
                Game game = new Game();
                Game.Player playerX = game.new Player(socketeer.accept(), 'X');
                Game.Player playerO = game.new Player(socketeer.accept(), 'O');
                playerX.setOpponent(playerO);
                playerO.setOpponent(playerX);
                game.currentPlayer = playerX;
                playerX.start();
                playerO.start();
            }
        } finally {
            socketeer.close();
        }
    }
}

/**
 * ���H�C��
 */
class Game {

    /**
     * �@���O���E�Ӥ���C �C�Ӥ���n��O�L�D���A�n��O�Ѫ��a�֦����C 
     * �]���A�ڭ̨ϥΤ@��²�檺���a�}�CPlayer[]�C 
     * �p�G�� null�A�h����������O�L�D���A�_�h�}�C�x�s��N�x�s�U�Ӥ�檱�a���ѷӡC
     */
    private Player[] board = {
        null, null, null,
        null, null, null,
        null, null, null};

    /**
     * ��e���a
     */
    Player currentPlayer;

    /**
     * ��^�ѽL����e���A�O�_�Ϩ䤤�@�Ӫ��a����Ĺ�a
     */
    public boolean hasWinner() {
        return
            (board[0] != null && board[0] == board[1] && board[0] == board[2])
          ||(board[3] != null && board[3] == board[4] && board[3] == board[5])
          ||(board[6] != null && board[6] == board[7] && board[6] == board[8])
          ||(board[0] != null && board[0] == board[3] && board[0] == board[6])
          ||(board[1] != null && board[1] == board[4] && board[1] == board[7])
          ||(board[2] != null && board[2] == board[5] && board[2] == board[8])
          ||(board[0] != null && board[0] == board[4] && board[0] == board[8])
          ||(board[2] != null && board[2] == board[4] && board[2] == board[6]);
    }

    /**
     * ��^�O�_�w�g�S���Ť��
     */
    public boolean fillBoard() {
        for (int i = 0; i < board.length; i++) {
            if (board[i] == null) {
                return false;
            }
        }
        return true;
    }

    /**
     * ���a���ղ��ʮɡA�|�Qplayer thread�I�s�C
     * ����k�ˬd���ʬO�_�X�k�G�]�N�O���A�ШD���ʪ����a�����O��e���a�A�åB�L���ղ��ʪ�������o�w�Q���ΡC 
     * �p�G���ʬO�X�k���A�h�C�����A�N��s�]�����N�]��O��X�A�U�@�Ӫ��a�ܬ���e���a�^�A
     * �åB��L���a�N���첾�ʳq���A�K�i�H��s�t�@�ӥΤ�ݡC
     */
    public synchronized boolean legalMove(int location, Player player) {
        if (player == currentPlayer && board[location] == null) {
            board[location] = currentPlayer;
            currentPlayer = currentPlayer.opponent;
            currentPlayer.otherPlayerMoved(location);
            return true;
        }
        return false;
    }

    /**
     * ���a�Ѧr���аO��ܡA�Ӧr���аO����X���Ρ�O���C 
     * ���F�P�Ȥ�ݳq�H�A���aplayer���@�ӱa�����JinputStream�M��X�youtputStream��socket�C
     * �t�~�b��reader��writer����"�y"�A�hŪ���̭�����r
     */
    class Player extends Thread {
        char mark;
        Player opponent;
        Socket socket;
        BufferedReader input;
        PrintWriter output;

        /**
         * ��socket�غc�@��handler thread�Amark ��l��stream�A��ܫe����w������C
         */
        public Player(Socket socket, char mark) {
            this.socket = socket;
            this.mark = mark;
            try {
                input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                output = new PrintWriter(socket.getOutputStream(), true);
                output.println("WELCOME " + mark);
                output.println("MESSAGE ���ݹ��s�W�u");
            } catch (IOException e) {
            	System.out.println("���a�_�u: " + e);
            }
        }

        /**
         * �������O�֪��q��
         */
        public void setOpponent(Player opponent) {
            this.opponent = opponent;
        }

        /**
         * �B�z��L���a���ʪ�����
         */
        public void otherPlayerMoved(int location) {
            output.println("OPPONENT_MOVED " + location);
            output.println(hasWinner() ? "DEFEAT" : fillBoard() ? "TIE" : "");
        }

        /**
         * �C��thread�����@�� run ��k
         */
        public void run() {
            try {
                // thread�b�Ҧ��s����Ұ�
                output.println("MESSAGE �Ҧ����a�w�W�u");

                // �i�D�Ĥ@�Ӫ��a����L�̤F
                if (mark == 'X') {
                    output.println("MESSAGE ����A�F");
                }

                // ���Ʊq�Ȥ������R�O�öi��B�z�C
                while (true) {
                    String command = input.readLine();
                    if (command.startsWith("MOVE")) {
                        int location = Integer.parseInt(command.substring(5));
                        if (legalMove(location, this)) {
                            output.println("VALID_MOVE");
                            output.println(hasWinner() ? "VICTORY"
                                         : fillBoard() ? "TIE"
                                         : "");
                        } else {
                            output.println("MESSAGE ���n�I�F�A���O���A�A�е���");
                        }
                    } else if (command.startsWith("QUIT")) {
                        return;
                    }
                }
            } catch (IOException e) {
                System.out.println("���a�_�u: " + e);
            } finally {
                try {socket.close();} catch (IOException e) {}
            }
        }
    }
}