package org.educa.game;

import java.io.*;
import java.net.Socket;

public class Request implements Runnable{

    private final Socket socket;

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
            String nombre="";
            String juego="";
            String mensaje[] = bfr.readLine().split(",");//El player informa si empieza, termina una partida y los datos necesarios
            //si el juego empieza, se recibe el nombre del jugador y el tipo de juego, y se le asigna el puerto
            if("Empezar".equalsIgnoreCase(mensaje[0])){
                nombre=mensaje[1];
                juego=mensaje[2];
                asignarPuertoYAnfritrion(pWriter,juego);
            }else{ //si termina, se recibe el id de la partida y se elimina de la memoria
                String idPartida=mensaje[1];
                Server.finPartida(idPartida);
            }
            System.out.println("Mensaje recibido: " + mensaje[1]); //todo Print debug

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Metodo sincronizado para asignar los puertos a los jugadores
     * @param pWriter recibe el printWriter
     */
    private synchronized void asignarPuertoYAnfritrion(PrintWriter pWriter,String juego) {
        //si el juego es dados, usa el metodo anfitrion() para asignarlo
        //comunica si se es o no anfitrion y el puerto, que se genera con el metodo generarPuerto()
        if("Dados".equalsIgnoreCase(juego)) {
            int nJugadores=2;
            boolean anfitrion = Server.anfitrion(nJugadores);
            pWriter.println(anfitrion);
            int puerto= Server.generarPuerto();
            pWriter.println(puerto);

            String infoPartida=Server.crearParejas(anfitrion,puerto);
            pWriter.println(infoPartida);
        }else{ //else para si se incorporan otros juegos con distinto numero de jugadores
            System.out.println("No hay otro tipo de juego");
        }
    }



}