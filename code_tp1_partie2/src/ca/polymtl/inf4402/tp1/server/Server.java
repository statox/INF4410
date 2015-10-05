package ca.polymtl.inf4402.tp1.server;

import java.rmi.ConnectException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import ca.polymtl.inf4402.tp1.shared.ServerInterface;

import java.util.UUID;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;

public class Server implements ServerInterface {
    // hashmap representing the file system
    private HashMap LockedFiles;
    private HashMap UnlockedFiles;

	public static void main(String[] args) {
		Server server = new Server();
		server.run();
	}

	public Server() {
		super();
        LockedFiles   = new HashMap<String, String>();
        UnlockedFiles = new HashMap<String, String>();
    }

	private void run() {
		if (System.getSecurityManager() == null) {
			System.setSecurityManager(new SecurityManager());
		}

		try {
			ServerInterface stub = (ServerInterface) UnicastRemoteObject
					.exportObject(this, 0);

			Registry registry = LocateRegistry.getRegistry();
			registry.rebind("server", stub);
			System.out.println("Server ready.");
		} catch (ConnectException e) {
			System.err
					.println("Impossible de se connecter au registre RMI. Est-ce que rmiregistry est lancé ?");
			System.err.println();
			System.err.println("Erreur: " + e.getMessage());
		} catch (Exception e) {
			System.err.println("Erreur: " + e.getMessage());
		}
	}

	/*
	 * Méthode accessible par RMI.
	 */
	@Override
	public String execute(String methodToCall) throws RemoteException {
        String result = "";
        switch (methodToCall){
            case "list": 
                System.out.println("Call list");
                result = list();   
                break;
            default:
                System.out.println("No method found");
                break;
        }
        return result;
    }

	/*
	 * Méthode accessible par RMI.
	 */
	@Override
	public Boolean execute(String methodToCall, String argumentToUse) throws RemoteException {
        Boolean result = false;
        switch (methodToCall){
            case "create": 
                System.out.println("call create");
                result = create(argumentToUse);
                break;
            default:
                System.out.println("No method found");
                break;
        }
        return result;
    }

	/*
	 * generate a unique id for the client to save in a local file
	 */
	public String generateClientId() throws RemoteException {
		return java.util.UUID.randomUUID().toString();
	}

    /*
     * Create a new unlocked file.
     * return:
     *      True  if the file was created
     *      False if the creation failed
     */
	public Boolean create(String name) throws RemoteException {
		if ( this.LockedFiles.get(name) == null && this.UnlockedFiles.get(name) == null ) {
            this.UnlockedFiles.put(name, "");
            return true;
        }
        return false;
	}

    /*
     * list the files locked and unlocked
     * return:
     *      a string representing the file system
     */
	public String list() throws RemoteException {
        String res = "";

        Iterator it = UnlockedFiles.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            res += pair.getKey() + "\t non verrouille \n";
        }
        it = LockedFiles.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
            res += pair.getKey() + "\t verrouille \n";
        }

        return res;
    }
    /*
     * Get the content of a file if the local version is different
     * of the one on the server
     * return:
     *      - a string containing the new version of the file if the
     *      version are different
     *      - an empty string if the files are the same
     */
	public String get(String nom, String checksum) throws RemoteException {
        //String file = 
        return "";
    }
}

