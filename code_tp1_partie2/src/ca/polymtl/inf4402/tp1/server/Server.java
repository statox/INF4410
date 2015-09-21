package ca.polymtl.inf4402.tp1.server;

import java.rmi.ConnectException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import ca.polymtl.inf4402.tp1.shared.ServerInterface;

import java.util.UUID;
import java.util.HashMap;

public class Server implements ServerInterface {
    // hashmap representing the file system
    private Map LockedFiles;
    private Map UnlockedFiles;

	public static void main(String[] args) {
		Server server = new Server();
		server.run();
	}

	public Server() {
		super();
        LockedFiles   = New HashMap<string, string>();
        UnlockedFiles = New HashMap<string, string>();
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
	 * generate a unique id for the client to save in a local file
	 */
	@Override
	public UUID generateClientId() throws RemoteException {
		return java.util.UUID.randomUUID();
	}

    /*
     * Create a new unlocked file.
     * return:
     *      True  if the file was created
     *      False if the creation failed
     */
	@Override
	public Boolean create(string name) throws RemoteException {
		if ( LockedFiles.get(name) == null && UnlockedFiles.get(name) == null ) {
            UnlockedFiles.add(name, "");
            return True;
        }
        return False;
	}

    /*
     * list the files locked and unlocked
     * return:
     *      a string representing the file system
     */
	@Override
	public String list() throws RemoteException {
        String res = ""

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
	@Override
	public String get(String nom, String checksum) throws RemoteException {
        //String file = 
    }
}

