package game;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * 多人井字遊戲伺服器
 *
 * 允許無限數量的玩家對玩
 */
public class TicTacToeServer {

    /**
     * 執行應用程式。配對連接的用戶端。
     */
    public static void main(String[] args) throws Exception {
        ServerSocket socketeer = new ServerSocket(1111);
        System.out.println("井字遊戲-伺服器運作中...");
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
 * 雙人遊戲
 */
class Game {

    /**
     * 一塊板有九個方塊。 每個方塊要麼是無主的，要麼是由玩家擁有的。 
     * 因此，我們使用一個簡單的玩家陣列Player[]。 
     * 如果為 null，則相應的方塊是無主的，否則陣列儲存格將儲存下該方格玩家的參照。
     */
    private Player[] board = {
        null, null, null,
        null, null, null,
        null, null, null};

    /**
     * 當前玩家
     */
    Player currentPlayer;

    /**
     * 返回棋盤的當前狀態是否使其中一個玩家成為贏家
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
     * 返回是否已經沒有空方塊
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
     * 當玩家嘗試移動時，會被player thread呼叫。
     * 此方法檢查移動是否合法：也就是說，請求移動的玩家必須是當前玩家，並且他嘗試移動的方塊不得已被佔用。 
     * 如果移動是合法的，則遊戲狀態將更新（此方格將設為O或X，下一個玩家變為當前玩家），
     * 並且其他玩家將收到移動通知，便可以更新另一個用戶端。
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
     * 玩家由字元標記表示，該字元標記為“X”或“O”。 
     * 為了與客戶端通信，玩家player有一個帶有其輸入inputStream和輸出流outputStream的socket。
     * 另外在用reader跟writer接收"流"，去讀取裡面的文字
     */
    class Player extends Thread {
        char mark;
        Player opponent;
        Socket socket;
        BufferedReader input;
        PrintWriter output;

        /**
         * 為socket建構一個handler thread，mark 初始化stream，顯示前兩條歡迎消息。
         */
        public Player(Socket socket, char mark) {
            this.socket = socket;
            this.mark = mark;
            try {
                input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                output = new PrintWriter(socket.getOutputStream(), true);
                output.println("WELCOME " + mark);
                output.println("MESSAGE 等待對手連上線");
            } catch (IOException e) {
            	System.out.println("玩家斷線: " + e);
            }
        }

        /**
         * 接受對手是誰的通知
         */
        public void setOpponent(Player opponent) {
            this.opponent = opponent;
        }

        /**
         * 處理其他玩家移動的消息
         */
        public void otherPlayerMoved(int location) {
            output.println("OPPONENT_MOVED " + location);
            output.println(hasWinner() ? "DEFEAT" : fillBoard() ? "TIE" : "");
        }

        /**
         * 每個thread都有一個 run 方法
         */
        public void run() {
            try {
                // thread在所有連接後啟動
                output.println("MESSAGE 所有玩家已上線");

                // 告訴第一個玩家輪到他們了
                if (mark == 'X') {
                    output.println("MESSAGE 輪到你了");
                }

                // 重複從客戶端獲取命令並進行處理。
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
                            output.println("MESSAGE 不要點了，不是換你，請等待");
                        }
                    } else if (command.startsWith("QUIT")) {
                        return;
                    }
                }
            } catch (IOException e) {
                System.out.println("玩家斷線: " + e);
            } finally {
                try {socket.close();} catch (IOException e) {}
            }
        }
    }
}