/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fusionmultimodal;

import fr.dgac.ivy.IvyException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author astierre
 */
public class FusionMultiModal {

   
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            IvyStroke ivyStroke = new IvyStroke();
        } catch (IvyException ex) {
            Logger.getLogger(FusionMultiModal.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
