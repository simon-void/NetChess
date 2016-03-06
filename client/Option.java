import java.awt.event.*;
import java.awt.*;
import javax.swing.*;

class Option
extends JPanel
implements ActionListener {

  private JButton exit,timer,standUp,resign,draw,undo,start;
  private JLabel timerLabel;
  private TimerDialog timerDialog;
  private Table table;
  private ChessButton chessButton;
  public final static int MY_TURN=0;
  public final static int OTHER_TURN=1;
  public final static int WAITING_FOR_BOTH_SET=2;
  public final static int WAITING_FOR_START=3;
  public final static int WAITING_FOR_START_OTHER=4;
  public final static int WATCHER=5;
  private int sitCounter,startCounter;
  private int status;
  private boolean blocked;

  public Option( Table table ) {
    this.table=table;
    sitCounter=0;
    startCounter=0;
    blocked=false;
    designLayout();
    setStatus( WATCHER );
  }

  private void designLayout() {
    setLayout( new GridLayout( 8,1 ) );
    JPanel space1=new JPanel();
    space1.setPreferredSize( new Dimension( 40,75 ) );
    JPanel space2=new JPanel();
    space2.setPreferredSize( new Dimension( 40,70 ) );
    JPanel space3=new JPanel();
    space3.setPreferredSize( new Dimension( 40,75 ) );
    start=new JButton( "Start" );
    start.addActionListener( this );
    start.setBorder( BorderFactory.createEtchedBorder() );
    undo=new JButton( "Zur�ck" );
    undo.addActionListener( this );
    undo.setBorder( BorderFactory.createEtchedBorder() );
    draw=new JButton( "Remis" );
    draw.addActionListener( this );
    draw.setBorder( BorderFactory.createEtchedBorder() );
    resign=new JButton( "Aufgeben" );
    resign.addActionListener( this );
    resign.setBorder( BorderFactory.createEtchedBorder() );
    standUp=new JButton( "Aufstehen" );
    standUp.addActionListener( this );
    standUp.setBorder( BorderFactory.createEtchedBorder() );
    timer=new JButton( "Timer" );
    timer.addActionListener( this );
    timer.setBorder( BorderFactory.createEtchedBorder() );
    timerLabel=new JLabel();
    timerLabel.setHorizontalAlignment( SwingConstants.CENTER );
    setTime( 999,999 );
    timerLabel.setPreferredSize( timerLabel.getPreferredSize() );
    setTime( 10,10 );
    exit=new JButton( "Ende" );
    exit.addActionListener( this );
    exit.setBorder( BorderFactory.createEtchedBorder() );

    JPanel uppermiddle=new JPanel( new GridLayout( 3,1 ) );
    uppermiddle.add( undo );
    uppermiddle.add( draw );
    uppermiddle.add( resign );
    JPanel lowermiddle=new JPanel( new GridLayout( 3,1 ) );
    lowermiddle.add( standUp );
    lowermiddle.add( timer );
    lowermiddle.add( timerLabel );

    GridBagLayout layout=new GridBagLayout();
    JPanel middle=new JPanel(  layout );
    layout.setConstraints( space1,getConstraints( 0,0,4,3,0 ) );
    middle.add( space1 );
    layout.setConstraints( uppermiddle,getConstraints( 0,3,4,3,0 ) );
    middle.add( uppermiddle );
    layout.setConstraints( space2,getConstraints( 0,6,4,3,0 ) );
    middle.add( space2 );
    layout.setConstraints( lowermiddle,getConstraints( 0,9,4,3,0 ) );
    middle.add( lowermiddle );
    layout.setConstraints( space3,getConstraints( 0,12,4,3,0 ) );
    middle.add( space3 );

    setLayout( new BorderLayout() );
    add( start,"North" );
    add( middle,"Center" );
    add( exit,"South" );
  }

  private GridBagConstraints getConstraints( int x,int y,int width,int height,int direct ) {
    GridBagConstraints out=new GridBagConstraints();
    out.gridx=x;
    out.gridy=y;
    out.gridwidth=width;
    out.gridheight=height;
    switch( direct ) {
      case -1:out.anchor=GridBagConstraints.NORTH;break;
      case  0:out.anchor=GridBagConstraints.CENTER;break;
      case  1:out.anchor=GridBagConstraints.SOUTH;break;
    }
    return out;
  }

  public void setStatus( int status ) {
    this.status=status;
    switch( status ) {
      case MY_TURN:
        exit.setEnabled( false );
        timer.setEnabled( false );
        standUp.setEnabled( false );
        resign.setEnabled( true );
        draw.setEnabled( true );
        undo.setEnabled( false );
        start.setEnabled( false );
        break;
      case OTHER_TURN:
        exit.setEnabled( false );
        timer.setEnabled( false );
        standUp.setEnabled( false );
        resign.setEnabled( true );
        draw.setEnabled( false );
        undo.setEnabled( true );
        start.setEnabled( false );
        break;
      case WAITING_FOR_BOTH_SET:
        exit.setEnabled( false );
        timer.setEnabled( true );
        standUp.setEnabled( true );
        resign.setEnabled( false );
        draw.setEnabled( false );
        undo.setEnabled( false );
        start.setEnabled( false );
        break;
      case WAITING_FOR_START:
        exit.setEnabled( false );
        timer.setEnabled( true );
        standUp.setEnabled( true );
        resign.setEnabled( false );
        draw.setEnabled( false );
        undo.setEnabled( false );
        start.setEnabled( true );
        break;
      case WAITING_FOR_START_OTHER:
        exit.setEnabled( false );
        timer.setEnabled( false );
        standUp.setEnabled( true );
        resign.setEnabled( false );
        draw.setEnabled( false );
        undo.setEnabled( false );
        start.setEnabled( false );
        break;
      case WATCHER:
        exit.setEnabled( true );
        timer.setEnabled( false );
        standUp.setEnabled( false );
        resign.setEnabled( false );
        draw.setEnabled( false );
        undo.setEnabled( false );
        start.setEnabled( false );
        break;
    }
  }

  public void standUpEvent( ChessButton button ) {
    sitCounter--;
    String buttonName=button.getName();
    String playerName=table.getPlayerName();
    if( !table.getTimer().isTicking() ) {
      if( (sitCounter==0)
        ||(table.getBoard().getStatus()==Chess.NONE)
        ||(buttonName.equals( playerName )) ) {
        setStatus( WATCHER );
        table.setStatus( Chess.NONE );
      }else{
        setStatus( WAITING_FOR_BOTH_SET );
      }
    }
    if( buttonName.equals( playerName ) ) {
      table.getTimer().swapTimers( true );
      table.getButtons().swapButtons( true );
    }
  }

  public void sitDownEvent( ChessButton button ) {
    chessButton=button;
    sitCounter++;
    String buttonName=button.getName();
    String playerName=table.getPlayerName();
    String color=button.getColor();
    if( sitCounter==2 ) {
      if( table.getBoard().getStatus()!=Chess.NONE ) {
        if( table.getTimer().isTicking() ) {
          if( buttonName.equals( playerName ) ) {
            if( (table.getMoves().isWhiteTurn()) ^ (color.equals( "Schwarz" )) ) {
              setStatus( MY_TURN );
            }else{
              setStatus( OTHER_TURN );
            }
          }
        }else{
          setStatus( WAITING_FOR_START );
        }
      }
    }else if( buttonName.equals( playerName ) ) {
      setStatus( WAITING_FOR_BOTH_SET );
    }
    if( buttonName.equals( playerName ) ) {
      if( color.equals( "Weiß" ) ) {
        table.getTimer().swapTimers( true );
        table.getButtons().swapButtons( true );
      }else{
        table.getTimer().swapTimers( false );
        table.getButtons().swapButtons( false );
      }
    }
  }

  public void switchTurnEvent() {
    if( status==MY_TURN ) {
      setStatus( OTHER_TURN );
    }else if( status== OTHER_TURN ) {
      setStatus( MY_TURN );
    }
  }

  public void block( boolean blockIt ) {
    blocked=blockIt;
  }

  public void actionPerformed( ActionEvent event ) {
    if( blocked ) {
      return;
    }
    String playername=table.getPlayerName();
    String msg=event.getActionCommand();
    if( msg.equals( "Ende" ) ) {
      table.read( "button:exit:"+playername );						//Ende
      table.exit();
    }else if( msg.equals( "Start" ) ) {							//Start
      table.read( "button:start:"+playername );
      table.prepareStartGame();
    }else if( msg.equals( "Aufgeben" ) ) {						//Aufgeben
      if( table.getBoard().getStatus()==Chess.WEISS ) {
        table.read( "button:resign:Weiß" );
        table.stopGame( ChessFrame.CHESSMATE_WHITE_GAVEUP );
      }else{
        table.read( "button:resign:Schwarz" );
        table.stopGame( ChessFrame.CHESSMATE_BLACK_GAVEUP );
      }
      setStatus( WAITING_FOR_START );
    }else if( msg.equals( "Remis" ) ) {
      table.read( "button:draw" );							//Remis
      draw.setEnabled( false );
    }else if( msg.equals( "Aufstehen" ) ) {						//Aufstehen
      table.cancelStartGame();
      if( table.getBoard().getStatus()==Chess.WEISS ) {
        table.read( "button:standUp:Weiß:"+playername );
      }else{
        table.read( "button:standUp:Schwarz:"+playername );
      }
      table.getButtons().removeName( playername );
    }else if( msg.equals( "Timer" ) ) {							//Timer
      Point local=table.getFrame().getLocation();
      local.translate( 110,300 );
      BothTimer timer=table.getTimer();
      timerDialog=new TimerDialog( table.getFrame(),local,timer.getMinutes(),timer.getIncrement() );
      table.read( "button:timer:"+timerDialog.minutes+":"+timerDialog.increment );
      setTime( timerDialog.minutes,timerDialog.increment );
      timer.setMinutesIncrement( timerDialog.minutes,timerDialog.increment );
    }else if( msg.equals( "Zurück" ) ) {						//Zurück
      undo.setEnabled( false );
      if( !table.getMoves().isEmpty() ) {
        table.read( "button:undo" );
      }
    }
  }

  public void setTime( int min,int inc ) {
    StringBuffer buffer=new StringBuffer( "Min:" );
    buffer.append( min );
    buffer.append( " Inc:" );
    buffer.append( inc );
    timerLabel.setText( buffer.toString() );
  }
}
