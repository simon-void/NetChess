import javax.swing.*;
import java.awt.event.*;
import java.awt.*;
import java.util.*;

class MainFrame
extends JFrame
implements ActionListener,Reader {

  private JButton exit;
  private Terminal terminal;
  private PlayerList players;
  private Player player;
  private TableEntrySet tableEntrySet;

  public MainFrame( Player player,String init ) {
    super( "Mainframe" );
    this.player=player;
    setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
    setContentPane( designLayout() );
    initFrame( init );
    pack();
    setIconImage( Images.get( "ICON" ) );
    setVisible( true );
  }

  private JPanel designLayout() {
    JPanel panel=new JPanel( new BorderLayout() );
    JPanel left =new JPanel( new BorderLayout( 2,1 ) );
    JButton start=new JButton( "neuer Tisch" );
    start.addActionListener( this );
    left.add( start,"North" );
    exit=new JButton( "Aufhören" );
    exit.addActionListener( this );
    left.add( exit,"South" );
    players=new PlayerList( 15,10 );
    left.add( players,"Center" );
    panel.add( left,"West" );
    JPanel middle=new JPanel( new BorderLayout() );
    tableEntrySet=new TableEntrySet( this );
    middle.add( new JScrollPane( tableEntrySet.getJPanel() ),"Center" );
    terminal=new Terminal( this );
    middle.add( terminal,"South" );
    middle.setPreferredSize( middle.getPreferredSize() );
    panel.add( middle,"Center" );
    return panel;
  }

  private void initFrame( String init ) {
    StringTokenizer st=new StringTokenizer( init,":",false );
    final int playerNumber=Integer.parseInt( st.nextToken() );
    for( int i=0;i<playerNumber;i++ ) {
      players.addName( st.nextToken() );
    }
    while( st.hasMoreTokens() ) {
      int tablenumber=Integer.parseInt( st.nextToken() );
      int minutes    =Integer.parseInt( st.nextToken() );
      int increment  =Integer.parseInt( st.nextToken() );
      int playerNr   =Integer.parseInt( st.nextToken() );
      tableEntrySet.addEntry( tablenumber,st.nextToken(),minutes,increment );
      for( int i=1;i<playerNr;i++ ) {
        tableEntrySet.addName( tablenumber,st.nextToken() );
      }
      String type=st.nextToken();
      if( type.equals( "none_free" ) ) {
        tableEntrySet.movePlayer( tablenumber,st.nextToken(),true,true );
        tableEntrySet.movePlayer( tablenumber,st.nextToken(),true,false );
      }else if( type.equals( "white_free" ) ) {
        tableEntrySet.movePlayer( tablenumber,st.nextToken(),true,false );
      }else if( type.equals( "black_free" ) ) {
        tableEntrySet.movePlayer( tablenumber,st.nextToken(),true,true );
      }
    }
  }

  public void read( String msg ) {
    StringBuffer buffer=new StringBuffer( "mainframe" );
    buffer.append( ":" );
    buffer.append( msg );
    player.readFrame( buffer.toString() );
  }

  public void write( String msg ) {
    String type=msg.substring( 0,msg.indexOf( ":" ) );
    String rest=msg.substring( msg.indexOf( ":" )+1 );
    if( type.equals( "message" ) ) {
      terminal.write( rest );
    }else if( type.equals( "update" ) ) {
      updateInfo( rest );
    }else{
      terminal.write( msg );
    }
  }

  public void updateInfo( String msg ) {
    if( msg.startsWith( "player" ) ){
      updatePlayerList( msg.substring( msg.indexOf( ":" )+1 ) );
    }else{
      updateTableSet( msg );
    }
  }

  private void updatePlayerList( String msg ) {
    String type=msg.substring( 0,msg.indexOf( ":" ) );
    String name=msg.substring( msg.indexOf( ":" )+1 );
    if( type.equals( "new" ) ) {
      players.addName( name );
    }else if( type.equals( "remove" ) ) {
      players.deleteName( name );
    }else
      System.out.println( "mainframe.updatePlayerList:\n"+msg );
  }

  private void updateTableSet( String msg ) {
    StringTokenizer st=new StringTokenizer( msg,":",false );
    String type=st.nextToken();
    int table=Integer.parseInt( st.nextToken() );
    if( type.equals( "tableplayer" ) ) {
      String task=st.nextToken();
      String name=st.nextToken();
      if( task.equals( "new" ) ) {
        tableEntrySet.addName( table,name );
      }else{
        tableEntrySet.removeName( table,name );
      }
    }else if( type.equals( "tabletime" ) ) {
      int minute=Integer.parseInt( st.nextToken() );
      int increment=Integer.parseInt( st.nextToken() );
      tableEntrySet.setTime( table,minute,increment );
    }else if( type.equals( "tablestatus" ) ) {
      boolean sit=st.nextToken().equals( "sit" );
      String name=st.nextToken();
      boolean white=st.nextToken().equals( "white" );
      tableEntrySet.movePlayer( table,name,sit,white );
    }else if( type.equals( "tablelist" ) ) {
      tableEntrySet.addEntry( table,st.nextToken(),10,10 );
    }else{
      System.out.println( "mainframe.updateTableSet:\n"+msg );
    }
    validate();
  }

  public String getPlayerName() {
    return player.getPlayerName();
  }

  public void enableExit( boolean enable ) {
    exit.setEnabled( enable );
  }

  public void actionPerformed( ActionEvent event ) {
    String msg=event.getActionCommand();
    if( msg.equals( "Aufhören" ) ) {
      player.close();
      System.exit( 1 );
    }else if( msg.equals( "neuer Tisch" ) ) {
      read( "createTable" );
    }else{
      System.out.println( msg );
    }
  }
}
