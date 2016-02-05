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

        init, wait, delete
    }

    private enum State {

        learn, run
    };

    
    private enum SelectionShape {

        ALL, RECTANGLE, ELLIPSE
    }
    private State state = State.run;
    private MouseState etat;
    private VocalStat vocalStat;
    private Stroke s;
    private Template templateEnAttente;
    private Point dernierPoint;
    private List<String> selection;
    private SelectionShape ss;
    private boolean deleteState=false;
    Recognizer recognizer;

    public IvyStroke() throws IvyException {
        vocalStat = VocalStat.init;
        recognizer = new Recognizer();
        etat = MouseState.release;
        listPoints = new ArrayList<>();
        dernierPoint = new Point(0, 0);
        selection = new ArrayList<>();
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

                dernierPoint.x = Integer.parseInt(args[0]);
                dernierPoint.y = Integer.parseInt(args[1]);
                try {
                    testerObjet();
                } catch (IvyException ex) {
                    Logger.getLogger(IvyStroke.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

        });

        bus.bindMsg("^Palette:MouseReleased x=(.*) y=(.*)", new IvyMessageListener() {
            public void receive(IvyClient client, String[] args) {
                etat = MouseState.release;
                if (state == State.learn) {
                    recognizer.addTemplates(new Template("oval", s));
                    recognizer.saveTemplates();
                } else if (s != null) {
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

        /*bus.bindMsg("^sra5 Text=(.*) Confidence", new IvyMessageListener() { //vocal
         public void receive(IvyClient client, String[] args) {
         System.out.println("je suis passer par ici:" + args[0]);
         if (args[0].equals("ici") || args[0].equals("la") || args[0].equals("... ici") || args[0].equals("... la")) {
         switch (vocalStat) {
         case wait:
         action(templateEnAttente);
         vocalStat = VocalStat.init;
         break;
         case init:
         break;
         }

         }
         }
         });*/
        bus.bindMsg("^sra5 Parsed=Action:position", new IvyMessageListener() { //vocal
            public void receive(IvyClient client, String[] args) {
                switch (vocalStat) {
                    case wait:
                        action(templateEnAttente);
                        vocalStat = VocalStat.init;
                        break;
                    case init:
                        break;
                }

            }
        });

        bus.bindMsg("^sra5 Parsed=Action:selection=(.*) Confidence", new IvyMessageListener() { //vocal
            public void receive(IvyClient client, String[] args) {
                System.out.println(args[0]);
                try {
                    switch (args[0]) {
                        case "ce rectangle":
                            ss = SelectionShape.RECTANGLE;
                            supprimer(SelectionShape.RECTANGLE);
                            break;
                        case "cette ellipse":
                            ss = SelectionShape.ELLIPSE;
                            supprimer(SelectionShape.ELLIPSE);
                            break;
                        case "cet objet":
                            ss = SelectionShape.ALL;
                            supprimer(SelectionShape.ALL);
                    }
                } catch (IvyException ie) {
                }

            }

        });

        bus.bindMsg("Palette:ResultatTesterPoint x=(.*) y=(.*) nom=(.*)", new IvyMessageListener() { //regarder les objet sous un point

            public void receive(IvyClient client, String[] args) {
                selection.add(args[2]);
                System.err.println("selection=" + args[2]);
            }
        }
        );

        bus.start(null);
    }

    private void supprimer(SelectionShape selectionShape) throws IvyException {
        System.out.println("je supprime ->" + selectionShape.toString());
        if (!selection.isEmpty() && deleteState==true) {
            switch (selectionShape) {
                case ALL:
                    bus.sendMsg("Palette:SupprimerObjet nom=" + selection.get(0));
                    selection.clear();
                    break;
                case RECTANGLE:
                    for (String str : selection) {
                        if (str.charAt(0) == 'R') {
                            bus.sendMsg("Palette:SupprimerObjet nom=" + str);
                            selection.clear();
                            break;
                        }
                    }
                    break;
                case ELLIPSE:
                    for (String str : selection) {
                        if (str.charAt(0) == 'E') {
                            bus.sendMsg("Palette:SupprimerObjet nom=" + str);
                            selection.clear();
                            break;
                        }
                    }
                    break;
            }
            //on supprime l'objet
            
        }
    }

    public void action(Template t) {
        System.out.println("dessine moi un " + t.getNom());
        try {
            switch (t.getNom()) {
                case "carrer":
                    bus.sendMsg("Palette:CreerRectangle x=" + dernierPoint.x + " y=" + dernierPoint.y);
                     deleteState=false;
                    break;
                case "oval":
                    bus.sendMsg("Palette:CreerEllipse x=" + dernierPoint.x + " y=" + dernierPoint.y);
                     deleteState=false;
                case "croix":
                    deleteState=true;
                 //   supprimer(ss);
            }
        } catch (IvyException ie) {
            System.out.println("can't send my message !");
        }

    }
    /*
     public void delete(double x, double y) throws IvyException {
     System.out.println("suppression");
     bus.sendMsg("Palette:TesterPoint x=" + (int) x + " y=" + (int) y);

     if (!selection.isEmpty()) {

     bus.sendMsg("Palette:SupprimerObjet nom=" + selection.get(0));    //on supprime l'objet
     selection.clear();
     }
     }
     */

    public void setLearnState() {
        state = State.learn;
        System.out.println("Etat LEARN activé !");
    }

    private void envoieStroke() {
        s.normalize();
    }

    private void testerObjet() throws IvyException {
        bus.sendMsg("Palette:TesterPoint x=" + (int) dernierPoint.x + " y=" + (int) dernierPoint.y);
    }

}
