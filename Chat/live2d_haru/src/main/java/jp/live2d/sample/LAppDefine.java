/**
 *
 *  You can modify and use this source freely
 *  only for the development of application related Live2D.
 *
 *  (c) Live2D Inc. All rights reserved.
 */
package jp.live2d.sample;

/*
 * 定数
 */
public class LAppDefine
{
	// デバッグ。trueのときにログを表示する。
	public static boolean DEBUG_LOG=true;
	public static boolean DEBUG_TOUCH_LOG=false;
	public static boolean DEBUG_DRAW_HIT_AREA=false;


	//  全体の設定-------------------------------------------------------------------------------------------
	// 画面
	public static final float VIEW_MAX_SCALE = 2f;
	public static final float VIEW_MIN_SCALE = 0.8f;

	public static final float VIEW_LOGICAL_LEFT = -1;
	public static final float VIEW_LOGICAL_RIGHT = 1;

	public static final float VIEW_LOGICAL_MAX_LEFT = -2;
	public static final float VIEW_LOGICAL_MAX_RIGHT = 2;
	public static final float VIEW_LOGICAL_MAX_BOTTOM = -2;
	public static final float VIEW_LOGICAL_MAX_TOP = 2;

	// モデルの後ろにある背景の画像ファイル
	//public static final String BACK_IMAGE_NAME = "image/back_class_normal.png" ;
	public static final String BACK_IMAGE_NAME = "image/blank.png" ;

	//  モデル定義----------------------------------------------------------------------------------------------------
	public static final String MODEL_HARU		= "live2d/haru/haru.model.json";
	//public static final String MODEL_HARU_A		= "live2d/haru/haru_01.model.json";
	//public static final String MODEL_HARU_B		= "live2d/haru/haru_02.model.json";
	//public static final String MODEL_SHIZUKU	= "live2d/shizuku/shizuku.model.json";
	//public static final String MODEL_WANKO 		= "live2d/wanko/wanko.model.json";

	// 外部定義ファイル(json)と合わせる
	static final String MOTION_GROUP_IDLE		="idle";		// アイドリング
	static final String MOTION_GROUP_TAP_BODY	="tap_body";	// 体をタップしたとき
	static final String MOTION_GROUP_FLICK_HEAD	="flick_head";	// 頭を撫でた時
	static final String MOTION_GROUP_PINCH_IN	="pinch_in";	// 拡大した時
	static final String MOTION_GROUP_PINCH_OUT	="pinch_out";	// 縮小した時
	static final String MOTION_GROUP_SHAKE		="shake";		// シェイク

	// 外部定義ファイル(json)と合わせる
	static final String HIT_AREA_HEAD		="head";
	static final String HIT_AREA_BODY		="body";

	// モーションの優先度定数
	public static final int PRIORITY_NONE		= 0;
	public static final int PRIORITY_IDLE		= 1;
	public static final int PRIORITY_NORMAL		= 2;
	public static final int PRIORITY_FORCE		= 3;

}
