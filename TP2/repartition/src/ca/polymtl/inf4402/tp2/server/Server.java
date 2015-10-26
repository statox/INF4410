package ca.polymtl.inf4402.tp2.server;

import java.rmi.ConnectException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import java.util.List;
import java.util.ArrayList;

import ca.polymtl.inf4402.tp2.operations.Operations;
import ca.polymtl.inf4402.tp2.shared.ServerInterface;

public class Server implements ServerInterface {

	public static void main(String[] args) {
		Server server = new Server();
		server.run();
	}

	public Server() {
		super();
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
	 * Get a list of operations and return the result
	 */
	@Override
    public int execute(ArrayList<String> operations) throws RemoteException {
        int total = 0;
        //Operations calculator = new Operations();

        for (String s : operations){
            String op   = s.split(" ")[0];
            int number  = Integer.parseInt(s.split(" ")[1]);

            if (op.equals("fib")){
                int toto = 0;
                //toto = calculator.fib(number);
                toto = Operations.fib(number);
                System.out.println("Fibo " + toto);
            }
        }

        return 0;
    }
}
