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

    }

    void verifStroke() {

    }

    public ArrayList<Template> getTemplates() {
        listTemplate = new ArrayList<Template>();
        BufferedReader in;
        try {
            in = new BufferedReader(new FileReader("listTemplates.txt"));
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
    }

    private void saveTemplates() {
        try {
            PrintWriter out = new PrintWriter(new FileWriter("templates.txt"));
            for (int i = 0; i < listTemplate.size(); i++) {
                listTemplate.get(i).write(out);
            }
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
