import javax.swing.*;

class BothTimer {

  private JPanel upper,lower;
  private Timer blackTimer,whiteTimer;
  private Table table;
  private boolean isPlaying,startOrder;

  public BothTimer( Table table ) {
    this.table=table;
    whiteTimer=new Timer( this );
    whiteTimer.start();
    lower=new JPanel();
    lower.add( whiteTimer.getJPanel() );
    blackTimer=new Timer( this );
    blackTimer.start();
    upper=new JPanel();
    JPanel blackT=blackTimer.getJPanel();
    upper.add( blackT );
    upper.setPreferredSize( blackT.getPreferredSize() );
    startOrder=true;
  }

  public void start() {
    isPlaying=true;
    blackTimer.resetTimer();
    whiteTimer.resetTimer();
    whiteTimer.startTimer();
    while( !whiteTimer.isRunning() ) {
      try{
        Thread.sleep( 5 );
      }catch( InterruptedException e ) {}
    }
  }

  public void toggle( boolean addTime ) {
    if( !isPlaying ) return;
    if( whiteTimer.isRunning() ) {
      whiteTimer.stopTimer( addTime );
      blackTimer.startTimer();
      while( whiteTimer.isRunning() ) {
        try{
          Thread.sleep( 5 );
        }catch( InterruptedException e ) {}
      }
    }else{
      whiteTimer.startTimer();
      blackTimer.stopTimer( addTime );
      while( !whiteTimer.isRunning() ) {
        try{
          Thread.sleep( 5 );
        }catch( InterruptedException e ) {}
      }
    }
  }

  public void reset() {
    isPlaying=false;
    if( whiteTimer.isRunning() ) {
      whiteTimer.stopTimer( false );
    }else{
      blackTimer.stopTimer( false );
    }
  }

  public void timeOutEvent( Timer timer ) {
    if( timer==whiteTimer ) {
      table.stopGame( ChessFrame.CHESSMATE_WHITE_TIMEOUT );
    }else{
      table.stopGame( ChessFrame.CHESSMATE_BLACK_TIMEOUT );
    }
  }

  public void setMinutesIncrement( int minutes,int increment ) {
    whiteTimer.setMinutes( minutes );
    blackTimer.setMinutes( minutes );
    whiteTimer.setIncrement( increment );
    blackTimer.setIncrement( increment );
  }

  public int getMinutes() {
    return whiteTimer.getMinutes();
  }

  public int getIncrement() {
    return whiteTimer.getIncrement();
  }

  public String getTime() {
    StringBuffer buffer=new StringBuffer( ":" );
    buffer.append( whiteTimer.getTime() );
    buffer.append( ":" );
    buffer.append( blackTimer.getTime() );
    return buffer.toString();
  }

  public void setTime( int whiteMin,int whiteSec,int blackMin,int blackSec ) {
    whiteTimer.setTime( whiteMin,whiteSec );
    blackTimer.setTime( blackMin,blackSec );
  }

  public boolean isTicking() {
    return isPlaying;
  }

  public JPanel getUpperTimer() {
    return upper;
  }

  public JPanel getLowerTimer() {
    return lower;
  }

  public void swapTimers( boolean normalOrder ) {
    if( startOrder && !normalOrder ) {
      upper.removeAll();
      lower.removeAll();
      upper.add( whiteTimer.getJPanel() );
      lower.add( blackTimer.getJPanel() );
      startOrder=false;
    }else if( !startOrder && normalOrder ) {
      upper.removeAll();
      lower.removeAll();
      upper.add( blackTimer.getJPanel() );
      lower.add( whiteTimer.getJPanel() );
      startOrder=true;
    }
  }
}
