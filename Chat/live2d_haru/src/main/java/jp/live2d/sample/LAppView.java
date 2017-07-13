/**
 *
 *  You can modify and use this source freely
 *  only for the development of application related Live2D.
 *
 *  (c) Live2D Inc. All rights reserved.
 */
package jp.live2d.sample;

import jp.live2d.framework.L2DMatrix44;
import jp.live2d.framework.L2DTargetPoint;
import jp.live2d.framework.L2DViewMatrix;
import jp.live2d.utils.android.AccelHelper;
import jp.live2d.utils.android.TouchManager;
import android.app.Activity;
import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;

/*
 * LAppViewは、GLSurfaceViewを継承するOpenGLのViewのサンプル
 *
 * 画面の初期化、ビューに関連するイベント処理（タッチ関連）などを行います。
 * Live2Dを含むOpenGLの描画処理は、LAppRenderに移譲します。
 *
 */
public class LAppView extends GLSurfaceView {
	//  ログ用タグ
	static public final String 		TAG = "LAppView";

	// 描画を行うView
	private LAppRenderer 				renderer ;

	// Live2D関連のイベントの管理。コンストラクタで参照を受け取る。
	private LAppLive2DManager 			delegate;
	private L2DMatrix44 				deviceToScreen;
	private L2DViewMatrix 				viewMatrix;// 画面の拡大縮小、移動用の行列
	private AccelHelper 				accelHelper;// 加速度センサの制御
	private TouchManager 				touchMgr;// ピンチなど
	private L2DTargetPoint 				dragMgr;// ドラッグによるアニメーションの管理

	GestureDetector 					gestureDetector;

	public LAppView(  Context context )
	{
		super( context ) ;
		setFocusable(true);
	}


	public void setLive2DManager( LAppLive2DManager live2DMgr)
	{
		this.delegate = live2DMgr ;
		this.renderer = new LAppRenderer( live2DMgr  ) ;

		setRenderer(renderer);

		gestureDetector = new GestureDetector(this.getContext()  , simpleOnGestureListener ) ;


		// デバイス座標からスクリーン座標に変換するための
		deviceToScreen=new L2DMatrix44();

		// 画面の表示の拡大縮小や移動の変換を行う行列
		viewMatrix=new L2DViewMatrix();

		// 表示範囲の設定
		viewMatrix.setMaxScale( LAppDefine.VIEW_MAX_SCALE );// 限界拡大率
		viewMatrix.setMinScale( LAppDefine.VIEW_MIN_SCALE );// 限界縮小率


		// 表示できる最大範囲
		viewMatrix.setMaxScreenRect(
				LAppDefine.VIEW_LOGICAL_MAX_LEFT,
				LAppDefine.VIEW_LOGICAL_MAX_RIGHT,
				LAppDefine.VIEW_LOGICAL_MAX_BOTTOM,
				LAppDefine.VIEW_LOGICAL_MAX_TOP
				);

		// タッチ関係のイベント管理
		touchMgr=new TouchManager();

		dragMgr  = new L2DTargetPoint();
	}


	public void startAccel(Activity activity)
	{
		// 加速度関係のイベント
		accelHelper = new AccelHelper(activity) ;
	}


	/*
	 * タッチイベント。
	 */
	@Override
	public boolean onTouchEvent(MotionEvent event)
	{
	    boolean ret = false ;
	    int touchNum;
	    switch (event.getAction())
	    {
	    case MotionEvent.ACTION_DOWN:
	        ret = true ;

			// タッチ数を取得
			touchNum = event.getPointerCount() ;

			if( touchNum == 1 )
			{
				touchesBegan(event.getX(),event.getY());
			}
			else if( touchNum == 2 )
			{
				touchesBegan(event.getX(0),event.getY(0),event.getX(1),event.getY(1));
			}
			else
			{
				// タッチ数 3以上
			}

	        break;
	    case MotionEvent.ACTION_UP:
	    	touchesEnded();
	        break;
	    case MotionEvent.ACTION_MOVE:
	    	// タッチ数を取得
			touchNum = event.getPointerCount() ;

			if( touchNum == 1 )
			{
				touchesMoved(event.getX(),event.getY());
			}
			else if( touchNum == 2 )
			{
				touchesMoved(event.getX(0),event.getY(0),event.getX(1),event.getY(1));
			}
			else
			{
				// タッチ数 3以上
			}
	        break;
	    case MotionEvent.ACTION_CANCEL:
	        break;
	    }
        ret |= gestureDetector.onTouchEvent(event) ;

        return ret ;
	}


	/*
	 * Activityが再開された時のイベント
	 */
	public void onResume()
	{
		if(accelHelper!=null)
		{
			if(LAppDefine.DEBUG_LOG)Log.d(TAG, "start accelHelper");
			accelHelper.start();
		}
	}


	/*
	 * Activityがポーズされた時のイベント
	 */
	public void onPause()
	{
		if(accelHelper!=null)
		{
			if(LAppDefine.DEBUG_LOG)Log.d(TAG, "stop accelHelper");
			accelHelper.stop();
		}
	}


	public void setupView( int width, int height)
	{
		float ratio=(float)height/width;
		float left = LAppDefine.VIEW_LOGICAL_LEFT;
		float right = LAppDefine.VIEW_LOGICAL_RIGHT;
		float bottom = -ratio;
		float top = ratio;

		viewMatrix.setScreenRect(left,right,bottom,top);// デバイスに対応する画面の範囲。 Xの左端, Xの右端, Yの下端, Yの上端

		float screenW=Math.abs(left-right);
		deviceToScreen.identity() ;
		deviceToScreen.multTranslate(-width/2.0f,height/2.0f );
		deviceToScreen.multScale( screenW/width , screenW/width );
	}


	public void update()
	{
		dragMgr.update();// ドラッグ用パラメータの更新
		delegate.setDrag(dragMgr.getX(), dragMgr.getY());

		accelHelper.update();

		if( accelHelper.getShake() > 1.5f )
		{
			if(LAppDefine.DEBUG_LOG)Log.d(TAG, "shake event");
			// シェイクモーションを起動する
			delegate.shakeEvent() ;
			accelHelper.resetShake() ;
		}

		delegate.setAccel(accelHelper.getAccelX(), accelHelper.getAccelY(), accelHelper.getAccelZ());
		renderer.setAccel(accelHelper.getAccelX(), accelHelper.getAccelY(), accelHelper.getAccelZ());
	}


	/*
	 * 画面表示の行列を更新。
	 *
	 * @param dx 移動幅
	 * @param dy 移動幅
	 * @param cx 拡大の中心
	 * @param cy 拡大の中心
	 * @param scale 拡大率
	 */
	public void updateViewMatrix(float dx, float dy, float cx, float cy,float scale,boolean enableEvent)
	{
		boolean isMaxScale=viewMatrix.isMaxScale();
		boolean isMinScale=viewMatrix.isMinScale();

		// 拡大縮小
		viewMatrix.adjustScale(cx, cy, scale);

		// 移動
		viewMatrix.adjustTranslate(dx, dy) ;

		if(enableEvent)
		{
			// 画面が最大になったときのイベント
			if( ! isMaxScale)
			{
				if(viewMatrix.isMaxScale())
				{
					delegate.maxScaleEvent();
				}
			}
			// 画面が最小になったときのイベント
			if( ! isMinScale)
			{
				if(viewMatrix.isMinScale())
				{
					delegate.minScaleEvent();
				}
			}
		}
	}


	private float transformDeviceToViewX(float deviceX)
	{
		float screenX = deviceToScreen.transformX( deviceX );// 論理座標変換した座標を取得。
		return  viewMatrix.invertTransformX(screenX);// 拡大、縮小、移動後の値。
	}


	private float transformDeviceToViewY(float deviceY)
	{
		float screenY = deviceToScreen.transformY( deviceY );// 論理座標変換した座標を取得。
		return  viewMatrix.invertTransformY(screenY);// 拡大、縮小、移動後の値。
	}


	/*
	 * タッチを開始したときのイベント
	 */
	public void touchesBegan(float p1x,float p1y)
	{
		if(LAppDefine.DEBUG_TOUCH_LOG)Log.v(TAG, "touchesBegan"+" x:"+p1x+" y:"+p1y);
		touchMgr.touchBegan(p1x,p1y);

		float x=transformDeviceToViewX( touchMgr.getX() );
		float y=transformDeviceToViewY( touchMgr.getY() );

		dragMgr.set(x, y);
	}


	public void touchesBegan(float p1x,float p1y,float p2x,float p2y)
	{
		if(LAppDefine.DEBUG_TOUCH_LOG)Log.v(TAG, "touchesBegan"+" x1:"+p1x+" y1:"+p1y+" x2:"+p2x+" y2:"+p2y);
		touchMgr.touchBegan(p1x,p1y,p2x,p2y);

		float x=transformDeviceToViewX( touchMgr.getX() );
		float y=transformDeviceToViewY( touchMgr.getY() );

		dragMgr.set(x, y);
	}


	/*
	 * ドラッグしたときのイベント
	 */
	public void touchesMoved(float p1x,float p1y)
	{
		if(LAppDefine.DEBUG_TOUCH_LOG)Log.v(TAG, "touchesMoved"+"x:"+p1x+" y:"+p1y);
		touchMgr.touchesMoved(p1x,p1y);
		float x=transformDeviceToViewX( touchMgr.getX() );
		float y=transformDeviceToViewY( touchMgr.getY() );

		dragMgr.set(x, y);

		final int FLICK_DISTANCE=100;// この値以上フリックしたらイベント発生

		// フリックイベントの判定

		if(touchMgr.isSingleTouch() && touchMgr.isFlickAvailable() )
		{
			float flickDist=touchMgr.getFlickDistance();
			if(flickDist>FLICK_DISTANCE)
			{

				float startX=transformDeviceToViewX( touchMgr.getStartX() );
				float startY=transformDeviceToViewY( touchMgr.getStartY() );
				delegate.flickEvent(startX,startY);
				touchMgr.disableFlick();
			}
		}
	}


	public void touchesMoved(float p1x,float p1y,float p2x,float p2y)
	{
		if(LAppDefine.DEBUG_TOUCH_LOG)Log.v(TAG, "touchesMoved"+" x1:"+p1x+" y1:"+p1y+" x2:"+p2x+" y2:"+p2y);
		touchMgr.touchesMoved(p1x,p1y,p2x,p2y);

		// 画面の拡大縮小、移動の設定
		float dx= touchMgr.getDeltaX() * deviceToScreen.getScaleX();
		float dy= touchMgr.getDeltaY() * deviceToScreen.getScaleY() ;
		float cx= deviceToScreen.transformX( touchMgr.getCenterX() ) * touchMgr.getScale();
		float cy= deviceToScreen.transformY( touchMgr.getCenterY() ) * touchMgr.getScale();
		float scale=touchMgr.getScale();

		if(LAppDefine.DEBUG_TOUCH_LOG)Log.v(TAG, "view  dx:"+dx+" dy:"+dy+" cx:"+cx+" cy:"+cy+" scale:"+scale);

		updateViewMatrix(dx,dy,cx,cy,scale,true);

		float x=transformDeviceToViewX( touchMgr.getX() );
		float y=transformDeviceToViewY( touchMgr.getY() );

		dragMgr.set(x, y);
	}


	/*
	 * タッチを終了したときのイベント
	 * @param event
	 */
	public void touchesEnded()
	{
		if(LAppDefine.DEBUG_TOUCH_LOG)Log.v(TAG, "touchesEnded");
		dragMgr.set(0,0);
	}


	public L2DViewMatrix getViewMatrix()
	{
		return viewMatrix;
	}


	/*
	 * Gestureの補助クラス。
	 */
	private final SimpleOnGestureListener simpleOnGestureListener = new SimpleOnGestureListener()
	{
        @Override
        public boolean onDoubleTap(MotionEvent event)
        {
        	return super.onDoubleTap(event) ;
        }

        @Override
        public boolean onDown(MotionEvent event)
        {
            super.onDown(event);
            return true ;
        }

        @Override
        public boolean onSingleTapConfirmed(MotionEvent event)
        {
        	float x=transformDeviceToViewX( touchMgr.getX() );
    		float y=transformDeviceToViewY( touchMgr.getY() );
          	boolean ret = delegate.tapEvent(x,y);//Live2D Event
          	ret |= super.onSingleTapUp(event);
            return ret ;
        }

        @Override
        public boolean onSingleTapUp(MotionEvent event)
        {
            return super.onSingleTapUp(event) ;
        }
    };

}

