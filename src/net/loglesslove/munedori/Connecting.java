package net.loglesslove.munedori;


import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.graphics.PixelFormat;
import android.view.Window;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

public class Connecting extends Activity implements View.OnClickListener {
	private static final int REQUEST_ENABLE_BT = 0;
	private static final int RQ_ENABLE_BT = 2;
	private static final int RQ_CONNECT_DEVICE = 1;
	protected static final int MSG_STATE_CHANGE = 1;
	protected static final int MSG_READ = 2;
	private int color;
	private WaitingView wview;
    private BluetoothAdapter     btAdapter;//BTアダプタ
    private BluetoothChatService chatService;
    private ArrayAdapter<String> devices;  //デバイス郡

    private int iAmEmigrator = 0;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFormat(PixelFormat.TRANSLUCENT);

        this.color = 0;
        int direction = 0; // 0:行く 1:来る
        int partnerColor = 0; // 相手の色
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
        	this.color = Integer.parseInt(extras.getString("color"));
        	direction =  Integer.parseInt(extras.getString("direction"));
        	partnerColor = Integer.parseInt(extras.getString("partnerColor"));
        }
        wview = new WaitingView(this, this.color);

        wview.setOnClickListener(this);

        setContentView(wview);

        // ここからBluetooth
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        if (btAdapter == null) {
            // Device does not support Bluetooth
        	 Toast.makeText(this,"Bluetoothがサポートされていません",
                 	Toast.LENGTH_SHORT).show();
//                 finish();

        }

    }



    //アプリ開始時に呼ばれる
    @Override
    public void onStart() {
        super.onStart();

        if (btAdapter != null) {

	        if (!btAdapter.isEnabled()) {
	            Intent enableIntent = new Intent(
	                BluetoothAdapter.ACTION_REQUEST_ENABLE);
	            startActivityForResult(enableIntent,RQ_ENABLE_BT);
	        } else {
	            if (chatService == null) chatService = new BluetoothChatService(this,handler);
	            chatService.setColor(this.color);

	        }

        }
    }

    //アプリレジューム時に呼ばれる
    @Override
    public synchronized void onResume() {
        super.onResume();
        if (chatService!=null) {
            if (chatService.getState()==BluetoothChatService.STATE_NONE) {
//            	chatService.stop();
                //Bluetoothの接続待ち(サーバ)
                chatService.start();
            }
        }
    }

    //アプリ破棄時に呼ばれる
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (chatService!=null) chatService.stop();
    }

    //チャットサーバから情報を取得するハンドラ
    private final Handler handler=new Handler() {
        //ハンドルメッセージ
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            //状態変更
            case MSG_STATE_CHANGE:
                switch (msg.arg1) {
                case BluetoothChatService.STATE_CONNECTED:
                	//String message = chatService.getColor() + "";
                	if (iAmEmigrator == 1) {
	                	String message = color + "";
	                    chatService.write(message.getBytes());
	                    chatService.start(); // 再開
                	}
                    addText("接続完了" + iAmEmigrator);
                    iAmEmigrator = 0;
                    break;
                case BluetoothChatService.STATE_CONNECTING:
                    addText("接続中");break;
                case BluetoothChatService.STATE_LISTEN:
//                    addText("接続待ち");break;
                case BluetoothChatService.STATE_NONE:
                    addText("未接続");
//                	if (iAmEmigrator == 1) {
//                		chatService.start(); // 再開
//                	}
                    iAmEmigrator = 0;
                    break;
                }
                break;
            //メッセージ受信
            case MSG_READ:
                addText("受信中");
                byte[] readBuf=(byte[])msg.obj;
                addText(new String(readBuf,0,msg.arg1));

                String partnersColor = new String(readBuf,0,msg.arg1);

	    		wview.immigrate(Integer.parseInt(partnersColor));
	    		iAmEmigrator = 0;
	    		chatService.start(); // 再開
                break;
            }
        }
    };
    //受信テキストの追加
    private void addText(final String text) {
    	Toast.makeText(this,text,Toast.LENGTH_SHORT).show();
        //ハンドラによるユーザーインタフェース操作
        handler.post(new Runnable(){
            public void run() {

//                lblReceive.setText(text+
//                    System.getProperty("line.separator")+
//                    lblReceive.getText());
            }
        });
    }


    //アプリ復帰時に呼ばれる
    public void onActivityResult(int requestCode,int resultCode,Intent data) {
        switch (requestCode) {
        //端末検索
        case RQ_CONNECT_DEVICE:
            if (resultCode==Activity.RESULT_OK) {
            	// アドレスを文字列として取得する
                String address = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);

                //Bluetoothの接続要求(クライアント)
                BluetoothDevice device = btAdapter.getRemoteDevice(address);
                chatService.connect(device);

                Toast.makeText(this,address,Toast.LENGTH_SHORT).show();

                this.iAmEmigrator = 1;

	    		wview.emigrate();
            }
            break;
        //BTを有効にした
        case RQ_ENABLE_BT:
            if (resultCode==Activity.RESULT_OK) {
                chatService=new BluetoothChatService(this,handler);

            } else {
//                Toast.makeText(this,"Bluetoothが有効ではありません" + resultCode,
//                	Toast.LENGTH_SHORT).show();
//                finish();
            }
            break;
        }
    }



    public void onClick(View v) {
    	if (btAdapter != null && btAdapter.isEnabled()) {
    		// 探す
    		startDiscovery();
        	// 発見有効にする
    		ensureDiscoverable();
    	} else {

	    	int ran = (int)(Math.random() * 2);
	    	int ran2 = (int)(Math.random() * 2);
	    	if (ran == 0) {
	    		wview.emigrate();
	    	} else {
	    		wview.immigrate(ran2);
	    	}
    	}
    }

//    @Override
//    public void onStop() {
//    	int a = 2;a++;
//    	this.wview = null;
//
//    }



    //他のBluetooth端末からの発見を有効化
    private void ensureDiscoverable() {
        if (btAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent=new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
        }
    }


    private void startDiscovery() {
    	Intent serverIntent=new Intent(this,DeviceListActivity.class);
        startActivityForResult(serverIntent,RQ_CONNECT_DEVICE);

    }


}