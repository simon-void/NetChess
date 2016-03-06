import java.net.*;
import java.io.*;

class ChessClient {

  private Player player;
  private ReaderThread reader;
  private BufferedWriter out;
  private Socket socket;
  private OutputStream outStream;

  public ChessClient( Socket socket,Player player ) {
    this.player=player;
    this.socket=socket;
    try{
      socket.setSoTimeout(0);
      outStream=socket.getOutputStream();
    }catch( IOException e ) {
      e.printStackTrace();
      System.exit(0);
    }
    out=new BufferedWriter( new OutputStreamWriter( outStream ) );
    try{
      reader=new ReaderThread( socket.getInputStream(),this );
    }catch( IOException e ) {
      e.printStackTrace();
      System.exit(0);
    }
    reader.start();
  }

  public void write( String msg ) {
    try{
      out.write( msg+"\n" );
      out.flush();
    }catch( IOException e ) {
      close();
    }
  }

  private void read( String msg ) {
    if( msg!=null ) {
      player.readClient( msg );
    }else{
      close();
    }
  }

  public void close() {
    try{
      out.close();
      reader.close();
      socket.close();
      System.out.println( "Serververbindung beendet" );
      System.exit( 1 );
    }catch( IOException e ) {
      System.exit(1);
    }
  }


  class ReaderThread
  extends Thread {

    private boolean stopp;
    private ChessClient client;
    private InputStream inStream;
    private BufferedReader in;

    public ReaderThread( InputStream inStream,ChessClient client ) {
      in=new BufferedReader( new InputStreamReader( inStream ) );
      this.inStream=inStream;
      this.client=client;
      stopp=false;
    }

    public void close() {
      stopp=true;
    }

    public void run() {
      try{
        while( !stopp ) {
          read( in.readLine() );
        }
      }catch( IOException e ) {}
      try{
        inStream.close();
      }catch( IOException e ) {
        e.printStackTrace();
      }
      client.close();
    }
  }

}
