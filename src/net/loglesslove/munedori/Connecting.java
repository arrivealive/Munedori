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
    private BluetoothAdapter     btAdapter;//BT�A�_�v�^
    private BluetoothChatService chatService;
    private ArrayAdapter<String> devices;  //�f�o�C�X�S

    private int iAmEmigrator = 0;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFormat(PixelFormat.TRANSLUCENT);

        this.color = 0;
        int direction = 0; // 0:�s�� 1:����
        int partnerColor = 0; // ����̐F
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
        	this.color = Integer.parseInt(extras.getString("color"));
        	direction =  Integer.parseInt(extras.getString("direction"));
        	partnerColor = Integer.parseInt(extras.getString("partnerColor"));
        }
        wview = new WaitingView(this, this.color);

        wview.setOnClickListener(this);

        setContentView(wview);

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
	            Intent enableIntent = new Intent(
	                BluetoothAdapter.ACTION_REQUEST_ENABLE);
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
        if (chatService!=null) {
            if (chatService.getState()==BluetoothChatService.STATE_NONE) {
//            	chatService.stop();
                //Bluetooth�̐ڑ��҂�(�T�[�o)
                chatService.start();
            }
        }
    }

    //�A�v���j�����ɌĂ΂��
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (chatService!=null) chatService.stop();
    }

    //�`���b�g�T�[�o��������擾����n���h��
    private final Handler handler=new Handler() {
        //�n���h�����b�Z�[�W
        @Override
        public void handleMessage(Message msg) {
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
                	}
                    addText("�ڑ�����" + iAmEmigrator);
                    iAmEmigrator = 0;
                    break;
                case BluetoothChatService.STATE_CONNECTING:
                    addText("�ڑ���");break;
                case BluetoothChatService.STATE_LISTEN:
//                    addText("�ڑ��҂�");break;
                case BluetoothChatService.STATE_NONE:
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
                addText(new String(readBuf,0,msg.arg1));

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

                //Bluetooth�̐ڑ��v��(�N���C�A���g)
                BluetoothDevice device = btAdapter.getRemoteDevice(address);
                chatService.connect(device);

                Toast.makeText(this,address,Toast.LENGTH_SHORT).show();

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


}