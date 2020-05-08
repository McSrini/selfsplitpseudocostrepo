/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.mcmaster.selfsplitpseudocostrepo.pure;

import static ca.mcmaster.selfsplitpseudocostrepo.Constants.*;
import ca.mcmaster.selfsplitpseudocostrepo.Parameters;
import static ca.mcmaster.selfsplitpseudocostrepo.Parameters.*; 
import static ca.mcmaster.selfsplitpseudocostrepo.cplex.Utilities.configureCplex;
import static ca.mcmaster.selfsplitpseudocostrepo.cplex.Utilities.getVariables;
import static ca.mcmaster.selfsplitpseudocostrepo.cplex.Utilities.getVars;
import ilog.concert.IloException;
import ilog.concert.IloNumVar;
import ilog.cplex.IloCplex;
import ilog.cplex.IloCplex.Status;
import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import static java.lang.System.exit;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.RollingFileAppender;

/**
 *
 * @author tamvadss
 */
public class PureSolver {
    private static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(PureSolver.class); 
  
    private    static Map  <Integer, List<String>> priority_Map= null;
    
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
        
        logger.info ("pure solver starting ..." );
        
        if (Parameters.USE_VAR_PRIORITIES){
           initializePriorityMap();
           assignVariablePriorities(cplex);
        }
         
        cplex.setParam( IloCplex.Param.TimeLimit, SOLUTION_CYCLE_TIME_IN_SECONDS );
        final int maxIterations = CLUSTER_SIZE * MAX_SOLUTION_CYCLES;
        for (int iter = ZERO; iter < maxIterations ; iter ++){
            cplex.solve ();
            //print progress
            boolean hasSolution = cplex.getStatus().equals( Status.Optimal) ||cplex.getStatus().equals( Status.Feasible)  ;
            logger.info ("iteration lprealx and solution," + iter + "," + cplex.getBestObjValue() +"," +(hasSolution? cplex.getObjValue(): BILLION) ) ;
             
            if (isHaltFilePresent()) exit(ONE);
           
            if (hasSolution)            {
                double mipgap = cplex.getBestObjValue() -cplex.getObjValue();
                mipgap = mipgap / (0.0000001 + cplex.getObjValue()) ;
                mipgap = Math.abs (mipgap );
                logger.info ("mipgap is "+ mipgap) ;
                if (mipgap<= Parameters.RELATIVE_MIP_GAP){
                    logger.info ("target mip gap reached") ;
                    exit(ONE);
                }                
            }
            
        }
        logger.info ("pure solver completed" );
    }
    
    //code copy pasted from Client.java and Utilities.java    
    private static void initializePriorityMap () throws Exception {
        File file = new File(PRIORITY_LIST_FILENAME );         
        if (file.exists()) {
            logger.info( "reading priority map ...");
            FileInputStream fis = new FileInputStream(PRIORITY_LIST_FILENAME);
            ObjectInputStream ois = new ObjectInputStream(fis);
            priority_Map  = (TreeMap) ois.readObject();
            ois.close();
            fis.close();  
            logger.info( "size of priority map" + priority_Map.size());
        }else {
            logger.info( "no variable priority map");
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
    
    private static boolean isHaltFilePresent (){
        File file = new File(HALT_FILE );         
        return file.exists();
    }
        
}
