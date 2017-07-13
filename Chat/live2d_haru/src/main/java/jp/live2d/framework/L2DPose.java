/**
 *
 *  You can modify and use this source freely
 *  only for the development of application related Live2D.
 *
 *  (c) Live2D Inc. All rights reserved.
 */
package jp.live2d.framework;


import java.io.InputStream ;
import java.util.ArrayList ;

import jp.live2d.ALive2DModel ;
import jp.live2d.id.PartsDataID ;
import jp.live2d.util.Json ;
import jp.live2d.util.Json.Value ;
import jp.live2d.util.UtFile ;
import jp.live2d.util.UtSystem ;

/*
 * パーツの切り替えを管理する。
 *
 */
public class L2DPose
{
	private ArrayList< L2DPartsParam[] > partsGroups;
	private long lastTime=0;
	private ALive2DModel lastModel=null;// パラメータインデックスが初期化されてるかどうかのチェック用。


	public L2DPose()
	{
		partsGroups=new ArrayList<L2DPartsParam[]>();
	}


	/*
	 * モデルのパラメータを更新。
	 * @param model
	 */
	public void updateParam(ALive2DModel model)
	{
		if ( model == null ) return;

		// 前回のモデルと同じではないので初期化が必要
		if( ! model.equals(lastModel) )
		{
			//  パラメータインデックスの初期化
			initParam(model);
		}

		lastModel = model;

		long  curTime = UtSystem.getTimeMSec();
		float deltaTimeSec = ( (lastTime == 0 ) ? 0 : ( curTime - lastTime )/1000.0f);
		lastTime = curTime;

		// 設定から時間を変更すると、経過時間がマイナスになることがあるので、経過時間0として対応。
		if (deltaTimeSec < 0) deltaTimeSec = 0;

		for (int i = 0; i < partsGroups.size(); i++)
		{
			normalizePartsOpacityGroup(model,partsGroups.get(i),deltaTimeSec);
			copyOpacityOtherParts(model, partsGroups.get(i));
		}
	}


	/*
	 * 表示を初期化。
	 * αの初期値が0でないパラメータは、αを1に設定する。
	 * @param model
	 */
	public void initParam(ALive2DModel model)
	{
		if ( model == null ) return;

		for (int i = 0; i < partsGroups.size(); i++)
		{
			L2DPartsParam partsGroup[]=partsGroups.get(i);
			for (int j = 0 ; j < partsGroup.length ; j++ )
			 {
				partsGroup[j].initIndex(model);

				 int partsIndex=partsGroup[j].partsIndex;
				 int paramIndex=partsGroup[j].paramIndex;
				 if(partsIndex<0)continue;// 存在しないパーツです

				 boolean v = ( model.getParamFloat( paramIndex ) != 0 ) ;
				 model.setPartsOpacity(partsIndex , (v ? 1.0f : 0.0f) ) ;
				 model.setParamFloat(paramIndex , (v ? 1.0f : 0.0f) ) ;

				 if(partsGroup[j].link==null)continue;
				 for(int k=0;k<partsGroup[j].link.size();k++)
				 {
					 partsGroup[j].link.get(k).initIndex(model);
				 }
			 }
		}
	}


	/*
	 * パーツのフェードイン、フェードアウトを設定する。
	 * @param model
	 * @param partsGroup
	 * @param deltaTimeSec
	 */
	public void normalizePartsOpacityGroup( ALive2DModel model, L2DPartsParam partsGroup[] , float deltaTimeSec )
	{
		 int visibleParts = -1 ;
		 float visibleOpacity = 1.0f ;

		 float CLEAR_TIME_SEC = 0.5f ;// この時間で不透明になる
		 float phi = 0.5f ;// 背景が出にくいように、１＞０への変化を遅らせる場合は、0.5よりも大きくする。ただし、あまり自然ではない
		 float maxBackOpacity = 0.15f ;


		 //  現在、表示状態になっているパーツを取得
		 for (int i = 0 ; i <  partsGroup.length; i++ )
		 {
			 int partsIndex=partsGroup[i].partsIndex;
			 int paramIndex=partsGroup[i].paramIndex;

			 if(partsIndex<0)continue;// 存在しないパーツです

			 if( model.getParamFloat( paramIndex ) != 0 )
			 {
				 if( visibleParts >= 0 )
				 {
					 break ;
				 }
				 visibleParts = i ;
				 visibleOpacity = model.getPartsOpacity(partsIndex) ;

				 //  新しいOpacityを計算
				 visibleOpacity += deltaTimeSec / CLEAR_TIME_SEC ;
				 if( visibleOpacity > 1 )
				{
					 visibleOpacity = 1 ;
				}
			 }
		 }

		 if( visibleParts < 0 )
		 {
			 visibleParts = 0 ;
			 visibleOpacity = 1 ;
		 }

		 //  表示パーツ、非表示パーツの透明度を設定する
		 for (int i = 0 ; i <  partsGroup.length ; i++ )
		 {
			 int partsIndex=partsGroup[i].partsIndex;
			 if(partsIndex<0)continue;// 存在しないパーツです

			 //  表示パーツの設定
			 if( visibleParts == i )
			 {
				 model.setPartsOpacity(partsIndex , visibleOpacity ) ;// 先に設定
			 }
			 //  非表示パーツの設定
			 else
			 {
				 float opacity = model.getPartsOpacity(partsIndex) ;
				 float a1 ;// 計算によって求められる透明度
				 if( visibleOpacity < phi )
				 {
					 a1 = visibleOpacity*(phi-1)/phi + 1 ; //  (0,1),(phi,phi)を通る直線式
				 }
				 else
				 {
					 a1 = (1-visibleOpacity)*phi/(1-phi) ; //  (1,0),(phi,phi)を通る直線式
				 }

				 // 背景の見える割合を制限する場合
				 float backOp = (1-a1)*(1-visibleOpacity) ;// 背景の
				 if( backOp > maxBackOpacity )
				 {
					 a1 = 1 - maxBackOpacity/( 1- visibleOpacity ) ;
				 }

				 if( opacity > a1 )
				{
					 opacity = a1 ;//  計算の透明度よりも大きければ（濃ければ）透明度を上げる
				}
				 model.setPartsOpacity(partsIndex , opacity ) ;
			 }
		 }
	 }


	/*
	 * パーツのαを連動する。
	 * @param model
	 * @param partsGroup
	 */
	public void copyOpacityOtherParts(ALive2DModel model, L2DPartsParam partsGroup[])
	{
		for (int i_group = 0; i_group < partsGroup.length; i_group++)
		{
			L2DPartsParam partsParam = partsGroup[i_group];

			if(partsParam.link==null)continue;// リンクするパラメータはない
			if(partsParam.partsIndex<0)continue;// 存在しないパーツ

			float opacity = model.getPartsOpacity( partsParam.partsIndex );

			for (int i_link = 0; i_link < partsParam.link.size(); i_link++)
			{
				L2DPartsParam linkParts = partsParam.link.get(i_link);

				if(linkParts.partsIndex<0)continue;// 存在しないパーツ
				model.setPartsOpacity(linkParts.partsIndex, opacity);
			}
		}
	}


	/*
	 * JSONファイルから読み込む
	 * 仕様についてはマニュアル参照。JSONスキーマの形式の仕様がある。
	 * @param in
	 * @return
	 * @throws IOException
	 */
	public static L2DPose load(InputStream in) throws Exception
	{
		byte[] buf = UtFile.load( in ) ;
		return load(buf);
	}


	/*
	 * JSONファイルから読み込む
	 * 仕様についてはマニュアル参照。JSONスキーマの形式の仕様がある。
	 * @param buf
	 * @return
	 * @throws Exception
	 */
	public static L2DPose load(byte[] buf ) throws Exception
	{
		L2DPose ret = new L2DPose();

		Value json = Json.parseFromBytes( buf ) ;

		// パーツ切り替え一覧
		Value poseListInfo = json.get("parts_visible");
		int poseNum = poseListInfo.getVector(null).size();

		for (int i_pose = 0; i_pose < poseNum; i_pose++) {
			Value poseInfo = poseListInfo.get(i_pose);

			// IDリストの設定
			Value idListInfo = poseInfo.get("group");
			int idNum = idListInfo.getVector(null).size();
			L2DPartsParam[] partsGroup=new L2DPartsParam[idNum];
			for (int i_group = 0; i_group < idNum; i_group++)
			{
				Value partsInfo=idListInfo.get(i_group);
				L2DPartsParam parts=new L2DPartsParam(partsInfo.get("id").toString());
				partsGroup[i_group] = parts;

				// リンクするパーツの設定
				if(partsInfo.get("link")==null)continue;// リンクが無いときもある
				Value linkListInfo = partsInfo.get("link");
				int linkNum = linkListInfo.getVector(null).size();
				parts.link=new ArrayList<L2DPartsParam>();

				for (int i_link = 0; i_link< linkNum; i_link++)
				{
					L2DPartsParam linkParts=new L2DPartsParam(linkListInfo.get(i_link).toString());
					parts.link.add(linkParts);
				}
			}
			ret.partsGroups.add(partsGroup);
		}
		return ret;
	}
}


/*
 * パーツインデックスを保持するクラス。
 * パーツにはパーツIDとモーションから設定するパーツパラメータIDがある。
 * 文字列で設定することもできるが、インデックスを取得してから設定したほうが高速。
 */
class L2DPartsParam
{
	String id;
	int paramIndex=-1;
	int partsIndex=-1;

	ArrayList<L2DPartsParam> link=null;// 連動するパーツ


	public L2DPartsParam(String id)
	{
		this.id=id;
	}


	/*
	 * パラメータとパーツのインデックスを初期化する。
	 * @param model
	 */
	public void initIndex(ALive2DModel model)
	{
		paramIndex=model.getParamIndex("VISIBLE:"+id);// パーツ表示のパラメータはVISIBLE:がつく。Live2Dアニメータの仕様。

		partsIndex=model.getPartsDataIndex(PartsDataID.getID(id));
		model.setParamFloat(paramIndex, 1);
		//Log.d("live2d",id+ " param:"+paramIndex+" parts:"+partsIndex);
	}
}