import java.net.*;
import java.io.*;
import java.util.*;

class Player {

  private String name;
  private HashMap chessboards;
  private MainFrame mainframe;
  private ChessClient client;

  public Player( String name,Socket socket,String mainboardInfo ) {
    client=new ChessClient( socket,this );
    this.name=name;
    chessboards=new HashMap( 10 );
    mainframe=new MainFrame( this,mainboardInfo );
  }

  public void openBoard( String info ) {
    int tablenr,pos;
    if( info.startsWith( "open:" ) ) {
      tablenr=Integer.parseInt( info.substring( 5 ) );
      chessboards.put( new Integer( tablenr ),new ChessFrame( this,tablenr ) );
    }else{
      info=info.substring( 5 );
      pos=info.indexOf( ":" );
      tablenr=Integer.parseInt( info.substring( 0,pos ) );
      chessboards.put( new Integer( tablenr ),new ChessFrame( this,tablenr,info.substring( pos+1 ) ) );
    }
    if( chessboards.size()==1 ) {
      mainframe.enableExit( false );
    }
  }

  public void closeBoard( ChessFrame board ) {
    chessboards.remove( new Integer( board.getNr() ) );
    board.dispose();
    if( chessboards.size()==0 ) {
      mainframe.enableExit( true );
    }
  }

  public String getPlayerName() {
    return name;
  }

  public void readClient( String msg ) {
    writeFrame( msg );
  }

  public void writeClient( String msg ) {
    client.write( msg );
  }

  public void readFrame( String msg ) {
    writeClient( msg );
  }

  public void writeFrame( String msg ) {
    try{
      if( msg.startsWith( "table" ) ) {
        int pos=msg.indexOf( ":" );
        int tablenr=Integer.parseInt( msg.substring( 5,pos ) );
        ((ChessFrame)chessboards.get( new Integer( tablenr ) )).write( msg.substring( pos+1 ) );
      }else if( msg.startsWith( "mainframe" ) ) {
        mainframe.write( msg.substring( 10 ) );
      }else if( msg.startsWith( "comand:" ) ) {
        msg=msg.substring( 7 );
        if( msg.startsWith( "open:" ) ) {
          openBoard( msg );
        }else if( msg.startsWith( "join:" ) ) {
          openBoard( msg );
        }else if( msg.startsWith( "update:" ) ) {
          mainframe.updateInfo( msg.substring( 7 ) );
        }else{
          System.out.println( "Player.writeFrame:"+msg );
        }
      }else{
        System.out.println( "Player.writeFrame:"+msg );
      }
    }catch( NumberFormatException e ) {
      System.out.println( "Player.writeFrame:\n"+e.toString() );
    }
  }

  public void close() {
    client.close();
  }

}
