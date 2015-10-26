package ca.polymtl.inf4402.tp2.shared;

import java.rmi.Remote;
import java.rmi.RemoteException;

import java.util.ArrayList;

import ca.polymtl.inf4402.tp2.operations.Operations;

public interface ServerInterface extends Remote {
	int execute(ArrayList<String> operations) throws RemoteException;
}
