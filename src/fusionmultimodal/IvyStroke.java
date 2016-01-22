/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fusionmultimodal;

import fr.dgac.ivy.Ivy;
import fr.dgac.ivy.IvyClient;
import fr.dgac.ivy.IvyException;
import fr.dgac.ivy.IvyMessageListener;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.input.DragEvent;

/**
 *
 * @author astierre
 */
public class IvyStroke {



    public Ivy bus;
    public List<Point2D.Double> listPoints;

    private enum MouseState {
        drag, release
    };

    private enum State {
        save, run
    };
    private State state = State.save;
    private MouseState etat;

    private Stroke s;

    Recognizer recognizer;
    public IvyStroke() throws IvyException {
        recognizer=new Recognizer();
        etat = MouseState.release;
        listPoints = new ArrayList<>();
        
        bus = new Ivy("IvyStroke", "IvyStroke Ready", null);
       
        bus.bindMsg("^Palette:mouseDragged x=(.*) y=(.*)", new IvyMessageListener() {
            public void receive(IvyClient client, String[] args) {
                if (etat == MouseState.release) {
                    etat = MouseState.drag;
                    s = new fusionmultimodal.Stroke();
                }
                double x = Double.parseDouble(args[0]);
                double y = Double.parseDouble(args[1]);
                Point2D.Double p = new Point2D.Double(x, y);
                // listPoints.add(new Point2D.Double(x, y));
                s.addPoint(p);
            }
        });

        bus.bindMsg("^Palette:MouseReleased x=(.*) y=(.*)", new IvyMessageListener() {
            public void receive(IvyClient client, String[] args) {
                etat = MouseState.release;
                if (state == State.save) {
                       recognizer.addTemplates(new Template("carre", s));
                       recognizer.saveTemplates();
                } else {
                    System.out.println("START Verif Stroke ");
                   s.normalize();
                   recognizer.setStrokeCourant(s);
                   Template t = recognizer.verifStroke();
                   if (t != null) {
                       System.out.println("Cette figure ressemblerai a s'y méprendre (et c'est peu de le dire !) à un :" + t.getNom());
                       dessin(t);
                   }
                }                
            }
        });

        bus.start(null);
    }

    
    
    public void dessin(Template t){   
        System.out.println("dessine moi un " + t.getNom());
     try {
        switch (t.getNom()) {
            case "carre" : 
                bus.sendMsg("Palette:CreerRectangle x=10 y=20");
                break;
            case "oval" : 
                bus.sendMsg("Palette:CreerEllipse x=10 y=20");
        }
      
       System.out.println("all is ok");
    } catch (IvyException ie) {
      System.out.println("can't send my message !");
    }
        
    }
    
      public void setRunState() {
        state = State.run;
        System.out.println("Etat RUN activé !");
    }
    
    private void envoieStroke() {
        s.normalize();
    }

}
