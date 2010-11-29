package net.loglesslove.munedori;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ImageView;

	public class Munedori extends Activity  implements View.OnClickListener {

		private ImageView blueButton;
		private ImageView yellowButton;

	    private static final int BLUE_BIRD =0,YELLOW_BIRD=1;

	    /** Called when the activity is first created. */
	    @Override
	    public void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        requestWindowFeature(Window.FEATURE_NO_TITLE);

	        //レイアウトの生成
	        LinearLayout layout=new LinearLayout(this);
	        layout.setBackgroundColor(Color.rgb(255,255,255));
	        layout.setOrientation(LinearLayout.HORIZONTAL);
	        setContentView(layout);


	        Bitmap blueImage = BitmapFactory.decodeResource(getResources(), R.drawable.stage_menu_blue);
	        Bitmap yellowImage = BitmapFactory.decodeResource(getResources(), R.drawable.stage_menu_orange);

	        layout.setBackgroundResource(R.drawable.stage_menu);
	        layout.setPadding(0, 327, 0, 0);


	        blueButton = new ImageView(this);
	        blueButton.setImageBitmap(blueImage);
	        blueButton.setOnClickListener(this);
	        blueButton.setBackgroundColor(Color.TRANSPARENT);
	        blueButton.setPadding(20, 0, 1, 0);
	        setLLParams(blueButton);
	        layout.addView(blueButton);


	        yellowButton = new ImageView(this);
	        yellowButton.setImageBitmap(yellowImage);
	        yellowButton.setOnClickListener(this);
	        yellowButton.setBackgroundColor(Color.TRANSPARENT);
	        yellowButton.setPadding(20, 0, 1, 0);
	        setLLParams(yellowButton);
	        layout.addView(yellowButton);
	    }

	    public void onClick(View view) {
        	Intent intent;

        	if (view == blueButton) {
    			intent=new Intent(view.getContext(),net.loglesslove.munedori.Torianime.class);
    			intent.putExtra("color", BLUE_BIRD + "");
//                startActivityForResult(intent,RQ_SETUP);
    			startActivity(intent);

        	} else if (view == yellowButton) {
    			intent=new Intent(view.getContext(),net.loglesslove.munedori.Torianime.class);
    			intent.putExtra("color", YELLOW_BIRD + "");
                startActivity(intent);
        	}
	    }

	    //ライナーレイアウトのパラメータ指定
	    private static void setLLParams(View view) {
	        view.setLayoutParams(new LinearLayout.LayoutParams(
	            LinearLayout.LayoutParams.WRAP_CONTENT,
	            LinearLayout.LayoutParams.WRAP_CONTENT));
	    }

	    //ライナーレイアウトのパラメータ指定
	    private static void setLLParams(View view,int w,int h) {
	        view.setLayoutParams(new LinearLayout.LayoutParams(w,h));
	    }


	}