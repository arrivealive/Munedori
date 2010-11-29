package net.loglesslove.munedori;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Handler;

//Bluetoothチャットサーバ
public class BluetoothChatService {
    //設定定数
    private static final String NAME="Munedori";
    private static final UUID   MY_UUID=
    	UUID.fromString("93e93ed3-68e7-43b2-9559-0003e999e6c5");
	//    UUID.fromString("fa87c0d0-afac-11de-8a39-0800200c9a66");

    //状態定数
    public static final int STATE_NONE      =0;
    public static final int STATE_LISTEN    =1;
    public static final int STATE_CONNECTING=2;
    public static final int STATE_CONNECTED =3;

    //変数
    private BluetoothAdapter adapter;
    private Handler          handler;
    private AcceptThread     acceptThread;
    private ConnectThread    connectThread;
    private ConnectedThread  connectedThread;
    private int              state;

    private int color;

    private String partnerName;

    //コンストラクタ
    public BluetoothChatService(Context context,Handler handler) {
        this.adapter=BluetoothAdapter.getDefaultAdapter();
        this.state  =STATE_NONE;
        this.handler=handler;

        this.color = 0;
    }

    //状態の指定
    private synchronized void setState(int state) {
        this.state=state;
        handler.obtainMessage(
            Torianime.MSG_STATE_CHANGE,state,-1).sendToTarget();
    }

    //状態の取得
    public synchronized int getState() {
        return state;
    }

    // 色の指定
    public void setColor(int color) {
    	this.color = color;
    }

    public int getColor() {
    	return this.color;
    }

    public String getPartnerName() {
    	return partnerName;
    }

    //Bluetoothの接続待ち(サーバ)
    public synchronized void start() {
        if (connectThread!=null) {
            connectThread.cancel();connectThread=null;}
        if (connectedThread!=null) {
            connectedThread.cancel();connectedThread=null;}
        if (acceptThread==null) {
            acceptThread=new AcceptThread();
            acceptThread.start();
        }
        setState(STATE_LISTEN);
    }

    //Bluetoothの接続要求(クライアント)
    public synchronized void connect(BluetoothDevice device) {
        if (state==STATE_CONNECTING) {
            if (connectThread!=null) {
                connectThread.cancel();connectThread=null;}
        }
        if (connectedThread!=null) {
            connectedThread.cancel();connectedThread=null;}
        connectThread=new ConnectThread(device);
        connectThread.start();
        setState(STATE_CONNECTING);
    }

    //Bluetooth接続完了後の処理
    public synchronized void connected(BluetoothSocket socket, BluetoothDevice device) {
        if (connectThread!=null) {
            connectThread.cancel();connectThread=null;}
        if (connectedThread!=null) {
            connectedThread.cancel();connectedThread=null;}
        if (acceptThread!=null) {
            acceptThread.cancel();acceptThread=null;}
        connectedThread=new ConnectedThread(socket);
        connectedThread.start();
        setState(STATE_CONNECTED);
    }

    //Bluetoothの切断
    public synchronized void stop() {
        if (connectThread!=null) {
            connectThread.cancel();connectThread=null;}
        if (connectedThread!=null) {
            connectedThread.cancel();connectedThread=null;}
        if (acceptThread!=null) {
            acceptThread.cancel();acceptThread=null;}
        setState(STATE_NONE);
    }

    //書き込み
    public void write(byte[] out) {
        ConnectedThread r;
        synchronized (this) {
            if (state!=STATE_CONNECTED) return;
            r=connectedThread;
        }
        r.write(out);
    }

    //Bluetoothの接続待ち(サーバ)(5)
    private class AcceptThread extends Thread {
        private BluetoothServerSocket serverSocket;

        //コンストラクタ
        public AcceptThread() {
            try {
            	// MY_UUID is the app's UUID string, also used by the client code
                serverSocket = adapter.listenUsingRfcommWithServiceRecord(NAME,MY_UUID);
            } catch (IOException e) {
            }
        }

        //処理
        public void run() {
            BluetoothSocket socket=null;
            // Keep listening until exception occurs or a socket is returned
            while (state!=STATE_CONNECTED) {
                try {
                    socket=serverSocket.accept();
                } catch (IOException e) {
                    break;
                // それ以外の例外
                } catch (Exception h) {
                    setState(STATE_NONE);
                	break;
                }
                // If a connection was accepted
                if (socket!=null) {
                    // Do work to manage the connection (in a separate thread)
                    synchronized (BluetoothChatService.this) {
                        switch (state) {
                        case STATE_LISTEN:
                        case STATE_CONNECTING:
                            connected(socket,socket.getRemoteDevice());
                            partnerName = socket.getRemoteDevice().getName(); // 相手方のデバイス名
                            break;
                        case STATE_NONE:
                        case STATE_CONNECTED:
                            try {
                                socket.close();
                            } catch (IOException e) {
                            }
                            break;
                        }
                    }
                }
            }
        }

        //キャンセル
        /** Will cancel the listening socket, and cause the thread to finish */
        public void cancel() {
            try {
                serverSocket.close();
            } catch (IOException e) {
            // それ以外の例外
            } catch (Exception h) {
            }
        }
    }

    //Bluetoothの接続要求(クライアント)(6)
    private class ConnectThread extends Thread {
        private BluetoothDevice device;
        private BluetoothSocket socket;

        //コンストラクタ
        public ConnectThread(BluetoothDevice device) {
            try {
                this.device=device;
                this.socket=device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
            }
        }

        //処理
        public void run() {
            adapter.cancelDiscovery();
            try {
                socket.connect();
            } catch (IOException e) {
                setState(STATE_LISTEN);
                try {
                    socket.close();
                } catch (IOException e2) {
                }
                BluetoothChatService.this.start();
                return;
            }
            synchronized (BluetoothChatService.this) {
                connectThread=null;
            }
            connected(socket,device);
        }

        //キャンセル
        public void cancel() {
            try {
                socket.close();
            } catch (IOException e) {
            }
        }
    }

    //Bluetooth接続完了後の処理(7)
    private class ConnectedThread extends Thread {
        private BluetoothSocket socket;
        private InputStream     input;
        private OutputStream    output;

        //コンストラクタ
        public ConnectedThread(BluetoothSocket socket) {
            try {
                this.socket=socket;
                this.input =socket.getInputStream();
                this.output=socket.getOutputStream();
            } catch (IOException e) {
            }
        }

        //処理
        public void run() {
            byte[] buf=new byte[1024];
            int bytes;
            while (true) {
                try {
                    bytes=input.read(buf);
                    handler.obtainMessage(Torianime.MSG_READ,
                        bytes,-1,buf).sendToTarget();
                } catch (IOException e) {
                    setState(STATE_LISTEN);
                    break;
                }
            }
        }

        //書き込み
        public void write(byte[] buf) {
            try {
                output.write(buf);
            } catch (IOException e) {
            }
        }

        //キャンセル
        public void cancel() {
            try {
                socket.close();
            } catch (IOException e) {
            }
        }
    }
}
