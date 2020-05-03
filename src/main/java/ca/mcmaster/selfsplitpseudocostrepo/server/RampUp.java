/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.mcmaster.selfsplitpseudocostrepo.server;

import ca.mcmaster.selfsplitpseudocostrepo.cplex.RampUpBranchhandler;
import ca.mcmaster.selfsplitpseudocostrepo.cplex.RampUpNodehandler;
import static ca.mcmaster.selfsplitpseudocostrepo.Constants.*;
import ca.mcmaster.selfsplitpseudocostrepo.Parameters;
import static ca.mcmaster.selfsplitpseudocostrepo.Parameters.*;
import ca.mcmaster.selfsplitpseudocostrepo.cplex.Utilities;
import static ca.mcmaster.selfsplitpseudocostrepo.cplex.Utilities.getVariables;
import static ca.mcmaster.selfsplitpseudocostrepo.cplex.Utilities.getVars;
import ilog.concert.IloException;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;
import ilog.cplex.IloCplex.Status;
import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 *
 * @author tamvadss
 */
public class RampUp {
    
    private IloCplex cplex ;
    private    static Map  <Integer, List<String>> priority_Map= null;
    
    public double getFrontier () throws Exception{
        
        cplex = new IloCplex ();
        cplex.importModel(  MIP_FILENAME);
        cplex.use (new RampUpNodehandler ()) ;
        cplex.use (new RampUpBranchhandler());
        
        //use pseudo cost branching
        //cplex.setParam( IloCplex.Param.MIP.Strategy.VariableSelect  ,  TWO);
        //cplex.setParam( IloCplex.Param.MIP.Limits.StrongCand  , BILLION );
        //cplex.setParam( IloCplex.Param.MIP.Limits.StrongIt ,  BILLION );
        //cplex.setParam( IloCplex.Param.Emphasis.MIP , MIP_EMPHASIS );
        
        Utilities.configureCplex(cplex);
        
        if (Parameters.USE_VAR_PRIORITIES){
           initializePriorityMap();
           assignVariablePriorities(cplex);
        }
        
        cplex.solve ();
        
        double rampUpSolution = BILLION;
        if (cplex.getStatus().equals(Status.Feasible)){
            rampUpSolution  = cplex.getObjValue();
        }
        
        Server.dualBound= cplex.getBestObjValue();
        
        cplex.end();
        
        return rampUpSolution;
        
    }
    
       
    //code copy pasted from Client.java and Utilities.java    
    private static void initializePriorityMap () throws Exception {
        File file = new File(PRIORITY_LIST_FILENAME );         
        if (file.exists()) {
            System.out.println( "reading priority map ...");
            FileInputStream fis = new FileInputStream(PRIORITY_LIST_FILENAME);
            ObjectInputStream ois = new ObjectInputStream(fis);
            priority_Map  = (TreeMap) ois.readObject();
            ois.close();
            fis.close();  
            System.out.println( "size of priority map" + priority_Map.size());
        }else {
            System.out.println( "no variable priority map");
        }
    }
    //code copy pasted from Client.java and Utilities.java    
    private static void assignVariablePriorities (IloCplex cplex) throws IloException {
        if (priority_Map!=null){
            Map<String, IloNumVar> varMap = getVariables (  cplex);
            for (Map.Entry <Integer, List<String>>  entry : priority_Map.entrySet()){
                int size = entry.getValue().size();
                int[] intArray = new int[size]; 
                for (int index = ZERO ; index < size ; index ++){
                    intArray[index] = entry.getKey();
                     
                    //System.out.println("priority value is "+ entry.getKey()) ;
                    
                    
                }
                cplex.setPriorities( getVars   ( varMap ,  entry.getValue()), intArray);
                
                //System.out.println("int array size "+ intArray.length );
                
            }
        }
    }
    
}
