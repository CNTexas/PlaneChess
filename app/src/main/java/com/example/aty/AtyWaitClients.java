package com.example.aty;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.network.broadcast.ThreadBroacastLuncher;
import com.example.network.broadcast.HelperBroascastGroup;
import com.example.network.broadcast.HelperIPAdress;
import com.example.network.model.MsgNet;
import com.example.network.model.sever.ServerInTele;
import com.example.utils.UtilDeserializable;
import com.example.yifeihappy.planechess.R;
import com.example.network.broadcast.DataBroaCastSerlied;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;


public class AtyWaitClients extends AppCompatActivity {
    public static final String TAG = "test";

    public static final String CREATE_ROOM = "create_room";//CREATEROOM
    public static final String CONNECT = "connect_to_sever";//CONNECT TO SERVER
    public static final String ENTER_ROOM = "enter_room";//ENTERROOM
    public static final String WELCOME = "welecome";//WELCOME
    public static final String REFUSE = "refuse";//REFUSE
    public static final String BEGIN = "begin";//BEGIN
    public static final String CBACK = "CBA";//CBA
    public static final String RBACK = "RBA";//RBA
    public static final int MULOUT_PORT = 31111;
    public static final int BEGIN_WHAT = 0x100;
    public static final int TO_HOME = 0x600;
    public static final int SOCKET_PORT = 20000;

    //服务器实例
    ServerInTele serverInTel = null;
    //组播帮助类，作用是加入组播或者加入组播后发送多播消息
    HelperBroascastGroup broascastGroupHelper = null;
    //服务器发送多播消息的线程
    ThreadBroacastLuncher broacastRooMIPThread = null;

    //服务器接收消息的线程，主要是tcp消息
    ServerAcceptThread serverAcceptThread = null;


    Button btnBegin = null;//click to start the game if all players hava entered.
    String roomIP = null;
    int playersNum;//the num of player

    //房主维护所有加入房间的客户的信息，包括该用户选择的颜色和IP
    Map mRoleIpAndColor = new TreeMap();//color and ip which players have selected and entered.

    //所有人的名字
    Map mRoleIpAndName = new TreeMap();//key = playerIp,value = planeColor

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == BEGIN_WHAT) {
                btnBegin.setEnabled(true);
                Toast.makeText(AtyWaitClients.this, "Click to begin...", Toast.LENGTH_LONG).show();


            }

            if (msg.what == TO_HOME) {
                Intent intentToHome = new Intent(AtyWaitClients.this, AtyMain.class);
                startActivity(intentToHome);
                Log.e("doits", "before finish");
                finish();
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wait_clients);

        //获取房间IP
        roomIP = HelperIPAdress.getIPByWifi(this);


        //获取上一个aty传过来的数据
        Intent intent = getIntent();

        final Bundle bundle = intent.getExtras();
        //上个活动传过来的游戏人数
        playersNum = Integer.parseInt(bundle.getString("playersNum"));

        //add room creater plane color to colorList.key = playerIP,value = color.

        //将服务器对应的role的名字和颜色与服务器ip其实也就是房间ip对应起来
        mRoleIpAndColor.put(roomIP, bundle.getCharSequence("planeColor"));
        mRoleIpAndName.put(roomIP, bundle.getString("playerName"));

        btnBegin = (Button) findViewById(R.id.btnBegin);
        TextView txtRoomPlayerNum = (TextView) findViewById(R.id.txtRoomPlayerNum);

        txtRoomPlayerNum.setText("飞机起飞点数：" + bundle.getString("startNums") + "\n" + "人数：" +
                bundle.getString("playersNum") + "\n" + "Host Name:" + bundle.getString("playerName") + "\n"
                + "Host Color:" + bundle.getString("planeColor") + "\n" + "ip" + roomIP);


        //多播消息的发送端口，创建多播需要一个输出端口可一个D类地址，D类地址被隐藏封装了，创建多播后要将自己加入多播组
        broascastGroupHelper = new HelperBroascastGroup(MULOUT_PORT);
        broascastGroupHelper.joinGroup();
        broascastGroupHelper.setLoopback(true);

//        发送广播，发送的内容是本房间的创建参数，要更改实时的信息就在这里，为什么要发送这么多无用信息
        DataBroaCastSerlied roomIPData = new DataBroaCastSerlied(CREATE_ROOM, roomIP, bundle.getString("playersNum"), roomIP, bundle.getString("planeColor"), bundle.getString("playerName"));
        broacastRooMIPThread = new ThreadBroacastLuncher(broascastGroupHelper, roomIPData.toString());
        broacastRooMIPThread.start();

        /**
         * 用刚才的数据和端口来创建服务器，服务器接收消息
         */
        try {

//            -------------------------------------------------------------------------------------------------------------------------
//            serverInTel = new ServerInTele(playersNum, SOCKET_PORT);
            serverInTel = ServerInTele.newInstance(playersNum, SOCKET_PORT);
            //            -------------------------------------------------------------------------------------------------------------------------

            serverInTel.accept();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(AtyWaitClients.this, "创建服务器失败", Toast.LENGTH_SHORT).show();
        }

        /**
         * 新建线程从服务器的消息队列中取出消息
         */
        serverAcceptThread = new ServerAcceptThread();
        serverAcceptThread.start();

        btnBegin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //服务端开始游戏
                AtyGameSever.startAtyGameSever(AtyWaitClients.this, AtyGameSever.class, serverInTel, roomIP);
            }
        });
    }


    /**
     * 新建线程重服务器的消息队列中取出消息，取出的数据结果通过handler发送到主线程
     */
    class ServerAcceptThread extends Thread {
        public Object myLock = new Object();
        private volatile boolean stopThread = false;

        public void setStopThread() {

            stopThread = true;
            this.interrupt();
            serverInTel.closeAll();

        }

        @Override
        public void run() {
            super.run();
            try {

                while (!stopThread) {
                    MsgNet msg = serverInTel.getData();

                    /**
                     *0x60应该指的是客户端第一次连接服务器的返回数据
                     */
                    if (msg.getFrom() == 0x60) {//the data client connect to serversocket.

                        //向指定index的客户端发送数据
                        int cIndex = Integer.parseInt(msg.getData());
                        DataBroaCastSerlied dataToSingleClient = new DataBroaCastSerlied(CONNECT, roomIP, msg.getData(), "NULL", "NULL", "NULL");//playersNum == cIndex
                        MsgNet msgToSend = new MsgNet(dataToSingleClient.toString(), (byte) 0x60);
                        //发送房间信息
                        Log.d(TAG, "向客户发送信息" + msg.toString());
                        serverInTel.send(cIndex, msgToSend);

                    } else {//the data client select a plane
                        UtilDeserializable deserializable = new UtilDeserializable();
                        DataBroaCastSerlied enterData = deserializable.deSerliBroacastData(msg.getData());

                        //判断房间IP是不是一样的，如果一样就是进入房间，服务器的行为根据后面的数据内容而定
                        if (enterData.getRoomIP().startsWith(roomIP)) {

                            DataBroaCastSerlied dataToAllClients = null;
                            MsgNet msgToSend = null;
                            /**
                             *假如是收到的进入房间的消息
                             */
                            if (enterData.getTag().startsWith(ENTER_ROOM)) {
                                //判断房间内是否已经存在该客户端IP
                                if (!mRoleIpAndColor.containsKey(enterData.getPlayerIP())) {
                                    //判断房间内是不是已经存在被选过的飞机颜色
                                    if (!mRoleIpAndColor.containsValue(enterData.getPlaneColor())) {

                                        //将该客户端加入房间
                                        mRoleIpAndColor.put(enterData.getPlayerIP(), enterData.getPlaneColor());
                                        mRoleIpAndName.put(enterData.getPlayerIP(), enterData.getPlayerName());

                                        //发送全局广播欢迎
                                        dataToAllClients = new DataBroaCastSerlied(WELCOME, roomIP, String.valueOf(playersNum), enterData.getPlayerIP(), enterData.getPlaneColor(), enterData.getPlayerName());
                                        msgToSend = new MsgNet(dataToAllClients.toString(), (byte) 0x00);
                                    } else {
                                        //发送全局广播拒绝
                                        dataToAllClients = new DataBroaCastSerlied(REFUSE, roomIP, String.valueOf(playersNum), enterData.getPlayerIP(), enterData.getPlaneColor(), enterData.getPlayerName());
                                        msgToSend = new MsgNet(dataToAllClients.toString(), (byte) 0x00);
                                    }
                                    serverInTel.sendToAll(msgToSend);
                                }
                            }
                            /**
                             * 假如接收到的是退出的消息就将客户端移除
                             */
                            if (enterData.getTag().startsWith(CBACK)) {

                                if (mRoleIpAndColor.containsKey(enterData.getPlayerIP())) {
                                    mRoleIpAndColor.remove(enterData.getPlayerIP());
                                }
                                if (mRoleIpAndName.containsKey(enterData.getPlayerIP())) {
                                    mRoleIpAndName.remove(enterData.getPlayerIP());
                                }
                                int backIndex = Integer.valueOf(enterData.getPlayersNum());
                                serverInTel.close(backIndex);
                                playersNum--;
                            }
                            /**
                             * 假如玩家已经够了就要开始游戏了,将所有role的所有信息全部打包发送
                             */
                            if (mRoleIpAndColor.size() == playersNum && playersNum != 1) {
                                String playersName = new String();
                                String playersIP = new String();
                                String playersColor = new String();
                                Iterator itr = mRoleIpAndColor.entrySet().iterator();
                                while (itr.hasNext()) {
                                    Map.Entry entry = (Map.Entry) itr.next();
                                    playersIP += entry.getKey() + "#";
                                    playersColor += entry.getValue() + "#";
                                    playersName += mRoleIpAndName.get(entry.getKey()) + "#";
                                }
                                dataToAllClients = new DataBroaCastSerlied(BEGIN, roomIP, String.valueOf(playersNum), playersIP, playersColor, playersName);
                                msgToSend = new MsgNet(dataToAllClients.toString(), (byte) 0x00);
                                serverInTel.sendToAll(msgToSend);
                                Message message = handler.obtainMessage();
                                message.what = BEGIN_WHAT;
                                handler.sendMessage(message);
                                broacastRooMIPThread.stopThread();
                            }
                            /**
                             *玩家数量一直为1返回主界面
                             */
                            if (playersNum == 1) {
                                Message messageToHome = handler.obtainMessage();
                                messageToHome.what = TO_HOME;
                                handler.sendMessage(messageToHome);
                                Log.e("doits", "after send message");
                                broacastRooMIPThread.stopThread();
                            }
                        }
                    }
                }
            } catch (InterruptedException e) {

            }
        }
    }


    /**
     * 强行退出
     */
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        final DataBroaCastSerlied roomBackData = new DataBroaCastSerlied(RBACK, roomIP, String.valueOf(playersNum), "NULL", "NULL", "NULL");
        MsgNet msgRBAck = new MsgNet(roomBackData.toString(), (byte) 0x00);
        serverInTel.sendToAll(msgRBAck);

        new Thread(new Runnable() {
            @Override
            public void run() {

                HelperBroascastGroup rBackBroascast = new HelperBroascastGroup(MULOUT_PORT);
                rBackBroascast.joinGroup();
                rBackBroascast.setLoopback(true);

                rBackBroascast.sendMsg(roomBackData.toString());
                rBackBroascast.sendMsg(roomBackData.toString());
                rBackBroascast.sendMsg(roomBackData.toString());

                Log.e("doit", roomBackData.toString());
                rBackBroascast.destory();
            }
        }).start();

        broacastRooMIPThread.stopThread();
    }

    @Override
    protected void onDestroy() {
        Log.e("doits", "indestroy server");
        serverAcceptThread.setStopThread();
        Log.e("doits", "ondestroy server");
        super.onDestroy();
    }
}

