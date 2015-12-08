package ca.polymtl.inf4402.tp1.client;

import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.lang.Math.*;

import java.util.UUID;
import java.util.Scanner;
import ca.polymtl.inf4402.tp1.shared.ServerInterface;
import java.io.*;
import java.security.*;

import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;

public class Client {
	public static void main(String[] args) {

		if (args.length == 1) {
            Client client = new Client(args[0]);
            client.run();
            return;
		}
        else if (args.length == 2) {
            Client client = new Client(args[0], args[1]);
            client.run();
            return;
		}
        else if (args.length == 3) {
            Client client = new Client(args[0], args[1], args[2]);
            client.run();
            return;
		}
        else {
            System.out.println("Invalid number of arguments");
            return;
        }

	}

	private ServerInterface localServerStub  = null;
	private String commandToCall             = null;
    private String argumentToSend            = null;
    private String pathToIDFile              = null;
    private String UUID                      = null;

	public Client(String command, String argument, String content) {
		super();

        // Initialize members
        this.commandToCall   = command;
        this.argumentToSend  = argument;
        this.pathToIDFile    = "./ID.file";
        this.UUID            = "";

		if (System.getSecurityManager() == null) {
			System.setSecurityManager(new SecurityManager());
		}

        // get local server
		localServerStub = loadServerStub("127.0.0.1");
	}

	public Client(String command, String argument) {
        this(command, argument, "");
    }
	public Client(String command) {
        this(command, "", "");
    }

	private void run() {
        generateClientID();

        switch (this.commandToCall){
            case "create": 
                create(this.argumentToSend);
                break;
            case "list": 
                list(); 
                break;
            case "lock": 
                lock(this.argumentToSend);
                break;
            case "push": 
                push(this.argumentToSend);
                break;
            case "get": 
                get(this.argumentToSend);
                break;
            case "syncLocalDir": 
                syncLocalDir();
                break;
            default:
                System.out.println("Wrong argument");
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

    private void generateClientID(){
        try {

            File file = new File(this.pathToIDFile); 

            // if the client doesnt have a UID already existing
            if (!file.exists()){
                // call method to get UUID from server
                String uid = localServerStub.execute("generateClientId");
                //System.out.println("uid recupere: " + uid.toString());

                // write uid to new file
                Boolean fileCreated = file.createNewFile();
                PrintWriter out = new PrintWriter(this.pathToIDFile);
                out.println(uid);
                out.close();

                // keep uid in memory
                this.UUID = uid;

            }else { // if the client already has a uid in a file
                this.UUID = new Scanner(file).next();
                //System.out.println("UID lu: " + this.UUID);
            }

        } catch (RemoteException e) {
            System.out.println("Erreur: " + e.getMessage());
        } catch (Exception e){
            System.out.println("Local RMI: Erreur: " + e.getMessage());
        }
    }

    private void list() {
        try {
            // call method to get file list
            String list = localServerStub.execute("list");
            System.out.println(list);
        } catch (RemoteException e) {
            System.out.println("Erreur: " + e.getMessage());
        } catch (Exception e){
            System.out.println("Local RMI: Erreur: " + e.getMessage());
        }
    }

    private void create(String fileToCreate) {
        try {
            // call method to create a file
            Boolean result = localServerStub.execute("create", fileToCreate);
            if (result){
                System.out.println(fileToCreate + " cree avec succes");
            } else {
                System.out.println("erreur a la creation de " + fileToCreate);
            }
        } catch (RemoteException e) {
            System.out.println("Erreur: " + e.getMessage());
        } catch (Exception e){
            System.out.println("Local RMI: Erreur: " + e.getMessage());
        }
    }

    private void lock(String fileToLock) {
        try {
            // calculate md5 of the file
            MessageDigest md  = MessageDigest.getInstance("MD5");
            String checksum   = md.digest(fileToLock.getBytes()).toString();

            // call method to lock a file
            String result = localServerStub.execute("lock", fileToLock, this.UUID, checksum);
            switch (result) {
                case "1":
                    System.out.println("Le fichier n existe pas");
                    break;
                case "2":
                    System.out.println("Le fichier est verrouille par un autre utilisateur");
                    break;
                case "3":
                    System.out.println("Le fichier n existe pas");
                    break;
                default:
                    System.out.println(fileToLock + " verrouille");
                    break;
            }
        } catch (Exception e){
            System.out.println("Local RMI: Erreur: " + e.getMessage());
        }
    }

    private void push(String fileToPush) {
        try {
            File file = new File("./" + fileToPush); 
            String content = "";
            // Read the local file
            if (file.exists()){
                Scanner scan = new Scanner(file);
                while (scan.hasNextLine()){
                    content += scan.nextLine() + "\n";
                }
                scan.close();
            }

            // call method to push a file
            String result = localServerStub.execute("push", fileToPush, content, this.UUID);
            System.out.println(result);
        } catch (Exception e){
            System.out.println("Local RMI: Erreur: " + e.getMessage());
        }
    }

    private void get(String fileToGet) {
        try {
            File file = new File("./" + fileToGet); 
            String content = "";
            String checksum = "-1";

            // create non existing file
            if (!file.exists()){
                // write uid to new file
                Boolean fileCreated  = file.createNewFile();
                PrintWriter out      = new PrintWriter("./" + fileToGet);
                out.println("");
                out.close();
            }

            // Read the local file
            if (file.exists()){
                Scanner scan = new Scanner(file);
                while (scan.hasNextLine()){
                    content += scan.nextLine() + "\n";
                }
                scan.close();

                //System.out.println("Contenu du fichier"); 
                //System.out.println(content); 

                // calculate md5 of the file
                MessageDigest md  = MessageDigest.getInstance("MD5");
                checksum   = md.digest(content.getBytes()).toString();
            }

            //System.out.println("Checksum: " + checksum); 

            // call method to get a file
            String newFile = localServerStub.execute("get", fileToGet, checksum);

            // if the file has changed add it to the local directory
            if (!newFile.isEmpty()){
                System.out.println(fileToGet + " synchronise");

                // write updated content to file
                PrintWriter out = new PrintWriter("./" + fileToGet);
                out.println(newFile);
                out.close();
            }else{
                System.out.println(fileToGet + " deja synchronise");
            }

        } catch (Exception e){
            System.out.println("Local RMI: Erreur: " + e.getMessage());
        }
    }

    private void syncLocalDir() {
        try{
            // Get the file names
            String[] files = localServerStub.execute("syncLocalDir").split(",");

            for (String file : files){
                System.out.println("Get " + file);
                this.get(file);
            }

        } catch (Exception e){
            System.out.println("Local RMI: Erreur: " + e.getMessage());
        }
    }

}
