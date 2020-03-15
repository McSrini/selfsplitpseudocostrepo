/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.mcmaster.selfsplitpseudocostrepo.client;

import static ca.mcmaster.selfsplitpseudocostrepo.Constants.*;
import static ca.mcmaster.selfsplitpseudocostrepo.Parameters.*;
import ca.mcmaster.selfsplitpseudocostrepo.server.*;
import ilog.concert.IloException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import static java.lang.System.exit;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.RollingFileAppender;

/**
 *
 * @author tamvadss
 */
public class Client {    
    
    private static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(Client.class); 
     
    private static String clientname = null;
     
    //all the leafs from the ramp up
    private static Map <String, Job> jobMap = new LinkedHashMap <String, Job> ();
    //only a few are available to each client
    private static  List<String> listOfAvailableNodes = new ArrayList<String>();
    //
    private static  List<String> listOfInProgressNodes = new ArrayList<String>();
     
    private  static  double lowestDualBound= BILLION; 
    private  static  double localIncumbent= BILLION; 
    
    static {
        logger.setLevel( LOGGING_LEVEL);
        PatternLayout layout = new PatternLayout("%5p  %d  %F  %L  %m%n");     
        try {
            RollingFileAppender rfa =new  RollingFileAppender(layout,LOG_FOLDER+ Client.class.getSimpleName()+ LOG_FILE_EXTENSION);
            rfa.setMaxBackupIndex(SIXTY);
            logger.addAppender(rfa);
            logger.setAdditivity(false);
        } catch (Exception ex) {
            ///
            System.err.println("Exit: unable to initialize logging"+ex);       
            exit(ONE);
        }
    } 
    
    public static void main(String[] args) throws IOException {
        
        clientname =  InetAddress.getLocalHost(). getHostName() ;
         
        try (
            Socket workerSocket = new Socket(SERVER_NAME, PORT_NUMBER);                
            ObjectOutputStream  outputStream = new ObjectOutputStream(workerSocket.getOutputStream());
            ObjectInputStream inputStream =  new ObjectInputStream(workerSocket.getInputStream());
            
        ){
            
            logger.info ("Client is starting ... "+ clientname) ;
            
            for (int iteration = ZERO ; iteration <  MAX_SOLUTION_CYCLES ; iteration  ++){
                
                //get work from server
                ClientRequestObject request = prepareRequest(iteration) ;
                logger.debug (" sending request " + request );
                outputStream.writeObject(request);
               
                ServerResponseObject response = (ServerResponseObject) inputStream.readObject();
                logger.debug (" got response " + response );
                processResponse(response) ;
                
                
                
                //solve for CYCLE_TIME
                long iterationEndTime = System.currentTimeMillis()+ THOUSAND*SOLUTION_CYCLE_TIME_IN_SECONDS;
                
                while (true){
                    
                    long timeRemainingMillisec = - System.currentTimeMillis() + iterationEndTime  ;
                    if (timeRemainingMillisec <=ZERO) break;
                    
                    logger.debug("timeRemainingMillisec " + timeRemainingMillisec) ;
                    
                    if (  timeRemainingMillisec/THOUSAND <   MINIMUM_TIME_QUANTUM_SECONDS){
                        logger.warn ("client is sleeping for milliseconds..." + timeRemainingMillisec);
                        Thread.sleep( timeRemainingMillisec);
                    }else {
                        //solve with CPLEX
                        solveWithCplex (timeRemainingMillisec);
                    }
                    
                    printProgress();
                    
                }
                                
            }//end for iterations
            
            //send final solution and bound to server
            ClientRequestObject request = prepareRequest( MAX_SOLUTION_CYCLES) ;
            outputStream.writeObject(request);
            logger.debug (" sending request " + request );
            
            logger.info ("Client is stopping ... "+ clientname) ;
             
        } catch (Exception ex) {
             System.err.println(ex);
        }
         
    }
    
    private static void  solveWithCplex(  double timeRemaining_Millisec ) throws  Exception   {
        String jobToWorkOn = null; 
        
        List<String> candidateJobs = new ArrayList<String> ();
        candidateJobs.addAll( listOfInProgressNodes);
        candidateJobs.addAll(  listOfAvailableNodes);
        
        for (Map.Entry<String, Job> entry : jobMap.entrySet()){
            if (!candidateJobs.contains(entry.getKey() )) continue;
            if (entry.getValue().lpRelax <= lowestDualBound) {
                lowestDualBound = entry.getValue().lpRelax ;
                jobToWorkOn= entry.getKey();
            }
        }
        
        //we now know which job to work on for timeRemaining
        if (jobToWorkOn == null){
            logger.warn( " client found no job to work on. Will sleep for millisec "+ timeRemaining_Millisec);
            Thread.sleep((long) timeRemaining_Millisec );
        }else {
            listOfInProgressNodes.remove( jobToWorkOn);
            listOfAvailableNodes.remove(jobToWorkOn );
            
            logger.debug("Solving job ... "+ jobToWorkOn + " has lp relax " +  jobMap.get(jobToWorkOn).lpRelax) ;
            
            jobMap.get(jobToWorkOn).solve(timeRemaining_Millisec, localIncumbent);
            if ( !jobMap.get(jobToWorkOn).isComplete()){
                listOfInProgressNodes.add( jobToWorkOn);
            }else {
                logger.info ("solved to completion "+ jobToWorkOn);
            }
            
            localIncumbent = Math.min (localIncumbent, jobMap.get(jobToWorkOn).solutionValue);
            lowestDualBound =  getLowestDualBound (listOfInProgressNodes, listOfAvailableNodes) ;
            
            logger.info("localIncumbent and lowestDualBound ,"+ localIncumbent + ","+ lowestDualBound);
            
        }
                 
    }
    
    private static double  getLowestDualBound ( List<String> listOfInProgressNodes,   List<String> listOfAvailableNodes) {
        double lowest = BILLION;
        
        List<String> candidateJobs = new ArrayList<String> ();
        candidateJobs.addAll( listOfInProgressNodes);
        candidateJobs.addAll(  listOfAvailableNodes);
        for (String nodeID : candidateJobs){
            double thisLpRelax = jobMap.get(nodeID).lpRelax;
            if (thisLpRelax < lowest)  lowest = thisLpRelax;
        }
        
        return lowest;
    }
    
    private static void  processResponse(ServerResponseObject response) {
        //
        
        if (response.haltFlag){
            logger.warn("Halt instruction recieved ... stopping") ;
            exit(ZERO);
        }
        
        if (response.leafPool_FromRampUp.isEmpty()){
            //this is not the first cycle
            
            logger.debug("response being processed") ;
            
            //update cutoff and available nodes list
            Client.localIncumbent = response.globalIncumbent;
            Client.listOfAvailableNodes= response.jobAssignmentList;
            
        }else {
            
            logger.debug("response being processed ... init ") ;
            
            //just getting started right after ramp up
            //initialize the job map and available nodes list
            for (Map.Entry <String, Leaf> entry : response.leafPool_FromRampUp.entrySet()){
                Client.jobMap.put(entry.getKey() , new Job ( entry.getValue()  ));
            }
            Client.listOfAvailableNodes= response.jobAssignmentList;
        }
       
        Client.lowestDualBound = Client.getLowestDualBound(listOfInProgressNodes, listOfAvailableNodes);
        
        logger.debug("lowest LP realx is " + Client.lowestDualBound ) ;
        
    }
    
    private static ClientRequestObject prepareRequest (int numCompletedIterations) {
        
        ClientRequestObject req = new ClientRequestObject ();
        
        req.clientName =  clientname;
        req.numberOfSolutionCyclesCompleted =numCompletedIterations;
        req.bestKnownSolution =  Client.localIncumbent;
        req.dualBound=Client.lowestDualBound;
        req.numJobsInProgress=Client.listOfInProgressNodes.size();
        req.untouchedJobs = Client.listOfAvailableNodes;
        
        return req;
    }
    
    private static void printProgress (){
        logger.debug("Progress report:\n Job map  size " + Client.jobMap.size() ) ;
        logger.debug("Avail nodes " + Client.listOfAvailableNodes ) ;
        logger.debug("In progress " + Client.listOfInProgressNodes) ;
        logger.debug("Local incumbent " + Client.localIncumbent) ;
        logger.debug("lprealx " + Client.lowestDualBound) ;
    }
    
}
