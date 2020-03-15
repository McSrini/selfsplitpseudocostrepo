/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ca.mcmaster.selfsplitpseudocostrepo.server;
 
import static ca.mcmaster.selfsplitpseudocostrepo.Constants.*;
import ca.mcmaster.selfsplitpseudocostrepo.client.ClientRequestObject;
import ilog.concert.IloException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
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

/**
 *
 * @author tamvadss
 */
public class Server {
    
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
    
    public static void main(String[] args) throws IOException, IloException {                
        
        ExecutorService executor = null;
        
        //ramp up the MIP and populate the ramp up leaf list
        RampUp rampup = new RampUp ();
        rampup.getFrontier();
        
        
        
        
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
