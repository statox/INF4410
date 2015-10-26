package ca.polymtl.inf4402.tp2.client;

import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.lang.Math.*;

import java.util.Scanner;
import java.util.List;
import java.util.ArrayList;
import java.io.*;

import ca.polymtl.inf4402.tp2.shared.ServerInterface;

public class Client {
	public static void main(String[] args) {
		String pathToOperations = null;

		if (args.length > 0) {
			pathToOperations = args[0];
		}

		Client client = new Client(pathToOperations);

		client.run();
	}

	private List<ServerInterface> serversPool  = null;
	private String pathToOperations            = null;

	public Client(String pathToOperations) {
		super();

		if (System.getSecurityManager() == null) {
			System.setSecurityManager(new SecurityManager());
		}

        // File containing the operations to execute
        this.pathToOperations = pathToOperations;

        // Pool of servers to do the calculations
        this.serversPool = new ArrayList<ServerInterface>();
		serversPool.add(loadServerStub("127.0.0.1"));
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
            // Read the operations file and keep its content in memory
            File file                = new File("./" + this.pathToOperations);
            ArrayList<String> operations  = new ArrayList<String>();

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


            serversPool.get(0).execute(operations);

		//} catch (RemoteException e) {
			//System.out.println("Erreur: " + e.getMessage());
		} catch (Exception e){
            System.out.println("Distant RMI: Erreur: " + e.getMessage());
        }
	}
}
