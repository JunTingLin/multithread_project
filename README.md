# multithread_project

## 110-2作業系統
## Thread 程式作業-多人井字遊戲
## 指導老師: 周立德教授

## 壹.	前言
本次OS作業要繳交Thread 小程式，因為對java比較熟悉，故選用java作為程式語言，用eclipse IDE開發。網路上看到許多用java多執行緒實現TCP網路的socket程式設計，但有鑑於求新求變，故基於TCP的架構下設計成可以「多人」連上sever「對玩」的井字小遊戲。

## 貳.	規劃
計劃是利用多執行緒，為連接到主伺服器的用戶端創建多個執行緒，並為遊戲本身創建執行緒。

## 參.	流程圖
![image](https://user-images.githubusercontent.com/92431095/168872986-3979e6d9-7ba6-48a5-ae81-f59647dbd252.png)

## 肆.	重要類別、函式、技術
### 一、	前端
#### Jframe類別
創建視窗框，內有各種參數、方法可去定義「遊戲窗框」的大小、標題、icon等。

#### Jpanel類別
它算是一個容器，舉凡Jlabel標籤、Jbutton按鈕、TextFiled輸入框等等都需要建立在容器之上。
因為井字遊戲，需要3x3的九宮格，故這邊使用的layout排版方式為GridLayout網格排版。

#### Jlabel類別
就如上面所提，它必須要建立在Jpanel上面的一個元件，這裡會去跑while迴圈去監聽，根據從輸入流得到的文字，去判斷Jlabel上面要呈現哪些訊息。


### 二、	後端
#### I.	Socket
在單執行緒下，存在一個問題，如果伺服器端在建立連線後傳送多條資訊給客戶端，客戶端是無法全部接收的，原因在於客戶端為單執行緒，只接受了第一條資訊，剩餘資訊阻塞等待下一次傳送。所以這造成了客戶端無法處理訊息佇列，每次只接收並輸出一條伺服器資訊，出現資訊不同步問題。

使用java的Socket網路通訊；兩端分別有一個套接字Socket，用於兩者之間的通訊。客戶端向伺服器傳送請求，建立socket進行連線。服務端則隨時監聽客戶端發起的請求。

##### ServerSocket類別
傳入port號，創建綁定到指定埠的伺服器socket

##### Socket類別
可傳入兩筆參數，第一個為sever ip address，第二個為port號。創建stream socket並將其連接到指定主機上的指定埠號。

##### accept方法
監聽要與此socket建立的連接並接受它。該方法將一直block阻塞，直到建立連接。

#### II.	Stream(流)
玩家由字元標記表示，該字元標記為“X”或“O”。
為了與客戶端通信，玩家player有一個帶有其輸入inputStream和輸出流outputStream的socket。另外再用reader跟writer接收"流"，去讀取裡面的文字


客戶端的主線程將監聽來自伺服器的消息。
第一條消息將是我們收到標記的“WELCOME”消息。
然後我們進入一個循環監聽“VALID_MOVE”、“OPPONENT_MOVED”、“VICTORY”、 “DEFEAT”、“TIE”、“OPPONENT_QUIT”或“MESSAGE”消息，並適當地處理每條消息。
“VICTORY”、“DEFEAT”和“TIE”詢問用戶是否要玩另一個遊戲。

如果是False，則退出迴圈並向伺服器發送“QUIT”消息。
如果收到 OPPONENT_QUIT 消息，則退出循環，且伺服器也會發送一條QUIT”消息。

##### Reader(讀進來)
從socket去getInputStream->讀到的二進位餵給InputStreamReader->再透過bufferedReader去readLine，讀出內容

##### Writer(寫出去)
從socket去getOutputStream->讀到的餵給PrintWriter->透過println的方式print在stream上面，並flush出去

##### flush方法
可以把stream推送出，但像PrinteWriter可在建構式下第二個參數，自動auto flush

#### III.	Thread
player會去繼承thread，把要執行的內容寫在player class裡的run，要執行時透過start函式呼叫。

○註程式碼內均有更完整的註解說明喔~~

## 伍.	遊戲畫面截圖
### 一、	書面文字DEMO
1. 啟動井字遊戲伺服器
![image](https://user-images.githubusercontent.com/92431095/168874027-44a31178-62fc-4e41-aa6c-a84d8107f7ed.png)

2. 這邊用虛擬機模擬一位線上玩家(client1、playerX)，連線到server ip
![image](https://user-images.githubusercontent.com/92431095/168874103-4a0f8e16-72f2-4819-9edb-98399ceacfcd.png)

3.另外一位玩家(client2、playerO)選擇和伺服器同網域，連線到localhost
![image](https://user-images.githubusercontent.com/92431095/168874135-2d1732ae-b96b-4464-b98c-3c609577bf66.png)

4. 進行井字遊戲對決
![image](https://user-images.githubusercontent.com/92431095/168874209-ee5f5554-46f3-4fc4-b663-62d89f0debae.png)

○註頭像設計純屬有趣，假如有冒犯到，請再告知😅

5. 平局&勝負

![image](https://user-images.githubusercontent.com/92431095/168874453-6974dcd8-4120-42f5-a9c9-48fd1e469a75.png)

6. 因為每位玩家都會分配到thread，所以要「多人」連上伺服器「對玩」也是可以的，如下圖:

![image](https://user-images.githubusercontent.com/92431095/168874647-794e1b87-851a-4663-ba0d-e98b440b241e.png)

從左邊監看視窗也可以看到，有一台server，跟八個client(四對玩家)

### 二、	影片文字DEMO
影片連結: https://youtu.be/pYok9U2lsQE

## 陸.	參考
+ Java多執行緒實現TCP網路Socket程式設計(C/S通訊)
+	java12官方文件說明
+	Java Sockets: Multithreaded Server

 
