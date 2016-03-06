import java.awt.event.*;
import javax.swing.*;

class BothButton
implements ActionListener {

  private JPanel upper,lower;
  private ChessButton black,white;
  private boolean startOrder;
  private Table table;

  public BothButton( Table table,Option option ) {
    this.table=table;
    white=new ChessButton( "Weiß"   ,option,this );
    lower=new JPanel();
    lower.add( white.getJPanel() );
    black=new ChessButton( "Schwarz",option,this );
    upper=new JPanel();
    upper.add( black.getJPanel() );
    startOrder=true;
  }

  public void setName( String name,boolean isWhite ) {
    if( isWhite ) {
      white.setName( name );
    }else{
      black.setName( name );
    }
  }

  public void removeName( String name ) {
    String whiteName=white.getName();
    String blackName=black.getName();
    if( name.equals( whiteName ) ) {
      white.deleteName();
      if( black.isEmpty() ) {
        black.setEnabled( true );
      }else if( blackName.equals( table.getPlayerName() ) ) {
        white.setEnabled( false );
      }
    }else if( name.equals( blackName ) ) {
      black.deleteName();
      if( white.isEmpty() ) {
        white.setEnabled( true );
      }else if( whiteName.equals( table.getPlayerName() ) ) {
        black.setEnabled( false );
      }
    }
  }

  public JPanel getUpperButton() {
    return upper;
  }

  public JPanel getLowerButton() {
    return lower;
  }

  public void swapButtons( boolean normalOrder ) {
    if( startOrder && !normalOrder ) {
      upper.removeAll();
      lower.removeAll();
      upper.add( white.getJPanel() );
      lower.add( black.getJPanel() );
      startOrder=false;
    }else if( !startOrder && normalOrder ) {
      upper.removeAll();
      lower.removeAll();
      upper.add( black.getJPanel() );
      lower.add( white.getJPanel() );
      startOrder=true;
    }
  }

  public void actionPerformed( ActionEvent event ) {
    ChessButton button;
    if( event.getActionCommand().equals( "Schwarz" ) ) {
      button=black;
    }else{
      button=white;
    }
    if( button==white ) {
      black.setEnabled( false );
      table.setStatus( Chess.WEISS );
    }else{
      white.setEnabled( false );
      table.setStatus( Chess.SCHWARZ );
    } 
    button.setName( table.getPlayerName() );
    StringBuffer buffer=new StringBuffer( "button:sit:" );
    buffer.append( button.getColor() );
    buffer.append( ":" );
    buffer.append( button.getName() );
    table.read( buffer.toString() );
  }
}

