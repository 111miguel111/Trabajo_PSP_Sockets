package org.educa.game;

import java.io.*;
import java.net.Socket;

public class Request implements Runnable{

    private final Socket socket;
    private boolean anfitrion;
    private static int puerto;

    /**
     *
     * @param socket
     */
    public Request(Socket socket){
        this.socket=socket;
    }

    /**
     *
     */
    public void run(){
        try (InputStream is = socket.getInputStream();
             InputStreamReader isr = new InputStreamReader(is);
             BufferedReader bfr = new BufferedReader(isr);
            OutputStream os = socket.getOutputStream();
            PrintWriter pWriter = new PrintWriter(os);){
            String nombre="";
            String juego="";
            String mensaje[] = bfr.readLine().split(",");//El player informa si empieza, termina una partida y los datos necesarios
            if("Empezar".equalsIgnoreCase(mensaje[0])){
                nombre=mensaje[1];
                juego=mensaje[2];
                asignarPuerto(pWriter,juego);
            }else{
                String idPartida=mensaje[1];
                Server.finPartida(idPartida);
            }
            System.out.println("Mensaje recibido: " + mensaje);


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     *
     * @param pWriter
     */
    private synchronized void asignarPuerto(PrintWriter pWriter,String juego) {
        if("Dados".equalsIgnoreCase(juego)) {
            int nJugadores=2;
            anfitrion = Server.anfitrion(nJugadores);
            pWriter.println(anfitrion);
            if (anfitrion) {
                puerto = Server.generarPuerto();
                pWriter.println(puerto);
            } else {
                pWriter.println(puerto + 1);
            }
        }else{
            System.out.println("No hay otro tipo de juego");
        }
    }



}