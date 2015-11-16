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
import java.util.Map;
import java.util.HashMap;
import java.io.*;

import ca.polymtl.inf4402.tp2.shared.ServerInterface;
import ca.polymtl.inf4402.tp2.client.ClientThread;

public class Client {
	public static void main(String[] args) {
		String pathToServersFile = null;
		String pathToOperations  = null;
        String secureMode        = null;

		if (args.length > 1) {
            secureMode         = args[0];
			pathToOperations   = args[1];
			pathToServersFile  = args[2];
		}

		Client client = new Client(pathToOperations, secureMode, pathToServersFile);

		client.run();
	}

	private List<ServerInterface> serversPool   = null;
	private List<Integer> nbAcceptedOperations  = null;
	private String pathToOperations             = null;
	private Boolean secureMode                  = null;

	public Client(String pathToOperations, String secureMode, String pathToServersFile) {
		super();

		if (System.getSecurityManager() == null) {
			System.setSecurityManager(new SecurityManager());
		}

        this.secureMode = false;
        if (secureMode.equals("s"))
            this.secureMode = true;


        // Fichier contenant les operations a executer
        this.pathToOperations = pathToOperations;

        // Pool des serveurs qui effectueront les calculs
        this.serversPool = new ArrayList<ServerInterface>();
        // Liste du nombre d'operations acceptees par chaque serveur
        // en une requete.
        // (En mode securise ces valeurs seront modifiees a la volee)
        this.nbAcceptedOperations = new ArrayList<Integer>();

        // chargement des serveurs
        File file                     = new File("./" + pathToServersFile);
        try{
            if (!file.exists()){
                System.out.println("Le fichier nexiste pas");
            }

            if (file.exists()){
                Scanner scan = new Scanner(file);
                while (scan.hasNextLine()){
                    serversPool.add(loadServerStub( scan.nextLine() ));
                    this.nbAcceptedOperations.add(5);
                }
                scan.close();
            }
        } catch (Exception e){
            System.out.println("Exception dans la lecture du fichier");
        }
        //serversPool.add(loadServerStub("l4712-08.info.polymtl.ca"));
        //serversPool.add(loadServerStub("l4712-09.info.polymtl.ca"));
        //serversPool.add(loadServerStub("l4712-10.info.polymtl.ca"));

        //this.nbAcceptedOperations = new ArrayList<Integer>();
        //this.nbAcceptedOperations.add(5);
        //this.nbAcceptedOperations.add(5);
        //this.nbAcceptedOperations.add(5);
	}

	private void run() {
        if (this.secureMode)
            RepartirCalculsSecurise();
        else
            RepartirCalculsNonSecurise();
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
	private void RepartirCalculsNonSecurise() {
		try {
            ArrayList<String> operations  = this.LireOperationsDepuisFichier();

            int nbOperationsExecutees = 0;
            int nbAcceptedOperations = this.nbAcceptedOperations.get(0);
            int sum = 0;
            ExecutorService pool      = Executors.newFixedThreadPool(3);
            ArrayList<Future<List<Object>>> futures          = new ArrayList<Future<List<Object>>>();

            /*
             * Recuperation du temps courant pour calculer le temps d'execution
             */
            System.out.println("Lancement mode non securise");
            long startTime = System.currentTimeMillis();

            while(nbOperationsExecutees < operations.size()){
                // Creation du chunk doperations a envoyer
                int indexFin = nbOperationsExecutees + nbAcceptedOperations;
                if (indexFin > operations.size())
                    indexFin = operations.size();

                ArrayList<String> chunk = new ArrayList<String>(operations.subList(nbOperationsExecutees, indexFin));
                // remise a zero des futures
                futures.clear();

                //System.out.println("envoi des operations " + nbOperationsExecutees + " a " + ( nbOperationsExecutees + nbAcceptedOperations  ));

                // Creation des threads qui calculeront le chunk sur chaque serveur
                int indexServer = -1;
                for (ServerInterface server : this.serversPool){
                    indexServer++;
                    Callable<List<Object>> callable  = new ClientThread(server, chunk, nbOperationsExecutees, indexServer);
                    // Recuperation du retour de chaque callable
                    Future<List<Object>> future      = pool.submit(callable);
                    futures.add(future);
                }

                // Attente des 3 resultats
                ArrayList<Integer> resultats = new ArrayList<Integer>();
                while (resultats.size() < this.serversPool.size()) {

                    // parcours des differents futures
                    for (int i=0; i<this.serversPool.size(); i++){
                        Future<List<Object>> future = futures.get(i);

                        // si on a recu le resultat du calcul
                        if (future != null  && future.isDone()){
                            //System.out.println("Nouveau resultat (nb resultats: " + resultats.size() + " / " + this.serversPool.size() + ")");
                            //System.out.println("Nombre de futurs: " + futures.size());

                            int resultat                           = (Integer) future.get().get(0);
                            ArrayList<String> operationsExecutees  = (ArrayList<String>) future.get().get(1);
                            indexServer                            = (Integer) future.get().get(2);
                            // quand le calcul na pas pu etre fait
                            if (resultat == -1){
                                //System.out.println("Echec des operations, on les renvoi au serveur " + indexServer);
                                // on renvoit les operations au serveur et on met a jour le Future
                                Callable<List<Object>> callable  = new ClientThread(this.serversPool.get(indexServer), operationsExecutees, nbOperationsExecutees, indexServer);
                                // Recuperation du retour de chaque callable
                                Future<List<Object>> newFuture = pool.submit(callable);
                                futures.set(i, newFuture);
                            }else{
                                //System.out.println("Succes des operations, on obtient: " + resultat);
                                resultats.add(resultat);
                                futures.set(i, null);
                            }
                        }
                    }
                }

                // Hashmap contenant la frequence de chaque resultat
                HashMap<Integer, Integer> frequences = new HashMap<Integer, Integer>();

                // calcul de la frequence d'apparition de chaque resultat
                for (Integer i : resultats){
                    // Si la map contient la valeur on lincremente
                    if (frequences.containsKey(i.intValue())){
                        frequences.put(i.intValue(), frequences.get(i.intValue())+1);
                    }else{
                        // On ajoute la valeur avec 1
                        frequences.put(i.intValue(), 1);
                    }
                }

                // parcours de la map pour recuperer la plus grande frequence
                int resultat = 0;
                int maximum = 0;
                Iterator it = frequences.entrySet().iterator();
                while(it.hasNext()){
                    HashMap.Entry pair = (HashMap.Entry)it.next();
                    Integer val = ( Integer )pair.getValue();
                    Integer key = ( Integer )pair.getKey();

                    if (val > maximum){
                        resultat = key;
                        maximum = val;
                    }
                    it.remove();
                }

                //System.out.println("Resultat choisi: " + resultat);
                sum = (sum + resultat) % 5000;
                // mise a jour de lindex des operations a envoyer
                nbOperationsExecutees = nbOperationsExecutees + nbAcceptedOperations;
            }

            /*
             * Calcul du temps d'execution
             */
            double executionTime = System.currentTimeMillis() - startTime;
            System.out.println("Somme finale: " + sum);
            System.out.println("Execution time: " + (executionTime / 1000) + "s");
            System.exit(0);

        } catch (Exception e){
            System.out.println("Exception dans le client: " + e.getMessage());
            System.out.println(e.getCause());
            e.printStackTrace();
        }
    }

    /*
     * Envoi des operations en mode non securise
     */
	private void RepartirCalculsSecurise() {
		try {

            ArrayList<String> operations  = this.LireOperationsDepuisFichier();

            /*
             * Recuperation du temps courant pour calculer le temps d'execution
             */
            System.out.println("Lancement mode securise");
            long startTime = System.currentTimeMillis();
	
            /*
             * Creation d'un thread par serveur et appel de leur methode call
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
                    // l'ensemble doperation a renvoye un resultat
                    if (future.isDone() ){ 
                        int resultat                           = (Integer) future.get().get(0);
                        ArrayList<String> operationsExecutees  = (ArrayList<String>) future.get().get(1);
                        int indexServer                        = (Integer) future.get().get(2);


                        // l'ensemble doperations a ete calcule avec succes
                        if (resultat != -1){ 
                            // mise a jour de la somme et du nombre doperations executees
                            sum = (sum + resultat) % 5000;
                            nbOperationsExecutees += operationsExecutees.size();

                            // On augmente le nombre doperations a envoyer au serveur la prochaine fois
                            this.nbAcceptedOperations.set(indexServer, this.nbAcceptedOperations.get(indexServer)+2);
                            //System.out.println("Serveur " + indexServer + " " + this.nbAcceptedOperations.get(indexServer) + "(+2)");

                        // l'ensemble doperation na pas pu etre calcule
                        }else{ 
                            // recuperation des operations echouees
                            operationsEchouees.addAll(operationsExecutees);

                            // On diminue le nombre doperations a envoyer au serveur la prochaine fois
                            this.nbAcceptedOperations.set(indexServer, this.nbAcceptedOperations.get(indexServer)-2);
                            if (this.nbAcceptedOperations.get(indexServer) < 2) 
                                this.nbAcceptedOperations.set(indexServer, 2);
                            //System.out.println("Serveur " + indexServer + " " + this.nbAcceptedOperations.get(indexServer) + "(-2)");
                        }

                    // l'ensemble doperation na pas encore renvoye de resultat
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
             * Calcul du temps d'execution
             */
            double executionTime = System.currentTimeMillis() - startTime;
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
     * Lis la liste des operations depuis un fichier et renvoit une arrayList
     */
    private ArrayList<String> LireOperationsDepuisFichier(){
        File file                     = new File("./" + this.pathToOperations);
        ArrayList<String> operations  = new ArrayList<String>();

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
     * Prends une liste d'operations a envoyer aux serveurs distants
     * repartis les operations sur les differents serveurs
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

                // Si l'iteration precedente a lance le calcul de la derniere
                // operation on sort de la boucle while
                if (!continuer)
                    break;

                // On limite l'index des operation a la derniere
                if (indexFin >= operations.size() - 1){
                    indexFin = operations.size() - 1;
                    continuer = false;
                }

                //System.out.println("\tserveur " + indexServer + ": " + (indexFin - index + 1) + "/" + nbOperationsAEnvoyer);

                // creation d'une liste d'operations a envoyer au serveur
                ArrayList<String> subOperations = new ArrayList<String>(operations.subList(index, indexFin + 1));

                // Creation d'un callable avec le bon serveur et la liste d'operation voulue
                Callable<List<Object>> callable  = new ClientThread(server, subOperations, index, indexServer);
                // Recuperation du retour du callable
                Future<List<Object>> future      = pool.submit(callable);
                futures.add(future);

                index = indexFin + 1;
            }
        }
        return futures;
    }
}
