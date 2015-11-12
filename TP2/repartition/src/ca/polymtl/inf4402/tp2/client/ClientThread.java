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
    private int index;
    private int indexServer;

    public ClientThread (ServerInterface server, ArrayList<String> operations, int index, int indexServer){
        this.server      = server;
        this.operations  = operations;
        this.index       = index;
        this.indexServer = indexServer;
    }

    public ArrayList<Integer> call() {
        try {

            int res = this.server.execute(this.operations);
            //System.out.println("Resultat dans le thread: " + res);

            ArrayList<Integer> list = new ArrayList<Integer>();
            list.add(res);
            list.add(this.index);
            list.add(this.operations.size());
            list.add(this.indexServer);

            return list;
        } catch (RemoteException e) {
            System.out.println("Remote exception dans le thread: " + e.getMessage());
        }
        return null;
    }
}
