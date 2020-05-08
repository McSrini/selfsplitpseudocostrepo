/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.mcmaster.selfsplitpseudocostrepo;

import static ca.mcmaster.selfsplitpseudocostrepo.Constants.*;
import org.apache.log4j.Level;

/**
 *
 * @author tamvadss
 */
public class Parameters {
        
    public static int SOLUTION_CYCLE_TIME_IN_SECONDS =    10*60 ; //ten minutes
    
    //solve each MIP for this much time
    public static int TIME_QUANTUM_SECONDS = SOLUTION_CYCLE_TIME_IN_SECONDS    /(FIVE);
    //do not solve a MIP unless this many seconds left
    public static int MINIMUM_TIME_QUANTUM_SECONDS = TIME_QUANTUM_SECONDS/10 ;
    
    public static int MAX_SOLUTION_CYCLES = (  5  *3600)/  SOLUTION_CYCLE_TIME_IN_SECONDS ; //five hours
    public static int RAMP_UP_MULT_FACTOR = 1; 
    public static int NUM_LEAFS_PER_WORKER =    RAMP_UP_MULT_FACTOR * MAX_SOLUTION_CYCLES  ;
    
    public static final double USE_IMPORTED_SOLUTION_AFTER_RAMPUP= 116   ;       
    
    public static int INCREASE_THE_NUMBER_OF_SOLUTION_CYCLES_BY_ = 100      ;
    
    public static final boolean USE_BARRIER_FOR_SOLVING_LP = false;
    public static final boolean DISABLE_CUTS = false;
    public static final boolean DISABLE_PRESOLVE_NODE = false;
    public static final boolean  DISABLE_PRESOLVE = false;
    
    //change this name for testing
    private static final String _MIP_FILENAME = "comp21-2idx";
    public static final String  MIP_FILENAME = _MIP_FILENAME + ".pre.sav"; 
    //public static final double   CUTOFF_TO_USE_FOR_DISTRIBUTION = 72815.75416157287;
     
    public static boolean   USE_VAR_PRIORITIES = true ;
    public static boolean   USE_PURE_CPLEX= false ;
    
    //use negative number to disable mip gap check
    public static final double  RELATIVE_MIP_GAP= -1 ; 
    public static final int  MIP_EMPHASIS = 0; 
    
    public static   final String LOG_FOLDER="./"  + "logs/" +  _MIP_FILENAME + "/" + 
            (USE_PURE_CPLEX? ("pure"+USE_VAR_PRIORITIES) : (USE_VAR_PRIORITIES? ("repo_new_"+NUM_LEAFS_PER_WORKER + "_"+ TIME_QUANTUM_SECONDS): "dist")) + "/"; 
    public static   final String LOG_FILE_EXTENSION = ".log";
    public static   final Level LOGGING_LEVEL= Level.INFO ;    
    
    public static String getParameters () {
        return "SOLUTION_CYCLE_TIME_IN_SECONDS " + SOLUTION_CYCLE_TIME_IN_SECONDS + 
                "TIME_QUANTUM_SECONDS "+ TIME_QUANTUM_SECONDS+
                "MAX_SOLUTION_CYCLES "+ MAX_SOLUTION_CYCLES+
                "NUM_LEAFS_PER_WORKER " + NUM_LEAFS_PER_WORKER+
                "_MIP_FILENAME " + _MIP_FILENAME+
                "USE_VAR_PRIORITIES "+ USE_VAR_PRIORITIES+
                " USE_PURE_CPLEX "+ USE_PURE_CPLEX+
                "MIP_EMPHASIS " + MIP_EMPHASIS+
                "LOG_FOLDER " + LOG_FOLDER +
                " USE_BARRIER_FOR_SOLVING_LP "+ USE_BARRIER_FOR_SOLVING_LP+
                " DISABLE_CUTS "+ DISABLE_CUTS+
                " INCREASE_THE_NUMBER_OF_SOLUTION_CYCLES_BY_ " + INCREASE_THE_NUMBER_OF_SOLUTION_CYCLES_BY_+
                " DISABLE_PRESOLVE " + DISABLE_PRESOLVE+
               " RELATIVE_MIP_GAP "+ RELATIVE_MIP_GAP ;
    }
               
}
