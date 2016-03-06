import javax.swing.*;
import java.awt.*;

class ChessButton {

  private JPanel panel;
  private JButton button;
  private JLabel label;
  private Option option;
  private String color,name;
  private boolean isEmpty;

  public ChessButton( String color,Option option,BothButton buttons ) {
    panel=new JPanel();
    panel.setLayout( new BorderLayout() );
    this.color=color;
    this.option=option;
    button=new JButton( "Setzen" );
    button.addActionListener( buttons );
    button.setActionCommand( color );
    button.setBorder( BorderFactory.createEtchedBorder() );
    panel.add( button,"West" );
    label=new JLabel( color );
    label.setBorder( BorderFactory.createEtchedBorder() );
    panel.add( label,"Center" );
    isEmpty=true;
    name=":";
  }

  public void setName( String name ) {
    this.name=name;
    isEmpty=false;
    label.setText( name );
    button.setText( color );
    button.setEnabled( false );
    option.sitDownEvent( this );
  }

  public String getName() {
    return name;
  }

  public String getColor() {
    return color;
  }

  public void setEnabled( boolean enable ) {
    button.setEnabled( enable );
  }

  public void deleteName() {
    isEmpty=true;
    label.setText( color );
    button.setText( "Setzen" );
    button.setEnabled( true );
    option.standUpEvent( this );
    name=":";
  }

  public boolean isEmpty() {
    return isEmpty;
  }

  public JPanel getJPanel() {
    return panel;
  }
}
