/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.mcmaster.selfsplitpseudocostrepo.client;

import static ca.mcmaster.selfsplitpseudocostrepo.Constants.*; 
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author tamvadss
 */
public class ClientRequestObject  implements Serializable {
    
    public String clientName ;
    public int numberOfSolutionCyclesCompleted = ZERO;
    public double bestKnownSolution = BILLION ;
    public double dualBound = BILLION ;
    //which jobs are complete, in progress , or untouched. Key is node ID got after  ramp up.
    public List <String> untouchedJobs = new ArrayList <String> ();
    public int numJobsInProgress= ZERO;
    
    public String toString () {
        String result ="";
        result += clientName + " "+ numberOfSolutionCyclesCompleted + " " + bestKnownSolution + " "+ dualBound + " "
                  + numJobsInProgress + " " + untouchedJobs.size();
        return result;
    }
    
}
