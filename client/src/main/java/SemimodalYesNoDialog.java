import javax.swing.*;
import java.awt.event.*;
import java.awt.*;

class SemimodalYesNoDialog
extends JDialog
implements ActionListener,FocusListener {

  public final static int UNDO=0;
  public final static int DRAW=1;
  private int type;
  private ChessFrame parent;
  private boolean alive;

  public SemimodalYesNoDialog( ChessFrame parent,String msg,int type ) {
    super( parent,"Achtung",false );
    this.parent=parent;
    this.type=type;
    alive=true;
    parent.block( true );
    setContentPane( designLayout( msg ) );
    pack();
    Point local=parent.getLocation();
    local.translate( 180,200 );
    setLocation( local );
    setVisible( true );
  }

  private JPanel designLayout( String msg ) {
    JPanel panel=new JPanel( new GridLayout( 2,1 ) );
    panel.add( new JLabel( msg ) );
    JPanel inner=new JPanel();
    JButton ja=new JButton( "Ja" );
    ja.addActionListener( this );
    inner.add( ja );
    JButton nein=new JButton( "Nein" );
    nein.addActionListener( this );
    inner.add( nein );
    panel.add( inner );
    return panel;
  }

  public void actionPerformed( ActionEvent event ) {
    setVisible( false );
    boolean doIt=event.getActionCommand().equals( "Ja" );
    switch( type ) {
      case DRAW:parent.semimodalDrawEvent( doIt );break;
      case UNDO:parent.semimodalUndoEvent( doIt );break;
    }
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
