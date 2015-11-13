package ca.polymtl.inf4402.tp2.server;

import java.rmi.ConnectException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import java.util.List;
import java.util.ArrayList;
import java.lang.Math;

import ca.polymtl.inf4402.tp2.operations.Operations;
import ca.polymtl.inf4402.tp2.shared.ServerInterface;


public class Server implements ServerInterface {

	public static void main(String[] args) {
		String acceptation = null;
        String malicieux   = null;

		if (args.length > 1) {
			acceptation = args[0];
            malicieux   = args[1];
		}

		Server server = new Server(acceptation, malicieux);
		server.run();
	}

    private int acceptation;
    private double malicieux;

	public Server(String acceptation, String malicieux) {
		super();

        System.out.println("arg " + acceptation + "malicieux " + malicieux);
        this.acceptation  = Integer.parseInt(acceptation);
        this.malicieux    = Double.parseDouble(malicieux);
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
    
        // if we have enough resources we return the result
        if (this.canCalculate(operations.size())){
            for (String s : operations){
                String op   = s.split(" ")[0];
                int number  = Integer.parseInt(s.split(" ")[1]);

                double maliceRandom = Math.random();
                //System.out.println("ratio de malice: " + maliceRandom + " : " + ( this.malicieux / 100 ));
                if ( maliceRandom < this.malicieux / 100){
                    total += (Math.random()*100) % 5000;
                    System.out.println("Malicieux(" + number + ")" + total);
                } else if (op.equals("fib")){
                    total += Operations.fib(number) % 5000;
                    System.out.println("Fibo(" + number + ")" + total);
                } else if (op.equals("prime")){
                    total += Operations.prime(number) % 5000;
                    System.out.println("Prime(" + number + ")" + total);
                } else if (op.equals("test")){
                    total += number % 5000;
                    System.out.println("Test(" + number + ")" + total);
                }
            }
        }else{ // if we dont have enough resources we return -1
            total = -1;
            System.out.println("REFUS");
        }

        return total;
    }

    /*
     * Calculate if the server has enough resources to do 
     * a serie of calculations
     */
    public Boolean canCalculate(int u){
        double tauxAcceptation = ((double)u - (double)this.acceptation)/(double)(9* this.acceptation);

        System.out.println("Taux dacceptation: " + tauxAcceptation);

        if (tauxAcceptation <= 0){
            return true;
        } else if (tauxAcceptation > 1){
            return false;
        } else {
            if (Math.random() < tauxAcceptation)
                return true;
        }

        return false;
    }
}
