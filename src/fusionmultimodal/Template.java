/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fusionmultimodal;

import fusionmultimodal.Stroke;
import java.awt.geom.Point2D;
import java.io.PrintWriter;
import java.util.StringTokenizer;

/**
 *
 * @author astierre
 */
public class Template {

  
    public Stroke stroke;
    public String nom;
    
    public Template(String nom, Stroke s) {     
        this.nom = nom;
        s.normalize();
        this.stroke = s;
    }
    
    public double calculDistance(Stroke s) {
        		double dist = 0.0;
        for(int i=1;i<s.size();i++)
        {
            Point2D.Double p0 = s.getPoint(i);
            Point2D.Double p1 = this.stroke.getPoint(i);
            dist+=p0.distance(p1);
        }
        return dist;
    }
    

    public void write(PrintWriter out)
    {
            out.print(nom);
            out.print(" ");
            for(int i=0;i<stroke.size();i++)
            {
                    Point2D.Double p = stroke.getPoint(i);
                    out.print(String.valueOf(p.getX()));
                    out.print(":");
                    out.print(String.valueOf(p.getX()));
                    out.print(" ");
            }
            out.println("");
    }

    public static Template read(String line)
    {
            StringTokenizer tok = new StringTokenizer(line, " ");
            String n = tok.nextToken();
            Stroke s = new Stroke();
            while(tok.hasMoreTokens())
            {
                    StringTokenizer tok2 = new StringTokenizer(tok.nextToken(), ":");
                    double x = Double.parseDouble(tok2.nextToken());
                    double y = Double.parseDouble(tok2.nextToken());
                    s.addPoint(new Point2D.Double(x,y));
            }
            return new Template(n, s);
    }    
}
