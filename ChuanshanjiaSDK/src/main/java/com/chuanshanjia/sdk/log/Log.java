package com.chuanshanjia.sdk.log;

import java.io.PrintStream;
import java.io.UnsupportedEncodingException;


public class Log{
	public static final int VERBOSE     = 0;
	public static final int DEBUG       = 1;
	public static final int INFO        = 2;
	public static final int WARN        = 3;
	public static final int ERROR       = 4;

	private static boolean _DEBUG = true;

	public static boolean is_DEBUG() {
		return _DEBUG;
	}

	private static int level = ERROR;
	private static PrintStream logOutput;
	private static final String hexDigits = "0123456789abcdef";

	public static void setDebugMode() {
		_DEBUG = false;  
	}

	public static void setLevel( int newLevel ){
		level = newLevel;
	}

	public static void setLogOutput( PrintStream ps ){
		logOutput = ps;
	}

	public static final void e(String tag, String msg ){
		if( _DEBUG ){
			android.util.Log.e( tag, msg );
		}else{
			if( level <= ERROR ){
				LogHelper.log( logOutput, "E", tag, msg, null );
			}
		}
	}

	public static final void e(String tag, String msg, Throwable tr ){
		if( _DEBUG ){
			android.util.Log.e( tag, msg, tr );
		}else{
			if( level <= ERROR ){
				LogHelper.log( logOutput, "E", tag, msg, tr );
			}
		}
	}

	public static final void w(String tag, String msg ){
		if( _DEBUG ){
			android.util.Log.w( tag, msg );
		}else{
			if( level <= ERROR ){
				LogHelper.log( logOutput, "W", tag, msg, null );
			}
		}
	}

	public static final void w(String tag, String msg, Throwable tr ){
		if( _DEBUG ){
			android.util.Log.w( tag, msg, tr );
		}else{
			if( level <= ERROR ){
				LogHelper.log( logOutput, "W", tag, msg, tr );
			}
		}
	}

	public static final void i(String tag, String msg ){
		if( _DEBUG ){
			android.util.Log.i( tag, msg );
		}else{
			if( level <= INFO ){
				LogHelper.log( logOutput, "I", tag, msg, null );
			}
		}
	}

	public static final void i(String tag, String msg, Throwable tr ){
		if( _DEBUG ){
			android.util.Log.i( tag, msg, tr );
		}else{
			if( level <= INFO ){
				LogHelper.log( logOutput, "I", tag, msg, tr );
			}
		}
	}

	public static final void d(String tag, String msg ){
		if( _DEBUG ){
			android.util.Log.d( tag, msg );
		}else{
			if( level <= DEBUG ){
				LogHelper.log( logOutput, "D", tag, msg, null );
			}
		}
	}

	public static final void d(String tag, String msg, Throwable tr ){
		if( _DEBUG ){
			android.util.Log.d( tag, msg, tr );
		}else{
			if( level <= DEBUG ){
				LogHelper.log( logOutput, "D", tag, msg, tr );
			}
		}
	}

	public static final void v(String tag, String msg ){
		if( _DEBUG ){
			android.util.Log.v( tag, msg );
		}else{
			if( level <= VERBOSE ){
				LogHelper.log( logOutput, "V", tag, msg, null );
			}
		}
	}

	public static final void v(String tag, String msg, Throwable tr ){
		if( _DEBUG ){
			android.util.Log.v( tag, msg, tr );
		}else{
			if( level <= VERBOSE ){
				LogHelper.log( logOutput, "V", tag, msg, tr );
			}
		}
	}

	public static final String dumpHex(byte[] bytes, int offset, int length ){
		StringBuilder sb = new StringBuilder();
		int end = Math.min( offset + length, bytes.length );
		while( offset < end ){
			byte b = bytes[ offset ];
			sb.append( " " )
			.append( hexDigits.charAt( ( b >>> 4 ) & 0x0f ) )
			.append( hexDigits.charAt( b & 0x0f ) );
			++ offset;
		}
		return sb.length() > 1 ? sb.substring( 1 ) : sb.toString();
	}

	public static final String dumpAscii(byte[] bytes, int offset, int length ){
		try{
			return new String( bytes, offset, length, "US-ASCII" );
		}catch( UnsupportedEncodingException e ){
			return "Can not decode as US-ASCII.";
		}
	}
}
