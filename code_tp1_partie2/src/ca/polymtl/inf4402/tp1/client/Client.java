package ca.polymtl.inf4402.tp1.client;

import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.lang.Math.*;

import ca.polymtl.inf4402.tp1.shared.ServerInterface;

public class Client {
	public static void main(String[] args) {

		if (args.length > 0) {
            Client client = new Client(arg[0]);
            client.run();
		}
		if (args.length > 1) {
            Client client = new Client(arg[0], arg[1]);
            client.run();
		}

	}

	private ServerInterface localServerStub = null;
	private String commandToCall   = null;
    private String argumentToSend  = null;
    private String pathToIDFile    = null;

	public Client(String command, String argument) {
		super();

        // Initialize members
        this.commandToCall   = command;
        this.argumentToSend  = argument;
        this.pathToIDFile    = "./ID.file";

		if (System.getSecurityManager() == null) {
			System.setSecurityManager(new SecurityManager());
		}

        // get local server
		localServerStub = loadServerStub("127.0.0.1");
	}

	private void run() {
        switch (this.commandToCall){
            case "create": 
                
                break;
            case "list": 
                
                break;
            case "syncLocalDir": 
                
                break;
            case "get": 
                
                break;
            case "lock": 
                
                break;
            case "push": 
                
                break;
            default:
                break;
        }
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

    private generateClientID(){

    }

    private void list() {
        try {
            // call method several times to get a mean result
            localServerStub.execute(argument);
        } catch (RemoteException e) {
            System.out.println("Erreur: " + e.getMessage());
        } catch (Exception e){
            System.out.println("Local RMI: Erreur: " + e.getMessage());
        }
    }
