package ca.polymtl.inf4402.tp2.client;

// Exception personnalisee lancee quand plus aucun serveur ne repond 
public class OutOfServersException extends Exception {
    public OutOfServersException(){
        super("No server is responding");
    }
    public OutOfServersException(String s){
        super(s);
    }
}
