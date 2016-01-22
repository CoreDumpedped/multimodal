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
                System.out.println("Point ajouté : x=" + x + " :y=" + y);
                s.addPoint(p);
            }
        });

        bus.bindMsg("^Palette:MouseReleased x=(.*) y=(.*)", new IvyMessageListener() {
            public void receive(IvyClient client, String[] args) {
                etat = MouseState.release;
                if (state == State.save) {
                       recognizer.addTemplates(new Template("rectangle", s));
                       recognizer.saveTemplates();
                } else {
                   
                }
                    
                
            }
        });

        bus.start(null);
    }

    private void envoieStroke() {
        s.normalize();
    }

}
