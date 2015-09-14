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
		String distantHostname = null;

		if (args.length > 0) {
			distantHostname = args[0];
		}

		Client client = new Client(distantHostname);

		if (args.length > 1) {
			client.power = Integer.parseInt(args[1]);
        }else {
			client.power = 1;
        }

		client.run();
	}

	FakeServer localServer = null; // Pour tester la latence d'un appel de
									// fonction normal.
	private ServerInterface localServerStub = null;
	private ServerInterface distantServerStub = null;
    public  Integer power;
    private Integer nbIterations = 500;

	public Client(String distantServerHostname) {
		super();

		if (System.getSecurityManager() == null) {
			System.setSecurityManager(new SecurityManager());
		}

		localServer = new FakeServer();
		localServerStub = loadServerStub("127.0.0.1");

		if (distantServerHostname != null) {
			distantServerStub = loadServerStub(distantServerHostname);
		}
	}

	private void run() {
        appelNormal();

        if (localServerStub != null) {
            appelRMILocal();
        }

        if (distantServerStub != null) {
            appelRMIDistant();
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

	private void appelNormal() {
        try {
		//long start = System.nanoTime();
		//int result = localServer.execute(4, 7);
		//long end = System.nanoTime();
		//System.out.println("Temps écoulé appel normal: " + (end - start) + " ns");
		//System.out.println("Résultat appel normal: " + result);

        // call method several times to get a mean result
        int result = 0;
            byte[] argument = new byte[(int)Math.pow(10, this.power)];
            for (int i=0; i<this.nbIterations; ++i) {
                long start  = System.nanoTime();
                localServer.execute(argument);
                long end    = System.nanoTime();
                result += (end - start)/ this.nbIterations;
            }
            System.out.println("Temps écoulé appel normal (power: " + this.power + "): " + result + " ns");
        }catch (Exception e){
            System.out.println("Local: Erreur: " + e.getMessage());
        }
	}

	private void appelRMILocal() {
		try {
			//long start = System.nanoTime();
            //int result = localServerStub.execute(4, 7);
            //int result = localServerStub.execute(argument);
			//long end = System.nanoTime();

			//System.out.println("Temps écoulé appel RMI local: " + (end - start)
					//+ " ns");
			//System.out.println("Résultat appel RMI local: " + result);

            // call method several times to get a mean result
            int result = 0;
            byte[] argument = new byte[(int)Math.pow(10, this.power)];
            for (int i=0; i<this.nbIterations; ++i) {
                long start  = System.nanoTime();
                localServerStub.execute(argument);
                long end    = System.nanoTime();
                result += (end - start)/ this.nbIterations;
            }
            System.out.println("Temps écoulé appel Local RMI (power: " + this.power + "): " + result + " ns");

		} catch (RemoteException e) {
			System.out.println("Erreur: " + e.getMessage());
		} catch (Exception e){
            System.out.println("Local RMI: Erreur: " + e.getMessage());
        }
	}

	private void appelRMIDistant() {
		try {
			//long start = System.nanoTime();
			//int result = distantServerStub.execute(4, 7);
			//long end = System.nanoTime();

			//System.out.println("Temps écoulé appel RMI distant: "
					//+ (end - start) + " ns");
			//System.out.println("Résultat appel RMI distant: " + result);

            // call method several times to get a mean result
            int result = 0;
            byte[] argument = new byte[(int)Math.pow(10, this.power)];
            for (int i=0; i<this.nbIterations; ++i) {
                long start  = System.nanoTime();
                distantServerStub.execute(argument);
                long end    = System.nanoTime();
                result += (end - start)/ this.nbIterations;
            }
            System.out.println("Temps écoulé appel Distant RMI (power: " + this.power + "): " + result + " ns");
		} catch (RemoteException e) {
			System.out.println("Erreur: " + e.getMessage());
		} catch (Exception e){
            System.out.println("Distant RMI: Erreur: " + e.getMessage());
        }
	}
}
