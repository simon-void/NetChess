import java.awt.*;
import javax.swing.*;
import java.util.*;

class Table
extends JPanel
implements Reader{

  private Chess board;
  private Option options;
  private Terminal terminal;
  private BothButton buttons;
  private PlayerList players;
  private MoveList moves;
  private BothTimer timers;
  private ChessFrame parent;
  private GridBagLayout gridBag;
  private int status;

  public final static int WATCHING=0;
  public final static int PLAYING=1;
  public final static int HE_PRESSED_START=2;
  public final static int I_PRESSED_START=3;

  Table( ChessFrame parent ) {
    status=WATCHING;
    this.parent=parent;
    designLayout();
    players.addName( getPlayerName() );
  }

  private void designLayout() {
    options  =new Option( this );
    buttons=new BothButton( this,options );
    timers=new BothTimer( this );
    board=new Chess( this );
    gridBag=new GridBagLayout();
    setLayout( gridBag );
    add( addComponent( options                                    ,0 ,0 ,4 ,13 ) ); 
    add( addComponent( board.getUI()                              ,4 ,0 ,10,10 ) ); 
    add( addComponent( terminal =new Terminal( this )             ,4 ,10,10,3  ) ); 
    add( addComponent(           new Panel()                      ,14,0 ,4 ,1  ) );
    add( addComponent( buttons.getUpperButton()                   ,14,1 ,4 ,1  ) ); 
    add( addComponent( timers.getUpperTimer()                     ,14,2 ,4 ,1  ) );
    add( addComponent(           new Panel()                      ,14,3 ,4 ,1  ) );
    add( addComponent( moves    =new MoveList()                   ,14,4 ,4 ,3  ) );
    add( addComponent(           new Panel()                      ,14,7 ,4 ,1  ) ); 
    add( addComponent( timers.getLowerTimer()                     ,14,8 ,4 ,1  ) ); 
    add( addComponent( buttons.getLowerButton()                   ,14,9 ,4 ,1  ) );
    add( addComponent( players  =new PlayerList()                 ,14,10,4 ,3  ) );
  }

  private Component addComponent( Component comp,int x,int y,int width,int height ){
    GridBagConstraints constrains=new GridBagConstraints();
    constrains.gridx=x;
    constrains.gridy=y;
    constrains.gridwidth=width;
    constrains.gridheight=height;
    gridBag.setConstraints( comp,constrains );
    return comp;
  }

  public void prepareStartGame() {
    if( status==HE_PRESSED_START )
      startGame();
    else
      status=I_PRESSED_START;
  }

  public void cancelStartGame() {
    status=WATCHING;
  }

  public void startGame() {
    status=PLAYING;
    board.startGame();
    moves.clear();
    if( board.getStatus()==Chess.WEISS ) {
      timers.start();
      options.setStatus( Option.MY_TURN );
      read( "startGame" );
    }else if( board.getStatus()==Chess.SCHWARZ ) {
      timers.start();
      options.setStatus( Option.OTHER_TURN );
    }else{
      timers.start();
      options.setStatus( Option.WATCHER );
    }
  }

  public void stopGame( int endOption ) {
    timers.reset();
    status=WATCHING;
    parent.stopGame( endOption );
    if( board.getStatus()!=Chess.NONE ) {
      options.setStatus( Option.WAITING_FOR_START );
    }
    read( "stopGame" );
    board.stopGameFromOutside();
  }

  public void setStatus( int status ) {
    board.setStatus( status );
  }

  public String getPlayerName() {
    return parent.getPlayerName();
  }

  public void exit() {
    parent.exit();
  }

  public void write( String msg ) {
    StringTokenizer st=new StringTokenizer( msg,":",false );
    String type=st.nextToken();
    if( type.equals( "message" ) ){
      StringBuffer buffer=new StringBuffer("");
      while( st.hasMoreTokens() ) {
        buffer.append( st.nextToken() );
        buffer.append( ":" );
      }
      buffer.deleteCharAt( buffer.length()-1 );
      terminal.write( buffer.toString() );
    }else if( type.equals( "newMove" ) ) {
      String move=st.nextToken();
      moves.addMove( move );
      StringBuffer buffer=new StringBuffer( move );
      while( st.hasMoreTokens() ) {
        buffer.append( ":" );
        buffer.append( st.nextToken() );
      }
      board.receiveMove( buffer.toString() );
    }else if( type.equals( "button" ) ) {
      StringBuffer buffer=new StringBuffer("");
      while( st.hasMoreTokens() ) {
        buffer.append( st.nextToken() );
        buffer.append( ":" );
      }
      buttonPressed( buffer.toString() );
    }else if( type.equals( "player" ) ) {
      try{
        players.addName( st.nextToken() );
      }catch(NoSuchElementException r){System.out.println( "caughtbefor" );}
    }else if( type.equals( "returnTime" ) ) {
      StringBuffer buffer=new StringBuffer("deliverTimeTo:");
      buffer.append( st.nextToken() );
      buffer.append( timers.getTime() );      
      read( buffer.toString() );
    }else if( type.equals( "undo" ) ) {
      board.undo();
      moves.deleteMove();
    }else if( type.equals( "askForUndo" ) ) {
      parent.askForUndo();
    }else if( type.equals( "draw" ) ) {
      stopGame( ChessFrame.REMIS_CHOSEN );
    }else if( type.equals( "askForDraw" ) ) {
      parent.askForDraw();
    }else if( type.equals( "time" ) ) {
      int whiteMin=Integer.parseInt( st.nextToken() );
      int whiteSec=Integer.parseInt( st.nextToken() );
      int blackMin=Integer.parseInt( st.nextToken() );
      int blackSec=Integer.parseInt( st.nextToken() );
      timers.setTime( whiteMin,whiteSec,blackMin,blackSec);
    }else if( type.equals( "stopGame" ) ) {
      if( status==PLAYING ) {
        stopGame( ChessFrame.SYNCHRONIZED_END );
      }
    }else{
      System.out.println( "uncaughtTableMSG:"+msg );
    }
  }

  private void buttonPressed( String msg ) {
    StringTokenizer st=new StringTokenizer( msg,",:",false );
    String button=st.nextToken();
    if( button.equals( "undo" ) ) {
      moves.deleteMove();
    }else if( button.equals( "resign" ) ) {
      if( st.nextToken().equals( "Weiß" ) ) {
        stopGame( ChessFrame.CHESSMATE_WHITE_GAVEUP );
      }else{
        stopGame( ChessFrame.CHESSMATE_BLACK_GAVEUP );
      }
    }else if( button.equals( "timer" ) ) {
      int minutes=Integer.parseInt( st.nextToken() );
      int increment=Integer.parseInt( st.nextToken() );
      setTime( minutes,increment );
    }else if( button.equals( "start" ) ) {
      if( status==WATCHING )
        status=HE_PRESSED_START;
      else if( status==I_PRESSED_START )
        startGame();
    }else if( button.equals( "exit" ) ) {
      String name=st.nextToken();
      players.deleteName( name );      
      buttons.removeName( name );
      cancelStartGame();
    }else if( button.equals( "sit" ) ) {
      String color=st.nextToken();
      String player=st.nextToken();
      if( color.equals( "Weiß" ) )
        buttons.setName( player,true );
      else
        buttons.setName( player,false );
    }else if( button.equals( "standUp" ) ) {
      st.nextToken();
      buttons.removeName( st.nextToken() );
      cancelStartGame();
    }else System.out.println( "Table.buttonPressed:\n"+button );
  }


  public void read( String msg ) {
    parent.read( msg );
  }

  public void setTime( int minutes,int increment ) {
    options.setTime( minutes,increment );
    timers.setMinutesIncrement(  minutes,increment );
  }

  public void changeSize( boolean bigger ) {
    terminal.changeSize( bigger );
    moves.changeSize( bigger );
    board.changeSize( bigger );
  }

  public Chess getBoard() {
    return board;
  }

  public Option getOptions() {
    return options;
  }

  public MoveList getMoves() {
    return moves;
  }

  public JFrame getFrame() {
    return parent;
  }

  public BothTimer getTimer() {
    return timers;
  }

  public BothButton getButtons() {
    return buttons;
  }
}
