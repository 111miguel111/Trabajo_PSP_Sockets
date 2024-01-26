package org.educa.game;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Server {
    private static boolean host =false;
    public static int port =5556;
    public static ArrayList<String> players =new ArrayList<>();
    public static ArrayList<String> couples =new ArrayList<>();
    private static int cp =1;
    private static int count =0;

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
                Thread thread = new Thread(p);
                thread.start();
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
     * Metodo sincronizado para asignar puertos a los diferentes jugadores
     * @return devuelve el puerto
     */
    public synchronized static int generatePort(){
        port = port +1;
        return port;
    }

    /**
     * metodo para crear los jugadores con su respectivo puerto, y las parejas para cada partida
     * @param data recibe los datos de los jugadores para añadirlos a su array
     */
    public synchronized static void savePlayersAndCouples(String data){
        players.add(data);
        String host="";
        String guest="";
        for(int i = 0; i< players.size(); i++){
            if(players.get(i).contains("true")){
                host= players.get(i);
            }else {
                guest= players.get(i);
            }
        }
        if(!host.isEmpty() && !guest.isEmpty()){
            players.remove(host);
            players.remove(guest);
            count = count +1;
            couples.add("Partida"+ count +","+host+","+guest);
        }
    }
    public synchronized static String startingData(String port){
        for(int i = 0; i< couples.size(); i++){
            if(couples.get(i).contains(port)){
                return couples.get(i);
            }
        }
        return null;
    }

    /**
     * Metodo sincronizado donde se asigna ser anfitrion del juego o no, tenga los jugadores que tenga
     * @param nPlayers recibe el numero de jugadores que tiene el juego
     * @return devuelve si se es anfitrion o no
     */
    public synchronized static boolean isHost(int nPlayers){
        //si el cp(currentPlayer) es igual al numero de jugadores del juego, se le hace anfitrion
        if (cp==nPlayers){
            host =true;
            cp=0;
        }else{ //si no, no es anfitrion
            host =false;
        }
        cp++;
        return host;
    }

    /**
     * metodo para finalizar la partida y eliminarla del server
     * @param idGame recibe el id de la partida
     */
    public synchronized static void endGame(String idGame) {
        for(int i = 0; i< couples.size(); i++) {
            if(couples.get(i).contains(idGame)) {
                couples.remove(i);
                System.out.println(idGame+" eliminada del servidor");
            }
        }
    }

}
