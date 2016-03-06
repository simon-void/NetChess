import java.awt.event.*;
import java.awt.*;
import javax.swing.*;

class TimerDialog
extends JDialog
implements ActionListener {

  public int minutes;
  public int increment;
  private JTextField minText,incText;

  public TimerDialog( JFrame parent,Point local,int minutes,int increment ) {
    super( parent,true );
    this.minutes=minutes;
    this.increment=increment;
    setContentPane( designLayout() );
    pack();
    setResizable( false );
    setLocation( local );
    setVisible( true );
  }

  private JPanel designLayout() {
    JPanel inner=new JPanel();
    JPanel outer=new JPanel();
    inner.add( new JLabel( "Minuten:" ) );
    inner.add( minText=new JTextField( 3 ) );
    inner.add( new JLabel( "extra:" ) );
    inner.add( incText=new JTextField( 3 ) );
    outer.setLayout( new GridLayout( 2,1 ) );
    outer.add( inner );
    JButton button=new JButton( "OK" );
    button.addActionListener( this );
    outer.add( button );
    setText( this.minutes,this.increment );
    return outer;
  }

  public void actionPerformed( ActionEvent event ) {
    try{
      minutes=new Integer( minText.getText() ).intValue();
      increment=new Integer( incText.getText() ).intValue();
      boolean changed=false;
      if( minutes<1 ) {
        minutes=1;
        changed=true;
      }
      if( minutes>999 ) {
        minutes=999;
        changed=true;
      }
      if( increment<0 ) {
        increment=0;
        changed=true;
      }
      if( increment>999 ) {
        increment=999;
        changed=true;
      }        
      if( changed ) {
        setText( minutes,increment );
      }else{
        this.minutes=minutes;
        this.increment=increment;
        dispose();
      }
    }catch(NumberFormatException e) {
      setText( this.minutes,this.increment );
    }
  }

  private void setText( int minutes,int increment ) {
    minText.setText( Integer.toString( this.minutes ) );
    incText.setText( Integer.toString( this.increment ) );
  }
}
