/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.mcmaster.selfsplitpseudocostrepo.cplex;

import static ca.mcmaster.selfsplitpseudocostrepo.Constants.*;
import static ca.mcmaster.selfsplitpseudocostrepo.Parameters.*;
import ca.mcmaster.selfsplitpseudocostrepo.server.Leaf;
import ilog.concert.IloException;
import ilog.concert.IloLPMatrix;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author tamvadss
 */
public class Utilities {
    
     public static IloCplex convertLeaf_To_Cplex (Leaf leaf , double cutoff) throws IloException {
        IloCplex cplex = new IloCplex ();
        cplex.importModel(  MIP_FILENAME);
        //use pseudo cost branching
        cplex.setParam( IloCplex.Param.MIP.Strategy.VariableSelect  ,  TWO);
        cplex.setParam( IloCplex.Param.Emphasis.MIP , MIP_EMPHASIS );
        //node file compressed to disk
        cplex.setParam( IloCplex.Param.MIP.Strategy.File , THREE);
        
        cplex.setParam( IloCplex.Param.MIP.Strategy.HeuristicFreq , -ONE);
      
        Map <String, IloNumVar> allVariables = getVariables(cplex) ;
        for (Map.Entry<String,Double> entry : leaf.upperBounds.entrySet()){
            updateVariableBounds (allVariables.get(entry.getKey()), entry.getValue(), true );
        }
        for (Map.Entry<String,Double> entry :leaf.lowerBounds.entrySet() ){
            updateVariableBounds (allVariables.get(entry.getKey()), entry.getValue(), false );
        }
         
        if (cutoff < BILLION) {
            //set cutoff
            cplex.setParam( IloCplex.Param.MIP.Tolerances.UpperCutoff, cutoff) ;
        }
        
        /*if (varPriorityMap!=null){
            if (varPriorityMap.size()>ZERO){
                applyVariablePriorityList(cplex ,  varPriorityMap );
            }
        }*/
        
        assignVariablePriorities(cplex);
        return  cplex ;
    }
         
    public static Map<String, IloNumVar> getVariables (IloCplex cplex) throws IloException{
        Map<String, IloNumVar> result = new HashMap<String, IloNumVar>();
        IloLPMatrix lpMatrix = (IloLPMatrix)cplex.LPMatrixIterator().next();
        IloNumVar[] variables  =lpMatrix.getNumVars();
        for (IloNumVar var :variables){
            result.put(var.getName(),var ) ;
        }
        return result;
    }
    
    private static void assignVariablePriorities (IloCplex cplex) {
        
    }
    
    private static   void updateVariableBounds(IloNumVar var, double newBound, boolean isUpperBound   )      throws IloException{
 
        if (isUpperBound){
            if ( var.getUB() > newBound ){
                //update the more restrictive upper bound
                var.setUB( newBound );
                //System.out.println(" var " + var.getName() + " set upper bound " + newBound ) ;
            }
        }else{
            if ( var.getLB() < newBound){
                //update the more restrictive lower bound
                var.setLB(newBound);
                //System.out.println(" var " + var.getName() + " set lower bound " + newBound ) ;
            }
        }  

    } 
    
}
