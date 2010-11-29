package net.loglesslove.munedori;
import java.util.Hashtable;
import java.util.ArrayList;

import android.content.res.Resources;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;


//サーフェイスビューの利用
public class ConnectingView extends SurfaceView
    implements SurfaceHolder.Callback,Runnable {
    private SurfaceHolder holder;//サーフェイスホルダー
    private Thread        thread;//スレッド

    private Bitmap image;//イメージ
    private Bitmap image2;//イメージ
    private Bitmap background;

    private final int STATUS_STOP = 0; // 静止状態
    private final int STATUS_MOVE = 1; // 移動中
    private final int STATUS_MOVE_LEFT = 2; // 左に移動中
    private final int STATUS_MOVE_RIGHT = 3; // 右に移動中
    private final int STATUS_TWEET = 4; // さえずり
    private final int STATUS_WINK = 5;
    private final int STATUS_EMIGRATE = 6;
    private final int STATUS_PRE_EMIGRATE = 7;
    private final int STATUS_IMMIGRATE = 8;
    private final int STATUS_PRE_IMMIGRATE = 9;

    private final int GETA_X = 0;
    private final int GETA_Y = -20;

    private final int ORIGIN_X = 99;
    private final int ORIGIN_Y = 338;

    private Hashtable<Integer, Status> winkScenario; // まばたきパターン
    private Hashtable<Integer, Status> moveLeftScenario; // 左に移動パターン
    private Hashtable<Integer, Status> moveRightScenario; // 右に移動パターン
    private Hashtable<Integer, Status> tweetScenario; // さえずりパターン

    private Hashtable<Integer, Status> emigrateScenario; // 出ていくシナリオ
    private Hashtable<Integer, Status> immigrateScenario; // 入ってくるシナリオ

    private Hashtable<String, Bitmap> images;

    private int color;

    private int frame;

    private int status = 0;

    private int    px = ORIGIN_X; //X座標
    private int    py = ORIGIN_Y; //Y座標

    private int partnersColor = 0; // 0:white, 1:orange
    private int    px2 = 0; //X座標
    private int    py2 = 0; //Y座標

    //    private Branch branch;

    //コンストラクタ
    public ConnectingView(Context context, int color) {
    	super(context);

    	this.color = color;

        //サーフェイスホルダーの生成(2)
        holder=getHolder();
        holder.addCallback(this);
        holder.setFixedSize(getWidth(),getHeight());

        //画像の読み込み
        this._initImages(color);

        int i = 0;

        winkScenario = new Hashtable<Integer, Status>();
        winkScenario.put(i++, new Status("01", 1, ORIGIN_X, ORIGIN_Y));
        winkScenario.put(i++, new Status("02", 1, ORIGIN_X, ORIGIN_Y));
        winkScenario.put(i++, new Status("01", 1, ORIGIN_X, ORIGIN_Y));

        i = 0;
        moveLeftScenario = new Hashtable<Integer, Status>();
        moveLeftScenario.put(i++, new Status("01", 1, ORIGIN_X, ORIGIN_Y));
        moveLeftScenario.put(i++, new Status("03", 2, 58, 364));
        moveLeftScenario.put(i++, new Status("04", 2, 31, 382));
        moveLeftScenario.put(i++, new Status("01", 10, ORIGIN_X-100, ORIGIN_Y+63));
        moveLeftScenario.put(i++, new Status("09", 2, 90-77, 314+52)); // TODO
        moveLeftScenario.put(i++, new Status("10", 2, 105-74, 311+44));
        moveLeftScenario.put(i++, new Status("09", 1, 90, 314));
        moveLeftScenario.put(i++, new Status("01", 1, ORIGIN_X, ORIGIN_Y));

        i = 0;
        moveRightScenario = new Hashtable<Integer, Status>();
        moveRightScenario.put(i++, new Status("01", 1, ORIGIN_X, 338));
        moveRightScenario.put(i++, new Status("09", 1));
        moveRightScenario.put(i++, new Status("10", 1));
        moveRightScenario.put(i++, new Status("10", 1)); // TODO
        moveRightScenario.put(i++, new Status("09", 1));
        moveRightScenario.put(i++, new Status("01", 1, ORIGIN_X, 338));

        i = 0;
        tweetScenario = new Hashtable<Integer, Status>();
        tweetScenario.put(i++, new Status("01", 1, ORIGIN_X, ORIGIN_Y));
        tweetScenario.put(i++, new Status("05", 3, 82, ORIGIN_Y));
        tweetScenario.put(i++, new Status("01", 3, ORIGIN_X, ORIGIN_Y));
        tweetScenario.put(i++, new Status("05", 3, 82, ORIGIN_Y));
        tweetScenario.put(i++, new Status("01", 1, ORIGIN_X, ORIGIN_Y));


        i = 0;
        emigrateScenario = new Hashtable<Integer, Status>();
        emigrateScenario.put(i++, new Status("01", 1, ORIGIN_X, ORIGIN_Y));
        emigrateScenario.put(i++, new Status("06", 3, 108, 377));
        emigrateScenario.put(i++, new Status("07", 1, 18, 289));
        emigrateScenario.put(i++, new Status("08", 1, 0, 179));
        emigrateScenario.put(i++, new Status("07", 1, 18-318, 289-142)); // 7
        emigrateScenario.put(i++, new Status("00", 74+50));
        emigrateScenario.put(i++, new Status("01", 1, ORIGIN_X, ORIGIN_Y));

        i = 0;
        immigrateScenario = new Hashtable<Integer, Status>();
        immigrateScenario.put(i++, new Status("01", 1, ORIGIN_X, ORIGIN_Y));
        immigrateScenario.put(i++, new Status("03", 2, 58, 364));
        immigrateScenario.put(i++, new Status("04", 2, 31, 382));
        immigrateScenario.put(i++, new Status("01", 1, ORIGIN_X-100, ORIGIN_Y+63));
//        immigrateScenario.put(i++, new Status("22", 1)); // 0
        immigrateScenario.put(i++, new Status("11", 1, 6, 394, new Status("22", 1, 351, 160))); // 5
//        immigrateScenario.put(i++, new Status("23", 1));
        immigrateScenario.put(i++, new Status("11", 1, 6, 394, new Status("23", 1, 225, 170))); // 5
        immigrateScenario.put(i++, new Status("11", 3, 6, 394, new Status("11", 1, 243, 209))); // 5
        immigrateScenario.put(i++, new Status("12", 7, 22, 358, new Status("12", 1, 235, 247)));
        immigrateScenario.put(i++, new Status("13", 3, 15, 364, new Status("13", 1, 223, 245)));
//        immigrateScenario.put(i++, new Status("12", 7, 15, -9)); // 22
        immigrateScenario.put(i++, new Status("12", 7, 22, 358, new Status("12", 1, 235, 247))); //向かい合う
//        immigrateScenario.put(i++, new Status("14", 3, 15, -9));
        immigrateScenario.put(i++, new Status("12", 3, 22, 358, new Status("14", 1, 219, 253))); //相手口をあける
//        immigrateScenario.put(i++, new Status("12", 7, 15, -9));
        immigrateScenario.put(i++, new Status("12", 7, 22, 358, new Status("12", 1, 235, 247))); //向かい合う
        immigrateScenario.put(i++, new Status("15", 3, 0, 356, new Status("12", 1, 235, 247))); // 自分口をあける
//        immigrateScenario.put(i++, new Status("12", 7, 15, -9));
        immigrateScenario.put(i++, new Status("12", 7, 22, 358, new Status("12", 1, 235, 247))); //向かい合う
//        immigrateScenario.put(i++, new Status("17", 5));
        immigrateScenario.put(i++, new Status("15", 5, 0, 356, new Status("14", 1, 219, 253))); // 両方口をあける
//        immigrateScenario.put(i++, new Status("12", 20, 15, -9));
        immigrateScenario.put(i++, new Status("12", 20, 22, 358, new Status("12", 1, 235, 247))); //向かい合う
//        immigrateScenario.put(i++, new Status("19", 3)); // 70
        immigrateScenario.put(i++, new Status("11", 3, 6, 394, new Status("19", 1, 235, 277))); // しゃがみこむ
//        immigrateScenario.put(i++, new Status("20", 1));
        immigrateScenario.put(i++, new Status("11", 1, 6, 394, new Status("20", 1, 96, 175))); // 飛ぶ
 //       immigrateScenario.put(i++, new Status("21", 1)); // 72
        immigrateScenario.put(i++, new Status("11", 1, 6, 394, new Status("21", 1, 27, 136))); // 飛ぶ2
        immigrateScenario.put(i++, new Status("09", 2, 90-77, 314+52)); // TODO
        immigrateScenario.put(i++, new Status("10", 2, 105-74, 311+44));
        immigrateScenario.put(i++, new Status("09", 1, 90, 314));
        immigrateScenario.put(i++, new Status("01", 1, ORIGIN_X, ORIGIN_Y));


        frame = 0;

    }


    private void _initImages(int color) {
        //画像の読み込み
        Resources r=getResources();
        image2 = null;

        if (color == 0) {
	        background = BitmapFactory.decodeResource(r,R.drawable.bg_white);
	        image=BitmapFactory.decodeResource(r,R.drawable.stage_white1);

	        images = new Hashtable<String, Bitmap>();
	        images.put("01", BitmapFactory.decodeResource(r,R.drawable.stage_white1));
	        images.put("02", BitmapFactory.decodeResource(r,R.drawable.stage_white2));
	        images.put("03", BitmapFactory.decodeResource(r,R.drawable.stage_white3));
	        images.put("04", BitmapFactory.decodeResource(r,R.drawable.stage_white4));
	        images.put("05", BitmapFactory.decodeResource(r,R.drawable.stage_white5));
	        images.put("06", BitmapFactory.decodeResource(r,R.drawable.stage_white6));
	        images.put("07", BitmapFactory.decodeResource(r,R.drawable.stage_white7));
	        images.put("08", BitmapFactory.decodeResource(r,R.drawable.stage_white8));
	        images.put("09", BitmapFactory.decodeResource(r,R.drawable.stage_white9));
	        images.put("10", BitmapFactory.decodeResource(r,R.drawable.stage_white10));
	        images.put("11", BitmapFactory.decodeResource(r,R.drawable.stage_white11_2));
	        images.put("12", BitmapFactory.decodeResource(r,R.drawable.stage_white12_2));
	        images.put("13", BitmapFactory.decodeResource(r,R.drawable.stage_white13_2));
	        images.put("14", BitmapFactory.decodeResource(r,R.drawable.stage_white14_2));
	        images.put("15", BitmapFactory.decodeResource(r,R.drawable.stage_white15_2));
//	        images.put("17", BitmapFactory.decodeResource(r,R.drawable.stage_white17_2));
//	        images.put("19", BitmapFactory.decodeResource(r,R.drawable.stage_white19_2));
//	        images.put("20", BitmapFactory.decodeResource(r,R.drawable.stage_white20_2));
//	        images.put("21", BitmapFactory.decodeResource(r,R.drawable.stage_white21_2));
//	        images.put("22", BitmapFactory.decodeResource(r,R.drawable.stage_white22_2));
//	        images.put("23", BitmapFactory.decodeResource(r,R.drawable.stage_white23_2));
	        images.put("00", BitmapFactory.decodeResource(r,R.drawable.noimage));

	        images.put("11_0", BitmapFactory.decodeResource(r,R.drawable.stage_orange11_1));
	        images.put("12_0", BitmapFactory.decodeResource(r,R.drawable.stage_orange12_1));
	        images.put("13_0", BitmapFactory.decodeResource(r,R.drawable.stage_orange13_1));
	        images.put("14_0", BitmapFactory.decodeResource(r,R.drawable.stage_orange14_1));
	        images.put("19_0", BitmapFactory.decodeResource(r,R.drawable.stage_orange19_1));
	        images.put("20_0", BitmapFactory.decodeResource(r,R.drawable.stage_orange20_1));
	        images.put("21_0", BitmapFactory.decodeResource(r,R.drawable.stage_orange21_1));
	        images.put("22_0", BitmapFactory.decodeResource(r,R.drawable.stage_orange22_1));
	        images.put("23_0", BitmapFactory.decodeResource(r,R.drawable.stage_orange23_1));

	        images.put("11_1", BitmapFactory.decodeResource(r,R.drawable.stage_white11_1));
	        images.put("12_1", BitmapFactory.decodeResource(r,R.drawable.stage_white12_1));
	        images.put("13_1", BitmapFactory.decodeResource(r,R.drawable.stage_white13_1));
	        images.put("14_1", BitmapFactory.decodeResource(r,R.drawable.stage_white14_1));
	        images.put("19_1", BitmapFactory.decodeResource(r,R.drawable.stage_white19_1));
	        images.put("20_1", BitmapFactory.decodeResource(r,R.drawable.stage_white20_1));
	        images.put("21_1", BitmapFactory.decodeResource(r,R.drawable.stage_white21_1));
	        images.put("22_1", BitmapFactory.decodeResource(r,R.drawable.stage_white22_1));
	        images.put("23_1", BitmapFactory.decodeResource(r,R.drawable.stage_white23_1));

        } else {

	        background = BitmapFactory.decodeResource(r,R.drawable.bg_orange);
	        image=BitmapFactory.decodeResource(r,R.drawable.stage_orange1);

	        images = new Hashtable<String, Bitmap>();
	        images.put("01", BitmapFactory.decodeResource(r,R.drawable.stage_orange1));
	        images.put("02", BitmapFactory.decodeResource(r,R.drawable.stage_orange2));
	        images.put("03", BitmapFactory.decodeResource(r,R.drawable.stage_orange3));
	        images.put("04", BitmapFactory.decodeResource(r,R.drawable.stage_orange4));
	        images.put("05", BitmapFactory.decodeResource(r,R.drawable.stage_orange5));
	        images.put("06", BitmapFactory.decodeResource(r,R.drawable.stage_orange6));
	        images.put("07", BitmapFactory.decodeResource(r,R.drawable.stage_orange7));
	        images.put("08", BitmapFactory.decodeResource(r,R.drawable.stage_orange8));
	        images.put("09", BitmapFactory.decodeResource(r,R.drawable.stage_orange9));
	        images.put("10", BitmapFactory.decodeResource(r,R.drawable.stage_orange10));
	        images.put("11", BitmapFactory.decodeResource(r,R.drawable.stage_orange11_2));
	        images.put("12", BitmapFactory.decodeResource(r,R.drawable.stage_orange12_2));
	        images.put("13", BitmapFactory.decodeResource(r,R.drawable.stage_orange13_2));
	        images.put("14", BitmapFactory.decodeResource(r,R.drawable.stage_orange14_2));
	        images.put("15", BitmapFactory.decodeResource(r,R.drawable.stage_orange15_2));
//	        images.put("17", BitmapFactory.decodeResource(r,R.drawable.stage_orange17_2));
//	        images.put("19", BitmapFactory.decodeResource(r,R.drawable.stage_orange19_2));
//	        images.put("20", BitmapFactory.decodeResource(r,R.drawable.stage_orange20_2));
//	        images.put("21", BitmapFactory.decodeResource(r,R.drawable.stage_orange21_2));
//	        images.put("22", BitmapFactory.decodeResource(r,R.drawable.stage_orange22_2));
//	        images.put("23", BitmapFactory.decodeResource(r,R.drawable.stage_orange23_2));
	        images.put("00", BitmapFactory.decodeResource(r,R.drawable.noimage));

	        images.put("11_0", BitmapFactory.decodeResource(r,R.drawable.stage_orange11_1));
	        images.put("12_0", BitmapFactory.decodeResource(r,R.drawable.stage_orange12_1));
	        images.put("13_0", BitmapFactory.decodeResource(r,R.drawable.stage_orange13_1));
	        images.put("14_0", BitmapFactory.decodeResource(r,R.drawable.stage_orange14_1));
	        images.put("19_0", BitmapFactory.decodeResource(r,R.drawable.stage_orange19_1));
	        images.put("20_0", BitmapFactory.decodeResource(r,R.drawable.stage_orange20_1));
	        images.put("21_0", BitmapFactory.decodeResource(r,R.drawable.stage_orange21_1));
	        images.put("22_0", BitmapFactory.decodeResource(r,R.drawable.stage_orange22_1));
	        images.put("23_0", BitmapFactory.decodeResource(r,R.drawable.stage_orange23_1));

	        images.put("11_1", BitmapFactory.decodeResource(r,R.drawable.stage_white11_1));
	        images.put("12_1", BitmapFactory.decodeResource(r,R.drawable.stage_white12_1));
	        images.put("13_1", BitmapFactory.decodeResource(r,R.drawable.stage_white13_1));
	        images.put("14_1", BitmapFactory.decodeResource(r,R.drawable.stage_white14_1));
	        images.put("19_1", BitmapFactory.decodeResource(r,R.drawable.stage_white19_1));
	        images.put("20_1", BitmapFactory.decodeResource(r,R.drawable.stage_white20_1));
	        images.put("21_1", BitmapFactory.decodeResource(r,R.drawable.stage_white21_1));
	        images.put("22_1", BitmapFactory.decodeResource(r,R.drawable.stage_white22_1));
	        images.put("23_1", BitmapFactory.decodeResource(r,R.drawable.stage_white23_1));

        }
    }

    //サーフェイスの生成(1)
    public void surfaceCreated(SurfaceHolder holder) {
        //スレッドの開始(3)
        thread=new Thread(this);
        thread.start();
    }

    //サーフェイスの変更(1)
    public void surfaceChanged(SurfaceHolder holder,
        int format,int w,int h) {
    }

    //サーフェイスの破棄(1)
    public void surfaceDestroyed(SurfaceHolder holder) {
        thread=null;
    }

    //スレッドの処理
    public void run() {
        Canvas canvas;

//        int status = this.STATUS_STOP;

        String currentImageKey = "";
        int currentDuration = 0;

    	// 場所1

    	int currentMotion = 0;

    	Matrix matrix = new Matrix();

    	Status currentImage;
    	Status currentPartner = null;
    	Hashtable<Integer, Status> currentScenario = new Hashtable<Integer, Status>();

    	// ずっとアニメーション
        while(thread!=null) {
//            Log.d("position1x", this.POSITION1_X+"");

        	//ダブルバッファリング（4）
            canvas=holder.lockCanvas();

            if (this.color == 0) {
            	canvas.drawColor(Color.rgb(255, 255, 255));
            } else {
            	canvas.drawColor(Color.rgb(249, 194, 113));
            }

            canvas.drawBitmap(background, 0 + GETA_X, 0 + GETA_Y, null);
            canvas.drawBitmap(image, px + GETA_X, py + GETA_Y, null);
            if (image2 != null) {
            	canvas.drawBitmap(image2, px2 + GETA_X, py2 + GETA_Y, null);
            }
            holder.unlockCanvasAndPost(canvas);
//Log.d("px", px + "");

            // 静止しているときは動くかどうかを躊躇する
            if (status == this.STATUS_STOP) {
            	currentScenario = null;
    			frame = 0;
    			currentDuration = 0;
    			currentMotion = 0;
    			px = ORIGIN_X;
    			py = ORIGIN_Y;

    			image2 = null;

	            int ran = (int)(Math.random()*10);
	            switch (ran) {
	            	// 移動
	            	case 0:
	            		status = this.STATUS_MOVE_LEFT;
	            		currentScenario = moveLeftScenario;
	            		break;
	            	// さえずり
	            	case 1:
	            		status = this.STATUS_TWEET;
	            		currentScenario = tweetScenario;
	            		break;
	            	// まばたき
	            	case 2:
	            		status = this.STATUS_WINK;
	            		currentScenario = winkScenario;
	            		break;
		            // 移動2
	            	case 3:
//	            		status = this.STATUS_MOVE_RIGHT;
//	            		currentScenario = moveRightScenario;
//	            		break;
	            	// 静止
	        		default:
	        			break;
	            }
//	            Log.d("ran", ran + "");

            } else if (status == this.STATUS_PRE_EMIGRATE) {
        		status = this.STATUS_EMIGRATE;
            	currentScenario = emigrateScenario;
    			frame = 0;
    			currentDuration = 0;
    			currentMotion = 0;
    			px = ORIGIN_X;
    			py = ORIGIN_Y;
    			image2 = null;
            } else if (status == this.STATUS_PRE_IMMIGRATE) {
            	status = this.STATUS_IMMIGRATE;
            	currentScenario = immigrateScenario;
    			frame = 0;
    			currentDuration = 0;
    			currentMotion = 0;
    			px = ORIGIN_X;
    			py = ORIGIN_Y;
    			image2 = null;
            }

//            Log.d("move", status + "");

            if (status != this.STATUS_STOP) {
        		// モーション終わりで静止
        		if (!currentScenario.containsKey(currentMotion)) {
        			status = this.STATUS_STOP;
        			frame = 0;
        			currentDuration = 0;
        			currentMotion = 0;
        			px = ORIGIN_X;
        			py = ORIGIN_Y;
        			image2 = null;
        		// 継続
        		} else {
        			// 今のフレームがフレーム数に達したとき
    				if (frame >= currentDuration) {
        				currentImage = (Status) currentScenario.get(currentMotion);
            			currentDuration = currentImage.getDuration(); // フレーム数
            			image = images.get(currentImage.getImageKey());
            			px = currentImage.getX();
            			py = currentImage.getY();
            			frame = 0;
//            			if (currentImage.isInverse()) {
//                			matrix.preScale(-1, 1);
//                			image = Bitmap.createBitmap(image, px, py, image.getWidth(), image.getHeight(), matrix, false);
//            			}


            			currentPartner = currentImage.getPartner();
            			if (currentPartner != null) {
                			image2 = images.get(currentPartner.getImageKey() + "_" + partnersColor);
                			px2 = currentPartner.getX();
                			py2 = currentPartner.getY();
            			} else {
                			image2 = null;
            			}

    					currentMotion++;
    				}
					frame++;
        		}
            }

//            Log.d("currentDuration", currentDuration + "");
//            Log.d("frame", frame + "");
//            Log.d("frsam", frame + "");
//            Log.d("currentDuration", currentDuration + "");
//            Log.d("currentPosition", currentPosition + "");

            //スリープ(5)
            try {
                Thread.sleep(20);
            } catch (Exception e) {
            }
        }
    }



//    public void emigrate(int partnersColor) {
//    	this.partnersColor = partnersColor;
//    	this.emigrate();
//    }
    public void emigrate() {
    	if (this.status != STATUS_EMIGRATE && this.status != STATUS_IMMIGRATE) {
    		this.status = STATUS_PRE_EMIGRATE;
    	} else {
    		this.status = STATUS_STOP;
    	}

    }

    public void immigrate(int partnersColor) {
    	this.partnersColor = partnersColor;
    	this.immigrate();
    }
    public void immigrate() {
    	if (this.status != STATUS_EMIGRATE && this.status != STATUS_IMMIGRATE) {
    		this.status = STATUS_PRE_IMMIGRATE;
    	} else {
    		this.status = STATUS_STOP;
    	}

    }


}