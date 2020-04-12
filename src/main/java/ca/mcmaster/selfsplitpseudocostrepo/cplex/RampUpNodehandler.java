/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.mcmaster.selfsplitpseudocostrepo.cplex;

import ca.mcmaster.selfsplitpseudocostrepo.server.Leaf;
import ca.mcmaster.selfsplitpseudocostrepo.server.Server;
import static ca.mcmaster.selfsplitpseudocostrepo.Constants.*;
import ca.mcmaster.selfsplitpseudocostrepo.Parameters;
import static ca.mcmaster.selfsplitpseudocostrepo.Parameters.*; 
import ilog.concert.IloException;
import ilog.cplex.IloCplex;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author tamvadss
 */
public class RampUpNodehandler extends IloCplex.NodeCallback {
 
    protected void main() throws IloException {
        //
        
        if ( CLUSTER_SIZE * NUM_LEAFS_PER_WORKER ==  getNremainingNodes64 ()){
            
            for (long nodeNum = ZERO; nodeNum < getNremainingNodes64(); nodeNum ++) {
                RampUpNodeAttachment nodeAttachment = (RampUpNodeAttachment) getNodeData (nodeNum) ;  
                
                System.out.println("Converting to leaf "+ getNodeId (nodeNum).toString()  + " having lp relax " + getObjValue(nodeNum) ) ;
                
                Leaf leaf =  nodeAttachment.convertToLeaf();
                leaf.lpRelax= getObjValue(nodeNum) ;
                leaf.nodeID = getNodeId(nodeNum) .toString();
                Server.leafPool_FromRampUp.put (leaf.nodeID, leaf) ;
            }
         
            abort ();
        }
    }
    
}