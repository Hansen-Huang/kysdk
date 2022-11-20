package com.chuanshanjia.sdk.log;

import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class LogHelper{
  /**
   * Never instantiate this class.
   */
  @SuppressWarnings( "unused" )
  private void log(){
  }
  
  private static SimpleDateFormat sdf = new SimpleDateFormat( "MM-dd kk:mm:ss.SSS" );
  
  public static void log(PrintStream ps, String level, String tag, String msg, Throwable throwable ){
    if( ps != null ){
      StringBuilder sb = new StringBuilder();
      sb.append( "[" )
          .append( sdf.format( new Date() ) )
          .append( "] " )
          .append( String.format( "%-10s ", Thread.currentThread().getName() ) )
          .append( level )
          .append( "/" )
          .append( tag )
          .append( " " )
          .append( msg );
      ps.println( sb.toString() );
      if( throwable != null ){
        throwable.printStackTrace( ps );
      }
      ps.flush();
    }
  }
}
