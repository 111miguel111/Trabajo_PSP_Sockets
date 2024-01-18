package org.educa.game;

public class Request implements Runnable{

    public Request(Socket socket){
        this.socket=socket();
    }

    public void run(){
        try (InputStream is = socket.getInputStream();
             InputStreamReader isr = new InputStreamReader(is);
             BufferedReader bfr = new BufferedReader(isr)){

            String mensaje = bfr.readLine();
            System.out.println("Mensaje recibido: " + mensaje);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}