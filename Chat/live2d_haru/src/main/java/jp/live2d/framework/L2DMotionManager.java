/**
 *
 *  You can modify and use this source freely
 *  only for the development of application related Live2D.
 *
 *  (c) Live2D Inc. All rights reserved.
 */
package jp.live2d.framework;

import jp.live2d.ALive2DModel ;
import jp.live2d.motion.AMotion ;
import jp.live2d.motion.MotionQueueManager ;

/*
 * L2DMotionManagerは、優先度(priority)を指定してモーションの再生を管理するためのクラスです。
 *
 * 主に親クラスのMotionQueueManager（標準のLive2Dライブラリ）に不足する以下の機能を補います。
 *
 * １．割り込みの制御
 *
 * 親クラスMotionQueueManagerでは、startMotionを呼び出すと新しいモーションが割り込みで
 * スタートし既存のモーションは終了します。（前後のモーションは滑らかに繋がります）
 *
 * L2DMotionManagerではセリフ等の割り込まれたくないモーションの場合に、割り込みを防ぐ仕組みを
 * 提供します。priority が同じ場合は、割り込みが発生せずに新しいモーションを無視します。
 *
 *
 * ２．音声のロードとの連携
 *
 * タップなどのイベントが発生した際に、音声のロードが完了しておらずモーションを即時開始すると
 * ズレてしまう場合があります。そのようなケースのために、次フレーム以降で再生することを予約する
 * 仕組みを提供します。
 *
 *
 *
 */
public class L2DMotionManager extends MotionQueueManager{

	//  メインモーションの優先度
	//  標準設定 0:再生してない 1:アイドリング(割り込んで良い) 2:通常(基本割り込みなし) 3:強制で開始
	private int currentPriority;//  現在再生中のモーションの優先度
	private int reservePriority;//  再生予定のモーションの優先度。再生中は0になる。モーションファイルを別スレッドで読み込むときの機能。


	/*
	 * 再生中のモーションの優先度
	 * @return
	 */
	public int getCurrentPriority()
	{
		return currentPriority;
	}


	/*
	 * 予約中のモーションの優先度
	 * @return
	 */
	public int getReservePriority()
	{
		return reservePriority;
	}


	/*
	 * 次に再生したいモーションのpriorityを渡して、再生予約できる状況か判断する
	 *
	 *
	 * @param priority
	 * @return
	 */
	public boolean reserveMotion(int priority)
	{
		if( reservePriority >= priority)
		{
			return false;// 再生予約がある(別スレッドで準備している)
		}
		if( currentPriority >= priority ){
			return false;// 再生中のモーションがある
		}
		reservePriority=priority;// モーション再生が非同期の場合は優先度を先に設定して予約しておく
		return true;
	}


	/*
	 * モーションを予約する
	 * @param val
	 */
	public void setReservePriority(int val)
	{
		reservePriority = val;
	}


	@Override
	public boolean updateParam(ALive2DModel model)
	{
		boolean updated=super.updateParam(model);
		if(isFinished()){
			currentPriority=0;// 再生中モーションの優先度を解除
		}
		return updated;
	}


	public int startMotionPrio(AMotion motion,int priority)
	{
		if(priority==reservePriority)
		{
			reservePriority=0;// 予約を解除
		}
		currentPriority=priority;// 再生中モーションの優先度を設定
		return super.startMotion(motion, false);//  第二引数はモーションデータを自動で削除するかどうか。Javaでは関係なし
	}
}
