import javax.swing.*;
import java.awt.event.*;
import java.util.*;

class PlayerList
extends JPanel {

  private TreeSet playerNames;
  private JTextArea textArea;

  public PlayerList() {
    this( 5,10 );
  }

  public PlayerList( int rows,int columns ) {
    playerNames=new TreeSet();
    textArea=new JTextArea( rows,columns );
    textArea.setEditable( false );
    add( new JScrollPane( textArea ) );
  }

  public void addName( String name ) {
    playerNames.add( name );
    paintNames();
  }

  public void deleteName( String name ) {
    playerNames.remove( name );
    paintNames();
  }

  private void paintNames() {
    StringBuffer buffer=new StringBuffer();
    Iterator iterator=playerNames.iterator();
    while( iterator.hasNext() ) {
      buffer.append( (String)iterator.next() );
      if( iterator.hasNext() ) {
        buffer.append( "\n" );
      }
    }
    textArea.setText( buffer.toString() );
  }
}
