/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fusionmultimodal.recognizer;

import fr.dgac.ivy.Ivy;
import fr.dgac.ivy.IvyClient;
import fr.dgac.ivy.IvyException;
import fr.dgac.ivy.IvyMessageListener;
import fusionmultimodal.Stroke;
import fusionmultimodal.Template;
import java.awt.geom.Point2D;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;

/**
 *
 * @author grimaar
 */
public class IvyRecognizer {

    Ivy bus;
    List<Template> listTemplate;
    boolean isLearning = false;

    enum State {

        I, D
    }
    private State s = State.I;
    private Stroke stroke;
    Recognizer recognizer;

    public IvyRecognizer() throws IvyException {
        bus = new Ivy("IvyRecognizer", "ivyRecognizer Ready", null);
        recognizer = new Recognizer();

        bus.bindMsg("^Palette:MousePressed x=(.*) y=(.*)", new IvyMessageListener() {
            public void receive(IvyClient client, String[] args) {
                switch (s) {
                    case I:
                        s = State.D;
                        stroke = new Stroke();
                        double x = Double.parseDouble(args[0]);
                        double y = Double.parseDouble(args[1]);
                        stroke.addPoint(new Point2D.Double(x, y));
                        break;
                    case D:
                        //Impossible
                        break;
                }

            }
        });

        bus.bindMsg("^Palette:MouseDragged x=(.*) y=(.*)", new IvyMessageListener() {
            public void receive(IvyClient client, String[] args) {
                switch (s) {
                    case I:
                        //Impossible
                        break;
                    case D:
                        s = State.D;
                        double x = Double.parseDouble(args[0]);
                        double y = Double.parseDouble(args[1]);
                        stroke.addPoint(new Point2D.Double(x, y));
                        break;
                }
            }
        });

        bus.bindMsg("^Palette:MouseReleased x=(.*) y=(.*)", new IvyMessageListener() {
            public void receive(IvyClient client, String[] args) {
                System.out.println("");
                switch (s) {
                    case I:
                        //Impossible
                        break;
                    case D:
                        s = State.I;
                        stroke.normalize();
                        if (isLearning) {
                            String s = (String) JOptionPane.showInputDialog("Quelle est la forme? ");
                            recognizer.addTemplates(new Template(s, stroke));
                            recognizer.saveTemplates();
                        } else {
                            recognizer.setStrokeCourant(stroke);
                            Template t = recognizer.verifStroke();
                            if (t != null) {
                                System.out.println("Cette figure ressemblerai a s'y méprendre (et c'est peu de le dire !) à un :" + t.getNom());
                                stroke = null;
                                try {
                                    sendMessage(t.getNom());
                                } catch (IvyException ex) {
                                    Logger.getLogger(IvyRecognizer.class.getName()).log(Level.SEVERE, null, ex);
                                }
                            }
                        }
                        break;
                }

            }

            
        });
        
        bus.start(null);
    }

    public void setLearnState(boolean state) {
        isLearning = state;
        System.out.println("Etat LEARN activé  = "+ state);
    }
    
    private void sendMessage(String nom) throws IvyException {
         bus.sendMsg("Recognizer:Forme nom=" + nom);
    }
}
