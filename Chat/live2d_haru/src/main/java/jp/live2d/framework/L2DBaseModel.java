/**
 *
 *  You can modify and use this source freely
 *  only for the development of application related Live2D.
 *
 *  (c) Live2D Inc. All rights reserved.
 */
package jp.live2d.framework;

import java.util.HashMap;
import java.util.Map;

import jp.live2d.ALive2DModel;
import jp.live2d.Live2D;
import jp.live2d.motion.AMotion;
import jp.live2d.motion.Live2DMotion;
import jp.live2d.motion.MotionQueueManager;

public class L2DBaseModel {
	//  モデル関連
	protected ALive2DModel		 		live2DModel=null;			//  Live2Dモデルクラス
	protected L2DModelMatrix 			modelMatrix=null;			//  Live2Dモデラー上の座標系からワールド座標系へ変換するための行列

	//  モーション・状態管理
	protected Map<String,AMotion> 	expressions ;			//  表情モーションデータ
	protected Map<String,AMotion> 	motions ;		//  モーションデータ

	protected L2DMotionManager 		mainMotionManager;		//  メインモーション
	protected L2DMotionManager 		expressionManager;		//  表情
	protected L2DEyeBlink 			eyeBlink;				//  自動目パチ
	protected L2DPhysics 			physics;				//  物理演算
	protected L2DPose 				pose;					//  ポーズ。腕の切り替えなど。

	protected boolean				debugMode=false;
	protected boolean 				initialized = false;	//  初期化状態
	protected boolean 				updating = false;		//  読み込み中ならtrue
	protected float 				alpha = 1;				//  透明度
	protected float 				accAlpha = 0;			//  透明度の増え幅
	protected boolean 				lipSync = false;		//  リップシンクが有効かどうか
	protected float 				lipSyncValue;			//  基本は0～1

	// 傾きの値。-1から1の範囲
	protected float 				accelX=0;
	protected float 				accelY=0;
	protected float 				accelZ=0;

	// 向く方向の値。-1から1の範囲
	protected float 				dragX=0;
	protected float 				dragY=0;

	protected long 					startTimeMSec;


	public L2DBaseModel()
	{
		// モーションマネージャーを作成
		mainMotionManager=new L2DMotionManager();// MotionQueueManagerクラスからの継承なので、使い方は同一
		expressionManager=new L2DMotionManager();

		motions=new HashMap< String , AMotion >();
		expressions=new HashMap< String , AMotion >();
	}


	public L2DModelMatrix getModelMatrix()
	{
		return modelMatrix;
	}


	public void setAlpha(float a)
	{
		if(a>0.999)a=1;
		if(a<0.001)a=0;
		alpha=a;
	}


	public float getAlpha()
	{
		return alpha;
	}


	/*
	 * 初期化されている場合はtrue。
	 * 更新と描画可能になったときに初期化完了とみなす。
	 *
	 * @return
	 */
	public boolean isInitialized() {
		return initialized;
	}


	public void setInitialized(boolean v)
	{
		initialized=v;
	}


	/*
	 * モデルの読み込み中はtrue。
	 * 更新と描画可能になったときに読み込み完了とみなす。
	 *
	 * @return
	 */
	public boolean isUpdating() {
		return updating;
	}


	public void setUpdating(boolean v)
	{
		updating=v;
	}


	/*
	 * Live2Dモデルクラスを取得する。
	 * @return
	 */
	public ALive2DModel getLive2DModel() {
		return live2DModel;
	}


	public void setLipSync(boolean v)
	{
		lipSync=v;
	}


	public void setLipSyncValue(float v)
	{
		lipSyncValue=v;
	}


	public void setAccel(float x,float y,float z)
	{
		accelX=x;
		accelY=y;
		accelZ=z;
	}


	public void setDrag(float x,float y)
	{
		dragX=x;
		dragY=y;
	}


	public MotionQueueManager getMainMotionManager()
	{
		return mainMotionManager;
	}


	public MotionQueueManager getExpressionManager()
	{
		return expressionManager;
	}

	public void loadModelData(String path)
	{
		if(live2DModel!=null)
		{
			live2DModel.deleteTextures();
		}
		IPlatformManager pm=Live2DFramework.getPlatformManager();


		if(debugMode)pm.log( "Load model : "+path);

		live2DModel = pm.loadLive2DModel(path);
		live2DModel.saveParam();

		if(Live2D.getError()!=Live2D.L2D_NO_ERROR)
		{
			// 読み込み失敗
			pm.log("Error : Failed to loadModelData().");
			return;
		}

		modelMatrix=new L2DModelMatrix(live2DModel.getCanvasWidth(),live2DModel.getCanvasHeight());
		modelMatrix.setWidth(2);
		modelMatrix.setCenterPosition(0, 0);// 中心に配置
	}


	public void loadTexture(int no,String path)
	{
		IPlatformManager pm=Live2DFramework.getPlatformManager();
		if(debugMode)pm.log( "Load Texture : "+path);

		pm.loadTexture(live2DModel, no, path);
	}

	public AMotion loadMotion(String name,String path)
	{
		IPlatformManager pm=Live2DFramework.getPlatformManager();
		if(debugMode)pm.log("Load Motion : "+path);


		Live2DMotion motion=null;


		byte[] buf = pm.loadBytes(path) ;
		motion=Live2DMotion.loadMotion(buf);

		if ( name !=null)
		{
			motions.put(name, motion);
		}

		return motion;
	}

	public void loadExpression(String name,String path)
	{
		IPlatformManager pm=Live2DFramework.getPlatformManager();
		if(debugMode)pm.log("Load Expression : "+path);

		try {
			expressions.put(name, L2DExpressionMotion.loadJson(pm.loadBytes(path)) );
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	public void loadPose( String path )
	{
		IPlatformManager pm=Live2DFramework.getPlatformManager();
		if(debugMode) pm.log("Load Pose : "+path);
		try
		{
			pose = L2DPose.load(pm.loadBytes(path));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	public void loadPhysics( String path )
	{
		IPlatformManager pm=Live2DFramework.getPlatformManager();
		if(debugMode) pm.log("Load Physics : "+path);
		try
		{
			physics = L2DPhysics.load(pm.loadBytes(path));

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public boolean hitTestSimple(String drawID,float testX,float testY)
	{
		if(alpha<1)return false;// 透明時は当たり判定なし。

		int drawIndex=live2DModel.getDrawDataIndex(drawID);
		if(drawIndex<0)return false;// 存在しない場合はfalse
		float[] points=live2DModel.getTransformedPoints(drawIndex);

		float left=live2DModel.getCanvasWidth();
		float right=0;
		float top=live2DModel.getCanvasHeight();
		float bottom=0;

		for (int j = 0; j < points.length; j=j+2)
		{
			float x = points[j];
			float y = points[j+1];
			if(x<left)left=x;	//  最小のx
			if(x>right)right=x;	//  最大のx
			if(y<top)top=y;		//  最小のy
			if(y>bottom)bottom=y;//  最大のy
		}

		float tx=modelMatrix.invertTransformX(testX);
		float ty=modelMatrix.invertTransformY(testY);

		return ( left <= tx && tx <= right && top <= ty && ty <= bottom ) ;
	}
}
