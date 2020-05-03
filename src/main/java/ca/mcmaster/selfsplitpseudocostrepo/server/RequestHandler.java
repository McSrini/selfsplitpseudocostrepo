/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.mcmaster.selfsplitpseudocostrepo.server;

import ca.mcmaster.selfsplitpseudocostrepo.client.ClientRequestObject;
import static ca.mcmaster.selfsplitpseudocostrepo.Constants.*; 
import ca.mcmaster.selfsplitpseudocostrepo.Parameters;
import static ca.mcmaster.selfsplitpseudocostrepo.Parameters.*; 
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import static java.lang.System.exit;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.RollingFileAppender;

/**
 *
 * @author tamvadss
 */
public class RequestHandler implements Runnable{
    
    private  Socket clientSocket  ;
    
    private static org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(RequestHandler .class); 
        
    static {
        logger.setLevel( LOGGING_LEVEL);
        PatternLayout layout = new PatternLayout("%5p  %d  %F  %L  %m%n");     
        try {
            RollingFileAppender rfa =new  RollingFileAppender(layout,LOG_FOLDER+  RequestHandler.class.getSimpleName()+ LOG_FILE_EXTENSION);
            rfa.setMaxBackupIndex(SIXTY);
            logger.addAppender(rfa);
            logger.setAdditivity(false);
        } catch (Exception ex) {
            ///
            System.err.println("Exit: unable to initialize logging"+ex);       
            exit(ONE);
        }
        
       
    } 
    
    
    public RequestHandler ( Socket clientSocket ){
        
        this.clientSocket = clientSocket;
        
       
        
    }
 
    public void run() {
        try (
               
                ObjectOutputStream  outputStream = new ObjectOutputStream( clientSocket.getOutputStream());
                ObjectInputStream inputStream =  new ObjectInputStream(clientSocket .getInputStream()); 
                
            ){
            
            ClientRequestObject requestFromClient =   (ClientRequestObject) inputStream.readObject() ;
            logger.debug (" request recieved from client " + requestFromClient  ) ;           
            
            while (true){          
                //note the request in our synchronized map
                Server.map_Of_IncomingRequests.put( requestFromClient.clientName , requestFromClient);
                
                //process request and prepare response
                //this includes updating the best known solution to the server 
                ServerResponseObject resp = prepareResponse (   requestFromClient );
                
                if (requestFromClient.numberOfSolutionCyclesCompleted >= MAX_SOLUTION_CYCLES ) {
                    //do not respond to client
                    logger.info("Client is done ... "+  requestFromClient.clientName);
                    break;
                }else {
                    //send response to client
                    outputStream.writeObject(resp);
                }
                
                
                //read the next request from the same client
                requestFromClient =   (ClientRequestObject) inputStream.readObject() ;
                logger.debug (" request recieved from client " + requestFromClient ) ;           
            }
                        
            
        }catch (Exception ex){
            ex.printStackTrace();
            System.err.println(ex.getMessage());
        }finally {
            try {
                clientSocket.close();
            }catch (IOException ioex){
                System.err.println(ioex );
            }
        }
    }


    private ServerResponseObject prepareResponse (  ClientRequestObject requestFromClient ) throws Exception{
                
        //first wait for all requests to come in
        if (! waitForAllClients()){
            throw new Exception ("ERROR: Request not recieved from all clients !") ;
        }
        
        //the first thread that notices an empty response map prepares all the responses
        //if you see a full response map, just skip 
        boolean isDynamicLoadBalancing = requestFromClient.numberOfSolutionCyclesCompleted > ZERO;
        populateResponseMap (isDynamicLoadBalancing);
         
        
        //remove this client's response from the response map , and return it  
        //no external locking needed since this is an atomic operation
        //but lock anayway
        //
        //the last thread to send its response must also clear the request map, in preparation for
        //the next solution cycle
        ServerResponseObject resp = null;
        synchronized ( Server.responseMap) {
            resp = Server.responseMap.remove( requestFromClient.clientName);         
            if (Server.responseMap.isEmpty()){
                Server.map_Of_IncomingRequests.clear();
            }
        }
        
         
        
        return resp;
        
    }
    
    private void populateResponseMap (boolean isDynamicLoadBalancing){
        synchronized ( Server.responseMap) {
            if (Server.responseMap.isEmpty()){
                //populate it
                
                //update the dual bound and the solution once the worers are running
                if (isDynamicLoadBalancing) updateGlobalIncumbent_And_DualBound ();
                
                logger.info ("Dual bound and best solution UPDATED to ," + Server.dualBound + "," +Server.bestKnownSolution) ;
                
                createAllResponses(isDynamicLoadBalancing);
                
                //if every worker is reporting no inprogress jobs and no available jobs, then
                //computation is complete
                if (isDynamicLoadBalancing && isComputationComplete()){
                    logger.info ("computation is complete, the global incumbent is provably optimal");
                    System.out.println ("computation is complete, the global incumbent is provably optimal");
                    for (ServerResponseObject response: Server.responseMap.values()){
                        response.haltFlag= true;
                    }
                }
                  
            }
        }
    }
    
    private boolean isComputationComplete (){
        int count =ZERO;
        for (ClientRequestObject req: Server.map_Of_IncomingRequests.values()){
            count += req.numJobsInProgress;
            count+= req.untouchedJobs.size();
            if (count > ZERO) break;
        } 
        return ZERO==count;
    }
    
    private void updateGlobalIncumbent_And_DualBound (){
        
        double lowestDualBoundOfAllClients = BILLION;
        for (ClientRequestObject req: Server.map_Of_IncomingRequests.values()){
            if (req.bestKnownSolution< Server.bestKnownSolution ) Server.bestKnownSolution  = req.bestKnownSolution;
            if (req.dualBound < lowestDualBoundOfAllClients) lowestDualBoundOfAllClients= req.dualBound;
        }
        Server.dualBound= lowestDualBoundOfAllClients;
    }
    
    
    //method has exclusive access to both synchronized maps
    //two cases, right afer ramp up, and dynamic load balancing
    private void createAllResponses(boolean isDynamicLoadBalancing){
        if (isDynamicLoadBalancing){
            LoadBalancer.dynamicLoadBalance ();
        }else {
            LoadBalancer.staticLoadBalance ();
        }
    }
    
 
       
    private boolean waitForAllClients () throws InterruptedException{
        
        boolean result = false;
        
        for  (int limit = ZERO; limit < SIXTY * TWO*TWO ; limit ++ ) {
            
            int countOfRecieved = ZERO;
            
            synchronized ( Server.map_Of_IncomingRequests) {
               countOfRecieved = Server.map_Of_IncomingRequests.size();
            }//synch
            
            if ( countOfRecieved < CLUSTER_SIZE){
                //sleep for a second
                Thread.sleep( THOUSAND);
            }else {
                result = true;
                break;
            }
            
           
        }//end for limit
        
        return result;
    }
        
}
