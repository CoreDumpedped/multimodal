/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fusionmultimodal;

import java.util.List;
import fusionmultimodal.Stroke;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

/**
 *
 * @author astierre
 */
public class Recognizer {

    List<Template> listTemplate;
    Stroke strokeCourant;

    public Recognizer() {
        listTemplate=new ArrayList<>();
        
    }

    public Template verifStroke() {
        getTemplates();
        Template closerTemplate = null;
        double min = 500000;
        double dist;
        for (Template t : listTemplate) {
            dist = t.calculDistance(strokeCourant);
            System.out.println("Verification Stroke : distance avec le dessin : "
                    + ""+ dist +" minumum : "+ min);
            if (dist < min) {
                min = dist;
                closerTemplate = t;
            }
        } 
        return closerTemplate;
    }

    public ArrayList<Template> getTemplates() {
        listTemplate = new ArrayList<Template>();
        BufferedReader in;
        try {
            in = new BufferedReader(new FileReader("stockage.txt"));
            String str;
            while ((str = in.readLine()) != null) {
                if (!str.trim().equals("")) {
                    listTemplate.add(Template.read(str));
                }
            }
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
                
       
        return (ArrayList<Template>) listTemplate;
    }

    
    public void addTemplates(Template t){
         System.out.println("ajout du " + t.nom + "dans la liste");
        listTemplate.add(t);
    }
    
    
   public void saveTemplates() {
        try {
            PrintWriter out = new PrintWriter(new FileWriter("stockage.txt",true));
            for (int i = 0; i < listTemplate.size(); i++) {
                listTemplate.get(i).write(out);
            }
            out.close();
            System.out.println("sauvegarde réussit");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
   
   public void setStrokeCourant (Stroke s) {
       strokeCourant = s;
   }
} 
