/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.mcmaster.selfsplitpseudocostrepo.server;
 
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.LinkedHashMap;

/**
 *
 * @author tamvadss
 */
public class ServerResponseObject  implements Serializable {
    
    //can be used to force the clients to exit
    public boolean haltFlag = false;
    
    //use as cutoff on the clients
    public double globalIncumbent ;
    // map used for load balancing, list of unused nodes to include in this workers round robin
    public List <String> jobAssignmentList= new ArrayList <String> (); 
    //all leafs sent to every client after ramp up
    public Map <String ,Leaf> leafPool_FromRampUp = new LinkedHashMap <String, Leaf> (); 
    
    public String toString (){
        String result = ""+globalIncumbent;
        result += "\n assignment \n";
        for (String str : jobAssignmentList){
            result += " "+ str;
        }
        result += "\n pool \n";
        for (Leaf leaf: leafPool_FromRampUp.values()){
            result+=leaf.toString();
        }
        return result;
    }
    
}
