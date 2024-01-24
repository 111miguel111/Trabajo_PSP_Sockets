package org.educa.game;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Semaphore;

public class Server {
    public static Map<String,String> partidas=new HashMap<String, String>();
    public static Semaphore[] dados={new Semaphore(1),new Semaphore(1)};
    private static boolean anfitrion=false;
    public static int puerto=5556;
    public static ArrayList<String> jugadores=new ArrayList<>();
    public static ArrayList<String> parejas=new ArrayList<>();
    private static int cp =1;
    private static int contador=0;

    /**
     * Metodo run donde se crea el servidor
     */
    public void run() {
        System.out.println("Creando socket servidor");
        Socket newSocket = null;
        try (ServerSocket serverSocket = new ServerSocket()) {
            System.out.println("Realizando el bind");
            InetSocketAddress addr = new InetSocketAddress("localhost", 5554);
            // asigna el socket a una dirección y puerto
            serverSocket.bind(addr);
            System.out.println("Aceptando conexiones");
            //Acepta las conexiones que le vayan entrando,
            //y crea hilos donde darle respuesta a las conexiones
            while (true) {
                newSocket = serverSocket.accept();
                System.out.println("Conexion recibida");
                Request p = new Request(newSocket);
                Thread hilo = new Thread(p);
                hilo.start();
                System.out.println("Esperando nueva conexión");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (newSocket != null) {
                    newSocket.close();
                }
            }catch(IOException e){
                e.printStackTrace();
            }
        }
    }


    /**
     * Metodo para crear los grupos de las partidas
     * @param anfitrion recibe si se es o no anfitrion
     */
    private void informarAnfitrion(Boolean anfitrion){

    }

    private void informarCliente(){

    }

    /**
     * Metodo sincronizado para borrar una partida del hashMap
     * @param idPartida recibe el id de la partida -> clave del hashMap
     */
    public synchronized static void finPartida(String idPartida){
        System.out.println(partidas.get(idPartida));

        partidas.remove(idPartida);
    }

    /**
     * Metodo sincronizado para asignar puertos a los diferentes jugadores
     * @return devuelve el puerto
     */
    public synchronized static int generarPuerto(){

        puerto=puerto+1;
        return puerto;
    }
    public synchronized static int generarIdPartida(boolean anfitrion){
        contador = contador+1;
        return contador;
    }
    public synchronized static void guardarJugadoresYCrearParejas(String datos){
        jugadores.add(datos);
        String anfitrion="";
        int posA=-1;
        String invitado="";
        int posI=-1;
        for(int i=0;i<jugadores.size();i++){
            if(jugadores.get(i).contains("true")){
                anfitrion= jugadores.get(i);
                posA=i;
            }else {
                invitado= jugadores.get(i);
                posI=i;
            }
        }
        if(!(anfitrion.isEmpty() || invitado.isEmpty())){
            jugadores.remove(posA);
            jugadores.remove(posI);
            contador = contador+1;
            parejas.add("Partida"+contador+","+anfitrion+","+invitado);
        }
    }
    public synchronized static String datosPartida(String puerto){
        for(int i=0;i<parejas.size();i++){
            if(parejas.get(i).contains(puerto)){
                return parejas.get(i);
            }
        }
        return null;
    }

    /**
     * Metodo sincronizado donde se asigna ser anfitrion del juego o no, tenga los jugadores que tenga
     * @param nJugadores recibe el numero de jugadores que tiene el juego
     * @return devuelve si se es anfitrion o no
     */
    public synchronized static boolean anfitrion(int nJugadores){
        //si el cp(currentPlayer) es igual al numero de jugadores del juego, se le hace anfitrion
        if (cp==nJugadores){
            anfitrion=true;
            cp=0;
        }else{ //si no, no es anfitrion
            anfitrion=false;
        }
        cp++;
        return anfitrion;
    }
}
