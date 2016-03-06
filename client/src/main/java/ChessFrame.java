import java.awt.*;
import java.util.*;
import java.awt.event.*;
import javax.swing.*;

public class ChessFrame
extends JFrame {

  public static final int CHESSMATE_WHITE=0;
  public static final int CHESSMATE_BLACK=1;
  public static final int CHESSMATE_WHITE_TIMEOUT=2;
  public static final int CHESSMATE_BLACK_TIMEOUT=3;
  public static final int CHESSMATE_WHITE_GAVEUP=4;
  public static final int CHESSMATE_BLACK_GAVEUP=5;
  public static final int PATT_WHITE=6;
  public static final int PATT_BLACK=7;
  public static final int REMIS_CHOSEN=8;
  public static final int REMIS_3TIMES_SAME_POSITION=9;
  public static final int REMIS_NOT_ENOUGH_POWER=10;
  public static final int RESIGN_WHITE=11;
  public static final int RESIGN_BLACK=12;
  public static final int SYNCHRONIZED_END=13;	

  private Table table;
  private int tableNumber;
  private Player player;
  private boolean bigSize;

  public ChessFrame( Player player,int tableNumber ) {
    this( player,tableNumber,"" );
  }

  public ChessFrame( Player player,int tableNumber,String info ) {
    super( "Tisch:"+Integer.toString( tableNumber ) );
    this.player=player;
    this.tableNumber=tableNumber;
    setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
    setContentPane( designLayout() );
    bigSize=true;
    modify( info );
    addKeyListener( 
      new KeyAdapter() {
        public void keyPressed( KeyEvent event ) {
          if( event.isAltDown() )
            changeSize();
        }
      }
    );
    setIconImage( Images.get( "ICON" ) );
    pack();
    setResizable( false );
    setVisible( true );
  }

  private JPanel designLayout() {
    JPanel screen = new JPanel();
    screen.setLayout( new BorderLayout() );
    table=new Table( this );
    screen.add( table,"Center" );
    return screen;
  }

  void changeSize(){
    bigSize=!bigSize;
    table.changeSize( bigSize );
    pack();
  }

  public void exit() {
    player.closeBoard( this );
  }

  public String getPlayerName() {
    return player.getPlayerName();
  }

  public void read( String msg ) {
    player.readFrame( "table"+tableNumber+":"+msg );
  }

  public void write( String msg ) {
    table.write( msg );
  } 

  public void stopGame( int endOption ) {
    switch( endOption ) {
      case CHESSMATE_WHITE:semimodalInfo( "Weiss ist matt","Schwarz hat gewonnen" );break;
      case CHESSMATE_BLACK:semimodalInfo( "Schwarz ist matt","Weiss hat gewonnen" );break;
      case CHESSMATE_WHITE_TIMEOUT:semimodalInfo( "Zeit von Weiss abgelaufen","Schwarz hat gewonnen" );break;
      case CHESSMATE_BLACK_TIMEOUT:semimodalInfo( "Zeit von Schwarz abgelaufen","Weiss hat gewonnen" );break;
      case CHESSMATE_WHITE_GAVEUP:semimodalInfo( "Weiss hat aufgegeben","Schwarz hat gewonnen" );break;
      case CHESSMATE_BLACK_GAVEUP:semimodalInfo( "Schwarz hat aufgegeben","Weiss hat gewonnen" );break;
      case PATT_WHITE:semimodalInfo( "Weiss ist patt","Unentschieden" );break;
      case PATT_BLACK:semimodalInfo( "Schwarz ist patt","Unentschieden" );break;
      case REMIS_CHOSEN:semimodalInfo( "remis gewählt","Unentschieden" );break;
      case REMIS_3TIMES_SAME_POSITION:semimodalInfo( "unentschieden wegen drei-maliger\nWiederhohlung der selben\nPosition"
                                                     ,"Unentschieden" );break;
      case REMIS_NOT_ENOUGH_POWER:semimodalInfo( "remis, nicht mehr genug Material\nzum Mattsetzen übrig ist"
                                                ,"Unentschieden" );break;
      case RESIGN_WHITE:semimodalInfo( "Weiss hat aufgegeben","Schwarz hat gewonnen" );break;
      case RESIGN_BLACK:semimodalInfo( "Schwarz hat aufgegeben","Weiss hat gewonnen" );break;
      case SYNCHRONIZED_END:semimodalInfo( "der Spieler mit wenigsten\nZeit hat verloren","synchronisiertes Ende" );break;
    }
  }

  private void semimodalInfo( String msg,String result ) {
    new SemimodalInfoDialog( this,msg,result );
  }

  public void askForDraw() {
    new SemimodalYesNoDialog( this,"Ihr Gegner bietet Ihnen Remis.Nehmen Sie An?",SemimodalYesNoDialog.DRAW );
  }

  public void semimodalDrawEvent( boolean doDraw ) {
    if( doDraw ) {
      read( "draw:" );
      table.stopGame( REMIS_CHOSEN );
    }else{
      StringBuffer buffer=new StringBuffer( "message:Spieler " );
      buffer.append( getPlayerName() );
      buffer.append( " hat ein Remis abgelehnt" );
      read( buffer.toString() );
    }
  }

  public void askForUndo() {
    new SemimodalYesNoDialog( this,"Ihr Gegner bittet um ein Undo.Ist das akzeptabel?",SemimodalYesNoDialog.UNDO );
  }

  public void semimodalUndoEvent( boolean doUndo ) {
    if( doUndo ) {
      table.getBoard().undo();
      table.getMoves().deleteMove();
      read( "undo:" );
    }else{
      StringBuffer buffer=new StringBuffer( "message:Spieler " );
      buffer.append( getPlayerName() );
      buffer.append( " hat ein Zurücknehmen abgelehnt" );
      read( buffer.toString() );
    }
  }

  public void block( boolean blockIt ) {
    table.getBoard().block( blockIt );
    table.getOptions().block( blockIt );
  }

  public int getNr() {
    return tableNumber;
  }

  private void modify( String info ) {
    if( info.indexOf( ":" )==-1 ) {
      return;
    }
    StringTokenizer st=new StringTokenizer( info,":",false );
    try{
      int minutes=Integer.parseInt( st.nextToken() );
      int increment=Integer.parseInt( st.nextToken() );
      String status=st.nextToken();
      String[] others=new String[ Integer.parseInt( st.nextToken() ) ];
      for( int i=0;i<others.length;i++ ) {
        others[i]=st.nextToken();
      }
      boolean playing=st.nextToken().equals( "playing" );
      String[] moves=new String[ Integer.parseInt( st.nextToken() ) ];
      for( int i=0;i<moves.length;i++ ) {
        moves[i]=st.nextToken();
      }
      for( int i=0;i<others.length;i++ ) {
        write( "player:"+others[i] );
      }
      if( status.equals( "both" ) ) {
        write( "button:sit:Weiß:"+others[0] );
        write( "button:sit:Schwarz:"+others[1] );
      }else if( status.equals( "white" ) ) {
        write( "button:sit:Weiß:"+others[0] );
      }else if( status.equals( "black" ) ) {
        write( "button:sit:Schwarz:"+others[0] );
      }
      table.setTime( minutes,increment );
      if( playing ) {
        table.startGame();
        for( int i=0;i<moves.length;i++ ) {
          write( "newMove:"+moves[i] );
        }
        read( "askForTime" );
      }
    }catch( Exception e ) {
      System.out.println( e.toString()+"\nChessFrame.modify:MalformedString:\n"+info );
    }
  }

}
