/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.mcmaster.selfsplitpseudocostrepo.client;

import static ca.mcmaster.selfsplitpseudocostrepo.Constants.*;
import ca.mcmaster.selfsplitpseudocostrepo.cplex.Utilities;
import ca.mcmaster.selfsplitpseudocostrepo.server.Leaf;
import ilog.concert.IloException;
import ilog.cplex.IloCplex;
import ilog.cplex.IloCplex.Status;

/**
 *
 * @author tamvadss
 */
public class Job {
    
    //raw leaf
    private Leaf leaf ;
    //leaf prometed into cplex
    private IloCplex cplex=null;
     
    private boolean isEnded = false;
    
    public  double lpRelax = BILLION;
    public  double solutionValue =BILLION;    
        
    public Job ( Leaf leaf ){
        this.leaf=leaf;
        this.lpRelax=leaf.lpRelax;
    }
    
    public void solve (double timeRemaining_Millis, double cutoff) throws IloException {
        
        if (null==cplex) cplex = Utilities.convertLeaf_To_Cplex(leaf, cutoff);
            
        cplex.setParam( IloCplex.Param.TimeLimit, timeRemaining_Millis / THOUSAND );
        if (cutoff < BILLION) {
            //set cutoff
            cplex.setParam( IloCplex.Param.MIP.Tolerances.UpperCutoff, cutoff) ;
        }
        cplex.solve();
        
        if (cplex.getStatus().equals( Status.Optimal)  || cplex.getStatus().equals( Status.Infeasible)  ){
            if (cplex.getStatus().equals( Status.Optimal)) {
                solutionValue= cplex.getObjValue();
                lpRelax = cplex.getBestObjValue();
            }else {
                //infeasible
                solutionValue=BILLION;
                lpRelax=BILLION;
            }
            cplex.end();
            isEnded = true;
        }else {
            if (cplex.getStatus().equals( Status.Feasible)) solutionValue= cplex.getObjValue();
            lpRelax = cplex.getBestObjValue();
        }
    }
        
    public boolean isComplete (){
        return isEnded;
    }
}
