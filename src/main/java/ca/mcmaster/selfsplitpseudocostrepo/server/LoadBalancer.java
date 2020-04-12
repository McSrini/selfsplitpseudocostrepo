/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.mcmaster.selfsplitpseudocostrepo.server;

import static ca.mcmaster.selfsplitpseudocostrepo.Constants.*;  
import static ca.mcmaster.selfsplitpseudocostrepo.Parameters.*;
import ca.mcmaster.selfsplitpseudocostrepo.client.ClientRequestObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author tamvadss
 * 
 * class methods should be used when you have an exclusive lock to the response map
 * //also note that , if you exclusive lock to the response map, there are no writers into the request map, so you can read from it
 * 
 */
public class LoadBalancer {
    
        
    public static void dynamicLoadBalance (){
        //collect all the unused jobs
        //assign them one by one to the worker with fewest remaining jobs
        Map < String, List <String>> jobAssignmentMapForDynamicLB  =LoadBalancer. prepareAssignmentMap_forDynamicLB ();
        
        for (String clientName : Server.map_Of_IncomingRequests.keySet()){
            ServerResponseObject resp = new ServerResponseObject();
            resp.globalIncumbent= Server.bestKnownSolution;
            resp.jobAssignmentList =  jobAssignmentMapForDynamicLB.get(clientName);
            Server.responseMap.put (clientName, resp) ; 
        }
    }
    
           
    //equal distribution of ramp up pool to each client
    public static void staticLoadBalance (){
        //NUM_LEAFS_PER_WORKER to each worker
        int clientNumber = ZERO;
        for (String clientName : Server.map_Of_IncomingRequests.keySet()){
            ServerResponseObject resp = new ServerResponseObject();
            resp.globalIncumbent=Server.bestKnownSolution;
            resp.leafPool_FromRampUp = Server.leafPool_FromRampUp;
            resp.jobAssignmentList =  getAssignmentmapForStaticLB(clientNumber);
            Server.responseMap.put (clientName, resp) ;     
            clientNumber ++;
        }
        
    }
    
    private static List <String> getAssignmentmapForStaticLB (int clientNumber){
        int count = ZERO;
        List <String > result = new ArrayList   <String>();
        for (String nodeID : Server.leafPool_FromRampUp.keySet()){
            if (clientNumber*NUM_LEAFS_PER_WORKER <= count && count < (clientNumber+ONE) *NUM_LEAFS_PER_WORKER){
                result.add( nodeID);
            }
            count ++;
        }
        return result;
    }
    
    private static  Map < String, List <String>>     prepareAssignmentMap_forDynamicLB (){
        Map < String, List <String>>  jobAssignmentMap = new HashMap < String, List <String>>  ();
        
        Map < String, Integer>  inProgressJobCount = new HashMap < String, Integer>  ();
        List<String> jobsAvailableForRedistribution =new ArrayList<String> ();
        for (Map.Entry< String, ClientRequestObject> entry :Server.map_Of_IncomingRequests.entrySet()){
            jobsAvailableForRedistribution.addAll(entry.getValue().untouchedJobs );      
            inProgressJobCount.put (entry.getKey(), entry.getValue().numJobsInProgress) ;
            //init result array
            jobAssignmentMap.put( entry.getKey(), new ArrayList<String> ()  );
        }
        
        //assign 1 untouched job to the poorest worker
        while ( true){
            if (jobsAvailableForRedistribution.size()==ZERO) break;
            String nodeID =  jobsAvailableForRedistribution.remove(ZERO);
                        
            //find pooerest worker and assign him this job
            String clientWithFewestJobs = getClientWithFewestJobs (   inProgressJobCount , jobAssignmentMap);
            jobAssignmentMap.get(clientWithFewestJobs ).add(nodeID);
            
        }
        
        return jobAssignmentMap;    
    }
    
    private static String getClientWithFewestJobs ( Map < String, Integer>  inProgressJobCount , 
                                                    Map < String, List <String>>  assignedJobs){
        String clientWithLowestJobCount = null;
        int lowest = BILLION;
        for (String clientname : Server.map_Of_IncomingRequests.keySet()){
            
            int thisCount = inProgressJobCount.get(clientname ) + assignedJobs.get (clientname) .size();
            if (thisCount < lowest){
                lowest = thisCount;
                clientWithLowestJobCount= clientname;                
            }
        }
        return clientWithLowestJobCount;
    }
    

    

}
