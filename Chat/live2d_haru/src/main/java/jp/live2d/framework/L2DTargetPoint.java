/**
 *
 *  You can modify and use this source freely
 *  only for the development of application related Live2D.
 *
 *  (c) Live2D Inc. All rights reserved.
 */
package jp.live2d.framework;

import jp.live2d.util.UtSystem ;

public class L2DTargetPoint {
	static public final int FRAME_RATE=30;// ドラッグの距離計算にのみ使う擬似フレームレート

	private float faceTargetX = 0 ;// 顔の向きの目標値（この値に近づいていく）
	private float faceTargetY = 0 ;

	private float faceX = 0 ;// 顔の向き -1..1
	private float faceY = 0 ;

	private float faceVX = 0 ;// 顔の向きの変化速度
	private float faceVY = 0 ;

	private long lastTimeSec = 0 ;


	public void set( float x , float y  )
	{
		faceTargetX = x ;
		faceTargetY = y ;
	}


	/*
	 * 横方向の値。
	 * @return -1から1の値
	 */
	public float getX()
	{
		return faceX;
	}


	/*
	 * 縦方向の値。
	 * @return -1から1の値
	 */
	public float getY()
	{
		return faceY;
	}


	/*
	 * 更新
	 * 首を中央から左右に振るときの平均的な早さは  秒程度。加速・減速を考慮して、その２倍を最高速度とする
	 * 顔のふり具合を、中央（０）から、左右は（±１）とする
	 */
	public void update()
	{
		// 計算用の設定
		final float TIME_TO_MAX_SPEED = 0.15f ;// 最高速度になるまでの時間
		final float FACE_PARAM_MAX_V = 40.0f / 7.5f ;// 7.5秒間に40分移動（5.3/sc)

		final float MAX_V =  FACE_PARAM_MAX_V / FRAME_RATE ;// 1frameあたりに変化できる速度の上限

		if( lastTimeSec == 0 )
		{
			lastTimeSec = UtSystem.getUserTimeMSec() ;
			return ;
		}

		long curTimeSec = UtSystem.getUserTimeMSec() ;

		float deltaTimeWeight = (float)(curTimeSec - lastTimeSec)*FRAME_RATE/1000.0f ;
		lastTimeSec = curTimeSec ;

		final float FRAME_TO_MAX_SPEED = TIME_TO_MAX_SPEED * FRAME_RATE  ;//sec*frame/sec
		final float MAX_A = deltaTimeWeight * MAX_V / FRAME_TO_MAX_SPEED ;// 1frameあたりの加速度

		float dx = (faceTargetX - faceX) ;
		float dy = (faceTargetY - faceY) ;

		if( dx == 0 && dy == 0 ) return ;
		float d = (float) Math.sqrt( dx*dx + dy*dy ) ;

		float vx = MAX_V * dx / d ;
		float vy = MAX_V * dy / d ;

		float ax = vx - faceVX ;
		float ay = vy - faceVY ;

		float a = (float) Math.sqrt( ax*ax + ay*ay ) ;

		if( a < -MAX_A || a > MAX_A )
		{
			ax *= MAX_A / a ;
			ay *= MAX_A / a ;
			a = MAX_A ;
		}

		faceVX += ax ;
		faceVY += ay ;

		{
			//            2  6           2               3
			//      sqrt(a  t  + 16 a h t  - 8 a h) - a t
			// v = --------------------------------------
			//                    2
			//                 4 t  - 2
			//(t=1)

			float max_v = 0.5f * ( (float)Math.sqrt( MAX_A*MAX_A + 16*MAX_A * d - 8*MAX_A * d ) - MAX_A ) ;
			float cur_v = (float) Math.sqrt( faceVX*faceVX + faceVY*faceVY ) ;

			if( cur_v > max_v )
			{
				faceVX *= max_v / cur_v ;
				faceVY *= max_v / cur_v ;
			}
		}

		faceX += faceVX ;
		faceY += faceVY ;
	}
}
