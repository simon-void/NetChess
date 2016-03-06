import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.util.*;
import java.awt.event.*;

class TableEntry {

  private int tableNr;
  private JLabel timeLabel,nrLabel;
  private MiniBoard miniBoard;
  private JTextArea textArea;
  private TreeSet players;
  private TableEntrySet parent;
  private String whitePlayer,blackPlayer;

  public TableEntry( int nr,String player,int minutes,int increment,TableEntrySet parent ) {
    tableNr=nr;
    this.parent=parent;
    StringBuffer buffer=new StringBuffer();
    buffer.append( minutes );
    buffer.append( "/" );
    buffer.append( increment );
    timeLabel=new JLabel( buffer.toString(),SwingConstants.CENTER );
    nrLabel=new JLabel( "Tisch:"+nr );
    miniBoard=new MiniBoard( this );
    players=new TreeSet();
    players.add( player );
    textArea=new JTextArea( "",2,20 );
    textArea.setEditable( false );
    textArea.append( player );
  }

  public int getNr() {
    return tableNr;
  }

  public JLabel getNrLabel() {
    return nrLabel;
  } 

  public JLabel getTimeLabel() {
    return timeLabel;
  }

  public MiniBoard getMiniBoard() {
    return miniBoard;
  }

  public JScrollPane getTextArea() {
    return new JScrollPane( textArea );
  }

  public void addPlayer( String name ) {
    players.add( name );
    paintNames();
  }

  public void removePlayer( String name ) {
    players.remove( name );
    if( players.size()==0 ) {
      parent.removeEntry( this );
    }else{
      paintNames();
    }
    if( whitePlayer!=null && whitePlayer.equals( name ) ) {
      whitePlayer=null;
      setStatus();
    }else if( blackPlayer!=null && blackPlayer.equals( name ) ) {
      blackPlayer=null;
      setStatus();
    }
  }

  public void sitDownPlayer( String name,boolean white ) {
    if( white ) {
      whitePlayer=name;
    }else{
      blackPlayer=name;
    }
    setStatus();
  }

  public void standUpPlayer( String name,boolean white ) {
    if( white ) {
      whitePlayer=null;
    }else{
      blackPlayer=null;
    }
    setStatus();
  }

  private void paintNames() {
    StringBuffer firstRow=new StringBuffer( "" );
    StringBuffer secondRow=new StringBuffer( "" );
    Iterator iterator=players.iterator();
    while( iterator.hasNext() ) {
      firstRow.append( (String)iterator.next() );
      firstRow.append( " " );
      if( iterator.hasNext() ) {
        secondRow.append( (String)iterator.next() );
        secondRow.append( " " );
      }else{
        break;
      }
    }
    textArea.setText( firstRow.toString() );
    textArea.append( "\n" );
    textArea.append( secondRow.toString() );
  }

  public void setTime( int min,int inc ) {
    StringBuffer buffer=new StringBuffer( Integer.toString( min ) );
    buffer.append( "/" );
    buffer.append( Integer.toString( inc ) );
    timeLabel.setText( buffer.toString() );
  }

  private void setStatus() {
    if( whitePlayer==null ) {
      if( blackPlayer==null ) {
        miniBoard.setStatus( MiniBoard.BOTH_FREE );
      }else{
        miniBoard.setStatus( MiniBoard.WHITE_FREE );
      }
    }else{
      if( blackPlayer==null ) {
        miniBoard.setStatus( MiniBoard.BLACK_FREE );
      }else{
        miniBoard.setStatus( MiniBoard.NONE_FREE );
      }
    }
  }

  public void joinBoard() {
    if( !players.contains( parent.getPlayerName() ) ) {
      parent.joinBoard( tableNr );
    }
  }
}
