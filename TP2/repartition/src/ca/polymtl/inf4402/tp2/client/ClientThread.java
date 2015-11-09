package ca.polymtl.inf4402.tp2.client;

import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.lang.Math.*;

import java.util.concurrent.Callable;
import java.util.Scanner;
import java.util.List;
import java.util.ArrayList;
import java.io.*;

import ca.polymtl.inf4402.tp2.shared.ServerInterface;

public class ClientThread implements Callable {

    private ServerInterface server;
    private ArrayList<String> operations = null;

    public ClientThread (ServerInterface server, ArrayList<String> operations){
        this.server      = server;
        this.operations  = operations;
    }

    public Integer call() {
        try {

            System.out.println("Hello from a thread!");
            int res = this.server.execute(this.operations);
            System.out.println("Resultat dans le thread: " + res);

            return res;
        } catch (RemoteException e) {
            System.out.println("Remote exception dans le thread: " + e.getMessage());
        }
        return -1;
    }
}
