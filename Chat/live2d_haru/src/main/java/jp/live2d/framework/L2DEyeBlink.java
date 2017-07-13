/**
 *
 *  You can modify and use this source freely
 *  only for the development of application related Live2D.
 *
 *  (c) Live2D Inc. All rights reserved.
 */
package jp.live2d.framework;

import jp.live2d.ALive2DModel ;
import jp.live2d.util.UtSystem ;

/*
 * 目パチモーション。
 * Live2DライブラリのEyeBlinkMotionクラスとほぼ同一。
 * 使用はどちらでも良いが拡張する場合はこちらを使う。
 */
public class L2DEyeBlink
{
	// 眼の状態定数
	enum EYE_STATE
	{
		STATE_FIRST ,
		STATE_INTERVAL ,
		STATE_CLOSING ,	// 閉じていく途中
		STATE_CLOSED ,	// 閉じている状態
		STATE_OPENING ,	// 開いていく途中
	};

	// ---- 内部データ ----
	long nextBlinkTime ;// 次回眼パチする時刻（msec）
	long stateStartTime ;// 現在のstateが開始した時刻

	EYE_STATE eyeState ;// 現在の状態

	boolean closeIfZero;// IDで指定された眼のパラメータが、0のときに閉じるなら true 、1の時に閉じるなら false

	String eyeID_L ;
	String eyeID_R ;
	// ------------ 設定 ------------
	int blinkIntervalMsec ;

	int closingMotionMsec ;// 眼が閉じるまでの時間
	int closedMotionMsec  ;// 閉じたままでいる時間
	int openingMotionMsec ;// 眼が開くまでの時間


	public L2DEyeBlink()
	{
		eyeState = EYE_STATE.STATE_FIRST ;

		blinkIntervalMsec = 4000 ;

		closingMotionMsec = 100 ;// 眼が閉じるまでの時間
		closedMotionMsec  =  50 ;// 閉じたままでいる時間
		openingMotionMsec = 150 ;// 眼が開くまでの時間

		closeIfZero = true ;// IDで指定された眼のパラメータが、0のときに閉じるなら true 、1の時に閉じるなら false

		eyeID_L = "PARAM_EYE_L_OPEN" ;
		eyeID_R = "PARAM_EYE_R_OPEN" ;
	}


	/*
	 * 次回の眼パチの時刻を決める。
	 * @return
	 */
	public long calcNextBlink()
	{
		long time = UtSystem.getUserTimeMSec() ;
		double r = Math.random() ;//0..1
		return (long) (time + r*( 2*blinkIntervalMsec - 1 )) ;
	}


	public void setInterval( int blinkIntervalMsec)
	{
		this.blinkIntervalMsec = blinkIntervalMsec ;
	}


	public void setEyeMotion( int closingMotionMsec , int closedMotionMsec , int openingMotionMsec )
	{
		this.closingMotionMsec = closingMotionMsec ;
		this.closedMotionMsec = closedMotionMsec ;
		this.openingMotionMsec = openingMotionMsec ;
	}


	/*
	 * モデルのパラメータを更新。
	 * @param model
	 */
	public void updateParam(ALive2DModel model)
	{
		long time = UtSystem.getUserTimeMSec() ;
		float eyeParamValue ;// 設定する値
		float t = 0 ;

		switch( this.eyeState )
		{
		case STATE_CLOSING:
			// 閉じるまでの割合を0..1に直す(blinkMotionMsecの半分の時間で閉じる)
			t = ( time - stateStartTime ) / (float)closingMotionMsec ;
			if( t >= 1 ){
				t = 1 ;
				this.eyeState = EYE_STATE.STATE_CLOSED ;// 次から開き始める
				this.stateStartTime = time ;
			}
			eyeParamValue = 1 - t ;
			break ;
		case STATE_CLOSED:
			t = ( time - stateStartTime ) / (float)closedMotionMsec ;
			if( t >= 1 ){
				this.eyeState = EYE_STATE.STATE_OPENING ;// 次から開き始める
				this.stateStartTime = time ;
			}
			eyeParamValue = 0 ;// 閉じた状態
			break ;
		case STATE_OPENING:
			t = ( time - stateStartTime ) / (float)openingMotionMsec ;
			if( t >= 1 ){
				t = 1 ;
				this.eyeState = EYE_STATE.STATE_INTERVAL ;// 次から開き始める
				this.nextBlinkTime = calcNextBlink() ;// 次回のまばたきのタイミングを始める時刻
			}
			eyeParamValue = t ;
			break ;
		case STATE_INTERVAL:
			// まばたき開始時刻なら
			if( this.nextBlinkTime < time ){
				this.eyeState = EYE_STATE.STATE_CLOSING ;
				this.stateStartTime = time ;
			}
			eyeParamValue = 1 ;// 開いた状態
			break ;
		case STATE_FIRST:
		default:
			this.eyeState = EYE_STATE.STATE_INTERVAL ;
			this.nextBlinkTime = calcNextBlink() ;// 次回のまばたきのタイミングを始める時刻
			eyeParamValue = 1 ;// 開いた状態
			break ;
		}

		if( ! closeIfZero ) eyeParamValue = -eyeParamValue ;

		// ---- 値を設定 ----
		model.setParamFloat( eyeID_L , eyeParamValue ) ;
		model.setParamFloat( eyeID_R , eyeParamValue ) ;
	}
}
