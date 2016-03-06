import javax.swing.*;
import java.awt.event.*;
import java.awt.*;
import java.util.*;

class SemimodalInfoDialog
extends JDialog
implements ActionListener,FocusListener {

  private ChessFrame parent;
  private boolean alive;

  public SemimodalInfoDialog( ChessFrame parent,String msg,String result ) {
    super( parent,result,false );
    this.parent=parent;
    parent.block( true );
    setContentPane( designLayout( msg,result ) );
    pack();
    Point local=parent.getLocation();
    local.translate( 180,200 );
    setLocation( local );
    setVisible( true );
  }

  private JPanel designLayout( String msg,String result ) {
    StringTokenizer st=new StringTokenizer( msg,"\n",false );
    JPanel panel=new JPanel( new GridLayout( st.countTokens()+1,1 ) );
    while( st.hasMoreTokens() ) {
      panel.add( new JLabel( st.nextToken() ) );
    }
    JButton ok=new JButton( "Ok" );
    ok.addActionListener( this );
    panel.add( ok );
    Dimension wantedWidth=new JLabel( result ).getPreferredSize();
    Dimension actualWidth=panel.getPreferredSize();
    if( wantedWidth.getWidth()+80>actualWidth.getWidth() ) {
      panel.setPreferredSize( new Dimension( (int)wantedWidth.getWidth()+80,(int)actualWidth.getHeight() ) );
    }
    return panel;
  }

  public void actionPerformed( ActionEvent event ) {
    setVisible( false );
    parent.block( false );
    alive=false;
    dispose();
  }

  public void focusGained( FocusEvent event ) {}

  public void focusLost( FocusEvent event ) {
    if( alive ) {
      requestFocus();
    }
  }
}
