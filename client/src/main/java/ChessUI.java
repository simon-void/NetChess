import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class ChessUI
extends JComponent {

  private final int WEISS_BAUER=11;
  private final int WEISS_TURM=12;
  private final int WEISS_PFERD=13;
  private final int WEISS_LÄUFER=14;
  private final int WEISS_DAME=15;
  private final int WEISS_KÖNIG=16;
  private final int SCHWARZ_BAUER=21;
  private final int SCHWARZ_TURM=22;
  private final int SCHWARZ_PFERD=23;
  private final int SCHWARZ_LÄUFER=24;
  private final int SCHWARZ_DAME=25;
  private final int SCHWARZ_KÖNIG=26;

  private final static Image WEISS_BAUER_GIF=Images.get( "W_BAUER" );
  private final static Image WEISS_TURM_GIF=Images.get( "W_TURM" );
  private final static Image WEISS_PFERD_GIF=Images.get( "W_PFERD" );
  private final static Image WEISS_LÄUFER_GIF=Images.get( "W_LÄUFER" );
  private final static Image WEISS_DAME_GIF=Images.get( "W_DAME" );
  private final static Image WEISS_KÖNIG_GIF=Images.get( "W_KÖNIG" );
  private final static Image SCHWARZ_BAUER_GIF=Images.get( "S_BAUER" );
  private final static Image SCHWARZ_TURM_GIF=Images.get( "S_TURM" );
  private final static Image SCHWARZ_PFERD_GIF=Images.get( "S_PFERD" );
  private final static Image SCHWARZ_LÄUFER_GIF=Images.get( "S_LÄUFER" );
  private final static Image SCHWARZ_DAME_GIF=Images.get( "S_DAME" );
  private final static Image SCHWARZ_KÖNIG_GIF=Images.get( "S_KÖNIG" );

  private int[][] chessBoard;			//repräsentiert das Schachbrett
  private int areaSize;				//die Länge,Breite eines Spielfeldes
  private int borderSize;			//die Größe der Umrandung des Spielfeldes
  private Point activatedArea;			//mousePressed über diesem Feld
  private Point aimedArea;			//mouseMoved   über diesem Feld
  private boolean whiteView;			//aus der Sicht von Weiß

  public ChessUI( Chess logic,int[][] chessBoard ) {
    this.chessBoard=chessBoard;
    borderSize=25;
    areaSize=50;
    whiteView=true;
    activatedArea=new Point( -1,-1 );
    aimedArea    =new Point( -1,-1 );

    addMouseListener( logic );
    addMouseMotionListener( logic );
    setBorder( BorderFactory.createCompoundBorder(
                 BorderFactory.createLineBorder( Color.black ),
                 BorderFactory.createBevelBorder( 0,Color.gray,Color.darkGray )
               )
    );
    setPreferredSize( new Dimension( 2*borderSize+8*areaSize,2*borderSize+8*areaSize ) );
    setDoubleBuffered( true );
    setVisible( true );
  }

  public void changeSize( boolean bigger ) {
    if( bigger ) {
      areaSize=50;
      borderSize=25;
    }else{
      areaSize=30;
      borderSize=15;
    }
    setPreferredSize( new Dimension( 2*borderSize+8*areaSize,2*borderSize+8*areaSize ) );
    invalidate();
  }

  public void setView( int status ) {
    if( status==Chess.SCHWARZ ) {
      whiteView=false;
    }else{
      whiteView=true; 
    }
    repaint();
  }


  public int getBorderSize() {
    return borderSize;
  }

  public int getAreaSize() {
    return areaSize;
  }

  public boolean isWhiteView() {
    return whiteView;
  }

  public void setActivatedArea( Point area ) {
    activatedArea=area;
  }

  public Point getActivatedArea() {
    return activatedArea;
  }

  public void setAimedArea( Point area ) {
    aimedArea=area;
  }

  public Point getAimedArea() {
    return aimedArea;
  }

  private void paintActivAreas( Graphics g ) {
    int activX,activY,aimedX,aimedY;
    if( whiteView ) {
      activX=activatedArea.x;
      activY=7-activatedArea.y;
      aimedX =aimedArea.x;
      aimedY =7-aimedArea.y;
    }else{
      activX=7-activatedArea.x;
      activY=activatedArea.y;
      aimedX =7-aimedArea.x;
      aimedY =aimedArea.y;
    }
    g.setColor( Color.darkGray );
    if( activatedArea.x!=-1 ) {
      g.drawRect( borderSize+activX*areaSize+1,
                  borderSize+activY*areaSize+1,
                  areaSize-3,
                  areaSize-3
      );
      if( aimedArea.x!=-1 ){
        g.drawRect( borderSize+aimedX*areaSize+1,
                    borderSize+aimedY*areaSize+1,
                    areaSize-3,
                    areaSize-3
        );
      }
    }
  }

  protected void paintComponent( Graphics g ) {
    paintBoard( g );
    paintFigures( g );
    paintActivAreas( g );
  }

  private void paintBoard( Graphics g ) {
    g.setColor( Color.white );
    g.fillRect( 0,0,2*borderSize+8*areaSize,2*borderSize+8*areaSize );
    g.setColor( Color.lightGray );
    g.drawRect( borderSize,borderSize,8*areaSize,8*areaSize );
    for( int i=0;i<8;i++ ) {
      for( int j=0;j<8;j++ ) {
        if( (( i+j )%2 )==1 ) {
          g.fillRect( borderSize+i*areaSize,borderSize+j*areaSize,areaSize,areaSize );
        }
      }
    }
  }

  private void paintFigures( Graphics g ) {
    for( int i=0;i<8;i++ ) {
      for( int j=0;j<8;j++ ) {
        if( (chessBoard[i][j]%10)%7!=0 ) {			//diese mod-Anwendung ist gegen ENPASSANT
          paintFigure( g,i,j,chessBoard[i][j] );
        }
      }
    }
  }

  private void paintFigure( Graphics g,int hight,int width,int figure ) {
    if( whiteView ) {
      hight=8-hight;
    }else{
      width=7-width;
      hight++;
    }
    int minX=borderSize+width*areaSize;
    int minY=borderSize+(hight-1)*areaSize;

    switch( figure ) {
      case WEISS_BAUER:
        if( areaSize==50 ) {
          g.drawImage( WEISS_BAUER_GIF,minX,minY,this );
        }else{
          g.drawImage( WEISS_BAUER_GIF,minX,minY,areaSize,areaSize,this );
        }
        break;
      case WEISS_TURM:
        if( areaSize==50 ) {
          g.drawImage( WEISS_TURM_GIF,minX,minY,this );
        }else{
          g.drawImage( WEISS_TURM_GIF,minX,minY,areaSize,areaSize,this );
        }
        break;
      case WEISS_PFERD:
        if( areaSize==50 ) {
          g.drawImage( WEISS_PFERD_GIF,minX,minY,this );
        }else{
          g.drawImage( WEISS_PFERD_GIF,minX,minY,areaSize,areaSize,this );
        }
        break;
      case WEISS_LÄUFER:
        if( areaSize==50 ) {
          g.drawImage( WEISS_LÄUFER_GIF,minX,minY,this );
        }else{
          g.drawImage( WEISS_LÄUFER_GIF,minX,minY,areaSize,areaSize,this );
        }
        break;
      case WEISS_DAME:
        if( areaSize==50 ) {
          g.drawImage( WEISS_DAME_GIF,minX,minY,this );
        }else{
          g.drawImage( WEISS_DAME_GIF,minX,minY,areaSize,areaSize,this );
        }
        break;
      case WEISS_KÖNIG:
        if( areaSize==50 ) {
          g.drawImage( WEISS_KÖNIG_GIF,minX,minY,this );
        }else{
          g.drawImage( WEISS_KÖNIG_GIF,minX,minY,areaSize,areaSize,this );
        }
        break;
      case SCHWARZ_BAUER:
        if( areaSize==50 ) {
          g.drawImage( SCHWARZ_BAUER_GIF,minX,minY,this );
        }else{
          g.drawImage( SCHWARZ_BAUER_GIF,minX,minY,areaSize,areaSize,this );
        }

        break;
      case SCHWARZ_TURM:
        if( areaSize==50 ) {
          g.drawImage( SCHWARZ_TURM_GIF,minX,minY,this );
        }else{
          g.drawImage( SCHWARZ_TURM_GIF,minX,minY,areaSize,areaSize,this );
        }
        break;
      case SCHWARZ_PFERD:
        if( areaSize==50 ) {
          g.drawImage( SCHWARZ_PFERD_GIF,minX,minY,this );
        }else{
          g.drawImage( SCHWARZ_PFERD_GIF,minX,minY,areaSize,areaSize,this );
        }
        break;
      case SCHWARZ_LÄUFER:
        if( areaSize==50 ) {
          g.drawImage( SCHWARZ_LÄUFER_GIF,minX,minY,this );
        }else{
          g.drawImage( SCHWARZ_LÄUFER_GIF,minX,minY,areaSize,areaSize,this );
        }
        break;
      case SCHWARZ_DAME:
        if( areaSize==50 ) {
          g.drawImage( SCHWARZ_DAME_GIF,minX,minY,this );
        }else{
          g.drawImage( SCHWARZ_DAME_GIF,minX,minY,areaSize,areaSize,this );
        }
        break;
      case SCHWARZ_KÖNIG:
        if( areaSize==50 ) {
          g.drawImage( SCHWARZ_KÖNIG_GIF,minX,minY,this );
        }else{
          g.drawImage( SCHWARZ_KÖNIG_GIF,minX,minY,areaSize,areaSize,this );
        }
        break;
    }
  }

}

