import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;

class MiniBoard
extends JComponent {

  public final static int BOTH_FREE=0;
  public final static int WHITE_FREE=1;
  public final static int BLACK_FREE=2;
  public final static int NONE_FREE=3;

  private final static Image BLACK_KING=Images.get( "S_KÖNIG" );
  private final static Image WHITE_KING=Images.get( "W_KÖNIG" );
  private final static Image BLACK_WHITE_KING=Images.get( "W_S_KÖNIG" );
  private int status;
  private int areasize;
  private TableEntry parent;

  public MiniBoard( TableEntry parent ) {
    this.parent=parent;
    status=BOTH_FREE;
    areasize=32;
    addMouseListener(
      new MouseAdapter() {
        public void mousePressed( MouseEvent event ) {
          joinBoard();
        }
      }
    );
    setBorder( new CompoundBorder( new BevelBorder(BevelBorder.RAISED),new LineBorder( Color.lightGray ) ) );
    setVisible( true );
  }

  public void setStatus( int status ) {
    this.status=status;
    repaint();
  }

  public Dimension getPreferredSize() {
    return new Dimension( areasize,areasize );
  }

  protected void paintComponent( Graphics g ) {
    g.setColor( Color.white );
    g.fillRect( 0,0,areasize,areasize );
    g.setColor( Color.lightGray );
    g.fillRect( 0,0,areasize/2,areasize/2 );
    g.fillRect( areasize/2,areasize/2,areasize/2,areasize/2 );
    switch( status ) {
      case BOTH_FREE: g.drawImage( BLACK_WHITE_KING,0,0,areasize,areasize,this );break;
      case WHITE_FREE:g.drawImage( WHITE_KING,0,0,areasize,areasize,this );break;
      case BLACK_FREE:g.drawImage( BLACK_KING,0,0,areasize,areasize,this );
    }
  }

  private void joinBoard() {
    parent.joinBoard();
  }
}
