package ca.polymtl.inf4402.tp2.client;

import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.lang.Math.*;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.Callable;
import java.util.Scanner;
import java.util.List;
import java.util.ArrayList;
import java.io.*;

import ca.polymtl.inf4402.tp2.shared.ServerInterface;
import ca.polymtl.inf4402.tp2.client.ClientThread;

public class Client {
	public static void main(String[] args) {
		String pathToOperations = null;

		if (args.length > 0) {
			pathToOperations = args[0];
		}

		Client client = new Client(pathToOperations);

		client.run();
	}

	private List<ServerInterface> serversPool   = null;
	private List<Integer> nbAcceptedOperations  = null;
	private String pathToOperations             = null;

	public Client(String pathToOperations) {
		super();

		if (System.getSecurityManager() == null) {
			System.setSecurityManager(new SecurityManager());
		}

        // File containing the operations to execute
        this.pathToOperations = pathToOperations;

        // Pool of servers to do the calculations
        this.serversPool = new ArrayList<ServerInterface>();
        serversPool.add(loadServerStub("l4712-08.info.polymtl.ca"));
        serversPool.add(loadServerStub("l4712-09.info.polymtl.ca"));
        serversPool.add(loadServerStub("l4712-10.info.polymtl.ca"));

        // Number of operations accepted by each servers (this list will
        // be modified over the time)
        this.nbAcceptedOperations = new ArrayList<Integer>();
        this.nbAcceptedOperations.add(10);
        this.nbAcceptedOperations.add(10);
        this.nbAcceptedOperations.add(10);
	}

	private void run() {
        RepartirCalculs();
	}

	private ServerInterface loadServerStub(String hostname) {
		ServerInterface stub = null;

		try {
			Registry registry = LocateRegistry.getRegistry(hostname);
			stub = (ServerInterface) registry.lookup("server");
		} catch (NotBoundException e) {
			System.out.println("Erreur: Le nom '" + e.getMessage()
					+ "' n'est pas défini dans le registre.");
		} catch (AccessException e) {
			System.out.println("Erreur: " + e.getMessage());
		} catch (RemoteException e) {
			System.out.println("Erreur: " + e.getMessage());
		}

		return stub;
	}

	private void RepartirCalculs() {
		try {
            /*
             * Read the operations file and keep its content in memory
             */
            File file                     = new File("./" + this.pathToOperations);
            ArrayList<String> operations  = new ArrayList<String>();
            ArrayList<String> repartition  = new ArrayList<String>();

            if (!file.exists()){
                System.out.println("Le fichier nexiste pas");
            }

            if (file.exists()){
                Scanner scan = new Scanner(file);
                while (scan.hasNextLine()){
                    operations.add( scan.nextLine() );
                }
                scan.close();
            }

	
            /*
             * Creation of the thread for each server and call of their method
             */
            ExecutorService pool      = Executors.newFixedThreadPool(3);
            Set<Future<Integer>> set  = new HashSet<Future<Integer>>();

            /*
             *for (ServerInterface server : this.serversPool) {
             *    System.out.println("Creation dun nouveau thread");
             *    Callable<Integer> callable  = new ClientThread(server, operations);
             *    Future<Integer> future      = pool.submit(callable);
             *    set.add(future);
             *}
             */
            int cpt = 0;
            while (cpt != operations.size() - 1){
                for (ServerInterface server : this.serversPool){
                    // Create a sublist of operations to execute
                    int borne = cpt + this.nbAcceptedOperations.get(this.serversPool.indexOf(server));
                    if (borne >= operations.size())
                        borne = operations.size()-1;

                    ArrayList<String> subOperations = new ArrayList<String>(operations.subList(cpt, borne));
                    System.out.println("Server " + this.serversPool.indexOf(server) + " de " + cpt + " a " + borne);
                    cpt = borne;

                    // create a callable with the right server and the new sublist
                    Callable<Integer> callable  = new ClientThread(server, subOperations);
                    // get the return of the callable
                    Future<Integer> future      = pool.submit(callable);
                    set.add(future);
                }
            }

            /*
             * Get the result of each thread and sum it
             */
            int sum = 0;
            System.out.println("Liste des resultats dans le client");
            for (Future<Integer> future : set) {
                System.out.println(future.get());
                sum += future.get();
            }
            System.out.printf("somme des resultats dans le client: " + sum);

            return;

        } catch (Exception e){
            System.out.println("Exception dans le client: " + e.getMessage());
        }
	}
}
