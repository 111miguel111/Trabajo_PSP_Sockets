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
    private static int puerto=5556;

    /**
     * Metodo run donde se crea el servidor
     *
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

    public synchronized static void finPartida(String idPartida){
        System.out.println(partidas.get(idPartida));

        partidas.remove(idPartida);
    }

    public synchronized static int generarPuerto(){
        puerto = puerto+2;
        return puerto;
    }
    //cp =1;
    public synchronized static boolean anfitrion(int nJugadores){
        /*
        cp++;
        if (cp==njugadores){
            anfitrion=true;
            cp=1
        }else{
            anfitrion=false
        }
         */

        return anfitrion;
    }
}
