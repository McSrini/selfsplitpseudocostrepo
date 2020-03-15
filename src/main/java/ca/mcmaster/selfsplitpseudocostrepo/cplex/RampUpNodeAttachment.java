/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.mcmaster.selfsplitpseudocostrepo.cplex;

import ca.mcmaster.selfsplitpseudocostrepo.server.Leaf;

/**
 *
 * @author tamvadss
 */
public class RampUpNodeAttachment {
   
  //branching condition from parent
  public String branchingVarName =null;
  public double branchingBound  ;
  public boolean isBranchingDirectionDown  ; //dir down < 
  
  public RampUpNodeAttachment parentNode=null;
  
  public boolean isRoot (){
    return null==parentNode;
  }
  
  public Leaf convertToLeaf (){
      Leaf leaf = new Leaf ();
    
      RampUpNodeAttachment current = this;
      while ( ! current.isRoot()) {
      
          //add my conditions to leaf 
          double bound = current.branchingBound;
          String variableName=current.branchingVarName;

          if (current.isBranchingDirectionDown){
            //upper bound        
            if (!leaf.upperBounds.containsKey( variableName)) leaf.upperBounds.put (variableName, bound) ;
          }else {
            if (!leaf.lowerBounds.containsKey( variableName)) leaf.lowerBounds.put (variableName, bound) ;
          }

          //climb up
          current= current.parentNode;
      }
      return leaf;
    
  }
}
 
