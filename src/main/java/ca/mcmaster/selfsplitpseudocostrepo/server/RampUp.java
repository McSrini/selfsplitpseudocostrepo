/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.mcmaster.selfsplitpseudocostrepo.server;

import ca.mcmaster.selfsplitpseudocostrepo.cplex.RampUpBranchhandler;
import ca.mcmaster.selfsplitpseudocostrepo.cplex.RampUpNodehandler;
import static ca.mcmaster.selfsplitpseudocostrepo.Constants.*;
import static ca.mcmaster.selfsplitpseudocostrepo.Parameters.*;
import ca.mcmaster.selfsplitpseudocostrepo.cplex.Utilities;
import ilog.concert.IloException;
import ilog.cplex.IloCplex;
import ilog.cplex.IloCplex.Status;

/**
 *
 * @author tamvadss
 */
public class RampUp {
    
    private IloCplex cplex ;
    
    public double getFrontier () throws IloException{
        
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
        
        cplex.solve ();
        
        double rampUpSolution = BILLION;
        if (cplex.getStatus().equals(Status.Feasible)){
            rampUpSolution  = cplex.getObjValue();
        }
        
        Server.dualBound= cplex.getBestObjValue();
        
        cplex.end();
        
        return rampUpSolution;
        
    }
    
}
