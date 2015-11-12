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
		String pathToOperations  = null;
        String secureMode        = null;

		if (args.length > 1) {
            secureMode       = args[0];
			pathToOperations = args[1];
		}

		Client client = new Client(pathToOperations, secureMode);

		client.run();
	}

	private List<ServerInterface> serversPool   = null;
	private List<Integer> nbAcceptedOperations  = null;
	private String pathToOperations             = null;
	private Boolean secureMode                  = null;

	public Client(String pathToOperations, String secureMode) {
		super();

		if (System.getSecurityManager() == null) {
			System.setSecurityManager(new SecurityManager());
		}

        this.secureMode = false;
        if (secureMode.equals("s"))
            this.secureMode = true;


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
        this.nbAcceptedOperations.add(5);
        this.nbAcceptedOperations.add(5);
        this.nbAcceptedOperations.add(5);
	}

	private void run() {
        if (this.secureMode)
            RepartirCalculsSecurise();
        else
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

    /*
     * Envoi des operations en mode non securise
     */
	private void RepartirCalculsSecurise() {
		try {
            ArrayList<String> operations  = this.LireOperationsDepuisFichier();

            /*
             * Get current time to calculate execution time
             */
            long startTime = System.currentTimeMillis();

            while(){

            }

        } catch (Exception e){
            System.out.println("Exception dans le client: " + e.getMessage());
            System.out.println(e.getCause());
            e.printStackTrace();
        }
    }

    /*
     * Envoi des operations en mode non securise
     */
	private void RepartirCalculs() {
		try {

            ArrayList<String> operations  = this.LireOperationsDepuisFichier();

            /*
             * Get current time to calculate execution time
             */
            long startTime = System.currentTimeMillis();
	
            /*
             * Creation of the thread for each server and call of their method
             */
            ArrayList<String> operationsAEnvoyer = new ArrayList<String>();
            operationsAEnvoyer.addAll(operations);
            ArrayList<Future<List<Object>>> futures  = new ArrayList<Future<List<Object>>>();
            int nbOperationsExecutees = 0;
            int sum = 0;
        
            // Envoi une premiere fois de toutes les operations
            futures = envoyerOperations(operationsAEnvoyer);

            // Tant que toutes les operations nont pas ete effectuees on les relance
            while(nbOperationsExecutees < operations.size()){

                // contiendra les resultats des operations echouees ou non terminees pour la prochaine iteration
                ArrayList<Future<List<Object>>> nextFutures  = new ArrayList<Future<List<Object>>>();

                // contiendra toutes les operations qui nont pas ete executees
                ArrayList<String> operationsEchouees = new ArrayList<String>();

                for (Future<List<Object>> future : futures) {
                    // lensemble doperation a renvoye un resultat
                    if (future.isDone() ){ 
                        int resultat                           = (Integer) future.get().get(0);
                        ArrayList<String> operationsExecutees  = (ArrayList<String>) future.get().get(1);
                        int indexServer                        = (Integer) future.get().get(2);

                        System.out.println("Operations " + operationsExecutees.size() + " = " + resultat);
                        for(String s : operationsExecutees)
                            System.out.println(s);

                        // lensemble doperations a ete calcule avec succes
                        if (resultat != -1){ 
                            // mise a jour de la somme et du nombre doperations executees
                            sum = (sum + resultat) % 5000;
                            nbOperationsExecutees += operationsExecutees.size();

                            // On augmente le nombre doperations a envoyer au serveur la prochaine fois
                            this.nbAcceptedOperations.set(indexServer, this.nbAcceptedOperations.get(indexServer)+2);
                            System.out.println("Serveur " + indexServer + " " + this.nbAcceptedOperations.get(indexServer) + "(+2)");

                        // lensemble doperation na pas pu etre calcule
                        }else{ 
                            // recuperation des operations echouees
                            operationsEchouees.addAll(operationsExecutees);

                            // On diminue le nombre doperations a envoyer au serveur la prochaine fois
                            this.nbAcceptedOperations.set(indexServer, this.nbAcceptedOperations.get(indexServer)-2);
                            if (this.nbAcceptedOperations.get(indexServer) < 2) 
                                this.nbAcceptedOperations.set(indexServer, 2);
                            System.out.println("Serveur " + indexServer + " " + this.nbAcceptedOperations.get(indexServer) + "(-2)");
                        }

                    // lensemble doperation na pas encore renvoye de resultat
                    } else { 
                       nextFutures.add(future); 
                    }
                }
                operationsAEnvoyer.clear();
                operationsAEnvoyer.addAll(operationsEchouees);

                // purge des futures
                futures.clear();
                // envoi des operations echouees
                futures = envoyerOperations(operationsEchouees);
                // ajout des operations non terminees
                futures.addAll(nextFutures);
            }

            /*
             * Calculate execution time
             */
            long executionTime = System.currentTimeMillis() - startTime;
            System.out.println("Execution time: " + (executionTime / 1000) + "s");


            System.out.printf("somme des resultats dans le client: " + sum);
            System.exit(0);

        } catch (Exception e){
            System.out.println("Exception dans le client: " + e.getMessage());
            System.out.println(e.getCause());
            e.printStackTrace();
        }
	}

    /*
     * Lis la list des operations depuis un fichier et renvoit une arrayList
     */
    private ArrayList<String> LireOperationsDepuisFichier(){
        /*
         * Read the operations file and keep its content in memory
         */
        File file                     = new File("./" + this.pathToOperations);
        ArrayList<String> operations  = new ArrayList<String>();
        //ArrayList<String> repartition  = new ArrayList<String>();

        try{
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
        } catch (Exception e){
            System.out.println("Exception dans la lecture du fichier");
        }
        
        return operations;
    }

    /*
     * Prends une list d'operations a envoyer aux serveurs distants
     * repartis les oparations sur les differents serveurs
     *
     * Retourne: Une liste de futures contenant
     *              - Le resultat des operations (peut valoir -1)
     *              - lindice de la premiere operation
     *              - Le nombre doperations effectuees
     */
    private ArrayList<Future<List<Object>>> envoyerOperations(ArrayList<String> operations) {
        ArrayList<Future<List<Object>>> futures  = new ArrayList<Future<List<Object>>>();
        ExecutorService pool      = Executors.newFixedThreadPool(3);


        if (!operations.isEmpty()){
            //System.out.println("\tEnvoyer operations: " + operations.size());

            int index          = 0;
            int indexServer    = -1;
            boolean continuer  = true;
            while(continuer){
                indexServer               = (indexServer+1)%3;
                ServerInterface server    = this.serversPool.get(indexServer);
                int nbOperationsAEnvoyer  = this.nbAcceptedOperations.get(indexServer);
                int indexFin              = index - 1 + nbOperationsAEnvoyer;

                // Si literation precedente a lance le calcul de la derniere
                // operation on sort de la boucle while
                if (!continuer)
                    break;

                // On limite lindex des operation a la derniere
                if (indexFin >= operations.size() - 1){
                    indexFin = operations.size() - 1;
                    continuer = false;
                }

                //System.out.println("\tserveur " + indexServer + ": " + (indexFin - index + 1) + "/" + nbOperationsAEnvoyer);

                // creation dune liste doperations a envoyer au serveur
                ArrayList<String> subOperations = new ArrayList<String>(operations.subList(index, indexFin + 1));

                // create a callable with the right server and the new sublist
                Callable<List<Object>> callable  = new ClientThread(server, subOperations, index, indexServer);
                // get the return of the callable
                Future<List<Object>> future      = pool.submit(callable);
                futures.add(future);

                index = indexFin + 1;
            }
        }
        return futures;
    }
}
