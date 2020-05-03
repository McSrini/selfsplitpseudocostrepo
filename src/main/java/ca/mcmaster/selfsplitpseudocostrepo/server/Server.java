/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.mcmaster.selfsplitpseudocostrepo.server;
 
import static ca.mcmaster.selfsplitpseudocostrepo.Constants.*;
import ca.mcmaster.selfsplitpseudocostrepo.Parameters;
import static ca.mcmaster.selfsplitpseudocostrepo.Parameters.LOGGING_LEVEL;
import static ca.mcmaster.selfsplitpseudocostrepo.Parameters.LOG_FILE_EXTENSION;
import static ca.mcmaster.selfsplitpseudocostrepo.Parameters.LOG_FOLDER;
import static ca.mcmaster.selfsplitpseudocostrepo.Parameters.*;
import ca.mcmaster.selfsplitpseudocostrepo.client.ClientRequestObject;
import ca.mcmaster.selfsplitpseudocostrepo.client.Job;
import ilog.concert.IloException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import static java.lang.System.exit;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map; 
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.RollingFileAppender;

/**
 *
 * @author tamvadss
 */
public class Server {
    
    private static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(Server .class); 
    
    
    //key is client name
    public static Map < String, ClientRequestObject >   map_Of_IncomingRequests   = Collections.synchronizedMap(new HashMap< String, ClientRequestObject > ()); 
    public static Map < String, ServerResponseObject >   responseMap  = Collections.synchronizedMap(new HashMap< String, ServerResponseObject > ()); 
    
    //node pool with predictable iteration order, so we can give the first 10 to the first worker, 
    //next 10 to the next worker, and so on
    public static Map<String, Leaf> leafPool_FromRampUp = new LinkedHashMap   <String, Leaf>  ( ) ;
    //global incumbent
    public static double bestKnownSolution = BILLION;
    //dual bound
    public static double dualBound = BILLION;
    
    static {
        logger.setLevel( LOGGING_LEVEL);
        PatternLayout layout = new PatternLayout("%5p  %d  %F  %L  %m%n");     
        try {
            RollingFileAppender rfa =new  RollingFileAppender(layout,LOG_FOLDER+  Server.class.getSimpleName()+ LOG_FILE_EXTENSION);
            rfa.setMaxBackupIndex(SIXTY);
            logger.addAppender(rfa);
            logger.setAdditivity(false);
        } catch (Exception ex) {
            ///
            System.err.println("Exit: unable to initialize logging"+ex);       
            exit(ONE);
        }
        
       
    } 
    
    
    public static void main(String[] args) throws Exception {     
        
        logger.info ( Parameters.getParameters());
        if (Parameters.INCREASE_THE_NUMBER_OF_SOLUTION_CYCLES_BY_> ONE) {
            MAX_SOLUTION_CYCLES*=INCREASE_THE_NUMBER_OF_SOLUTION_CYCLES_BY_;
            logger.info ("MAX_SOLUTION_CYCLES changed to "+ MAX_SOLUTION_CYCLES) ;
        }
        
        ExecutorService executor = null;
        
        //ramp up the MIP and populate the ramp up leaf list        
        RampUp rampup = new RampUp ();
        
               
        Server.bestKnownSolution = rampup.getFrontier();
        
        logger.info ("ramp up complete") ;
              
        
        try (
                //try with resources 
                ServerSocket serverSocket = new ServerSocket(PORT_NUMBER);               
                
            ) {
            String hostname =  InetAddress.getLocalHost(). getHostName() ;
            System.out.println("The   server is running..." + hostname);
            
            executor = Executors.newFixedThreadPool( CLUSTER_SIZE );     
            
            while (true ){
                Socket clientSocket = serverSocket.accept();
                RequestHandler requestHandler = new RequestHandler(clientSocket) ;
                executor.execute(requestHandler);                  
            }
            
        } catch(Exception ex) {
            ex.printStackTrace();
            System.err.println(ex.getMessage());
        }finally{
            if (executor!=null){
                executor.shutdown();
            }
        }
    }
    
}
