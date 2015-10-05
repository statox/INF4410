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
    private String contentToSend             = null;
    private String pathToIDFile              = null;
    private String UUID                      = null;

	public Client(String command, String argument, String content) {
		super();

        // Initialize members
        this.commandToCall   = command;
        this.argumentToSend  = argument;
        this.contentToSend   = content;
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
                String uid = localServerStub.execute("create");
                System.out.println("uid recupere: " + uid.toString());

                // write uid to new file
                Boolean fileCreated = file.createNewFile();
                PrintWriter out = new PrintWriter(this.pathToIDFile);
                out.println(uid);
                out.close();

                // keep uid in memory
                this.UUID = uid;

            }else { // if the client already has a uid in a file
                this.UUID = new Scanner(file).next();
                System.out.println("UID lu: " + this.UUID);
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
            System.out.println("Liste renvoyee par le seveur:\n" + list);
        } catch (RemoteException e) {
            System.out.println("Erreur: " + e.getMessage());
        } catch (Exception e){
            System.out.println("Local RMI: Erreur: " + e.getMessage());
        }
    }

    private void create(String fileToCreate) {
        try {
            // call method to create a file
            localServerStub.execute("create", fileToCreate);
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
            localServerStub.execute("lock", fileToLock, this.UUID, checksum);
        //} catch (RemoteException e) {
            //System.out.println("Erreur: " + e.getMessage());
        } catch (Exception e){
            System.out.println("Local RMI: Erreur: " + e.getMessage());
        }
    }

    private void push(String fileToLock) {
        try {
            // call method to push a file
            localServerStub.execute("push", fileToLock, this.contentToSend, this.UUID);
        } catch (Exception e){
            System.out.println("Local RMI: Erreur: " + e.getMessage());
        }
    }
}
