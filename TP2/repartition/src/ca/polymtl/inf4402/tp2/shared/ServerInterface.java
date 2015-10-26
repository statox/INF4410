package ca.polymtl.inf4402.tp2.shared;

import java.rmi.Remote;
import java.rmi.RemoteException;

import java.util.ArrayList;

public interface ServerInterface extends Remote {
	int execute(ArrayList<String> operations) throws RemoteException;
}
