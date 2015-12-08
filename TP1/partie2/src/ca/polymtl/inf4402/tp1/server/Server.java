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
import java.security.*;

public class Server implements ServerInterface {
    // hashmap representing the file system
    private HashMap LockedFiles;
    private HashMap Files;

	public static void main(String[] args) {
		Server server = new Server();
		server.run();
	}

	public Server() {
		super();
        LockedFiles   = new HashMap<String, String>();
        Files = new HashMap<String, String>();
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
            case "generateClientId": 
                System.out.println("Generate client ID");
                result = generateClientId();   
                System.out.println(result); 
                break;
            case "list": 
                System.out.println("Call list");
                result = list();   
                break;
            case "syncLocalDir": 
                System.out.println("Call syncLocalDir");
                result = syncLocalDir();   
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
	 * Méthode accessible par RMI.
	 */
	@Override
	public String execute(String methodToCall, String argument1, String argument2) throws RemoteException {
        String result = "";
        switch (methodToCall){
            case "get": 
                System.out.println("call get");
                result = get(argument1, argument2);
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
	public String execute(String methodToCall, String argument1, String argument2, String argument3) throws RemoteException {
        String result = "";
        switch (methodToCall){
            case "lock": 
                System.out.println("call lock");
                result = lock(argument1, argument2, argument3);
                break;
            case "push": 
                System.out.println("call push");
                result = push(argument1, argument2, argument3);
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
		if ( this.Files.get(name) == null ) {
            this.Files.put(name, "");
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

        if (this.Files.isEmpty()){
            res = "0 fichiers";
        } else {
            Iterator it = Files.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pair = (Map.Entry)it.next();
        
                // check if the file is locked
                String lockString =  "\t non verrouille \n";
                if ( LockedFiles.containsKey(pair.getKey())) {
                    String client = LockedFiles.get(pair.getKey()).toString();
                    lockString =   "\t verrouille par " + client + "\n";
                }

                res += pair.getKey() + lockString;
            }
        }
        return res;
    }

    /*
     * Lock a file so that only one user can modify it
     *
     *
     *
     */
    public String lock(String nom, String clientID, String checksumClient) throws RemoteException {
        try {
            String file = this.Files.get(nom).toString();

            // If file is not in the file system end the method
            if ( file == null ) {
                return "1";
            }

            // If file is already lock end the method
            if ( LockedFiles.containsKey(nom)) {
                return "2";
            }

            // calculate md5 of the file
            MessageDigest md  = MessageDigest.getInstance("MD5");
            String checksum   = md.digest(file.getBytes()).toString();

            // Lock the file
            this.LockedFiles.put(nom, clientID);

            this.getState();
            // if checksums are different return the file
            if (checksum != checksumClient) {
                return file;
            } 

        } catch (Exception e){
            System.out.println("MD5 exception" + e.getMessage());
        }
        return "3";
    }

    /*
     * Push file to client
     *
     */
    public String push(String nom, String contenu, String clientID) throws RemoteException {
        String file = this.Files.get(nom).toString();

        // If file is not in the file system end the method
        if ( file == null ) {
            System.out.println("Error file doesnt exists");
            return "Le fichier n existe pas";
        }

        // If file is not already lock end the method
        if ( !LockedFiles.containsKey(nom)) {
            System.out.println("Error file not locked");
            return "Le fichier n est pas verrouille";
        }

        // If file is locked by another client end the operation
        if ( !LockedFiles.get(nom).equals(clientID)) {
            System.out.println("Error file locked by another user");
            return "Le fichier est verrouille par un autre utilisateur";
        }else { // unlock the file
            LockedFiles.remove(nom);
        }

        // update the content of the file
        Files.put(nom, contenu);

        //this.getState();
        return nom + " a ete envoye au serveur";
    }

    /*
     * Get the content of a file if the local version is different
     * of the one on the server
     * return:
     *      - a string containing the new version of the file if the
     *      version are different
     *      - an empty string if the files are the same
     */
    private String get(String nom, String checksumClient) throws RemoteException {
        try{
            String file = this.Files.get(nom).toString();

            // calculate md5 of the file
            MessageDigest md  = MessageDigest.getInstance("MD5");
            String checksum   = md.digest(file.getBytes()).toString();

            // If the client has a different version of the file send it
            if ( checksum != checksumClient ) {
                return file;
            }

        } catch (Exception e){
            System.out.println("MD5 exception" + e.getMessage());
        }

        return "";
        //return null;
    }

    /*
     * list all the files (to be used by syncLocalDir)
     * return:
     *      a string with all the file names separated by ','
     */
	public String syncLocalDir() throws RemoteException {
        String res = "";

        Iterator it = Files.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
    
            res += pair.getKey() + ",";
        }

        return res;
    }

	public String getState() throws RemoteException {
        String res = "";

        Iterator it = Files.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry pair = (Map.Entry)it.next();
    
            // check if the file is locked
            String lockString =  "\t non verrouille \n";
            if ( LockedFiles.containsKey(pair.getKey())) {
                lockString =   "\t verrouille par " + this.LockedFiles.get(pair.getKey()) + " \n";
            }

            res += pair.getKey() + lockString;
            res += "contenu: " + pair.getValue();
        }

        System.out.println("contenu du serveur");
        System.out.println(res); 

        return res;
    }
}

