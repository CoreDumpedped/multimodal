/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fusionmultimodal;

import com.sun.glass.ui.EventLoop;
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

    private enum VocalStat {

        init, wait
    }

    private enum State {

        learn, run
    };
    private State state = State.run;
    private MouseState etat;
    private VocalStat vocalStat;
    private Stroke s;
    private Template templateEnAttente;
    private Point dernierPoint;

    Recognizer recognizer;

    public IvyStroke() throws IvyException {
        vocalStat = VocalStat.init;
        recognizer = new Recognizer();
        etat = MouseState.release;
        listPoints = new ArrayList<>();
        dernierPoint=new Point(0, 0);
        bus = new Ivy("IvyStroke", "IvyStroke Ready", null);

        bus.bindMsg("^Palette:MouseDragged x=(.*) y=(.*)", new IvyMessageListener() {
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
                vocalStat = VocalStat.init;
            }
        });

        bus.bindMsg("^Palette:MouseClicked x=(.*) y=(.*)", new IvyMessageListener() {
            public void receive(IvyClient client, String[] args) {
                System.err.println("recup=" + dernierPoint.x );
                dernierPoint.x=Integer.parseInt(args[0]);
                dernierPoint.y=Integer.parseInt(args[1]);
            }
        });

        bus.bindMsg("^Palette:MouseReleased x=(.*) y=(.*)", new IvyMessageListener() {
            public void receive(IvyClient client, String[] args) {
                etat = MouseState.release;
                if (state == State.learn) {
                    recognizer.addTemplates(new Template("oval", s));
                    recognizer.saveTemplates();
                } else if (s != null) {
                    System.out.println("START Verif Stroke ");
                    s.normalize();
                    recognizer.setStrokeCourant(s);
                    Template t = recognizer.verifStroke();
                    if (t != null) {
                        System.out.println("Cette figure ressemblerai a s'y méprendre (et c'est peu de le dire !) à un :" + t.getNom());
                        //  dessin(t);
                        templateEnAttente = t;
                        vocalStat = VocalStat.wait;
                        s = null;
                    }
                }
            }
        });

        bus.bindMsg("^sra5 Text=(.*) Confidence", new IvyMessageListener() { //vocal
            public void receive(IvyClient client, String[] args) {
                System.out.println("je suis passer par ici:" + args[0]);
                if (args[0].equals("ici") || args[0].equals("la") || args[0].equals("... ici") || args[0].equals("... la")) {
                    switch (vocalStat) {
                        case wait:
                            dessin(templateEnAttente);
                            vocalStat = VocalStat.init;
                            break;
                        case init:
                            break;
                    }

                }
            }
        });

        bus.start(null);
    }

    public void dessin(Template t) {
        System.out.println("dessine moi un " + t.getNom());
        try {
            switch (t.getNom()) {
                case "carrer":
                    bus.sendMsg("Palette:CreerRectangle x="+dernierPoint.x +" y="+dernierPoint.y);
                    break;
                case "oval":
                    bus.sendMsg("Palette:CreerEllipse x="+dernierPoint.x +" y="+dernierPoint.y);
            }

            System.out.println("all is ok");
        } catch (IvyException ie) {
            System.out.println("can't send my message !");
        }

    }

    public void setLearnState() {
        state = State.learn;
        System.out.println("Etat LEARN activé !");
    }

    private void envoieStroke() {
        s.normalize();
    }

}
