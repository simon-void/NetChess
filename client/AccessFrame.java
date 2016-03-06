import javax.swing.*;
import java.awt.event.*;
import java.awt.*;
import java.util.*;
import java.net.*;
import java.io.*;

class AccessFrame
extends JFrame
implements ActionListener {

  private JComboBox serverList;
  private JTextField textField;
  private JTextArea textArea;
  private Socket socket;
  private BufferedReader reader;
  private BufferedWriter writer;
  private TreeMap servers;

  public static void main( String[] args ) {
    new AccessFrame();
  }

  public AccessFrame() {
    super( "geben Sie eine Server-IP ein" );
    addWindowListener(
      new WindowAdapter() {
        public void windowClosing( WindowEvent event ) {
          if( socket!=null ) {
            try{
              socket.close();
            }catch( IOException e ) {}
          }
          dispose();
          System.exit( 1 );
        }
      }
    );
    setContentPane( designLayout() );
    pack();
    setVisible( true );
    Images.init( this );
  }

  private JPanel designLayout() {
    JPanel panel=new JPanel();
    panel.setLayout( new BorderLayout() );
    JPanel intern=new JPanel();
    intern.setLayout( new GridLayout( 4,1 ) );
    intern.add( new JLabel( "Server" ) );
    serverList=new JComboBox( getServerList() );
    serverList.setEditable( true );
    serverList.addActionListener( this );
    intern.add( serverList );
    intern.add( new JLabel( "Name" ) );
    textField=new JTextField();
    textField.setEditable( false );
    textField.addActionListener( this );
    intern.add( textField );
    panel.add( intern,"West" );
    textArea=new JTextArea( 5,30 );
    textArea.setEditable( false );
    textArea.setToolTipText( "programmiert von Stephan Schröder, Dezember2001" );
    panel.add( new JScrollPane( textArea ),"Center" );
    return panel;
  }

  public void actionPerformed( ActionEvent event )
  {
    Object source=event.getSource();
    if( source==textField ) {
      initPlayerName( textField.getText() );
    }else{
      getSocket( ((String)serverList.getSelectedItem()).trim() );
    }
  }

  private void getSocket( String server )
  {
    textField.setEditable( false );
    try{
      socket=new Socket( server,2309 );
      socket.setSoTimeout( 5000 );
      writer=new BufferedWriter( new OutputStreamWriter( socket.getOutputStream() ) );
      reader=new BufferedReader( new InputStreamReader(  socket.getInputStream()  ) );
      textField.setEditable( true );
      textField.requestFocus();
      textArea.setText( "Verbindung hergestellt\nGeben Sie einen Namen ein" );
      saveServer( server );
    }catch( UnknownHostException unknownHostException ) {
      textArea.setText( "Unbekannter Server\n\n"+unknownHostException.getMessage() );
    }
    catch( InterruptedIOException ioException ) {
      textArea.setText( "Nach 5 Sekunden noch keine Antwort vom Server\n\n"+ioException.getMessage() );
    }
    catch( IOException e ) {
      textArea.setText( "Verbindungsfehler\n\n"+e.getMessage() );
    }
  }

  private void initPlayerName( String name )
  {
    if( name.indexOf( ":" )!=-1 ) {
      textArea.setText( "Der Name darf kein : enthalten." );
      return;
    }
    String serveranswer="keine Antwort";
    try{
      writer.write( name+"\n" );
      writer.flush();
      serveranswer=reader.readLine();
    }catch( IOException e ){System.out.println(e.toString());}
    if( serveranswer.startsWith( "name accepted:" ) ){
      new Player( name,socket,serveranswer.substring( 14 ) );
      dispose();
    }else if( serveranswer.equals( "name dinided" ) )
      textArea.setText( "Der Name ist bereits vorhanden.\nW�hlen Sie einen neuen." );
    else
      textArea.setText( "Ungültige Serverantwort:\n"+serveranswer );
  }

  private String[] getServerList() {
    servers=new TreeMap();
    try{
      BufferedReader reader=new BufferedReader( new FileReader( "ServerListe.txt" ) );
      String line=reader.readLine();
      int i=1;
      while( line!=null ) {
        servers.put( new Integer(i++),line );
        line=reader.readLine();
      }
    }catch( IOException e ) {}
    String[] servernames=new String[servers.size()+1];
    servernames[0]="localhost";
    Iterator iterator=servers.values().iterator();
    for( int j=1;j<servernames.length;j++ ) {
      servernames[j]=(String)iterator.next();
    }
    return servernames;
  }

  private void saveServer( String server ) {
    if( server.equals( "localhost" ) || (servers.size()!=0 && servers.containsValue( server )) ) {
      return;
    }
    if( servers.size()==3 ) {
      servers.remove( new Integer(3) );
    }
    servers.put( new Integer( 0 ),server );
    StringBuffer buffer=new StringBuffer();
    Iterator iterator=servers.values().iterator();
    while( iterator.hasNext() ) {
      buffer.append( (String)iterator.next() );
      buffer.append( "\n" );
    }
    try{
      File file=new File( ".","ServerListe.txt" );
      file.delete();
      file.createNewFile();
      BufferedWriter writer=new BufferedWriter( new FileWriter( file ) );
      writer.write( buffer.toString() );
      writer.flush();
      writer.close();
    }catch( IOException e ) {}
  }
}
