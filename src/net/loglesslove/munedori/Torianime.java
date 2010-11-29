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
    private BluetoothAdapter     btAdapter;//BT�A�_�v�^
    private BluetoothChatService chatService;
    private ArrayAdapter<String> devices;  //�f�o�C�X�S

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

        //���C�A�E�g�̐���
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
//		// �摜�̓ǂݍ���
//		Resources r = getResources();
//		Bitmap image;
//        // ��
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

        // ��������Bluetooth
        btAdapter = BluetoothAdapter.getDefaultAdapter();
        if (btAdapter == null) {
            // Device does not support Bluetooth
        	 Toast.makeText(this,"Bluetooth���T�|�[�g����Ă��܂���",
                 	Toast.LENGTH_SHORT).show();
//                 finish();

        }

    }



    //�A�v���J�n���ɌĂ΂��
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

    //�A�v�����W���[�����ɌĂ΂��
    @Override
    public synchronized void onResume() {
        super.onResume();
        // BT���L���łȂ�
//        if (!btAdapter.isEnabled()) {
//
//        } else {
	        if (chatService!=null) {
	            if (chatService.getState()==BluetoothChatService.STATE_NONE) {
	//            	chatService.stop();
	                //Bluetooth�̐ڑ��҂�(�T�[�o)
	                chatService.start();
	            }
	        }
//        }
    }

    //�A�v���j�����ɌĂ΂��
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (chatService!=null) chatService.stop();
        chatService = null;
        watchingThread = null;

    }

    //�`���b�g�T�[�o��������擾����n���h��
    private final Handler handler=new Handler() {
        //�n���h�����b�Z�[�W
        @Override
        public void handleMessage(Message msg) {
        	String partnerName = null;
            switch (msg.what) {
            //��ԕύX
            case MSG_STATE_CHANGE:
                switch (msg.arg1) {
                case BluetoothChatService.STATE_CONNECTED:
                	//String message = chatService.getColor() + "";
                	if (iAmEmigrator == 1) {
	                	String message = color + "";
	                    chatService.write(message.getBytes());
	                    chatService.start(); // �ĊJ
                	} else {
                        partnerName = chatService.getPartnerName();
                	}
//                    addText("�ڑ�����" + iAmEmigrator);
                    addText("�ڑ�����");

                    if (partnerName != null) {
//                        addText(partnerName + "����V�тɂ��܂����B");
                    }

                    iAmEmigrator = 0;
                    break;
                case BluetoothChatService.STATE_CONNECTING:
                    addText("�ڑ���");break;
                case BluetoothChatService.STATE_LISTEN:
//                    addText("�ڑ��҂�");break;
                case BluetoothChatService.STATE_NONE:
//                    addText("�ڑ��҂�");
                	addText("���ڑ�");
//                	if (iAmEmigrator == 1) {
//                		chatService.start(); // �ĊJ
//                	}
                    iAmEmigrator = 0;
                    break;
                }
                break;
            //���b�Z�[�W��M
            case MSG_READ:
                addText("��M��");
                byte[] readBuf=(byte[])msg.obj;
//                addText(new String(readBuf,0,msg.arg1));

                String partnersColor = new String(readBuf,0,msg.arg1);

	    		wview.immigrate(Integer.parseInt(partnersColor));
	    		iAmEmigrator = 0;
	    		chatService.start(); // �ĊJ
                break;
            }
        }
    };
    //��M�e�L�X�g�̒ǉ�
    private void addText(final String text) {
    	Toast.makeText(this,text,Toast.LENGTH_SHORT).show();
        //�n���h���ɂ�郆�[�U�[�C���^�t�F�[�X����
        handler.post(new Runnable(){
            public void run() {

//                lblReceive.setText(text+
//                    System.getProperty("line.separator")+
//                    lblReceive.getText());
            }
        });
    }


    //�A�v�����A���ɌĂ΂��
    public void onActivityResult(int requestCode,int resultCode,Intent data) {
        switch (requestCode) {
        //�[������
        case RQ_CONNECT_DEVICE:
            if (resultCode==Activity.RESULT_OK) {
            	// �A�h���X�𕶎���Ƃ��Ď擾����
                String address = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                String name = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_NAME);

                //Bluetooth�̐ڑ��v��(�N���C�A���g)
                BluetoothDevice device = btAdapter.getRemoteDevice(address);
                chatService.connect(device);

                Toast.makeText(this,name + "�֗V�тɂ����܂����B",Toast.LENGTH_SHORT).show();

                this.iAmEmigrator = 1;

	    		wview.emigrate();
            }
            break;
        //BT��L���ɂ���
        case RQ_ENABLE_BT:
            if (resultCode==Activity.RESULT_OK) {
                chatService=new BluetoothChatService(this,handler);

            } else {
//                Toast.makeText(this,"Bluetooth���L���ł͂���܂���" + resultCode,
//                	Toast.LENGTH_SHORT).show();
//                finish();
            }
            break;
        }
    }



    public void onClick(View v) {
    	if (btAdapter != null && btAdapter.isEnabled()) {
    		// �T��
    		startDiscovery();
        	// �����L���ɂ���
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



    //����Bluetooth�[������̔�����L����
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


    // View�̏�Ԃ��Ď�
	@Override
	public void run() {
		// TODO �����������ꂽ���\�b�h�E�X�^�u
		boolean nowWaiting = true;
//		Toast.makeText(this,"...",Toast.LENGTH_SHORT).show();

    	// �����ƃA�j���[�V����
        while(watchingThread!=null) {

        	nowWaiting = wview.isWaiting();
			if (nowWaiting != previousWaiting) {
//				Toast.makeText(this,"�I",Toast.LENGTH_SHORT).show();
				// �~�܂���
				if (!previousWaiting) {
					// �`���b�g���Ă����ꍇ�͎~�߂�
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
	        //�X���[�v(5)
	        try {
	            Thread.sleep(500);
	        } catch (Exception e) {
	        }
        }
	}


}