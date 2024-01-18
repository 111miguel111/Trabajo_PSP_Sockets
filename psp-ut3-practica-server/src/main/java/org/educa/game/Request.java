package org.educa.game;

import java.io.*;
import java.net.Socket;

public class Request implements Runnable{

    private final Socket socket;

    public Request(Socket socket){
        this.socket=socket;
    }

    public void run(){
        try (InputStream is = socket.getInputStream();
             InputStreamReader isr = new InputStreamReader(is);
             BufferedReader bfr = new BufferedReader(isr);
            OutputStream os = socket.getOutputStream();
            PrintWriter pWriter = new PrintWriter(os);){

            String mensaje = bfr.readLine();
            System.out.println("Mensaje recibido: " + mensaje);
            pWriter.println("HOLA");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}