/**
 *
 *  You can modify and use this source freely
 *  only for the development of application related Live2D.
 *
 *  (c) Live2D Inc. All rights reserved.
 */
package jp.live2d.utils.android;

import java.util.Map;



public interface ModelSetting {

	// モデルデータについて
	String getModelName()		 ;
	String getModelFile()		 ;

	// テクスチャについて
	int getTextureNum()			 ;
	String getTextureFile(int n) ;
	String[] getTextureFiles() ;

	// あたり判定について
	int getHitAreasNum()		;
	String getHitAreaID(int n)	;
	String getHitAreaName(int n);

	// 物理演算、パーツ切り替え、表情ファイルについて
	String getPhysicsFile()		;
	String getPoseFile()		;
	int getExpressionNum();
	String getExpressionFile(int n)	;
	String[] getExpressionFiles()	;
	String getExpressionName(int n)	;
	String[] getExpressionNames()	;

	// モーションについて
	String[] getMotionGroupNames();
	int getMotionNum(String name);

	String getMotionFile(String name,int n)		;
	String getMotionSound(String name,int n)	;
	int getMotionFadeIn(String name,int n)		;
	int getMotionFadeOut(String name,int n)		;

	// 表示位置
	boolean getLayout(Map<String,Float> layout);

	// 初期パラメータについて
	int getInitParamNum();
	float getInitParamValue(int n);
	String getInitParamID(int n);

	// 初期パーツ表示について
	int getInitPartsVisibleNum();
	float getInitPartsVisibleValue(int n);
	String getInitPartsVisibleID(int n);

	String[] getSoundPaths() ;
}
