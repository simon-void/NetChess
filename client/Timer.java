import javax.swing.*;
import java.awt.*;

class Timer
implements Runnable {

  private int minutes,min;
  private int seconds,sec,deciSeconds;
  private int increment;
  private BothTimer parent;
  private JLabel label;
  private JPanel panel;
  private boolean stopp,running;

  public Timer( BothTimer parent ) {
    this.parent=parent;
    minutes=10;
    seconds=0;
    deciSeconds=0;
    increment=10;
    stopp=false;
    running=false;
    label=new JLabel( getTimeString( minutes,seconds ) );
    panel=new JPanel();
    panel.add( label );
  }

  public void setMinutes( int minutes ) {
    this.minutes=minutes;
    label.setText( getTimeString( minutes,seconds ) );
  }

  public int getMinutes() {
    return minutes;
  }

  public void setIncrement( int increment ) {
    this.increment=increment;
  }

  public int getIncrement() {
    return increment;
  }

  public void run() {
    resetTimer();
    while( true ) {
      label.setText( getTimeString( min,sec ) );
      synchronized( this ) {
        try{
          wait();
        }catch( InterruptedException e ) {}
      }
      running=true;
      while( !stopp ) {
        label.setText( getTimeString( min,sec ) );
        try{
          Thread.sleep( 100 );
        }catch( InterruptedException e ) {}
        if( deciSeconds!=9 ) {
          deciSeconds++;
        }else{
          deciSeconds=0;
          synchronized( this ) {
            if( sec!=0 ) {
              sec--;
            }else{
              if( min!=0 ) {
                sec=59;
                min--;
              }else{
                stopTimer( false );
                parent.timeOutEvent( this );
              }
            }
          }
        }
      }
      stopp=false;
      running=false;
    }
  }

  public void start() {
    new Thread( this ).start();
  }

  public void startTimer() {
    synchronized( this ) {
      notify();
    }
  }

  public void stopTimer( boolean addTime ) {
    stopp=true;
    if( addTime ) {
      addIncrement();
    }
  }

  public void resetTimer() {
    min=minutes;
    sec=seconds;
    deciSeconds=0;
    label.setText( getTimeString( minutes,seconds ) );
  }

  private void addIncrement() {
    if( (sec+=increment%60)>59 ) {
      sec=sec%60;
      min++;
    }
    min+=( increment/60 );
  }

  public boolean isRunning() {
    return running;
  }

  private String getTimeString( int minutes,int seconds ) {
    StringBuffer buffer=new StringBuffer();
    if( minutes<10 ) {
      buffer.append( " " );
    }
    buffer.append( minutes );
    buffer.append( ":" );
    if( seconds<10 ) {
      buffer.append( "0" );
    }
    buffer.append( seconds );
    return buffer.toString();
  }

  public void setTime( int min,int sec ) {
    synchronized( this ) {
      this.min=min;
      this.sec=sec;
    }
    label.setText( getTimeString( min,sec ) );
  }

  public String getTime() {
    StringBuffer buffer=new StringBuffer();
    synchronized( this ) {
      int min=this.min;
      int sec=this.sec;
      buffer.append( min );
      buffer.append( ":" );
      buffer.append( sec );
    }

    return buffer.toString();
  }

  public JPanel getJPanel() {
    return panel;
  }
}
