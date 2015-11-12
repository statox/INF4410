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
        this.nbAcceptedOperations.add(5);
        this.nbAcceptedOperations.add(5);
        this.nbAcceptedOperations.add(5);
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
            ArrayList<String> operationsAEnvoyer = new ArrayList<String>();
            operationsAEnvoyer.addAll(operations);
            ArrayList<Future<List<Integer>>> futures  = new ArrayList<Future<List<Integer>>>();
            int nbOperationsExecutees = 0;
            int sum = 0;
        
            // Tant que toutes les operations nont pas ete effectuees on les relance
            while(nbOperationsExecutees < operations.size()){
                System.out.println("\n\nnouvelle iteration (operations executees: " 
                        + nbOperationsExecutees + "/" + operations.size() +")");

                //for (String s : operationsAEnvoyer)
                    //System.out.println(s);

                // envoi de toutes les operations qui nont pas encore reussi
                futures = envoyerOperations(operationsAEnvoyer);

                // contiendra toutes les operations qui nont pas ete executees
                ArrayList<String> operationsEchouees = new ArrayList<String>();

                for (Future<List<Integer>> future : futures) {
                    int resultat      = future.get().get(0);
                    int index         = future.get().get(1);
                    int nbOperations  = future.get().get(2);
                    int indexServer   = future.get().get(3);

                    //System.out.println("Operations " + index + " a " + ( index + nbOperations ) + " = " + resultat);
                    if (resultat != -1){ // lensemble doperations a ete calcule avec succes
                        sum = (sum + resultat) % 5000;
                        nbOperationsExecutees += nbOperations;
                        // On augmente le nombre doperations a envoyer au serveur la prochaine fois
                        this.nbAcceptedOperations.set(indexServer, this.nbAcceptedOperations.get(indexServer)+2);
                        System.out.println("Serveur " + indexServer + " " + this.nbAcceptedOperations.get(indexServer) + "(+2)");
                    }else{ // lensemble doperation na pas pu etre calcule
                        ArrayList<String> aAjouterDansLesEchecs = new ArrayList<String>();
                        aAjouterDansLesEchecs = new ArrayList<String>(operationsAEnvoyer.subList(index, index + nbOperations));
                        operationsEchouees.addAll(aAjouterDansLesEchecs);
                        // On diminue le nombre doperations a envoyer au serveur la prochaine fois
                        this.nbAcceptedOperations.set(indexServer, this.nbAcceptedOperations.get(indexServer)-2);
                        if (this.nbAcceptedOperations.get(indexServer) < 2) 
                            this.nbAcceptedOperations.set(indexServer, 2);
                        System.out.println("Serveur " + indexServer + " " + this.nbAcceptedOperations.get(indexServer) + "(-2)");
                    }
                }
                operationsAEnvoyer.clear();
                operationsAEnvoyer = operationsEchouees;
            }

            System.out.printf("somme des resultats dans le client: " + sum);
            System.exit(0);

        } catch (Exception e){
            System.out.println("Exception dans le client: " + e.getMessage());
        }
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
    private ArrayList<Future<List<Integer>>> envoyerOperations(ArrayList<String> operations) {
        ArrayList<Future<List<Integer>>> futures  = new ArrayList<Future<List<Integer>>>();
        ExecutorService pool      = Executors.newFixedThreadPool(3);

        //System.out.println("\nOperations a envoyer");
        //for (String s : operations)
            //System.out.println(s);
        System.out.println("\n");

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

            // creation dune liste doperations a envoyer au serveur
            ArrayList<String> subOperations = new ArrayList<String>(operations.subList(index, indexFin + 1));

            //System.out.println("Serveur " + indexServer + ": "
                    //+ subOperations.get(0) + "(" + index + ") - "
                    //+ subOperations.get(subOperations.size() - 1) + "(" + indexFin + ")");
            System.out.println("Serveur " + indexServer + ": " + subOperations.size());


            // create a callable with the right server and the new sublist
            Callable<List<Integer>> callable  = new ClientThread(server, subOperations, index, indexServer);
            // get the return of the callable
            Future<List<Integer>> future      = pool.submit(callable);
            futures.add(future);

            index = indexFin + 1;
        }
        return futures;
    }
}
