import javax.swing.*;
import java.awt.event.*;
import java.awt.*;

class MoveList
extends JPanel {

  private int moveNumber;
  private JTextArea textArea;
  private boolean whiteTurn;

  public MoveList() {
    this( 17,10 );
  }

  public MoveList( int rows,int columns ) {
    moveNumber=0;
    whiteTurn=true;
    textArea=new JTextArea( rows,columns );
    textArea.setEditable( false );
    JScrollPane scrollPane=new JScrollPane( textArea,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,JScrollPane.HORIZONTAL_SCROLLBAR_NEVER );
    scrollPane.getVerticalScrollBar().addAdjustmentListener(
      new AdjustmentListener() {
        public void adjustmentValueChanged( AdjustmentEvent event ) {
          Adjustable adjust=event.getAdjustable();
          if( !((JScrollBar)adjust).getValueIsAdjusting() ) {
            adjust.setValue( adjust.getMaximum() );
          }
        }
      }
    );
    add( scrollPane );
  }

  public void addMove( String move ) {
    if( whiteTurn ) {
      whiteTurn=false;
      textArea.append( new Integer( ++moveNumber ).toString()+". "+move+"   " );
    }else{
      whiteTurn=true;
      textArea.append( move+"\n" );
    }
  }

  public void deleteMove() {
    String history=textArea.getText();
    int pos;
    if( whiteTurn ) {
      whiteTurn=false;
      pos=history.lastIndexOf( " " );
      history=history.substring( 0,pos+1 );
    }else{
      whiteTurn=true;
      pos=history.lastIndexOf( "\n" );
      history=history.substring( 0,pos+1 );
      moveNumber--;
    }
    textArea.setText( history );
  }

  public void clear() {
    moveNumber=0;
    whiteTurn=true;
    textArea.setText( "" );
  }

  public boolean isEmpty() {
    return moveNumber==0;
  }

  public boolean isWhiteTurn() {
    return whiteTurn;
  }

  public void changeSize( boolean bigger ) {
    if( bigger ) {
      textArea.setRows( 17 );
    }else{
      textArea.setRows( 6 );
    }
    invalidate();
  }
}
