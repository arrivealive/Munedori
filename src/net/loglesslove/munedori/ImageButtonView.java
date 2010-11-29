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
	private Bitmap image; // �C���[�W

	public ImageButtonView(Context context, int color) {
		super(context);
//		setBackgroundColor(Color.WHITE);

		// �摜�̓ǂݍ���
		Resources r = context.getResources();
        // ��
        if (color == 0) {
        	image = BitmapFactory.decodeResource(r, R.drawable.white_button);
        } else {
        	image = BitmapFactory.decodeResource(r, R.drawable.orange_button);
        }
        setImageBitmap(image);
	}

}
