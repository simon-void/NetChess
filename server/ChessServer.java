import java.net.*;
import java.io.*;
import java.util.*;

class ChessServer {

  private Hashtable players;
  private Hashtable tables;

  public static void main( String[] args ) {
    new ChessServer();
  }

  public ChessServer() {
    Socket socket;
    players=new Hashtable( 30 );
    tables=new Hashtable( 20 );
    try{
      ServerSocket server=new ServerSocket( 2309,50 );
      StringBuffer buffer=new StringBuffer( "Server läuft\nIP:   " );
      buffer.append( InetAddress.getLocalHost().getHostAddress() );
      buffer.append( "\nName: " );
      buffer.append( InetAddress.getLocalHost().getHostName() );
      buffer.append( "\nzum Abrechen STRG+C drücken" );
      protocol( buffer.toString() );
      while( true ) {
        socket=server.accept();
        new NewUserProcessingThread( socket,this );
      }
    }catch(Exception e){
      protocol( "ServerException in accept()\nServer is down" );
    }
  }

  public void protocol( String msg ) {
    System.out.println( msg );
  }

  public void addPlayer( InternPlayer player ) {
    String name=player.getName();
    StringBuffer buffer=new StringBuffer( "comand:update:player:new:" );
    buffer.append( name );
    player.informAllOther( buffer.toString() );
    players.put( name,player );
  }

  public synchronized void removePlayer( InternPlayer player ) {
    String name=player.getName();
    StringBuffer buffer=new StringBuffer( "comand:update:player:remove:" );
    buffer.append( name );
    synchronized( this ) {
      player.informAllOther( buffer.toString() );
      players.remove( name );
      InternBoard board;
      HashSet loneBoards=new HashSet( 4 );
      BoardIterator iterator=getBoards();
      while( iterator.hasNext() ) {
        board=iterator.next();
        if( board.containsPlayer( name ) ) {
          loneBoards.add( board );
        }
      }
      Iterator iter=loneBoards.iterator();
      while( iter.hasNext() ) {
        ((InternBoard)iter.next()).removePlayer( player );
      }
    }
    player.close();
  }

  public synchronized int addBoard( InternPlayer player ) {
    int boardmax=tables.size()+1;
    Integer key=new Integer( 0 );
    for( int i=1;i<=boardmax;i++ ) {
      key=new Integer( i );
      if( !tables.containsKey( key ) ) {
        break;
      }
    }
    InternBoard board=new InternBoard( this,key.intValue(),player );
    tables.put( key,board );
    board.addPlayer( player );
    return key.intValue();
  }

  public synchronized void removeBoard( InternBoard empty ) {
    tables.remove( new Integer( empty.getNr() ) );
  }

  public InternBoard getBoard( String nr ) {
    return (InternBoard)tables.get( new Integer( nr ) );
  }

  public synchronized String getBoardCode( String name ) {
    StringBuffer buffer=new StringBuffer();
    buffer.append( players.size()+1 );
    buffer.append( ":" );
    buffer.append( name );
    PlayerIterator playerIterator=getPlayers();
    while( playerIterator.hasNext() ) {
      buffer.append( ":" );
      buffer.append( playerIterator.next().getName() );
    }
    BoardIterator boards=getBoards();
    while( boards.hasNext() ) {
      buffer.append( ":" );
      buffer.append( boards.next().getSmallCode() );
    }
    return buffer.toString();
  }

  public PlayerIterator getPlayers() {
    return new PlayerIterator() {

      private Iterator iterator=players.values().iterator();

      public boolean hasNext() {
        return iterator.hasNext();
      }

      public InternPlayer next() {
        return (InternPlayer)iterator.next();
      }
    };
  }

  public BoardIterator getBoards() {
    return new BoardIterator() {

      private Iterator iterator=tables.values().iterator();

      public boolean hasNext() {
        return iterator.hasNext();
      }

      public InternBoard next() {
        return (InternBoard)iterator.next();
      }
    };
  }
}

class InternPlayer {

  private ChessServer parent;
  private String name;
  private Socket socket;
  private ThreadReader reader;
  private OutputStream outStream;
  private BufferedWriter out;
  private boolean closed;

  InternPlayer( String name,Socket socket,InputStream inStream,OutputStream outStream,ChessServer parent ) {
    this.parent=parent;
    this.name=name;
    this.socket=socket;
    this.outStream=outStream;
    out=new BufferedWriter( new OutputStreamWriter( outStream ) );
    reader=new ThreadReader( inStream,this );
    reader.start();
  }

  public void close() {
    if( closed ) {
      return;
    }
    closed=true;
    try{
      outStream.close();
      reader.close();
      socket.close();
      parent.removePlayer( this );
    }catch( IOException e ) {}
  }
      
  private void write( String msg ) {
    try{
      out.write( msg+"\n" );
      out.flush();
    }catch( IOException e ) {}
  }

  private void read( String msg ) {
    if( msg.startsWith( "table" ) ) {
      controllTableMsg( msg );
    }else if( msg.startsWith( "mainframe" ) ) {
      if( msg.startsWith( "mainframe:message:" ) ) {
        informAllOther( msg );
      }else if( msg.startsWith( "mainframe:joinTable:" ) ) {
        InternBoard board=parent.getBoard( msg.substring( 20 ) );
        StringBuffer buffer=new StringBuffer( "comand:join:" );
        buffer.append( board.getCode() );
        write( buffer.toString() );
        board.addPlayer( this );
        buffer=new StringBuffer( "table" );
        buffer.append( board.getNr() );
        buffer.append( ":player:" );
        buffer.append( name );
        informTable( buffer.toString() );
        buffer=new StringBuffer( "comand:update:" );
      }else if( msg.startsWith( "mainframe:createTable" ) ) {
        StringBuffer buffer=new StringBuffer( "comand:open:" );
        int integ=parent.addBoard( this );
        buffer.append( integ);
        write( buffer.toString() );
      }
    }else
      protocol( "read unknown message:\n"+msg );
  }

  private void controllTableMsg( String msg ) {
    InternBoard board=parent.getBoard( msg.substring( 5,msg.indexOf( ":" ) ) );
    StringTokenizer st=new StringTokenizer( msg,":",false );
    st.nextToken();
    String type=st.nextToken();
    if( type.equals( "newMove" ) ) {
      board.addMove( st.nextToken(),st.nextToken(),st.nextToken(),st.nextToken(),st.nextToken(),this );
    }else if( type.equals( "button" ) ) {
      String msgType=st.nextToken();
      if( msgType.equals( "timer" ) ) {
        int minutes  =Integer.parseInt( st.nextToken() );
        int increment=Integer.parseInt( st.nextToken() );
        board.setTime( minutes,increment,this );
      }else if( msgType.equals( "exit" ) ) {
        board.removePlayer( this );
      }else if( msgType.equals( "sit" ) ) {
        boolean isWhite=st.nextToken().equals( "Weiß" );
        board.sitPlayer( this,isWhite );
      }else if( msgType.equals( "standUp" ) ) {
        boolean isWhite=st.nextToken().equals( "Weiß" );
        board.standUpPlayer( this,isWhite );
      }else if( msgType.equals( "draw" ) ) {
        board.askForDraw( this );
      }else if( msgType.equals( "undo" ) ) {
        board.askForUndo( this );
      }else{
        informTable( msg );
      }
    }else if( type.equals( "undo" ) ) {
      board.undo( this );
    }else if( type.equals( "askForTime" ) ) {
      board.askForTime( name );
    }else if( type.equals( "deliverTimeTo" ) ) {
      String newPlayer=st.nextToken();
      StringBuffer buffer=new StringBuffer();
      buffer.append( st.nextToken() );
      buffer.append( ":" );
      buffer.append( st.nextToken() );
      buffer.append( ":" );
      buffer.append( st.nextToken() );
      buffer.append( ":" );
      buffer.append( st.nextToken() );
      board.deliverTimeTo( newPlayer,buffer.toString() );
    }else if( type.equals( "startGame" ) ) {
      board.startGame();
    }else if( type.equals( "stopGame" ) ) {
      board.stopGame( this );
    }else{
      informTable( msg );
    }
  }

  public void informThis( String msg ) {
    write( msg );
  }

  public void informTable( String msg ) {
    int table=new Integer( msg.substring( 5,msg.indexOf( ":" ) ) ).intValue();
    boolean found=false;
    BoardIterator iter=parent.getBoards();
    while( iter.hasNext() ) {
      InternBoard board=iter.next();
      if( board.getNr()==table ) {
        PlayerIterator iterator=parent.getPlayers();
        while( iterator.hasNext() ) {
          InternPlayer player=iterator.next();
            if( board.containsPlayer( player.getName() ) && player!=this ) {
              player.write( msg );
            }
        }
        found=true;
        break;
      }
    }
    if( !found ) {
      protocol( "board not Found Exception in InternPlayer.informTable" );
      return;
    }
  }

  public void informAllOther( String msg ) {
    PlayerIterator iterator=parent.getPlayers();
    while( iterator.hasNext() ) {
      InternPlayer player=iterator.next();
      if( player!=this ) {
        player.write( msg );
      }
    }
  }

  public void informAll( String msg ) {
    PlayerIterator iterator=parent.getPlayers();
    while( iterator.hasNext() ) {
      iterator.next().write( msg );
    }
  }

  public String getName() {
    return name;
  }

  public void protocol( String msg ) {
    parent.protocol( msg );
  }

 
  class ThreadReader
  extends Thread {

    private boolean stopp;
    private InputStream inStream;
    private BufferedReader in;
    private InternPlayer owner;

    ThreadReader( InputStream inStream,InternPlayer owner ) {
      in=new BufferedReader( new InputStreamReader( inStream ) );
      this.inStream=inStream;
      this.owner=owner;
      stopp=false;
    }

    private void close() {
      stopp=true;
    }

    public void run() {
      String msg;
      while( !stopp ) {
        try{
          msg=in.readLine();
          if( msg==null )
            break;
          owner.read( msg );
        }catch( IOException e ) {
          break;
        }
      }
      try{
        inStream.close();
      }catch( IOException e ) {
        owner.protocol( e.getMessage() );
      }
      owner.close();
    }
  }
}

class NewUserProcessingThread
extends Thread {

  private ChessServer parent;
  private Socket socket;

  NewUserProcessingThread( Socket socket,ChessServer parent ) {
    this.parent=parent;
    this.socket=socket;
    start();
  }

  public void run() {
    try{
      InputStream inStream=socket.getInputStream();
      OutputStream outStream=socket.getOutputStream();
      BufferedReader in =new BufferedReader( new  InputStreamReader(  inStream ) );
      BufferedWriter out=new BufferedWriter( new OutputStreamWriter( outStream ) );
      String name;
      boolean isNewUsername;
      do{
        name=in.readLine();
        isNewUsername=isNewName( name );
        if( !isNewUsername ) {
          out.write( "name dinided\n" );
          out.flush();
        }
      }while( !isNewUsername );
      if( name==null )
        throw new IOException( "Playername = null" );
      StringBuffer buffer=new StringBuffer( "name accepted:" );
      buffer.append( parent.getBoardCode( name ) );
      buffer.append( "\n" );
      out.write( buffer.toString() );
      out.flush();
      parent.addPlayer( new InternPlayer( name,socket,inStream,outStream,parent ) );
    }catch( IOException e ){
      try{
        socket.close();
      }catch( IOException i ) {}
      if( !e.getMessage().equals( "Playername = null" ) ) {
        parent.protocol( e.toString()+"\nSystem stabil" );
      }
    }
  }

  private boolean isNewName( String name ) {
    PlayerIterator iterator=parent.getPlayers();
    while( iterator.hasNext() ) {
      if( iterator.next().getName().equals( name ) ) {
        return false;
      }
    }
    return true;
  }
}

class InternBoard {

  private int number,minutes,increment;
  private String white,black;
  private HashSet others;
  private String[] moves;
  private int movecounter;
  private ChessServer parent;
  private boolean playing;

  InternBoard( ChessServer parent,int number,InternPlayer player ) {
    this.parent=parent;
    this.number=number;
    minutes=10;
    increment=10;
    others=new HashSet( 10 );
    moves=new String[60];
    movecounter=0;
    playing=false;
    
    StringBuffer buffer=new StringBuffer( "comand:update:tablelist:" );
    buffer.append( number );
    buffer.append( ":" );
    buffer.append( player.getName() );
    player.informAll( buffer.toString() );
  }

  public String getSmallCode() {
    StringBuffer buffer=new StringBuffer();
    buffer.append( number );
    buffer.append( ":" );
    buffer.append( minutes );
    buffer.append( ":" );
    buffer.append( increment );
    buffer.append( ":" );
    buffer.append( others.size() );
    buffer.append( ":" );
    Iterator iterator=others.iterator();
    while( iterator.hasNext() ) {
      buffer.append( (String)iterator.next() );
      buffer.append( ":" );
    }
    if( white==null ) {
      if( black==null ) {
        buffer.append( "both_free" );
      }else{
        buffer.append( "white_free:" );
        buffer.append( black );
      }
    }else{
      if( black==null ) {
        buffer.append( "black_free:" );
        buffer.append( white );
      }else{
        buffer.append( "none_free:" );
        buffer.append( white );
        buffer.append( ":" );
        buffer.append( black );
      }
    }
    return buffer.toString();
  }

  public String getCode() {
    StringBuffer buffer=new StringBuffer();
    buffer.append( number );
    buffer.append( ":" );
    buffer.append( minutes );
    buffer.append( ":" );
    buffer.append( increment );
    buffer.append( ":" );
    if( black!=null&&white!=null ) {
      buffer.append( "both" );
      buffer.append( ":" );
      buffer.append( getSize() );
      buffer.append( ":" );
      buffer.append( white );
      buffer.append( ":" );
      buffer.append( black );
      buffer.append( ":" );
    }else if( black!=null&&white==null ) {
      buffer.append( "black" );
      buffer.append( ":" );
      buffer.append( getSize() );
      buffer.append( ":" );
      buffer.append( black );
      buffer.append( ":" );
    }else if( black==null&&white!=null ) {
      buffer.append( "white" );
      buffer.append( ":" );
      buffer.append( getSize() );
      buffer.append( ":" );
      buffer.append( white );
      buffer.append( ":" );
    }else{
      buffer.append( "none" );
      buffer.append( ":" );
      buffer.append( getSize() );
      buffer.append( ":" );
    }
    String player;
    Iterator iterator=others.iterator();
    do{
      player=(String)iterator.next();
      if( !player.equals( white )&&!player.equals( black ) ) {
        buffer.append( player );
        buffer.append( ":" );
      }
    }while( iterator.hasNext() );
    if( playing ) {
      buffer.append( "playing:" );
    }else{
      buffer.append( "notplaying:" );
    }
    buffer.append( movecounter );
    buffer.append( ":" );
    for( int i=0;i<movecounter;i++ ) {
      buffer.append( moves[i] );
      buffer.append( ":" );
    }
    return buffer.toString();
  }

  public void startGame() {
    playing=true;
  }

  public void stopGame( InternPlayer player ) {
    synchronized( this ) {
      if( !playing ) return;
      playing=false;
    }
    movecounter=0;
    StringBuffer buffer=new StringBuffer( "table" );
    buffer.append( number );
    buffer.append( ":stopGame" );
    try{
      Thread.sleep( 1000 );
    }catch( InterruptedException e ) {}
    player.informTable( buffer.toString() );
  }

  public void sitPlayer( InternPlayer player,boolean whiteplace ) {
    String name=player.getName();
    StringBuffer buffer=new StringBuffer( "comand:update:tablestatus:" );
    buffer.append( number );
    buffer.append( ":sit:" );
    buffer.append( name );
    if( whiteplace ) {
      white=name;
      buffer.append( ":white" );
    }else{
      black=name;
      buffer.append( ":black" );
    }
    player.informAll( buffer.toString() );
    buffer=new StringBuffer( "table" );
    buffer.append( number );
    buffer.append( ":button:sit:" );
    if( whiteplace ) {
      buffer.append( "Weiß:" );
    }else{
      buffer.append( "Schwarz:" );
    }
    buffer.append( name );
    player.informTable( buffer.toString() );
  }

  public void standUpPlayer( InternPlayer player,boolean whiteStandUp ) {
    String name=player.getName();
    StringBuffer buffer=new StringBuffer( "comand:update:tablestatus:" );
    buffer.append( number );
    buffer.append( ":standUp:" );
    buffer.append( name );
    if( whiteStandUp ) {
      white=null;
      buffer.append( ":white" );
    }else{
      black=null;
      buffer.append( ":black" );
    }
    player.informAll( buffer.toString() );
    buffer=new StringBuffer( "table" );
    buffer.append( number );
    buffer.append( ":button:standUp:" );
    if( whiteStandUp ) {
      buffer.append( "white:" );
    }else{
      buffer.append( "black:" );
    }
    buffer.append( name );
    player.informTable( buffer.toString() );
    if( white==null && black==null && playing ) {
      buffer=new StringBuffer( "table" );
      buffer.append( number );
      buffer.append( ":button:resign:" );
      if( whiteStandUp ) {
        buffer.append( "Schwarz" );
      }else{
        buffer.append( "Weiss" );
      }
      player.informTable( buffer.toString() );
    }
  }

  public void addPlayer( InternPlayer player ) {
    String name=player.getName();
    others.add( name );
    StringBuffer buffer=new StringBuffer( "comand:update:tableplayer:" );
    buffer.append( number );
    buffer.append( ":new:" );
    buffer.append( name );
    player.informAll( buffer.toString() );
    buffer=new StringBuffer( "table" );
    buffer.append( number );
    buffer.append( ":player:" );
    buffer.append( name );
    player.informTable( buffer.toString() );
    buffer=new StringBuffer( "table" );
    buffer.append( number );
    buffer.append( ":message:neuer Spieler " );
    buffer.append( name );
    player.informTable( buffer.toString() );
  }

  public void removePlayer( InternPlayer player ) {
    String name=player.getName();
    if( !others.remove( name ) ) {
      return;
    }
    if( name.equals( white ) ) {
      standUpPlayer( player,true );
    }else if( name.equals( black ) ) {
      standUpPlayer( player,false );
    }
    StringBuffer buffer=new StringBuffer( "comand:update:tableplayer:" );
    buffer.append( number );
    buffer.append( ":remove:" );
    buffer.append( name );
    player.informAll( buffer.toString() );
    buffer=new StringBuffer( "table" );
    buffer.append( number );
    buffer.append( ":button:exit:" );
    buffer.append( name );
    player.informTable( buffer.toString() );
    buffer=new StringBuffer( "table" );
    buffer.append( number );
    buffer.append( ":message:Spieler " );
    buffer.append( name );
    buffer.append( " hat den Tisch verlassen" );
    player.informTable( buffer.toString() );
    if( others.size()==0 ) {
      parent.removeBoard( this );
    }
  }

  public void askForTime( String newPlayer ) {
    StringBuffer buffer=new StringBuffer( "table" );
    buffer.append( number );
    buffer.append( ":returnTime:" );
    buffer.append( newPlayer );
    String timeSpender;
    if( white!=null )
      timeSpender=white;
    else if( black!=null )
      timeSpender=black;
    else
      timeSpender=(String)others.iterator().next();
    getPlayerByName( timeSpender ).informThis( buffer.toString() );
  }

  public void deliverTimeTo( String newPlayer,String time ) {
    StringBuffer buffer=new StringBuffer( "table" );
    buffer.append( number );
    buffer.append( ":time:" );
    buffer.append( time );
    getPlayerByName( newPlayer ).informThis( buffer.toString() );
  }

  public void addMove( String move,String whiteMin,String whiteSec,String blackMin,String blackSec,InternPlayer player ) {
    if( movecounter==moves.length ) {
      String[] newMoves=new String[ moves.length+40 ];
      System.arraycopy( moves,0,newMoves,0,moves.length );
      moves=newMoves;
    }
    moves[movecounter++]=move;
    StringBuffer buffer=new StringBuffer( "table" );
    buffer.append( number );
    buffer.append( ":newMove:" );
    buffer.append( move ); 
    buffer.append( ":" );
    buffer.append( whiteMin );
    buffer.append( ":" );
    buffer.append( whiteSec );
    buffer.append( ":" );
    buffer.append( blackMin );
    buffer.append( ":" );
    buffer.append( blackSec );
    player.informTable( buffer.toString() );
  }

  public void askForUndo( InternPlayer player ) {
    try{
      String askedBy=player.getName();
      StringBuffer buffer=new StringBuffer( "table" );
      buffer.append( number );
      buffer.append( ":askForUndo:" );
      if( askedBy.equals( white ) ) {
        getPlayerByName( black ).informThis( buffer.toString() );
      }else{
        getPlayerByName( white ).informThis( buffer.toString() );
      }
      buffer=new StringBuffer( "table" );
      buffer.append( number );
      buffer.append( ":message:" );
      buffer.append( askedBy );
      buffer.append( " möchte seinen Zug zurücknehmen" );
      player.informTable( buffer.toString() );
    }catch( NoSuchElementException e ) {}
  }

  public void undo( InternPlayer player ) {
    if( movecounter!=0 ) {
      movecounter--;
      StringBuffer buffer=new StringBuffer( "table" );
      buffer.append( number );
      buffer.append( ":undo:" );
      player.informTable( buffer.toString() );
    }
  }

  public void askForDraw( InternPlayer player ) {
    try{
      String askedBy=player.getName();
      StringBuffer buffer=new StringBuffer( "table" );
      buffer.append( number );
      buffer.append( ":askForDraw:" );
      if( askedBy.equals( white ) ) {
        getPlayerByName( black ).informThis( buffer.toString() );
      }else{
        getPlayerByName( white ).informThis( buffer.toString() );
      }
      buffer=new StringBuffer( "table" );
      buffer.append( number );
      buffer.append( ":message:" );
      buffer.append( askedBy );
      buffer.append( " bietet Remis an" );
      player.informTable( buffer.toString() );
    }catch( NoSuchElementException e ) {}
  }

  public void setTime( int min,int inc,InternPlayer player ) {
    minutes=min;
    increment=inc;
    String name=player.getName();
    StringBuffer buffer=new StringBuffer( "comand:update:tabletime:" );
    buffer.append( number );
    buffer.append( ":" );
    buffer.append( min );
    buffer.append( ":" );
    buffer.append( inc );
    player.informAll( buffer.toString() );
    buffer=new StringBuffer( "table" );
    buffer.append( number );
    buffer.append( ":button:timer:" );
    buffer.append( min );
    buffer.append( ":" );
    buffer.append( inc );
    player.informTable( buffer.toString() );
  }

  private InternPlayer getPlayerByName( String name ) throws NoSuchElementException {
    PlayerIterator iterator=parent.getPlayers();
    while( iterator.hasNext() ) {
      InternPlayer player=iterator.next();
      if( player.getName().equals( name ) ) {
        return player;
      }
    }
    throw new NoSuchElementException( "No Player called "+name+" found" );
  }

  public boolean containsPlayer( String name ) {
    return others.contains( name );
  }

  public int getSize() {
    return others.size();
  }

  public int getNr() {
    return number;
  }

  private void protocol( String msg ) {
    parent.protocol( msg );
  }
}

interface PlayerIterator {

  public InternPlayer next();
  public boolean hasNext();

}

interface BoardIterator {

  public InternBoard next();
  public boolean hasNext();

}
