import java.awt.event.*;
import java.awt.*;
import javax.swing.*;

class PawnChangeDialog
extends JDialog
implements ActionListener {

  public static final int QUEEN_SELECTED =1;
  public static final int ROCK_SELECTED  =2;
  public static final int KNIGHT_SELECTED=3;
  public static final int BISHOP_SELECTED=4;
  public int selected;

  public PawnChangeDialog( JFrame parent,Point local ) {
    super( parent,true );
    setContentPane( designLayout() );
    pack();
    setResizable( false );
    setLocation( local );
    setVisible( true );
  }

  private JPanel designLayout() {
    JPanel panel=new JPanel();
    JButton button;
    panel.add( new JLabel( "Verwandeln in" ) );
    button=new JButton( "Dame" );
    button.addActionListener( this );
    panel.add( button );
    button=new JButton( "Turm" );
    button.addActionListener( this );
    panel.add( button );
    button=new JButton( "Springer" );
    button.addActionListener( this );
    panel.add( button );
    button=new JButton( "Läufer" );
    button.addActionListener( this );
    panel.add( button );
    return panel;
  }

  public void actionPerformed( ActionEvent event ) {
    String com=event.getActionCommand();
    if( com.equals( "Dame" ) ) {
      selected=QUEEN_SELECTED;
    }else if( com.equals( "Turm" ) ) {
      selected=ROCK_SELECTED;
    }else if( com.equals( "Springer" ) ) {
      selected=KNIGHT_SELECTED;
    }else if(com.equals( "Läufer" ) ) {
      selected=BISHOP_SELECTED;
    }
    dispose();
  }
}
