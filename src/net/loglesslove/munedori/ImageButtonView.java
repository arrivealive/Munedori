package net.loglesslove.munedori;
import android.content.res.Resources;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class ImageButtonView extends ImageView {
	private Bitmap image; // イメージ

	public ImageButtonView(Context context, int color) {
		super(context);
//		setBackgroundColor(Color.WHITE);

		// 画像の読み込み
		Resources r = context.getResources();
        // 白
        if (color == 0) {
        	image = BitmapFactory.decodeResource(r, R.drawable.white_button);
        } else {
        	image = BitmapFactory.decodeResource(r, R.drawable.orange_button);
        }
        setImageBitmap(image);
	}

}
