package org.educa.game;

import java.io.*;
import java.net.Socket;

public class Request implements Runnable{

    private final Socket socket;
    private boolean host;
    private int port;
    private String game =null;
    /**
     * Constructor del Request
     * @param socket recibe el socket
     */
    public Request(Socket socket){
        this.socket=socket;
    }

    /**
     * Metodo run de los hilos
     */
    public void run(){
        //se abren los flujos de comunicacion
        try (InputStream is = socket.getInputStream();
             InputStreamReader isr = new InputStreamReader(is);
             BufferedReader bfr = new BufferedReader(isr);
            OutputStream os = socket.getOutputStream();
            PrintWriter pWriter = new PrintWriter(os);){
            String gameType= "";
            String[] message = bfr.readLine().split(",");//El player informa si empieza, termina una partida y los datos necesarios
            //si el juego empieza, se recibe el nombre del jugador y el tipo de juego, y se le asigna el puerto
            if("Empezar".equalsIgnoreCase(message[0])){
                gameType=message[2];
                assignPort(pWriter,gameType);
            }else{ //si termina, se recibe el id de la partida y se elimina de la memoria
                Server.endGame(message[1]);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Metodo sincronizado para asignar los puertos a los jugadores
     * @param pWriter recibe el printWriter
     * @param gameType recibe el tipo de juego
     */
    private synchronized void assignPort(PrintWriter pWriter, String gameType) {
        //si el juego es dados, usa el metodo anfitrion() para asignarlo
        //comunica si se es o no anfitrion y el puerto, que se genera con el metodo generarPuerto()
        if("Dados".equalsIgnoreCase(gameType)) {
            int nPlayers=2;
            host = Server.isHost(nPlayers);
            pWriter.println(host);
            pWriter.flush();
            port = Server.generatePort();
            pWriter.println(port);
            pWriter.flush();
            Server.savePlayersAndCouples(""+ host +","+ port);
            while(this.game ==null){
                this.game =Server.startingData(""+ port);
            }
            pWriter.println(this.game);
            pWriter.flush();

        }else{ //else para si se incorporan otros juegos con distinto numero de jugadores
            System.out.println("No hay otro tipo de juego");
        }
    }
}