import java.util.*;
import java.awt.*;
import java.io.File;
import java.net.URL;

public class Images{
  private final static HashMap images=new HashMap();
  private final static String[][] strings={{"S_BAUER"   , "Schwarz_Bauer.gif"},
                                           {"S_PFERD"   , "Schwarz_Pferd.gif"},
                                           {"S_LÄUFER"  , "Schwarz_Läufer.gif"},
                                           {"S_TURM"    , "Schwarz_Turm.gif"},
                                           {"S_DAME"    , "Schwarz_Dame.gif"},
                                           {"S_KÖNIG"   , "Schwarz_König.gif"},
                                           {"W_BAUER"   , "Weiss_Bauer.gif"},
                                           {"W_PFERD"   , "Weiss_Pferd.gif"},
                                           {"W_LÄUFER"  , "Weiss_Läufer.gif"},
                                           {"W_TURM"    , "Weiss_Turm.gif"},
                                           {"W_DAME"    , "Weiss_Dame.gif"},
                                           {"W_KÖNIG"   , "Weiss_König.gif"},
                                           {"W_S_KÖNIG" , "Weiss_Schwarz_König.gif"},
                                           {"ICON"      , "Icon.gif"}};


  public static void init( Component component ){
    Toolkit toolkit=component.getToolkit();
    MediaTracker mt=new MediaTracker( component );
    final String imgPath = "img"+File.separator;
    for( int i=0;i<strings.length;i++ ) {
      URL url=Images.class.getResource( imgPath+strings[i][1] );
      Image img=toolkit.createImage( url );
      images.put( strings[i][0],img );
      mt.addImage( img,i );
    }
    try{
      mt.waitForAll();
    }catch( InterruptedException e ) {}
  }

  public static Image get( String imageName ) {
    return (Image)images.get( imageName );
  }
}
