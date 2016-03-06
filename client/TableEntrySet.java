import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.util.*;

class TableEntrySet
extends JPanel {

  private TreeMap entryMap;
  private MainFrame parent;
  private int entryWidth,entryHeight,entrySpace;

  public TableEntrySet( MainFrame parent ) {
    this.parent=parent;
    entryMap=new TreeMap();
    setLayout( null );
    setBorder( new LineBorder( Color.black ) );
  }

  public void addEntry( int nr,String player,int minutes,int increment ) {
    entryMap.put( new Integer( nr ),new TableEntry( nr,player,minutes,increment,this ) );
    designLayout();
  }

  public void removeEntry( TableEntry entry ) {
    entryMap.remove( new Integer( entry.getNr() ) );
    designLayout();
  }

  public JPanel getJPanel() {
    return this;
  }

  private void designLayout() {
    removeAll();
    int size=entryMap.size();
    int xspace=20;
    int yspace=3;
    if( size==0 ) {
      repaint();
      return;
    }
    int maxLabelWidth=0;
    int miniBoardWidth=0;
    int textWidth=0;
    int maxHeight=0;
    Iterator iterator=entryMap.values().iterator();
    TableEntry entry=(TableEntry)iterator.next();
    Dimension dim=entry.getTimeLabel().getPreferredSize();
    maxLabelWidth=(int)dim.getWidth();
    maxHeight=2*(int)dim.getHeight();
    dim=entry.getNrLabel().getPreferredSize();
    if( dim.getWidth()>maxLabelWidth ) {
      maxLabelWidth=(int)dim.getWidth();
    }
    dim=entry.getMiniBoard().getPreferredSize();
    if( dim.getHeight()>maxHeight ) {
      maxHeight=(int)dim.getHeight();
    }
    miniBoardWidth=(int)dim.getWidth();
    dim=entry.getTextArea().getPreferredSize();
    if( dim.getHeight()>maxHeight ) {
      maxHeight=(int)dim.getHeight();
    }
    textWidth=(int)dim.getWidth();
    while( iterator.hasNext() ) {
      entry=(TableEntry)iterator.next();
      dim=entry.getNrLabel().getPreferredSize();
      if( dim.getWidth()>maxLabelWidth ) {
        maxLabelWidth=(int)dim.getWidth();
      }
      dim=entry.getTimeLabel().getPreferredSize();
      if( dim.getWidth()>maxLabelWidth ) {
        maxLabelWidth=(int)dim.getWidth();
      }
    }
    setPreferredSize( new Dimension( maxLabelWidth+miniBoardWidth+textWidth+4*xspace,(yspace+maxHeight)*size+yspace ) );
    iterator=entryMap.values().iterator();
    for( int i=0;i<size;i++ ) {
      entry=(TableEntry)iterator.next();
      JLabel timeLabel=entry.getTimeLabel();
      JLabel nrLabel  =entry.getNrLabel();
      MiniBoard miniBoard=entry.getMiniBoard();
      JScrollPane text  =entry.getTextArea();
      dim=nrLabel.getPreferredSize();
      int width =(int)dim.getWidth();
      int height=(int)dim.getHeight();
      nrLabel.setBounds( xspace+(maxLabelWidth-width)/2,yspace+(yspace+maxHeight)*i+(maxHeight-2*height)/3,width,height );
      dim=timeLabel.getPreferredSize();
      width =(int)dim.getWidth();
      height=(int)dim.getHeight();
      timeLabel.setBounds( xspace+(maxLabelWidth-width)/2,yspace+(yspace+maxHeight)*i+2*((maxHeight-2*height)/3)+height,width,height );
      dim=miniBoard.getPreferredSize();
      width =(int)dim.getWidth();
      height=(int)dim.getHeight();
      miniBoard.setBounds( 2*xspace+maxLabelWidth,yspace+(yspace+maxHeight)*i+(maxHeight-height)/2,width,height );
      dim=text.getPreferredSize();
      width =(int)dim.getWidth();
      height=(int)dim.getHeight();
      text.setBounds( 3*xspace+maxLabelWidth+miniBoardWidth,yspace+(yspace+maxHeight)*i+(maxHeight-height)/2,width,height );
      add( nrLabel );
      add( timeLabel );
      add( miniBoard );
      add( text );
    }
    entryWidth=4*xspace+maxLabelWidth+miniBoardWidth+textWidth;
    entryHeight=maxHeight;
    entrySpace=yspace;
    repaint();
  }

  protected void paintComponent( Graphics g ) {
    super.paintComponent( g );
    final int size=entryMap.size();
    if( size==0 ) {
      return;
    }
    g.setColor( Color.black );
    for( int i=0;i<size;i++ ) {
      int ystart=entrySpace/2+  i  *(entryHeight+entrySpace);
      int yend  =entrySpace  +(i+1)*(entryHeight+entrySpace);
      g.drawLine( 0,ystart,entryWidth,ystart );
      g.drawLine( 0,ystart,0,yend );
      g.drawLine( entryWidth,ystart,entryWidth,yend );
    }
    g.drawLine( 0,entrySpace+size*(entryHeight+entrySpace),entryWidth,entrySpace+size*(entryHeight+entrySpace) );
  }

  public void addName( int table,String name ) {
    ((TableEntry)entryMap.get( new Integer( table ) )).addPlayer( name );
  }

  public void removeName( int table,String name ) {
    TableEntry entry=((TableEntry)entryMap.get( new Integer( table ) ));
    if( entry!=null ) {
      entry.removePlayer( name );
    }
  }

  public void movePlayer( int table,String name,boolean sitDown,boolean white ) {
    TableEntry entry=(TableEntry)entryMap.get( new Integer( table ) );
    if( sitDown ) {
      entry.sitDownPlayer( name,white );
    }else{
      entry.standUpPlayer( name,white );
    }
  }

  public void setTime( int table,int minutes,int increment ) {
    ((TableEntry)entryMap.get( new Integer( table ) )).setTime( minutes,increment );
  }

  public void joinBoard( int nr ) {
    parent.read( "joinTable:"+nr );
  }

  public String getPlayerName() {
    return parent.getPlayerName();
  }
}
