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

    public ArrayList<Object> call() {
        try {

            int res = this.server.execute(this.operations);
            //System.out.println("Resultat dans le thread: " + res);

            ArrayList<Object> list = new ArrayList<Object>();
            list.add(res);
            list.add(this.operations);
            list.add(this.indexServer);

            return list;
        //} catch (RemoteException e) {
        } catch (Exception e) {
            //System.out.println("\nException dans le thread du serveur " + this.indexServer + ":");
            //System.out.println(e.getMessage());

            // Si une exception est lancee par la methode d'appel distant, on renvoit -2 en resultat
            // pour pouvoir traiter le cas dans le Client
            ArrayList<Object> list = new ArrayList<Object>();
            list.add(-2);
            list.add(this.operations);
            list.add(this.indexServer);

            return list;
        }
        //return null;
    }
}
