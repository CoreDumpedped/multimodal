/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fusionmultimodal;

import fusionmultimodal.recognizer.Recognizer;
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


    private enum State {
        learn, run
    };

    
    private enum SelectionShape {
        ALL, RECTANGLE, ELLIPSE
    }
    
    private enum Etat{init,carrer,rond,croix,deplacer};
    
    private State state = State.run;

    private Stroke s;
    private Template templateEnAttente;
    private Point dernierPoint;
    private List<String> selection;

    private boolean deleteState=false;
    Recognizer recognizer;
    private Etat etat;

    public IvyStroke() throws IvyException {
        etat=Etat.init;
        recognizer = new Recognizer();

        listPoints = new ArrayList<>();
        dernierPoint = new Point(0, 0);
        selection = new ArrayList<>();
        bus = new Ivy("IvyStroke", "IvyStroke Ready", null);

        bus.bindMsg("^Palette:MouseClicked x=(.*) y=(.*)", new IvyMessageListener() {       
            public void receive(IvyClient client, String[] args) {      
                switch(etat){
                    case init:
                        sauvegarderPoint(Integer.parseInt(args[0]), Integer.parseInt(args[1]));
                        break;
                    case carrer:
                        sauvegarderPoint(Integer.parseInt(args[0]), Integer.parseInt(args[1]));
                        break;
                    case croix:
                        sauvegarderPoint(Integer.parseInt(args[0]), Integer.parseInt(args[1]));
                        break;
                    case rond:
                        sauvegarderPoint(Integer.parseInt(args[0]), Integer.parseInt(args[1]));
                    case deplacer:
                        sauvegarderPoint(Integer.parseInt(args[0]), Integer.parseInt(args[1]));
                        break;
                }
            }
        });

        
        //todo la bonne syntaxd
        bus.bindMsg("^Recognizer:Forme nom=(.*)", new IvyMessageListener() {
            public void receive(IvyClient client, String[] args) {
                String forme=args[0];   
                System.out.println("forme reconnue: " + forme);
                switch(forme){
                    case "carre":
                        etat=Etat.carrer;
                        deleteState = false;
                        break;
                    case "oval":
                        etat=Etat.rond;
                        deleteState = false;
                        break;
                    case "croix":
                        etat=Etat.croix;
                        deleteState = true;
                        break;
                    case "vague":
                        etat=Etat.deplacer;
                        deleteState = false;
                        break;
                    default:
                        System.err.println("j'ai pas comprit la forme");
                        break;
                }
            }
        });

// a la reception de ici ou la ect
        bus.bindMsg("^sra5 Parsed=Action:position(.*)", new IvyMessageListener() { //vocal
            public void receive(IvyClient client, String[] args) {
                switch(etat){
                    case carrer:
                        dessineMoiunCarrer();
                        break;
                    case rond:
                        dessineMoiunRond();
                        break;
                    default:             
                        break;
                }
            }
        });

        
        // a la reception de cette objet,cette ellipse pour la suppression ou deplacement
        bus.bindMsg("^sra5 Parsed=Action:selection=(.*) Confidence(.*)", new IvyMessageListener() { //vocal
            public void receive(IvyClient client, String[] args) {
                switch(etat){
                    case croix:
                        suppression(args[0]);
                        break;
                    case deplacer:
                        System.out.println("deplacement");
                        deplacement(args[0]);
                        break;
                    default:
                        break;
                }
            }
        });

  //regarder les objet sous un point      
        bus.bindMsg("Palette:ResultatTesterPoint x=(.*) y=(.*) nom=(.*)", new IvyMessageListener() { 
            public void receive(IvyClient client, String[] args) {
                selection.add(args[2]);
            }
        }
        );
        bus.start(null);
    }

    
        private void suppression(String objet) {
        try {
            System.out.println("suppression=" + objet);
            switch (objet) {
                case "ce rectangle":
                    supprimer(SelectionShape.RECTANGLE);
                    break;
                case "cette ellipse":
                    supprimer(SelectionShape.ELLIPSE);
                    break;
                case "cet objet":
                    System.out.println("cet objet va vraiment etre supprimer");
                    supprimer(SelectionShape.ALL);
                    break;
            }
        } catch (IvyException ie) {
        }
    }

    
    private void supprimer(SelectionShape selectionShape) throws IvyException {
       
        if (!selection.isEmpty() && deleteState==true) {
            System.out.println("dans le if");
            switch (selectionShape) {
                case ALL:
                    System.out.println("Tout va disparaitre");
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
                System.out.println("dehors");
            //on supprime l'objet
           
        }
    }


    
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
    
    /**
     * sauvegarde les coordonner courante
     * regarder si des objet son situé à cette coordonner
     * @param x
     * @param y 
     */
    private void sauvegarderPoint(int x,int y){
        dernierPoint.x = x;
        dernierPoint.y = y;
        try {
            testerObjet();
        } catch (IvyException ex) {
            Logger.getLogger(IvyStroke.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void dessineMoiunCarrer(){
        try {
            bus.sendMsg("Palette:CreerRectangle x=" + dernierPoint.x + " y=" + dernierPoint.y);
        } catch (IvyException ex) {
            Logger.getLogger(IvyStroke.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void dessineMoiunRond(){
        try {
            bus.sendMsg("Palette:CreerEllipse x=" + dernierPoint.x + " y=" + dernierPoint.y);
        } catch (IvyException ex) {
            Logger.getLogger(IvyStroke.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
    private void supprimerRectangle(SelectionShape selectionShape,int x,int y) throws IvyException {
        for (String str : selection) {
            if (str.charAt(0) == 'R') {
                bus.sendMsg("Palette:DeplacerObjet nom=" + str + " x=" +x + " y=" + y);
                selection.clear();
                break;
            }
        }

    }
    
    /**
     * fonction de deplacement
     * @param arg 
     */
    private void deplacement(String objet) {
        try {
            System.out.println("Deplacement=" + objet);
            
            switch (objet) {
                case "ce rectangle":
                    supprimerRectangle(SelectionShape.RECTANGLE,10,10); //TODO BON COORDONNER
                    break;
                case "cette ellipse":
                    
                    break;
                case "cet objet":
                    
                    break;
            }
        } catch (IvyException ex) {
            Logger.getLogger(IvyStroke.class.getName()).log(Level.SEVERE, null, ex);
        }
          
    }
    

}
