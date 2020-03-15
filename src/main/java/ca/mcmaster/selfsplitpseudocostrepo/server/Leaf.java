/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.mcmaster.selfsplitpseudocostrepo.server;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author tamvadss
 */
public class Leaf  implements Serializable {
    public Map <String, Double > upperBounds = new HashMap <String, Double > ();
    public Map <String, Double > lowerBounds = new HashMap <String, Double > ();
    
    public double lpRelax;
    public String nodeID;
   
    public String toString (){
        String ubs ="";
        String lbs ="";
        for (Map.Entry<String, Double> entry : this.upperBounds.entrySet()){        
            ubs+= entry.getKey() + "=" +entry.getValue() + " ";
        }
        for (Map.Entry<String, Double> entry : this.lowerBounds.entrySet()){        
            lbs+= entry.getKey() + "=" +entry.getValue() + " ";
        }
        return  "\n" +nodeID +  " \n UB: "+ ubs + "\n LB: " + lbs + " lprealx "+lpRelax + "\n";
    }
}