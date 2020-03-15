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
import ilog.concert.IloException;
import ilog.cplex.IloCplex;

/**
 *
 * @author tamvadss
 */
public class RampUp {
    
    private IloCplex cplex ;
    
    public void getFrontier () throws IloException{
        
        cplex = new IloCplex ();
        cplex.importModel(  MIP_FILENAME);
        cplex.use (new RampUpNodehandler ()) ;
        cplex.use (new RampUpBranchhandler());
        //use full strong branching
        cplex.setParam( IloCplex.Param.MIP.Strategy.VariableSelect  ,  THREE);
        cplex.setParam( IloCplex.Param.MIP.Limits.StrongCand  , BILLION );
        cplex.setParam( IloCplex.Param.MIP.Limits.StrongIt ,  BILLION );
        cplex.setParam( IloCplex.Param.Emphasis.MIP , MIP_EMPHASIS );
        
        cplex.solve ();
        cplex.end();
        
    }
    
}
