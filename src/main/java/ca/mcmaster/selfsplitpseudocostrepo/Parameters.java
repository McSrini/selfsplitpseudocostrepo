/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.mcmaster.selfsplitpseudocostrepo;

/**
 *
 * @author tamvadss
 */
public class Parameters {
        
    public static int SOLUTION_CYCLE_TIME_IN_SECONDS =   6*60 ; //six minutes
    
    //solve each MIP for this much time
    public static int TIME_QUANTUM_SECONDS = SOLUTION_CYCLE_TIME_IN_SECONDS;
    //do not solve a MIP unless this many seconds left
    public static int MINIMUM_TIME_QUANTUM_SECONDS = 10 ;
    
    public static int MAX_SOLUTION_CYCLES = 3; //(  5  *3600)/  SOLUTION_CYCLE_TIME_IN_SECONDS ; //five hours
    public static int RAMP_UP_MULT_FACTOR = 1; 
    public static int NUM_LEAFS_PER_WORKER = 3 ; //RAMP_UP_MULT_FACTOR * MAX_SOLUTION_CYCLES;
    
    public static final String MIP_FILENAME = "timtab1.pre.sav"; 
    
    
    
    public static final int  MIP_EMPHASIS =  3; 
     
    
}
