/**
 *
 *  You can modify and use this source freely
 *  only for the development of application related Live2D.
 *
 *  (c) Live2D Inc. All rights reserved.
 */
package jp.live2d.utils.android;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

/*
 * Javaのバッファのユーティリティ。
 * OpenGLでバッファクラスの情報が必要なため
 *
 */
public class BufferUtil {

	/*
	 * FloatBufferを作成する。
	 * @param floatCount
	 * @return
	 */
	public static FloatBuffer createFloatBuffer( int floatCount ) {
		ByteBuffer data = ByteBuffer.allocateDirect( floatCount * 4);
	    data.order(ByteOrder.nativeOrder());
		FloatBuffer p1 = data.asFloatBuffer() ;
		return p1;
	}


	/*
	 * FloatBufferを初期化する。
	 * @param preBuffer
	 * @param array この配列の値で初期化する
	 * @return
	 */
	public static FloatBuffer setupFloatBuffer( FloatBuffer preBuffer , float []array){
		// nullか、容量不足のときに再生成
		if( preBuffer == null || preBuffer.capacity() < array.length ){
			preBuffer = createFloatBuffer( array.length * 2 ) ;// ２倍のサイズで作成
		}
		else{
			preBuffer.clear() ;
		}
		preBuffer.put(array) ;
		preBuffer.position(0) ;
		return preBuffer ;
	}


	/*
	 * ShortBufferを作成する。
	 * @param shortCount
	 * @return
	 */
	public static ShortBuffer createShortBuffer( int shortCount ) {
		ByteBuffer data = ByteBuffer.allocateDirect( shortCount * 4);
	    data.order(ByteOrder.nativeOrder());
	    ShortBuffer p1 = data.asShortBuffer() ;
		return p1;
	}


	/*
	 * ShortBufferを初期化する。
	 * @param preBuffer
	 * @param array この配列の値で初期化する
	 * @return
	 */
	public static ShortBuffer setupShortBuffer( ShortBuffer preBuffer , short[] array ) {
		// nullか、容量不足のときに再生成
		if( preBuffer == null || preBuffer.capacity() < array.length ){
			preBuffer = createShortBuffer( array.length * 2 ) ;// ２倍のサイズで作成
		}
		else{
			preBuffer.clear() ;
		}

		preBuffer.clear() ;
		preBuffer.put(array) ;
		preBuffer.position(0) ;

		return preBuffer ;
	}


	/*
	 * ByteBufferを作成する。
	 * @param count
	 * @return
	 */
	public static ByteBuffer createByteBuffer( int count ) {
		ByteBuffer data = ByteBuffer.allocateDirect( count * 4);
	    data.order(ByteOrder.nativeOrder());
		return data ;
	}


	public static ByteBuffer setupByteBuffer( ByteBuffer preBuffer , byte []array){
		// nullか、容量不足のときに再生性
		if( preBuffer == null || preBuffer.capacity() < array.length ){
			preBuffer = createByteBuffer( array.length * 2 ) ;// ２倍のサイズで作成
		}
		else{
			preBuffer.clear() ;
		}
		preBuffer.put(array) ;
		preBuffer.position(0) ;
		return preBuffer ;
	}


	public static Buffer setupIntBuffer(IntBuffer preBuffer,int []array){
			// nullか、容量不足のときに再生成
			if( preBuffer == null || preBuffer.capacity() < array.length ){
				preBuffer = createIntBuffer( array.length * 2 ) ;// ２倍のサイズで作成
			}
			else{
				preBuffer.clear() ;
			}

			preBuffer.clear() ;
			preBuffer.put(array) ;
			preBuffer.position(0) ;

			return preBuffer ;
	}


	public static IntBuffer createIntBuffer(int count) {
		ByteBuffer data = ByteBuffer.allocateDirect( count * 4);
	    data.order(ByteOrder.nativeOrder());
		IntBuffer p1 = data.asIntBuffer() ;
		return p1;
	}
}
