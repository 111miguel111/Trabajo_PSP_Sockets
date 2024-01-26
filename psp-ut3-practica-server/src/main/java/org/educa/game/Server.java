package org.educa.game;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Server {
    private static boolean host = false;//Variable statica encargada de decirnos si el cliente es o no anfitrion
    protected static int port = 5556;//Variable statica encargada de decirnos el puerto del cliente
    protected static ArrayList<String> players = new ArrayList<>();//Variable statica encargada de guardar las parejas de clientes anfitrion, invitado
    protected static ArrayList<String> couples = new ArrayList<>();//Variable statica encargada de guardar los clientes hasta asignarles una pareja
    private static int cp = 1;//Variable statica encargada de ayudarnos a saber cual es el jugador actual y asi dependiendo del modo de juego crear tantos invitados como necesitemos y un anfitrion
    private static int count = 0;//Variable statica encargada de poner nombres diferentes a cada partida

    /**
     * Metodo run donde se crea el servidor
     */
    public void run() {
        System.out.println("Creando socket servidor");
        Socket newSocket = null;
        try (ServerSocket serverSocket = new ServerSocket()) {
            System.out.println("Realizando el bind");
            InetSocketAddress addr = new InetSocketAddress("localhost", 5554);
            //Asigna el socket a una dirección y puerto
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
            System.out.println("El servidor ha fallado al iniciarse.");
            //e.printStackTrace();
        } finally {
            try {
                if (newSocket != null) {
                    newSocket.close();
                }
            } catch (IOException e) {
                //e.printStackTrace();
            }
        }
    }


    /**
     * Metodo sincronizado para asignar puertos a los diferentes jugadores
     *
     * @return devuelve el puerto
     */
    protected synchronized static int generatePort() {
        port = port + 1;
        return port;
    }

    /**
     * Metodo para crear los jugadores con su respectivo puerto, y las parejas para cada partida
     *
     * @param data recibe los datos de los jugadores para añadirlos a su array
     */
    protected synchronized static void savePlayersAndCouples(String data) {
        players.add(data);
        String host = "";
        String guest = "";
        //Recorremos la lista de jugadores en busca de un anfitrion e invitado
        for (int i = 0; i < players.size(); i++) {
            if (players.get(i).contains("true")) {
                host = players.get(i);
            } else {
                guest = players.get(i);
            }
        }
        //Si hemos encontrado un anfitrion e invitado lo meteremos en la lista de parejas y los quitaremos de la lista de jugadores
        if (!host.isEmpty() && !guest.isEmpty()) {
            players.remove(host);
            players.remove(guest);
            count = count + 1;
            couples.add("Partida" + count + "," + host + "," + guest);
        }
    }

    /**
     * Metodo sincronizado encargado de dar la informacion de la partida en la que esta un cliente
     *
     * @param port El puerto del cliente para saber en que partida se encuentra
     * @return La informacion de la partida, encaso de no tener una partida asignada devolveremos null.
     */
    protected synchronized static String startingData(String port) {
        //Recorremos las partidas en busca de una en la que coincida el puerto del jugador
        for (int i = 0; i < couples.size(); i++) {
            if (couples.get(i).contains(port)) {
                return couples.get(i);
            }
        }
        return null;
    }

    /**
     * Metodo sincronizado donde se asigna ser anfitrion del juego o no, tenga los jugadores que tenga
     *
     * @param nPlayers recibe el numero de jugadores que tiene el juego
     * @return devuelve si se es anfitrion o no
     */
    protected synchronized static boolean isHost(int nPlayers) {
        //Si el cp(currentPlayer) es igual al numero de jugadores del juego, se le hace anfitrion
        if (cp == nPlayers) {
            host = true;
            cp = 0;
        } else { //si no, no es anfitrion
            host = false;
        }
        cp++;
        return host;
    }

    /**
     * metodo para finalizar la partida y eliminarla del server
     *
     * @param idGame recibe el id de la partida
     */
    protected synchronized static void endGame(String idGame) {
        //Recorremos las partidas en busa de la que tenga el mismo id para eliminarla
        for (int i = 0; i < couples.size(); i++) {
            if (couples.get(i).contains(idGame)) {
                couples.remove(i);
                System.out.println(idGame + " eliminada del servidor");
            }
        }
    }

}
