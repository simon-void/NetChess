import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

class Terminal
extends JPanel
implements ActionListener {

  private int rows;
  private int columns;
  private JTextField textField;
  private JTextArea  textArea;
  private Reader parent;

  public Terminal( Reader parent ) {
    this( 5,39,parent );
  }

  public Terminal( int rows,int columns,Reader parent ) {
    this.parent=parent;
    this.columns=columns;
    this.rows=rows;
    designLayout();
    setVisible( true );
  }

  private void designLayout() {
    setLayout( new BorderLayout() );
    textField=new JTextField( "" );
    textField.addActionListener( this );
    add( textField,"North" );
    textArea =new JTextArea( rows,columns );
    textArea.setRows( rows );
    textArea.setEditable( false );
    textArea.setLineWrap( true );
    textArea.setWrapStyleWord( true );
    JScrollPane scrollPane=new JScrollPane( textArea,
                                            JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                                            JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
    );
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
    add( scrollPane,"Center" );
  }

  private void read( String msg ) {
    StringBuffer buffer=new StringBuffer( parent.getPlayerName() );
    buffer.append( ":" );
    buffer.append( msg );
    write( buffer.toString() );
    parent.read( ( buffer.insert( 0,"message:" ) ).toString() );
  }

  public void write( String msg ) {
    if( textArea.getText().equals( "" ) ) {
      textArea.setText( msg );
    }else{
      textArea.append( "\n"+msg );
    }
  }

  public void changeSize( boolean bigger ) {
    if( bigger ) {
      columns=39;
    }else{
      columns=23;
    }
    textArea.setColumns( columns );
    textField.setColumns( columns );
    invalidate();
  }

  public void actionPerformed( ActionEvent event ) {
    String msg=textField.getText();
    textField.setText( "" );
    read( msg );
  }
}
