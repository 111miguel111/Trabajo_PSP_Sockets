package org.educa.game;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.Semaphore;

public class Server {

    private static Map<String,String> partidas;
    public static Semaphore[] dados={new Semaphore(1),new Semaphore(1)};
    private static boolean anfitrion=false;
    public static int puerto=5556;
    public static int puertoPartida=0;
    private static int cp =1;

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
    private void crearGrupos(Boolean anfitrion){
        try {
        if(anfitrion){
            Server.dados[0].acquire();
        }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
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
