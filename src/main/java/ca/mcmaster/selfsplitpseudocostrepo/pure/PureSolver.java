/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.mcmaster.selfsplitpseudocostrepo.pure;

import static ca.mcmaster.selfsplitpseudocostrepo.Constants.*;
import static ca.mcmaster.selfsplitpseudocostrepo.Parameters.*; 
import static ca.mcmaster.selfsplitpseudocostrepo.cplex.Utilities.configureCplex;
import ilog.concert.IloException;
import ilog.cplex.IloCplex;
import ilog.cplex.IloCplex.Status;
import static java.lang.System.exit;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.RollingFileAppender;

/**
 *
 * @author tamvadss
 */
public class PureSolver {
    private static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(PureSolver.class); 
  
    
    static {
        logger.setLevel( LOGGING_LEVEL);
        PatternLayout layout = new PatternLayout("%5p  %d  %F  %L  %m%n");     
        try {
            RollingFileAppender rfa =new  RollingFileAppender(layout,LOG_FOLDER+ PureSolver.class.getSimpleName()+ LOG_FILE_EXTENSION);
            rfa.setMaxBackupIndex(SIXTY);
            logger.addAppender(rfa);
            logger.setAdditivity(false);
        } catch (Exception ex) {
            ///
            System.err.println("Exit: unable to initialize logging"+ex);       
            exit(ONE);
        }
    } 

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {
        // TODO code application logic here
        
        IloCplex cplex = new IloCplex ();
        cplex.importModel(  MIP_FILENAME);
        
        configureCplex (cplex );
         
        cplex.setParam( IloCplex.Param.TimeLimit, SOLUTION_CYCLE_TIME_IN_SECONDS );
        final int maxIterations = CLUSTER_SIZE * MAX_SOLUTION_CYCLES;
        for (int iter = ZERO; iter < maxIterations ; iter ++){
            cplex.solve ();
            //print progress
            boolean hasSolution = cplex.getStatus().equals( Status.Optimal) ||cplex.getStatus().equals( Status.Feasible)  ;
            logger.info ("iteration lprealx and solution," + iter + "," + cplex.getBestObjValue() +"," +(hasSolution? cplex.getObjValue(): BILLION) ) ;
             
            
        }
        logger.info ("pure solver completed" );
    }
    
}
