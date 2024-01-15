package org.educa.game;

public class Server {

    private Map <String> partidas;

    public void run() { //TODO datagram
        System.out.println("Creando socket servidor");
        Socket newSocket = null;
        try (ServerSocket serverSocket = new ServerSocket()) {
            System.out.println("Realizando el bind");
            InetSocketAddress addr = new InetSocketAddress("localhost", 5555);
            // asigna el socket a una dirección y puerto
            serverSocket.bind(addr);
            System.out.println("Aceptando conexiones");
            aceptarLlamadas();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (newSocket != null) {
                newSocket.close();
            }
        }
    }

    private void aceptarLlamadas(){
        while (true) {
            newSocket = serverSocket.accept();
            System.out.println("Conexion recibida");
            Request p = new Request(newSocket);
            Thread hilo = new Thread(p);
            hilo.start();
            System.out.println("Esperando nueva conexión");
        }
    }

    private void crearGrupos(){

    }

    private void informarCliente(){

    }

    private void finPartida(){

    }


}
