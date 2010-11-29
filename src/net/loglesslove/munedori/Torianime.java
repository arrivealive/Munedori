package net.loglesslove.munedori;


import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.DeadObjectException;
import android.os.Handler;
import android.os.Message;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.Window;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

public class Torianime extends Activity implements View.OnClickListener, Runnable {
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

    private Thread watchingThread;
    private boolean previousWaiting = true;

    private ImageButtonView ibview;


    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFormat(PixelFormat.TRANSLUCENT);

        this.color = 0;
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
        	this.color = Integer.parseInt(extras.getString("color"));
        }

        //レイアウトの生成
        FrameLayout layout=new FrameLayout(this);
        layout.setBackgroundColor(Color.rgb(255,255,255));
 //       layout.setOrientation(LinearLayout.HORIZONTAL);
        setContentView(layout);

        wview = new WaitingView(this, this.color);
//        wview.setOnClickListener(this);
        wview.setLayoutParams(new LinearLayout.LayoutParams(
	            LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        layout.addView(wview);

        ibview = new ImageButtonView(this, this.color);
//        ibview = new ImageView(this);
//		// 画像の読み込み
//		Resources r = getResources();
//		Bitmap image;
//        // 白
//        if (color == 0) {
//        	image = BitmapFactory.decodeResource(r, R.drawable.white_button);
//        } else {
//        	image = BitmapFactory.decodeResource(r, R.drawable.orange_button);
//        }
//        ibview.setImageBitmap(image);

        ibview.setOnClickListener(this);
//        ibview.setBackgroundColor(Color.TRANSPARENT);
//        connectionButton.setPadding(20, 0, 1, 0);
        ibview.setLayoutParams(new LinearLayout.LayoutParams(
	            LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        layout.addView(ibview);

        watchingThread = new Thread(this);
        watchingThread.start();

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
	            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
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
        // BTが有効でない
//        if (!btAdapter.isEnabled()) {
//
//        } else {
	        if (chatService!=null) {
	            if (chatService.getState()==BluetoothChatService.STATE_NONE) {
	//            	chatService.stop();
	                //Bluetoothの接続待ち(サーバ)
	                chatService.start();
	            }
	        }
//        }
    }

    //アプリ破棄時に呼ばれる
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (chatService!=null) chatService.stop();
        chatService = null;
        watchingThread = null;

    }

    //チャットサーバから情報を取得するハンドラ
    private final Handler handler=new Handler() {
        //ハンドルメッセージ
        @Override
        public void handleMessage(Message msg) {
        	String partnerName = null;
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
                	} else {
                        partnerName = chatService.getPartnerName();
                	}
//                    addText("接続完了" + iAmEmigrator);
                    addText("接続完了");

                    if (partnerName != null) {
//                        addText(partnerName + "から遊びにきました。");
                    }

                    iAmEmigrator = 0;
                    break;
                case BluetoothChatService.STATE_CONNECTING:
                    addText("接続中");break;
                case BluetoothChatService.STATE_LISTEN:
//                    addText("接続待ち");break;
                case BluetoothChatService.STATE_NONE:
//                    addText("接続待ち");
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
//                addText(new String(readBuf,0,msg.arg1));

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
                String name = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_NAME);

                //Bluetoothの接続要求(クライアント)
                BluetoothDevice device = btAdapter.getRemoteDevice(address);
                chatService.connect(device);

                Toast.makeText(this,name + "へ遊びにいきました。",Toast.LENGTH_SHORT).show();

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


    // Viewの状態を監視
	@Override
	public void run() {
		// TODO 自動生成されたメソッド・スタブ
		boolean nowWaiting = true;
//		Toast.makeText(this,"...",Toast.LENGTH_SHORT).show();

    	// ずっとアニメーション
        while(watchingThread!=null) {

        	nowWaiting = wview.isWaiting();
			if (nowWaiting != previousWaiting) {
//				Toast.makeText(this,"！",Toast.LENGTH_SHORT).show();
				// 止まった
				if (!previousWaiting) {
					// チャットしていた場合は止める
//			        if (chatService!=null) chatService.stop();
				}
			}

			if (ibview != null) {
				if (nowWaiting) {
//					ibview.setVisibility(View.VISIBLE);
					ibview.setClickable(true);
				} else {
//					ibview.setVisibility(View.INVISIBLE);
					ibview.setClickable(false);
				}
			}

			previousWaiting = nowWaiting;

			//			Log.d("waiting", nowWaiting ? "true": "false");
	        //スリープ(5)
	        try {
	            Thread.sleep(500);
	        } catch (Exception e) {
	        }
        }
	}


}