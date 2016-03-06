import java.awt.event.*;
import java.awt.*;
import java.util.*;

class Chess
implements MouseListener,MouseMotionListener {

  private final int BAUER=1;
  private final int TURM=2;
  private final int PFERD=3;
  private final int LÄUFER=4;
  private final int DAME=5;
  private final int KÖNIG=6;
  public static final int NONE=0;
  public static final int WEISS=10;
  public static final int SCHWARZ=20;

  private int[][] chessBoard;
  private int[][] futureBoard;				//brauche ich in isBound() und soll nicht neu init werden
  private String[] savedBoardsWhite;			//Stellung gesichert,weiss ist dran
  private String[] savedBoardsBlack;			//Stellung gesichert,schwarz ist dran=>Patt wegen 3mal gleiche Stellung
  private int savedNumberWhite;				//anzahl der gespeicherten Stellungen Weiss
  private int savedNumberBlack;				//anzahl der gespeicherten Stellungen Schwarz
  private Table parent;
  private ChessUI chessUI;
  private BothTimer timers;
  private boolean whiteTurn;
  private boolean playing;
  private boolean whiteShortRochade;
  private boolean whiteLongRochade;
  private boolean blackShortRochade;
  private boolean blackLongRochade;
  private PawnChangeDialog pawnChange;
  private int whiteEnpassant;
  private int blackEnpassant;
  private int status;
  private boolean blocked;

  public Chess(Table parent ) {
    chessBoard=new int[8][8];
    this.parent=parent;
    timers=parent.getTimer();
    chessUI=new ChessUI( this,chessBoard );
    lineUpFigures();

    futureBoard=new int[8][8];
    savedBoardsWhite=new String[50];
    savedBoardsBlack=new String[50];
    savedNumberWhite=0;
    savedNumberBlack=0;

    whiteTurn=true;
    playing=false;
    whiteShortRochade=true;
    whiteLongRochade=true;
    blackShortRochade=true;
    blackLongRochade=true;
    whiteEnpassant=-1;
    blackEnpassant=-1;
    status=NONE;
    blocked=false;
  }

  public void mouseClicked( MouseEvent event )  {} 
  public void mouseEntered( MouseEvent event )  {}
  public void mouseExited( MouseEvent event )   {}
  public void mouseReleased( MouseEvent event ) {}
  public void mouseDragged( MouseEvent event )  {}

  public void mousePressed( MouseEvent event ) {
    if( !blocked ) {
      Point oldP=getActivatedArea();
      Point newP=getSelectedArea( event.getPoint() );
      if( (oldP.x!=-1)&&(newP.x!=-1) ) {
        repaint();
        activateArea( new Point(-1,-1) );
        if( isMoveable( oldP,newP ) ) {
          move( oldP,newP );
        }
      }else{
        if( isSelectable( newP ) ) {
          activateArea( newP );
          unaimArea();
        }
        repaint();
      }
    }
  }

  public void mouseMoved( MouseEvent event ) {
    if( !blocked ) {
      if( getActivatedArea().x!=-1 ) {
        Point oldP=getAimedArea();
        Point newP=getSelectedArea( event.getPoint() );
        boolean moveable=isMoveable( getActivatedArea(),newP );
        if( (newP.x!=oldP.x)||(newP.y!=oldP.y) ) {
          if( moveable ) {
            aimArea( newP );
          }else{
            unaimArea();
          }
          if( moveable||oldP.x!=-1 ) {
            repaint();
          }
        }
      }
    }
  }
 
  public void startGame() {
    whiteTurn=true;
    playing=true;
    lineUpFigures();
    resetMemoBoard();
    memoBoard();
  }

  public void stopGameFromOutside() {
    playing=false;
  }

  private void stopGameFromWithin( int endOption ) {
    parent.stopGame( endOption );
  }

  public void setStatus( int status ) {
    this.status=status;
    if( status==SCHWARZ ) {
      chessUI.setView( SCHWARZ );
    }else{
      chessUI.setView( WEISS );
    }
  }

  public int getStatus() {
    return status;
  }

  public ChessUI getUI() {
    return chessUI;
  }

  public void changeSize( boolean bigger ) {
    chessUI.changeSize( bigger );
  }

  public void block( boolean blockIt ) {
    blocked=blockIt;
  }

  private void sendMove( Point from,Point to ) {
    sendMove( from,to,"" );
  }

  private void sendMove( Point from,Point to,String msg ) {
    StringBuffer move=new StringBuffer();
    switch( from.x ) {
      case 0:move.append( "a" );break;
      case 1:move.append( "b" );break;
      case 2:move.append( "c" );break;
      case 3:move.append( "d" );break;
      case 4:move.append( "e" );break;
      case 5:move.append( "f" );break;
      case 6:move.append( "g" );break;
      case 7:move.append( "h" );break;
    }
    move.append( from.y+1 );
    move.append( "-" );
    switch( to.x ) {
      case 0:move.append( "a" );break;
      case 1:move.append( "b" );break;
      case 2:move.append( "c" );break;
      case 3:move.append( "d" );break;
      case 4:move.append( "e" );break;
      case 5:move.append( "f" );break;
      case 6:move.append( "g" );break;
      case 7:move.append( "h" );break;
    }
    move.append( to.y+1 );
    move.append( msg );
    parent.getMoves().addMove( move.toString() );
    move.insert( 0,"newMove:" );
    move.append( timers.getTime() );
    parent.read( move.toString() );
  }

  public void receiveMove( String move ) {
    Point from,to;
    switch( move.charAt( 0 ) ) {
      case 'a': from=new Point( 0,Integer.parseInt( move.substring( 1,2 ) )-1 );break;
      case 'b': from=new Point( 1,Integer.parseInt( move.substring( 1,2 ) )-1 );break;
      case 'c': from=new Point( 2,Integer.parseInt( move.substring( 1,2 ) )-1 );break;
      case 'd': from=new Point( 3,Integer.parseInt( move.substring( 1,2 ) )-1 );break;
      case 'e': from=new Point( 4,Integer.parseInt( move.substring( 1,2 ) )-1 );break;
      case 'f': from=new Point( 5,Integer.parseInt( move.substring( 1,2 ) )-1 );break;
      case 'g': from=new Point( 6,Integer.parseInt( move.substring( 1,2 ) )-1 );break;
      case 'h': from=new Point( 7,Integer.parseInt( move.substring( 1,2 ) )-1 );break;
      default:from=new Point( -1,-1 );
    }
    switch( move.charAt( 3 ) ) {
      case 'a': to=new Point( 0,Integer.parseInt( move.substring( 4,5 ) )-1 );break;
      case 'b': to=new Point( 1,Integer.parseInt( move.substring( 4,5 ) )-1 );break;
      case 'c': to=new Point( 2,Integer.parseInt( move.substring( 4,5 ) )-1 );break;
      case 'd': to=new Point( 3,Integer.parseInt( move.substring( 4,5 ) )-1 );break;
      case 'e': to=new Point( 4,Integer.parseInt( move.substring( 4,5 ) )-1 );break;
      case 'f': to=new Point( 5,Integer.parseInt( move.substring( 4,5 ) )-1 );break;
      case 'g': to=new Point( 6,Integer.parseInt( move.substring( 4,5 ) )-1 );break;
      case 'h': to=new Point( 7,Integer.parseInt( move.substring( 4,5 ) )-1 );break;
      default:to=new Point( -1,-1 );
    }
    StringTokenizer st=new StringTokenizer( move,":",false );
    st.nextToken();
    int whiteMin=Integer.parseInt( st.nextToken() );
    int whiteSec=Integer.parseInt( st.nextToken() );
    int blackMin=Integer.parseInt( st.nextToken() );
    int blackSec=Integer.parseInt( st.nextToken() );
    timers.setTime( whiteMin,whiteSec,blackMin,blackSec );
    if( move.charAt( 5 )==':' ) {
      move( from,to );
    }else{
      move( from,to,move.substring( 5,6 ) );
    }
  }

  private void switchTurn( boolean addTime ) {
    parent.getOptions().switchTurnEvent();
    timers.toggle( addTime );
    if( whiteTurn ) {
      whiteTurn=false;
    }else{
      whiteTurn=true;
    }
  }

  private boolean isMyTurn() {
    return ((status==WEISS)&&(whiteTurn))||((status==SCHWARZ)&&(!whiteTurn));
  }

  private Point getActivatedArea() {
    return chessUI.getActivatedArea();
  }

  private void deactivateArea() {
    chessUI.setActivatedArea( new Point( -1,-1 ) );
  }

  private void activateArea( Point area ) {
    chessUI.setActivatedArea( area );
  }

  private Point getAimedArea() {
    return chessUI.getAimedArea();
  }

  private void unaimArea() {
    chessUI.setAimedArea( new Point( -1,-1) );
  }

  private void aimArea( Point area ) {
    chessUI.setAimedArea( area );
  }

  private void repaint() {
    chessUI.repaint();
  }

  private boolean isFreeArea( int[][] board,Point area ) {
    return board[area.y][area.x]==0;
  }

  private boolean isWhiteArea( int[][] board,Point area ) {
    return board[area.y][area.x]/10==1;
  }

  private boolean isBlackArea( int[][] board,Point area ) {
    return board[area.y][area.x]/10==2;
  }

  private int getFigure( int[][] board,Point area ) {
    return board[area.y][area.x];
  }

  private void clearArea( int[][] board,Point area ) {
    board[area.y][area.x]=0;
  }

  private void moveFigure( int[][] board,Point from,Point to ) {
    board[to.y][to.x]=getFigure( board,from );
    clearArea( board,from );
  }

  private Point getSelectedArea( Point point ) {
    int borderSize=chessUI.getBorderSize();
    int areaSize  =chessUI.getAreaSize();
    Point area = new Point( -1,-1 );
    if( point.x<=borderSize ||
        point.x>=borderSize+8*areaSize ||
        point.y<=borderSize ||
        point.y>=borderSize+8*areaSize ) {
      return area;
    }
    point.x-=borderSize;
    point.y-=borderSize;
    area.x=point.x / areaSize;
    area.y=point.y / areaSize;
    if( chessUI.isWhiteView() ) {
      area.y=7-area.y;
    }else{
      area.x=7-area.x;
    }
    return area;
  }

  private boolean isSelectable( Point area ) {
    if( area.x==-1 ) {
      return false;
    }
    if( (     isWhiteArea( chessBoard,area )
          &&( whiteTurn  )
          &&( status==WEISS  )
          &&( figureIsNotStunned( area ) )
          &&  playing
        )||( 
              isBlackArea( chessBoard,area )
          &&( !whiteTurn )
          &&( status==SCHWARZ  )
          &&( figureIsNotStunned( area ) )
          &&  playing
           )
    ) {
      return true;
    }
    return false;
  }

  private void move( Point from,Point to ) {
    move( from,to,"" );
  }

  private void move( Point from,Point to,String pawnTo ) {
    boolean pawnChanged=false;
    int figure=getFigure( chessBoard,from );
    whiteEnpassant=-1;
    blackEnpassant=-1;
    switch( figure%10 ) {
      case BAUER:if( Math.abs(from.y-to.y)==2 ) {
                   if( figure/10==1 ) {
                     whiteEnpassant=from.x;
                   }else{
                     blackEnpassant=from.x;
                   }
                 }break;
      case TURM:if( from.equals( new Point( 0,0 ) ) ) {
                  whiteLongRochade=false;
                }
                if( from.equals( new Point( 7,0 ) ) ) {
                  whiteShortRochade=false;
                }
                if( from.equals( new Point( 0,7 ) ) ) {
                  blackLongRochade=false;
                }
                if( from.equals( new Point( 7,7 ) ) ) {
                  blackShortRochade=false;
                }
                break;
      case KÖNIG:if( isWhiteArea( chessBoard,from ) ) {
                   whiteShortRochade=false;
                   whiteLongRochade=false;
                 }else{
                   blackShortRochade=false;
                   blackLongRochade=false;
                 }
    }
    if( ( figure%10==BAUER )
        &&( isFreeArea( chessBoard,to ) )				//Enpassant
        &&( from.x!=to.x ) 
    ) {
      if( isWhiteArea( chessBoard,from ) ) {
        clearArea( chessBoard,new Point( to.x,to.y-1 ) );
      }else{
        clearArea( chessBoard,new Point( to.x,to.y+1 ) );
      }
    }
    if( figure%10==BAUER && pawnTo.equals( "" ) ) {						//Bauer auf Grundlinie
      if( isWhiteArea( chessBoard,from )&&from.y==6 ) {						//selbst gespielt
        Point local=parent.getFrame().getLocation();
        local.translate( 115,200 );
        pawnChange=new PawnChangeDialog( parent.getFrame(),local );
        switch( pawnChange.selected ) {
          case PawnChangeDialog.QUEEN_SELECTED :chessBoard[ from.y ][ from.x ]=WEISS+DAME;break;
          case PawnChangeDialog.ROCK_SELECTED  :chessBoard[ from.y ][ from.x ]=WEISS+TURM;break;
          case PawnChangeDialog.KNIGHT_SELECTED:chessBoard[ from.y ][ from.x ]=WEISS+PFERD;break;
          case PawnChangeDialog.BISHOP_SELECTED:chessBoard[ from.y ][ from.x ]=WEISS+LÄUFER;break;
          default:                              chessBoard[ from.y ][ from.x ]=WEISS+DAME;break;
        }
        pawnChanged=true;
      } else
      if( isBlackArea( chessBoard,from )&&from.y==1 ) {
        Point local=parent.getFrame().getLocation();
        local.translate( 115,200 );
        pawnChange=new PawnChangeDialog( parent.getFrame(),local  );
        switch( pawnChange.selected ) {
          case PawnChangeDialog.QUEEN_SELECTED :chessBoard[ from.y ][ from.x ]=SCHWARZ+DAME;break;
          case PawnChangeDialog.ROCK_SELECTED  :chessBoard[ from.y ][ from.x ]=SCHWARZ+TURM;break;
          case PawnChangeDialog.KNIGHT_SELECTED:chessBoard[ from.y ][ from.x ]=SCHWARZ+PFERD;break;
          case PawnChangeDialog.BISHOP_SELECTED:chessBoard[ from.y ][ from.x ]=SCHWARZ+LÄUFER;break;
          default:                              chessBoard[ from.y ][ from.x ]=SCHWARZ+DAME;break;
        }
        pawnChanged=true;
      }
    }
    if( !pawnTo.equals( "" ) ) {								//Bauer auf Grundlinie
      if( isWhiteArea( chessBoard,from ) ){							//vom anderen
        switch( pawnTo.charAt( 0 ) ) {
          case 'D':chessBoard[ from.y ][ from.x ]=WEISS+DAME;break;
          case 'T':chessBoard[ from.y ][ from.x ]=WEISS+TURM;break;
          case 'S':chessBoard[ from.y ][ from.x ]=WEISS+PFERD;break;
          case 'L':chessBoard[ from.y ][ from.x ]=WEISS+LÄUFER;break;
          default :chessBoard[ from.y ][ from.x ]=WEISS+DAME;
        }
      } else {
        switch( pawnTo.charAt( 0 ) ) {
          case 'D':chessBoard[ from.y ][ from.x ]=SCHWARZ+DAME;break;
          case 'T':chessBoard[ from.y ][ from.x ]=SCHWARZ+TURM;break;
          case 'S':chessBoard[ from.y ][ from.x ]=SCHWARZ+PFERD;break;
          case 'L':chessBoard[ from.y ][ from.x ]=SCHWARZ+LÄUFER;break;
          default :chessBoard[ from.y ][ from.x ]=SCHWARZ+DAME;
        }
      }
      pawnChanged=true;
    }

    moveFigure( chessBoard,from,to );					//der eigentliche Zug
    if( !pawnChanged ) {
      if( isMyTurn() ) {
        sendMove( from,to );						//,der dann noch verschickt wird
      }
    }else{
      if( pawnTo.equals( "" ) ) {
        switch( pawnChange.selected ) {
          case PawnChangeDialog.QUEEN_SELECTED :if( isMyTurn() )sendMove( from,to,"D" );break;
          case PawnChangeDialog.ROCK_SELECTED  :if( isMyTurn() )sendMove( from,to,"T" );break;
          case PawnChangeDialog.KNIGHT_SELECTED:if( isMyTurn() )sendMove( from,to,"S" );break;
          case PawnChangeDialog.BISHOP_SELECTED:if( isMyTurn() )sendMove( from,to,"L" );break;
          default:                              if( isMyTurn() )sendMove( from,to,"D" );break;
        }
      }
    }

    if( figure%10==KÖNIG ) {				//Rochade
      if( from.equals( new Point( 4,0 ) ) ) {
        if( to.equals( new Point( 2,0 ) ) ) {
          moveFigure( chessBoard,new Point( 0,0 ),new Point( 3,0 ) );
        }
        if( to.equals( new Point( 6,0 ) ) ) {
          moveFigure( chessBoard,new Point( 7,0 ),new Point( 5,0 ) );
        }
      }
      if( from.equals( new Point( 4,7 ) ) ) {
        if( to.equals( new Point( 2,7 ) ) ) {
          moveFigure( chessBoard,new Point( 0,7 ),new Point( 3,7 ) );
        }
        if( to.equals( new Point( 6,7 ) ) ) {
          moveFigure( chessBoard,new Point( 7,7 ),new Point( 5,7 ) );
        }
      }
    }
    if( isWhiteArea( chessBoard,to ) ) {
      if( isPatt( SCHWARZ ) ) {
        if( isBlackCheck( chessBoard ) ) {
          stopGameFromWithin( ChessFrame.CHESSMATE_BLACK );
        }else{
          stopGameFromWithin( ChessFrame.PATT_BLACK );
        }
      }
    }else{
      if( isPatt( WEISS ) ) {
        if( isWhiteCheck( chessBoard ) ) {
          stopGameFromWithin( ChessFrame.CHESSMATE_WHITE );
        }else{
          stopGameFromWithin( ChessFrame.PATT_WHITE );
        }
      }
    }
    switchTurn( true );
    memoBoard();
    repaint();
  }

  public boolean isMoveable( Point from,Point to ) {
    if( to.x==-1 ) {
      return false;
    }
    int figure=getFigure( chessBoard,from );
    boolean canMove;
    switch( figure%10 ) {
      case BAUER :canMove= isPawnMoveable( from,to );break;
      case TURM  :canMove= isRockMoveable( from,to );break;
      case PFERD :canMove= isKnightMoveable( from,to );break;
      case LÄUFER:canMove= isBishopMoveable( from,to );break;
      case DAME  :canMove= isQueenMoveable( from,to );break;
      case KÖNIG :canMove= isKingMoveable( from,to );break;
      default    :canMove= false;
    }
    return canMove && !isBound( from,to );
  }

  private boolean isPawnMoveable( Point from,Point to ) {
    if( isWhiteArea( chessBoard,from ) ) {
      if(   !(
            ( (Math.abs( from.x-to.x)==1)&&(from.y==to.y-1) )
          ||( (from.x==to.x) &&( (from.y==to.y-1)||(from.y==to.y-2) ) )
             )
      ) {
        return false;
      }
      if( from.x!=to.x ) {
        return isBlackArea( chessBoard,to )||( from.y==4&&to.x==blackEnpassant);
      }
      if( from.y==7 || !isFreeArea( chessBoard,new Point( from.x,from.y+1 ) ) ) {
        return false;
      }
      if( from.y==to.y-1 ) {
        return true;
      }
      return isFreeArea( chessBoard,to )&&from.y==1;
    }else{									//black
      if(   !(
            ( (Math.abs( from.x-to.x)==1)&&(from.y==to.y+1) )
          ||( (from.x==to.x) &&( (from.y==to.y+1)||(from.y==to.y+2) ) )
             )
      ) {
        return false;
      }
      if( from.x!=to.x ) {
        return isWhiteArea( chessBoard,to )||( from.y==3&&to.x==whiteEnpassant);
      }
      if( from.y==0 || !isFreeArea( chessBoard,new Point( from.x,from.y-1 ) ) ) {
        return false;
      }
      if( from.y==to.y+1 ) {
        return true;
      }
      return isFreeArea( chessBoard,to )&&from.y==6;
    }
  }

  private boolean isRockMoveable( Point from,Point to ) {
    boolean white=isWhiteArea( chessBoard,from );
    if( to.x==-1 ) {
      return false;
    }
    if( (from.x!=to.x)&&(from.y!=to.y) ) {
      return false;
    }
    if( from.x==to.x ) {
      if( from.y==to.y ) {
        return false;
      }
      if( from.y<to.y ) {
        for( int i=from.y+1;i<to.y;i++ ) {
          if( !isFreeArea( chessBoard,new Point( from.x,i ) ) ) {
            return false;
          }
        }
        if( white ) {
          return !isWhiteArea( chessBoard,to );
        }
        return !isBlackArea( chessBoard,to );
      }
      for( int i=to.y+1;i<from.y;i++ ) {
        if( !isFreeArea( chessBoard,new Point( from.x,i ) ) ) {
          return false;
        }
      }
      if( white ) {
        return !isWhiteArea( chessBoard,to );
      }
      return !isBlackArea( chessBoard,to );
    }					//from.y==to.y
    if( from.x<to.x ) {
      for( int i=from.x+1;i<to.x;i++ ) {
        if( !isFreeArea( chessBoard,new Point( i,from.y ) ) ) {
          return false;
        }
      }
      if( white ) {
        return !isWhiteArea( chessBoard,to );
      }
      return !isBlackArea( chessBoard,to );
    }
    for( int i=to.x+1;i<from.x;i++ ) {
      if( !isFreeArea( chessBoard,new Point( i,from.y ) ) ) {
        return false;
      }
    }
    if( white ) {
      return !isWhiteArea( chessBoard,to );
    }
    return !isBlackArea( chessBoard,to );
  }

  private boolean isKnightMoveable( Point from,Point to ) {
    boolean white=isWhiteArea( chessBoard,from );
    if( ( Math.abs( from.x-to.x )+Math.abs( from.y-to.y ) )!=3 ) {
      return false;
    }
    if( (from.x==to.x)||(from.y==to.y) ) {
      return false;
    }
    if( white ) {
      return !isWhiteArea( chessBoard,to );
    }else{
      return !isBlackArea( chessBoard,to );
    } 
  }

  private boolean isBishopMoveable( Point from,Point to ) {
    boolean white=isWhiteArea( chessBoard,from );
    if( Math.abs( from.x-to.x )!=Math.abs( from.y-to.y ) ) {
      return false;
    }
    if( from.x==to.x ) {
      return false;
    }
    if( from.x<to.x ) {
      if( from.y<to.y ) {
        for( int i=1;i<to.x-from.x;i++ ) {
          if( !isFreeArea( chessBoard,new Point( from.x+i,from.y+i ) ) ) {
            return false;
          }
        }
        if( white ) {
          return !isWhiteArea( chessBoard,to );
        }
        return !isBlackArea( chessBoard,to );
      }					//from.x<to.x && from.y>to.y
      for( int i=1;i<to.x-from.x;i++ ) {
        if( !isFreeArea( chessBoard,new Point( from.x+i,from.y-i ) ) ) {
          return false;
        }
      }
      if( white ) {
        return !isWhiteArea( chessBoard,to );
      }
      return !isBlackArea( chessBoard,to );
    }							//from.x>to.x && from.y<to.y
    if( from.y<to.y ) {
      for( int i=1;i<from.x-to.x;i++ ) {
        if( !isFreeArea( chessBoard,new Point( from.x-i,from.y+i ) ) ) {
          return false;
        }
      }
      if( white ) {
        return !isWhiteArea( chessBoard,to );
      }
      return !isBlackArea( chessBoard,to );
    }							//from.x>to.x && from.y>to.y
    for( int i=1;i<from.x-to.x;i++ ) {
      if( !isFreeArea( chessBoard,new Point( from.x-i,from.y-i ) ) ) {
        return false;
      }
    }
    if( white ) {
      return !isWhiteArea( chessBoard,to );
    }
    return !isBlackArea( chessBoard,to );
  }

  private boolean isQueenMoveable( Point from,Point to ) {
    return isBishopMoveable( from,to )||isRockMoveable( from,to );
  }

  private boolean isKingMoveable( Point from,Point to ) {
    boolean white=isWhiteArea( chessBoard,from );
    if( white ) {
      if( (from.x==4)&&(from.y==0)&&(to.y==0) ) {
        if( to.x==6 ) {
          return isFreeArea( chessBoard,new Point( 5,0 ) )
          &&isFreeArea( chessBoard,new Point( 6,0 ) )
          &&!isBound( from,new Point( 5,0 ) )
          &&whiteShortRochade;
        }
        if( to.x==2) {
          return isFreeArea( chessBoard,new Point( 1,0 ) )
                 &&isFreeArea( chessBoard,new Point( 2,0 ) )
                 &&isFreeArea( chessBoard,new Point( 3,0 ) )
                 &&!isBound( from,new Point ( 3,0 ) )
                 &&whiteLongRochade;
        }
      }
      return !isWhiteArea( chessBoard,to )&&( Math.abs( from.x-to.x )<=1 )
             &&( Math.abs( from.y-to.y )<=1 );
    }else{
      if( (from.x==4)&&(from.y==7)&&(to.y==7) ) {
        if( to.x==6 ) {
          return isFreeArea( chessBoard,new Point( 5,7 ) )
                 &&isFreeArea( chessBoard,new Point( 6,7 ) )
                 &&!isBound( from,new Point ( 5,7 ) )
                 &&blackShortRochade;
        }
        if( to.x==2) {
          return isFreeArea( chessBoard,new Point( 1,7 ) )
                 &&isFreeArea( chessBoard,new Point( 2,7 ) )
                 &&isFreeArea( chessBoard,new Point( 3,7 ) )
                 &&!isBound( from,new Point ( 3,7 ) )
                 &&blackLongRochade;
        }
      }
      return !isBlackArea( chessBoard,to )&&( Math.abs( from.x-to.x )<=1 )
             &&( Math.abs( from.y-to.y )<=1 );
    }
  }

  private boolean isBound( Point from,Point to ) {
    for( int i=0;i<8;i++ ) {
      System.arraycopy( chessBoard[i],0,futureBoard[i],0,8 );
    }
    moveFigure( futureBoard,from,to );
    if( isWhiteArea( chessBoard,from ) ) {
      return isWhiteCheck( futureBoard );
    }else{
      return isBlackCheck( futureBoard );
    }
  }

  private boolean isWhiteCheck( int[][] board ) {			//Weiss steht im Schach
    Point kingPos=new Point( -1,-1 );
    for( int y=0;y<8;y++ ) {
      for( int x=0;x<8;x++ ) {
        if( getFigure( board,new Point( x,y ) )==WEISS+KÖNIG ) {
          kingPos=new Point( x,y );
          break;
        }
      }
      if( kingPos.x!=-1 ) {
        break;
      }
    }
    return isRockQueenCheck( board,kingPos )||isBishopQueenCheck( board,kingPos )
         ||isKnightCheck( board,kingPos )||isPawnCheck( board,kingPos )
         ||isKingCheck( board,kingPos );
  }

  private boolean isBlackCheck( int[][] board ) {			//Schwarz steht im Schach
    Point kingPos=new Point( -1,-1 );
    for( int y=7;y>-1;y-- ) {
      for( int x=7;x>-1;x-- ) {
        if( getFigure( board,new Point( x,y ) )==SCHWARZ+KÖNIG ) {
          kingPos=new Point( x,y );
          break;
        }
      }
      if( kingPos.x!=-1 ) {
        break;
      }
    }
    return isRockQueenCheck( board,kingPos )||isBishopQueenCheck( board,kingPos )
         ||isKnightCheck( board,kingPos )||isPawnCheck( board,kingPos )
         ||isKingCheck( board,kingPos );
  }

  private boolean isPawnCheck( int[][] board,Point kingPos ) {		//Schach durch Bauer?
    if( isWhiteArea( board,kingPos ) ){
      Point[] possibleBlackPawnPlaces=new Point[2];
      possibleBlackPawnPlaces[0]=new Point( kingPos );
      possibleBlackPawnPlaces[0].translate( -1,1 );
      possibleBlackPawnPlaces[1]=new Point( kingPos );
      possibleBlackPawnPlaces[1].translate(  1,1 );
      for( int i=0;i<2;i++ ) {
        int x=(int)possibleBlackPawnPlaces[i].getX();
        int y=(int)possibleBlackPawnPlaces[i].getY();
        if( x>=0 && x<=7 && y<=7 && getFigure( board,possibleBlackPawnPlaces[i] )==SCHWARZ+BAUER ) {
          return true;
        }
      }
      return false;
    }else{
      Point[] possibleWhitePawnPlaces=new Point[2];
      possibleWhitePawnPlaces[0]=new Point( kingPos );
      possibleWhitePawnPlaces[0].translate( -1,-1 );
      possibleWhitePawnPlaces[1]=new Point( kingPos );
      possibleWhitePawnPlaces[1].translate(  1,-1 );
      for( int i=0;i<2;i++ ) {
        int x=(int)possibleWhitePawnPlaces[i].getX();
        int y=(int)possibleWhitePawnPlaces[i].getY();
        if( x>=0 && x<=7 && y>=0 && getFigure( board,possibleWhitePawnPlaces[i] )==WEISS+BAUER ) {
          return true;
        }
      }
      return false;
    }
  }

  private boolean isRockQueenCheck( int[][] board,Point kingPos ) {
    boolean whiteKing=isWhiteArea( board,kingPos );
    int figure;
    for( int i=1;i<8;i++ ) {						//1.Fall
      if( kingPos.y+i>7 ) {
        break;
      }
      if( !isFreeArea( board,new Point( kingPos.x,kingPos.y+i ) ) ) {
        figure=getFigure( board,new Point( kingPos.x,kingPos.y+i ) );
        if( ( whiteKing&&( (figure==SCHWARZ+DAME)||(figure==SCHWARZ+TURM) ) )
          ||(!whiteKing&&( (figure==WEISS+DAME)  ||  (figure==WEISS+TURM) ) )
        ) {
          return true;
        }
        break;
      }
    }
    for( int i=1;i<8;i++ ) {						//2.Fall
      if( kingPos.y-i<0 ) {
        break;
      }
      if( !isFreeArea( board,new Point( kingPos.x,kingPos.y-i ) ) ) {
        figure=getFigure( board,new Point( kingPos.x,kingPos.y-i ) );
        if( ( whiteKing&&( (figure==SCHWARZ+DAME)||(figure==SCHWARZ+TURM) ) )
          ||(!whiteKing&&( (figure==WEISS+DAME)  ||  (figure==WEISS+TURM) ) )
        ) {
          return true;
        }
        break;
      }
    }									//3.Fall
    for( int i=1;i<8;i++ ) {
      if( kingPos.x-i<0 ) {
        break;
      }
      if( !isFreeArea( board,new Point( kingPos.x-i,kingPos.y ) ) ) {
        figure=getFigure( board,new Point( kingPos.x-i,kingPos.y ) );
        if( ( whiteKing&&( (figure==SCHWARZ+DAME)||(figure==SCHWARZ+TURM) ) )
          ||(!whiteKing&&( (figure==WEISS+DAME)  ||  (figure==WEISS+TURM) ) )
        ) {
          return true;
        }
        break;
      }
    }									//4.Fall
    for( int i=1;i<8;i++ ) {
      if( kingPos.x+i>7 ) {
        break;
      }
      if( !isFreeArea( board,new Point( kingPos.x+i,kingPos.y ) ) ) {
        figure=getFigure( board,new Point( kingPos.x+i,kingPos.y ) );
        if( ( whiteKing&&( (figure==SCHWARZ+DAME)||(figure==SCHWARZ+TURM) ) )
          ||(!whiteKing&&( (figure==WEISS+DAME)  ||  (figure==WEISS+TURM) ) )
        ) {
          return true;
        }
        break;
      }
    }
    return false;
  }

  private boolean isKnightCheck( int[][] board,Point kingPos ) {
    int pferd;
    Point[] knightPos=new Point[8];
    knightPos[0]=new Point( kingPos.x-2,kingPos.y+1 );
    knightPos[1]=new Point( kingPos.x-2,kingPos.y-1 );
    knightPos[2]=new Point( kingPos.x+2,kingPos.y+1 );
    knightPos[3]=new Point( kingPos.x+2,kingPos.y-1 );
    knightPos[4]=new Point( kingPos.x-1,kingPos.y+2 );
    knightPos[5]=new Point( kingPos.x+1,kingPos.y+2 );
    knightPos[6]=new Point( kingPos.x-1,kingPos.y-2 );
    knightPos[7]=new Point( kingPos.x+1,kingPos.y-2 );
    for( int i=0;i<8;i++ ) {
      if( (knightPos[i].x<8)
        &&(knightPos[i].x>-1)
        &&(knightPos[i].y<8)
        &&(knightPos[i].y>-1)
      ) {
        if( ( pferd=getFigure( board,new Point( knightPos[i].x,knightPos[i].y ) ) )%10==PFERD ) {
          if( isWhiteArea( board,kingPos ) ) {
            if( pferd==SCHWARZ+PFERD ) {
              return true;
            }
          }else{
            if( pferd==WEISS+PFERD ) {
              return true;
            }
          }
        }
      }
    }
    return false;
  }

  private boolean isBishopQueenCheck( int[][] board,Point kingPos ) {
    boolean whiteKing=isWhiteArea( board,kingPos );
    int figure;
    for( int i=1;i<8;i++ ) {						//1.Fall
      if( (kingPos.x+i>7)||(kingPos.y+i>7) ) {
        break;
      }
      if( !isFreeArea( board,new Point( kingPos.x+i,kingPos.y+i ) ) ) {
        figure=getFigure( board,new Point( kingPos.x+i,kingPos.y+i ) );
        if( ( whiteKing&&( (figure==SCHWARZ+DAME)||(figure==SCHWARZ+LÄUFER) ) )
          ||(!whiteKing&&( (figure==WEISS+DAME)  ||  (figure==WEISS+LÄUFER) ) )
        ) {
          return true;
        }
        break;
      }
    }
    for( int i=1;i<8;i++ ) {						//2.Fall
      if( (kingPos.x+i>7)||(kingPos.y-i<0) ) {
        break;
      }
      if( !isFreeArea( board,new Point( kingPos.x+i,kingPos.y-i ) ) ) {
        figure=getFigure( board,new Point( kingPos.x+i,kingPos.y-i ) );
        if( ( whiteKing&&( (figure==SCHWARZ+DAME)||(figure==SCHWARZ+LÄUFER) ) )
          ||(!whiteKing&&( (figure==WEISS+DAME)  ||  (figure==WEISS+LÄUFER) ) )
        ) {
          return true;
        }
        break;
      }
    }									//3.Fall
    for( int i=1;i<8;i++ ) {
      if( (kingPos.x-i<0)||(kingPos.y+i>7) ) {
        break;
      }
      if( !isFreeArea( board,new Point( kingPos.x-i,kingPos.y+i ) ) ) {
        figure=getFigure( board,new Point( kingPos.x-i,kingPos.y+i ) );
        if( ( whiteKing&&( (figure==SCHWARZ+DAME)||(figure==SCHWARZ+LÄUFER) ) )
          ||(!whiteKing&&( (figure==WEISS+DAME)  ||  (figure==WEISS+LÄUFER) ) )
        ) {
          return true;
        }
        break;
      }
    }									//4.Fall
    for( int i=1;i<8;i++ ) {
      if( (kingPos.x-i<0)||(kingPos.y-i<0) ) {
        break;
      }
      if( !isFreeArea( board,new Point( kingPos.x-i,kingPos.y-i ) ) ) {
        figure=getFigure( board,new Point( kingPos.x-i,kingPos.y-i ) );
        if( ( whiteKing&&( (figure==SCHWARZ+DAME)||(figure==SCHWARZ+LÄUFER) ) )
          ||(!whiteKing&&( (figure==WEISS+DAME)  ||  (figure==WEISS+LÄUFER) ) )
        ) {
          return true;
        }
        break;
      }
    }
    return false;
  }

  private boolean isKingCheck( int[][] board,Point kingPos ) {		//Schach durch KÖNIG
    if( kingPos.x+1<8 ) {
      for( int i=-1;i<2;i++ ) {
        if( (kingPos.y+i<0)||(kingPos.y+i>7) ) {
          continue;
        }
        if(  getFigure( board,new Point( kingPos.x+1,kingPos.y+i ) )%10==KÖNIG ) {
          return true;
        }
      }
    }
    if( kingPos.x-1>-1 ) {
      for( int i=-1;i<2;i++ ) {
        if( (kingPos.y+i<0)||(kingPos.y+i>7) ) {
          continue;
        }
        if(  getFigure( board,new Point( kingPos.x-1,kingPos.y+i ) )%10==KÖNIG ) {
          return true;
        }
      }
    }
    if( kingPos.y+1<8 ) {
      if(  getFigure( board,new Point( kingPos.x,kingPos.y+1 ) )%10==KÖNIG ) {
        return true;
      }
    }
    if( kingPos.y-1>-1 ) {
      if(  getFigure( board,new Point( kingPos.x,kingPos.y-1 ) )%10==KÖNIG ) {
        return true;
      }
    }
    return false;
  }

  private boolean isPatt( int color ) {				//kann sich color noch bewegen
    boolean patt=true;
    Point area=new Point( -1,-1 );
    for( int y=0;y<8;y++ ) {
      for( int x=0;x<8;x++ ) {
        area=new Point( x,y );
        if( ( color==WEISS   && isWhiteArea( chessBoard,area ) && figureIsNotStunned( area) )
          ||( color==SCHWARZ && isBlackArea( chessBoard,area ) && figureIsNotStunned( area) )
        ) {
          patt=false;
          break;
        }
      }
      if( !patt ) {
        break;
      }
    }
    return patt;
  }

  private boolean figureIsNotStunned( Point area ) {
    switch( getFigure( chessBoard,area )%10 ) {
      case BAUER :return pawnIsNotStunned(   area );
      case TURM  :return rockIsNotStunned(   area );
      case PFERD :return knightIsNotStunned( area );
      case LÄUFER:return bishopIsNotStunned( area );
      case DAME  :return queenIsNotStunned(  area );
      case KÖNIG :return kingIsNotStunned(   area );
      default:return false;
    }
  }

  private boolean pawnIsNotStunned( Point from ) {
    if( isWhiteArea( chessBoard,from ) ) {
      if( (from.x>0)&&(from.x<7) ) {
        return isMoveable( from,new Point( from.x  ,from.y+1 ) )
             ||isMoveable( from,new Point( from.x+1,from.y+1 ) )
             ||isMoveable( from,new Point( from.x-1,from.y+1 ) )
             ||isMoveable( from,new Point( from.x  ,from.y+2 ) ) ;   
      }else if( from.x==0 ) {
        return isMoveable( from,new Point( from.x  ,from.y+1 ) )
             ||isMoveable( from,new Point( from.x+1,from.y+1 ) )
             ||isMoveable( from,new Point( from.x  ,from.y+2 ) );
      }else{
        return isMoveable( from,new Point( from.x  ,from.y+1 ) )
             ||isMoveable( from,new Point( from.x-1,from.y+1 ) )
             ||isMoveable( from,new Point( from.x  ,from.y+2 ) );
      }
    }else{
      if( (from.x>0)&&(from.x<7) ) {
        return isMoveable( from,new Point( from.x  ,from.y-1 ) )
             ||isMoveable( from,new Point( from.x+1,from.y-1 ) )
             ||isMoveable( from,new Point( from.x-1,from.y-1 ) )
             ||isMoveable( from,new Point( from.x  ,from.y-2 ) ) ;   
      }else if( from.x==0 ) {
        return isMoveable( from,new Point( from.x  ,from.y-1 ) )
             ||isMoveable( from,new Point( from.x+1,from.y-1 ) )
             ||isMoveable( from,new Point( from.x  ,from.y-2 ) );
      }else{
        return isMoveable( from,new Point( from.x  ,from.y-1 ) )
             ||isMoveable( from,new Point( from.x-1,from.y-1 ) )
             ||isMoveable( from,new Point( from.x  ,from.y-2 ) );
      }
    }
  }

  private boolean rockIsNotStunned( Point from ) {
    for( int i=1;i<8;i++ ) {
      if( from.x+i>7 ) {
        break;
      }
      if( isMoveable( from,new Point( from.x+i,from.y ) ) ) {
        return true;
      }
    }
    for( int i=1;i<8;i++ ) {
      if( from.x-i<0 ) {
        break;
      }
      if( isMoveable( from,new Point( from.x-i,from.y ) ) ) {
        return true;
      }
    }
    for( int i=1;i<8;i++ ) {
      if( from.y+i>7 ) {
        break;
      }
      if( isMoveable( from,new Point( from.x,from.y+i ) ) ) {
        return true;
      }
    }
    for( int i=1;i<8;i++ ) {
      if( from.y-i<0 ) {
        break;
      }
      if( isMoveable( from,new Point( from.x,from.y-i ) ) ) {
        return true;
      }
    }
    return false;
  }

  private boolean knightIsNotStunned( Point from ) {
    Point[] knightPos=new Point[8];
    knightPos[0]=new Point( from.x+2,from.y-1 );
    knightPos[1]=new Point( from.x+2,from.y+1 );
    knightPos[2]=new Point( from.x-2,from.y-1 );
    knightPos[3]=new Point( from.x-2,from.y+1 );
    knightPos[4]=new Point( from.x+1,from.y-2 );
    knightPos[5]=new Point( from.x-1,from.y-2 );
    knightPos[6]=new Point( from.x+1,from.y+2 );
    knightPos[7]=new Point( from.x-1,from.y+2 );
    for( int i=0;i<8;i++ ) {
      if( (knightPos[i].x>7)||(knightPos[i].x<0)||(knightPos[i].y>7)||(knightPos[i].y<0) ) {
        continue;
      }
      if( isMoveable( from,knightPos[i] ) ) {
        return true;
      }
    }
    return false;
  }

  private boolean bishopIsNotStunned( Point from ) {
    for( int i=1;i<8;i++ ) {
      if( (from.x+i>7)||(from.y+i>7) ) {
        break;
      }
      if( isMoveable( from,new Point( from.x+i,from.y+i ) ) ) {
        return true;
      }
    }
    for( int i=1;i<8;i++ ) {
      if( (from.x+i>7)||(from.y-i<0) ) {
        break;
      }
      if( isMoveable( from,new Point( from.x+i,from.y-i ) ) ) {
        return true;
      }
    }
    for( int i=1;i<8;i++ ) {
      if( (from.x-i<0)||(from.y+i>7) ) {
        break;
      }
      if( isMoveable( from,new Point( from.x-i,from.y+i ) ) ) {
        return true;
      }
    }
    for( int i=1;i<8;i++ ) {
      if( (from.x-i<0)||(from.y-i<0) ) {
        break;
      }
      if( isMoveable( from,new Point( from.x-i,from.y-i ) ) ) {
        return true;
      }
    }
    return false;
  }

  private boolean queenIsNotStunned( Point from ) {
    return bishopIsNotStunned( from )
           ||rockIsNotStunned( from );
  }

  private boolean kingIsNotStunned( Point from ) {
    for( int i=-1;i<2;i++ ) {
      if( (from.x+i<0)||(from.x+i>7) ) {
        continue;
      }
      if( from.y+1<8 ) {
        if( isMoveable( from,new Point( from.x+i,from.y+1 ) ) ) {
          return true;
        }
      }
      if( from.y-1>-1 ) {
        if( isMoveable( from,new Point( from.x+i,from.y-1 ) ) ) {
          return true;
        }
      }
    }
    if( from.x+1<8 ) {
      if( isMoveable( from,new Point( from.x+1,from.y ) ) ) {
        return true;
      }
    }
    if( from.x-1>-1 ) {
      if( isMoveable( from,new Point( from.x-1,from.y ) ) ) {
        return true;
      }
    }
    return false;
  }

  private void memoBoard() {						//Patt wegen dreimaliger Zugwiederhohlung,Undo
    if( whiteTurn ) {							//Stellungen werden gespeichert
      if( savedNumberWhite==savedBoardsWhite.length ) {
        String[] newSavedBoardsW=new String[ savedBoardsWhite.length+30 ];					
        System.arraycopy( savedBoardsWhite,0,newSavedBoardsW,0,savedBoardsWhite.length );
        savedBoardsWhite=newSavedBoardsW;
      }
      savedBoardsWhite[ savedNumberWhite ]=boardToString( chessBoard );
      int occureCounter=1;
      for( int i=savedNumberWhite-1;i>=0;i-- ) {
        if( savedBoardsWhite[i].length() != savedBoardsWhite[ savedNumberWhite ].length() ) {
          break;
        }
        if( savedBoardsWhite[i].equals( savedBoardsWhite[ savedNumberWhite ] ) ) {
          occureCounter++;
          if( occureCounter==3 ) {
            stopGameFromWithin( ChessFrame.REMIS_3TIMES_SAME_POSITION );
          }
        }
      }
      savedNumberWhite++;
    }else{											//Schwarz ist dran
      if( savedNumberBlack==savedBoardsBlack.length ) {
        String[] newSavedBoardsB=new String[ savedBoardsBlack.length+30 ];					
        System.arraycopy( savedBoardsBlack,0,newSavedBoardsB,0,savedBoardsBlack.length );
        savedBoardsBlack=newSavedBoardsB;
      }
      savedBoardsBlack[ savedNumberBlack ]=boardToString( chessBoard );
      int occureCounter=1;
      for( int i=savedNumberBlack-1;i>=0;i-- ) {
        if( savedBoardsBlack[i].length() != savedBoardsBlack[ savedNumberBlack ].length() ) {
          break;
        }
        if( savedBoardsBlack[i].equals( savedBoardsBlack[ savedNumberBlack ] ) ) {
          occureCounter++;
          if( occureCounter==3 ) {
            stopGameFromWithin( ChessFrame.REMIS_3TIMES_SAME_POSITION );
          }
        }
      }
      savedNumberBlack++;
    }
  }

  private String boardToString( int[][] board ) {
    StringBuffer codBoard=new StringBuffer( 128 );
    int figure;
    for( int x=0;x<8;x++ ) {
      for( int y=0;y<8;y++ ) {
        if( (figure=getFigure( board,new Point( x,y ) ))!=0 ) {
          codBoard.append( figure );
          codBoard.append( x );
          codBoard.append( y );
        }
      }
    }
    return codBoard.toString();
  }

  private void stringToBoard( String codBoard ) {
      for( int x=0;x<8;x++ ) {
        for( int y=0;y<8;y++ ) {
          clearArea( chessBoard,new Point( x,y ) );
        }
      }
    int length=codBoard.length()/4;
    for( int i=0;i<length;i++ ) {
      int j  =new Integer( codBoard.substring( i*4,i*4+2 ) ).intValue();
      chessBoard[ (int)codBoard.charAt(i*4+3)-48 ][ (int)codBoard.charAt(i*4+2)-48 ]=j;
    }
  }

  private void resetMemoBoard() {
    savedNumberWhite=0;
    savedNumberBlack=0;
  }

  public void undo() {
    switchTurn( false );
    if( whiteTurn ) {
      stringToBoard( savedBoardsWhite[ --savedNumberWhite ] );
    }else{
      stringToBoard( savedBoardsBlack[ --savedNumberBlack ] );
    }
    repaint();
  }

  private void lineUpFigures() {
    for( int i=0;i<8;i++ ) {
      chessBoard[1][i]=WEISS+BAUER;
    }
    chessBoard[0][0]=WEISS+TURM;
    chessBoard[0][7]=WEISS+TURM;
    chessBoard[0][1]=WEISS+PFERD;
    chessBoard[0][6]=WEISS+PFERD;
    chessBoard[0][2]=WEISS+LÄUFER;
    chessBoard[0][5]=WEISS+LÄUFER;
    chessBoard[0][3]=WEISS+DAME;
    chessBoard[0][4]=WEISS+KÖNIG;

    for( int i=0;i<8;i++ ) {
      chessBoard[6][i]=SCHWARZ+BAUER;
    }
    chessBoard[7][0]=SCHWARZ+TURM;
    chessBoard[7][7]=SCHWARZ+TURM;
    chessBoard[7][1]=SCHWARZ+PFERD;
    chessBoard[7][6]=SCHWARZ+PFERD;
    chessBoard[7][2]=SCHWARZ+LÄUFER;
    chessBoard[7][5]=SCHWARZ+LÄUFER;
    chessBoard[7][3]=SCHWARZ+DAME;
    chessBoard[7][4]=SCHWARZ+KÖNIG;

    for( int y=2;y<6;y++ ) {
      for( int x=0;x<8;x++ ) {
        chessBoard[y][x]=0;
      }
    }
    repaint();
  }
}
