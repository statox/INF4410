package ca.polymtl.inf4402.tp1.shared;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ServerInterface extends Remote {
	//int execute(int a, int b) throws RemoteException;
	//void execute(byte[] arg) throws RemoteException;
	String execute(String methodToCall) throws RemoteException;
	Boolean execute(String methodToCall, String argumentToUse) throws RemoteException;
	String execute(String methodToCall, String argument1, String argument2, String argument3) throws RemoteException;
}
