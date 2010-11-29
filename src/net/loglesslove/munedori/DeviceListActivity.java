package net.loglesslove.munedori;
import java.util.Set;


import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

//�[�������A�N�e�B�r�e�B
public class DeviceListActivity extends Activity
    implements AdapterView.OnItemClickListener, View.OnClickListener {
    public static String EXTRA_DEVICE_ADDRESS="device_address";
    public static String EXTRA_DEVICE_NAME="device_name";

    private BluetoothAdapter     btAdapter;//BT�A�_�v�^
    private ArrayAdapter<String> devices;  //�f�o�C�X�S

    Button button;

    //�A�v���������ɌĂ΂��
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setResult(Activity.RESULT_CANCELED);

        Log.d("DeviceListActivity", "created");

        //���C�A�E�g�̐���
        LinearLayout layout=new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        setContentView(layout);

        TextView textView = new TextView(this);
        setLLParams(textView);
        textView.setBackgroundColor(android.graphics.Color.BLACK);
        textView.setTextColor(android.graphics.Color.WHITE);
        textView.setPadding(5, 10, 5, 10);
        layout.addView(textView);
        textView.setText("����Ⴂ�ʐM���s���u������v��I�����Ă�������");
        textView.setTextSize(13.0f);


        //�f�o�C�X
        devices = new ArrayAdapter<String>(this,R.layout.rowdata);

        //���X�g�r���[�̐���
        ListView listView=new ListView(this);
        setLLParams(listView);
        listView.setAdapter(devices);
        layout.addView(listView);
        listView.setOnItemClickListener(this);

        // �{�^���̍쐬
        button = new Button(this);
        button.setText("�߂�");
        button.setOnClickListener(this);
        setLLParams(button);
        layout.addView(button);

        //�u���[�h�L���X�g���V�[�o�[�̒ǉ�
        IntentFilter filter;
        filter=new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(receiver,filter);
        filter=new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(receiver,filter);

        //Bluetooth�[���̌����J�n(1)
        btAdapter=BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> pairedDevices=btAdapter.getBondedDevices();
        if (pairedDevices.size()>0) {
            for (BluetoothDevice device:pairedDevices) {
                devices.add(device.getName()+
                    System.getProperty("line.separator")+
                    device.getAddress());
            }
        }
        if (btAdapter.isDiscovering()) btAdapter.cancelDiscovery();
        btAdapter.startDiscovery();
    }

    //�A�v���j�����ɌĂ΂��
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (btAdapter!=null) btAdapter.cancelDiscovery();
        this.unregisterReceiver(receiver);
    }

    //�N���b�N���ɌĂ΂��
    public void onItemClick(AdapterView<?>av,View v,int arg2,long arg3) {
        //Bluetooth�[���̌����L�����Z��
        btAdapter.cancelDiscovery();

        //�߂�l�̎w��
        String info   =((TextView) v).getText().toString();
        String address=info.substring(info.length()-17);
        String name=info.substring(0, info.indexOf(System.getProperty("line.separator")));
        Intent intent =new Intent();
        intent.putExtra(EXTRA_DEVICE_ADDRESS,address);
        intent.putExtra(EXTRA_DEVICE_NAME,name);
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

//    //�I�v�V�������j���[�������ɌĂ΂��
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        super.onCreateOptionsMenu(menu);
//        MenuItem item0=menu.add(0,0,0,"�[�������𒆎~");
//        item0.setIcon(android.R.drawable.ic_search_category_default);
////        MenuItem item1=menu.add(0,1,0,"�����L��");
////        item1.setIcon(android.R.drawable.ic_menu_call);
//        return true;
//    }
//
//    //�I�v�V�������j���[�I�����ɌĂ΂��
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        switch (item.getItemId()) {
//        //����
//        case 0:
//            //Bluetooth�[���̌����L�����Z��
//            btAdapter.cancelDiscovery();
//
//            //�߂�l�̎w��
//            Intent intent =new Intent();
//            intent.putExtra(EXTRA_DEVICE_ADDRESS, "");
//            setResult(Activity.RESULT_CANCELED, intent);
//            finish();
//
//            return true;
//        }
//        return false;
//    }

    //���C�i�[���C�A�E�g�̃p�����[�^�w��
    private static void setLLParams(View view) {
        view.setLayoutParams(new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.FILL_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT));
    }

    //�u���[�h�L���X�g���V�[�o�[
    private final BroadcastReceiver receiver=new BroadcastReceiver() {
        //Bluetooth�[���̌������ʂ̎擾(3)
        @Override
        public void onReceive(Context context, Intent intent) {
            String action=intent.getAction();

            //Bluetooth�[������
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device=intent.
                    getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device.getBondState()!=BluetoothDevice.BOND_BONDED) {
                    devices.add(device.getName()+
                        System.getProperty("line.separator")+
                        device.getAddress());
                }
            }
            //Bluetooth�[����������
            else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                android.util.Log.e("","Bluetooth�[����������");
            }
        }
    };

	@Override
	public void onClick(View v) {
		// TODO �����������ꂽ���\�b�h�E�X�^�u
		if (v == button) {
            //Bluetooth�[���̌����L�����Z��
            btAdapter.cancelDiscovery();

            //�߂�l�̎w��
            Intent intent =new Intent();
            intent.putExtra(EXTRA_DEVICE_ADDRESS, "");
            setResult(Activity.RESULT_CANCELED, intent);
            finish();

		}
	}
}