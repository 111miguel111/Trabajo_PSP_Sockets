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
            String mensaje = bfr.readLine();
            System.out.println("Mensaje recibido: " + mensaje);
            asignarPuerto(pWriter);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     *
     * @param pWriter
     */
    private synchronized void asignarPuerto(PrintWriter pWriter) {
        anfitrion=Server.anfitrion();
        pWriter.println(anfitrion);
        if(anfitrion) {
            puerto = Server.generarPuerto();
            pWriter.println(puerto);
        }else {
            pWriter.println(puerto+1);
        }
    }


}