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

//端末検索アクティビティ
public class DeviceListActivity extends Activity
    implements AdapterView.OnItemClickListener, View.OnClickListener {
    public static String EXTRA_DEVICE_ADDRESS="device_address";
    public static String EXTRA_DEVICE_NAME="device_name";

    private BluetoothAdapter     btAdapter;//BTアダプタ
    private ArrayAdapter<String> devices;  //デバイス郡

    Button button;

    //アプリ生成時に呼ばれる
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setResult(Activity.RESULT_CANCELED);

        Log.d("DeviceListActivity", "created");

        //レイアウトの生成
        LinearLayout layout=new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        setContentView(layout);

        TextView textView = new TextView(this);
        setLLParams(textView);
        textView.setBackgroundColor(android.graphics.Color.BLACK);
        textView.setTextColor(android.graphics.Color.WHITE);
        textView.setPadding(5, 10, 5, 10);
        layout.addView(textView);
        textView.setText("すれ違い通信を行う「飼い主」を選択してください");
        textView.setTextSize(13.0f);


        //デバイス
        devices = new ArrayAdapter<String>(this,R.layout.rowdata);

        //リストビューの生成
        ListView listView=new ListView(this);
        setLLParams(listView);
        listView.setAdapter(devices);
        layout.addView(listView);
        listView.setOnItemClickListener(this);

        // ボタンの作成
        button = new Button(this);
        button.setText("戻る");
        button.setOnClickListener(this);
        setLLParams(button);
        layout.addView(button);

        //ブロードキャストレシーバーの追加
        IntentFilter filter;
        filter=new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(receiver,filter);
        filter=new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(receiver,filter);

        //Bluetooth端末の検索開始(1)
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

    //アプリ破棄時に呼ばれる
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (btAdapter!=null) btAdapter.cancelDiscovery();
        this.unregisterReceiver(receiver);
    }

    //クリック時に呼ばれる
    public void onItemClick(AdapterView<?>av,View v,int arg2,long arg3) {
        //Bluetooth端末の検索キャンセル
        btAdapter.cancelDiscovery();

        //戻り値の指定
        String info   =((TextView) v).getText().toString();
        String address=info.substring(info.length()-17);
        String name=info.substring(0, info.indexOf(System.getProperty("line.separator")));
        Intent intent =new Intent();
        intent.putExtra(EXTRA_DEVICE_ADDRESS,address);
        intent.putExtra(EXTRA_DEVICE_NAME,name);
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

//    //オプションメニュー生成時に呼ばれる
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        super.onCreateOptionsMenu(menu);
//        MenuItem item0=menu.add(0,0,0,"端末検索を中止");
//        item0.setIcon(android.R.drawable.ic_search_category_default);
////        MenuItem item1=menu.add(0,1,0,"発見有効");
////        item1.setIcon(android.R.drawable.ic_menu_call);
//        return true;
//    }
//
//    //オプションメニュー選択時に呼ばれる
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        switch (item.getItemId()) {
//        //検索
//        case 0:
//            //Bluetooth端末の検索キャンセル
//            btAdapter.cancelDiscovery();
//
//            //戻り値の指定
//            Intent intent =new Intent();
//            intent.putExtra(EXTRA_DEVICE_ADDRESS, "");
//            setResult(Activity.RESULT_CANCELED, intent);
//            finish();
//
//            return true;
//        }
//        return false;
//    }

    //ライナーレイアウトのパラメータ指定
    private static void setLLParams(View view) {
        view.setLayoutParams(new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.FILL_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT));
    }

    //ブロードキャストレシーバー
    private final BroadcastReceiver receiver=new BroadcastReceiver() {
        //Bluetooth端末の検索結果の取得(3)
        @Override
        public void onReceive(Context context, Intent intent) {
            String action=intent.getAction();

            //Bluetooth端末発見
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device=intent.
                    getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device.getBondState()!=BluetoothDevice.BOND_BONDED) {
                    devices.add(device.getName()+
                        System.getProperty("line.separator")+
                        device.getAddress());
                }
            }
            //Bluetooth端末検索完了
            else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                android.util.Log.e("","Bluetooth端末検索完了");
            }
        }
    };

	@Override
	public void onClick(View v) {
		// TODO 自動生成されたメソッド・スタブ
		if (v == button) {
            //Bluetooth端末の検索キャンセル
            btAdapter.cancelDiscovery();

            //戻り値の指定
            Intent intent =new Intent();
            intent.putExtra(EXTRA_DEVICE_ADDRESS, "");
            setResult(Activity.RESULT_CANCELED, intent);
            finish();

		}
	}
}