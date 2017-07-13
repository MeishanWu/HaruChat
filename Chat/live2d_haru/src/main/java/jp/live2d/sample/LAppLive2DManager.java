/**
 *  You can modify and use this source freely
 *  only for the development of application related Live2D.
 *
 *  (c) Live2D Inc. All rights reserved.
 */
package jp.live2d.sample;

import java.util.ArrayList;
import java.util.Date;

import javax.microedition.khronos.opengles.GL10;

import jp.live2d.Live2D;
import jp.live2d.framework.L2DViewMatrix;
import jp.live2d.framework.Live2DFramework;
import android.app.Activity;
import android.text.format.DateFormat;
import android.util.Log;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import static jp.live2d.sample.MainHaruActivity.tag;

/*
 *  LAppLive2DManagerは、Live2D関連の司令塔としてモデル、ビュー、イベント等を管理するクラス（のサンプル実装）になります。
 *
 *  外部（ゲーム等アプリケーション本体）とLive2D関連クラスとの連携をこのクラスでラップして独立性を高めています。
 *
 *  ビュー（LAppView）で発生したイベントは、このクラスのイベント処理用メソッド（tapEvent()等）を呼び出します。
 *  イベント処理用メソッドには、イベント発生時のキャラクターの反応（特定アニメーション開始等）を記述します。
 *
 *  このサンプルで取得しているイベントは、タップ、ダブルタップ、シェイク、ドラッグ、フリック、加速度、キャラ最大化・最小化です。
 *
 */
public class LAppLive2DManager
{
	//  ログ用タグ
	static public final String 	TAG = "SampleLive2DManager";

	private LAppView 				view;						// モデル表示用View

	// モデルデータ
	private ArrayList<LAppModel>	models;


	//  ボタンから実行できるサンプル機能
	private int 					modelCount		=-1;
	private boolean 				reloadFlg;					//  モデル再読み込みのフラグ



	public LAppLive2DManager()
	{
		Live2D.init();
		Live2DFramework.setPlatformManager(new PlatformManager());

		models = new ArrayList<LAppModel>();
	}


	public void releaseModel()
	{
		for(int i=0;i<models.size();i++)
		{
			models.get(i).release();// テクスチャなどを解放
		}

		models.clear();
	}


	/*
	 * モデルの管理状態などの更新。
	 *
	 * レンダラ（LAppRenderer）のonDrawFrame()からモデル描画の前に毎回呼ばれます。
	 * モデルの切り替えなどが必要な場合はここで行なって下さい。
	 *
	 * モデルのパラメータ（モーション）などの更新はdrawで行って下さい。
	 *
	 * @param gl
	 */
	public void update(GL10 gl)
	{
		view.update();
		if(reloadFlg)
		{
			// モデル切り替えボタンが押された時、モデルを再読み込みする
			reloadFlg=false;
			//4 model -> one
			/**
			int no = modelCount % 4;

			try {
				switch (no) {
				case 0:// ハル
					releaseModel();

					models.add(new LAppModel());
					models.get(0).load(gl, LAppDefine.MODEL_HARU);
					models.get(0).feedIn();
					break;
				case 1:// しずく
					releaseModel();

					models.add(new LAppModel());
					models.get(0).load(gl, LAppDefine.MODEL_SHIZUKU);
					models.get(0).feedIn();
					break;
				case 2:// わんこ
					releaseModel();

					models.add(new LAppModel());
					models.get(0).load(gl, LAppDefine.MODEL_WANKO);
					models.get(0).feedIn();
					break;
				case 3:// 複数モデル
					releaseModel();

					models.add(new LAppModel());
					models.get(0).load(gl, LAppDefine.MODEL_HARU_A);
					models.get(0).feedIn();

					models.add(new LAppModel());
					models.get(1).load(gl, LAppDefine.MODEL_HARU_B);
					models.get(1).feedIn();
					break;
				default:

					break;
				}
			 */
			try{
				releaseModel();
				models.add(new LAppModel());
				models.get(0).load(gl, LAppDefine.MODEL_HARU);
				models.get(0).feedIn();
			} catch (Exception e) {
				// ファイルの指定ミスかメモリ不足が考えられる。復帰か中断が必要
				Log.e(TAG,"Failed to load."+e.getStackTrace());
				MainHaruActivity.exit();
			}
		}
	}


	/*
	 * noを指定してモデルを取得
	 *
	 * @param no
	 * @return
	 */
	public LAppModel getModel(int no)
	{
		if(no>=models.size())return null;
		return models.get(no);
	}


	public int getModelNum()
	{
		return models.size();
	}


	//=========================================================
	// 	アプリケーション側（MainHaruActivity）から呼ばれる処理
	//=========================================================
	/*
	 * LAppView(Live2Dを表示するためのView)を生成します。
	 *
	 * @param act
	 * @return
	 */
	public LAppView  createView(Activity act)
	{
		// Viewの初期化
		view = new LAppView( act ) ;
		view.setLive2DManager(this);
		view.startAccel(act);
		return view ;
	}


	/*
	 * Activityが再開された時のイベント
	 */
	public void onResume()
	{
		if(LAppDefine.DEBUG_LOG)Log.d(TAG, "onResume");
		view.onResume();
	}


	/*
	 * Activityがポーズされた時のイベント
	 */
	public void onPause()
	{
		if(LAppDefine.DEBUG_LOG)Log.d(TAG, "onPause");
		view.onPause();
	}


	/*
	 * GLSurfaceViewの画面変更時のイベント
	 * @param context
	 * @param width
	 * @param height
	 */
	public void onSurfaceChanged(GL10 gl, int width, int height)
	{
		if(LAppDefine.DEBUG_LOG)Log.d(TAG, "onSurfaceChanged "+width+" "+height);
		view.setupView(width,height);

		if(getModelNum()==0)
		{

			changeModel();

		}
	}


	//=========================================================
	// 	アプリケーションからのサンプルイベント
	//=========================================================
	/*
	 * モデルの切り替え
	 */
	public void changeModel()
	{
		reloadFlg=true;// フラグだけ立てて次回update時に切り替え
		modelCount++;
	}


	//=========================================================
	// 	LAppViewから呼ばれる各種イベント
	//=========================================================
	/*
	 * タップしたときのイベント
	 * @param x	タップの座標 x
	 * @param y	タップの座標 y
	 * @return
	 */
	public boolean tapEvent(float x,float y)
	{
		if(LAppDefine.DEBUG_LOG)Log.d(TAG, "tapEvent view x:"+x+" y:"+y);

		for (int i=0; i<models.size(); i++)
		{
			if(models.get(i).hitTest(  LAppDefine.HIT_AREA_HEAD,x, y ))
			{
				// 顔をタップしたら表情切り替え
				if(LAppDefine.DEBUG_LOG)Log.d(TAG, "Tap face.");
				models.get(i).setRandomExpression();
			}
			else if(models.get(i).hitTest( LAppDefine.HIT_AREA_BODY,x, y))
			{
				if(LAppDefine.DEBUG_LOG)Log.d(TAG, "Tap body.");
				models.get(i).startRandomMotion(LAppDefine.MOTION_GROUP_TAP_BODY, LAppDefine.PRIORITY_NORMAL );
				String time = DateFormat.format("MM/dd/yy HH:mm:ss", new Date().getTime()).toString();
				Message mMessage = new Message(time, Kaomoji.randomkaomoji("happy"), "Haru");
				MainHaruActivity.messages.add(mMessage);
				MainHaruActivity.messageAdapter.notifyDataSetChanged();
				if (MainHaruActivity.messages.size() != 0){
					MainHaruActivity.mRecyclerView.smoothScrollToPosition(MainHaruActivity.messages.size() - 1);
				}
			}
		}
		return true;
	}


	/*
	 * フリックした時のイベント
	 *
	 * LAppView側でフリックイベントを感知した時に呼ばれ
	 * フリック時のモデルの動きを開始します。
	 *
	 * @param
	 * @param
	 * @param flickDist
	 */
	public void flickEvent(float x,float y)
	{
		if(LAppDefine.DEBUG_LOG)Log.d(TAG, "flick x:"+x+" y:"+y);

		for (int i=0; i<models.size(); i++)
		{
			if(models.get(i).hitTest( LAppDefine.HIT_AREA_HEAD, x, y ))
			{
				if(LAppDefine.DEBUG_LOG)Log.d(TAG, "Flick head.");
				models.get(i).startRandomMotion(LAppDefine.MOTION_GROUP_FLICK_HEAD, LAppDefine.PRIORITY_NORMAL );
			}
		}
	}


	/*
	 * 画面が最大になったときのイベント
	 */
	public void maxScaleEvent()
	{
		if(LAppDefine.DEBUG_LOG)Log.d(TAG, "Max scale event.");

		for (int i=0; i<models.size(); i++)
		{
			models.get(i).startRandomMotion(LAppDefine.MOTION_GROUP_PINCH_IN,LAppDefine.PRIORITY_NORMAL );
		}
	}


	/*
	 * 画面が最小になったときのイベント
	 */
	public void minScaleEvent()
	{
		if(LAppDefine.DEBUG_LOG)Log.d(TAG, "Min scale event.");

		for (int i=0; i<models.size(); i++)
		{
			models.get(i).startRandomMotion(LAppDefine.MOTION_GROUP_PINCH_OUT,LAppDefine.PRIORITY_NORMAL );
		}
	}


	/*
	 * シェイクイベント
	 *
	 * LAppView側でシェイクイベントを感知した時に呼ばれ、
	 * シェイク時のモデルの動きを開始します。
	 */
	public void shakeEvent()
	{
		if(LAppDefine.DEBUG_LOG)Log.d(TAG, "Shake event.");

		for (int i=0; i<models.size(); i++)
		{
			models.get(i).startRandomMotion(LAppDefine.MOTION_GROUP_SHAKE,LAppDefine.PRIORITY_FORCE );
		}
	}


	public void setAccel(float x,float y,float z)
	{
		for (int i=0; i<models.size(); i++)
		{
			models.get(i).setAccel(x, y, z);
		}
	}


	public void setDrag(float x,float y)
	{
		for (int i=0; i<models.size(); i++)
		{
			models.get(i).setDrag(x, y);
		}
	}


	public L2DViewMatrix getViewMatrix()
	{
		return view.getViewMatrix();
	}
}
