package org.educa.game;

import java.io.*;
import java.net.*;

public class Player extends Thread {
    private String gameType;
    private static int puerto=5555;
    public Player(String name, String gameType) {
        super.setName(name);
        this.gameType = gameType;
    }

    @Override
    public void run() {
        System.out.println("Start player");
        System.out.println("Conectando al server");
        comunicacionServidor();
    }

    private static void crearDatagrama(String [] mensajeServer) {
        System.out.println("Creando socket datagram");
        InetSocketAddress addr = new InetSocketAddress("localhost", 5556);
        InetSocketAddress adrToSend = new InetSocketAddress("localhost", 5555);
        try (DatagramSocket datagramSocket = new DatagramSocket(addr)){
            System.out.println("Enviando mensaje");

            if("host".equalsIgnoreCase(mensajeServer[0])) {
                sendDatagram(adrToSend, datagramSocket);
            }else{
                receiveDatagram(datagramSocket);
            }

            System.out.println("Terminado");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void comunicacionServidor(){
        try (Socket clientSocket = new Socket()) {
            System.out.println("Estableciendo la conexión");
            // Para indicar la dirección IP y el número de puerto del socket stream servidor
            // al que se desea conectar, el método connect() hace uso de un objeto
            // de la clase java.net.InetSocketAddress.
            SocketAddress addr = new InetSocketAddress("localhost", 5555);
            clientSocket.connect(addr);
            try (OutputStream os = clientSocket.getOutputStream();
                 PrintWriter pWriter = new PrintWriter(os);
                InputStream is = clientSocket.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is))){

                String mensaje = "mensaje desde el cliente " + this.getName();
                pWriter.println(mensaje);
                pWriter.flush();
                //System.out.println("Mensaje enviado");
                String [] mensajeServer = reader.readLine().split(",");
                crearDatagrama(mensajeServer);
                System.out.println();

            }
            System.out.println("Terminado");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void sendDatagram(InetSocketAddress adrToSend, DatagramSocket datagramSocket) throws IOException {
       // DatagramPacket datagrama = new DatagramPacket(TOKEN.getBytes(), TOKEN.getBytes().length, adrToSend);
       // datagramSocket.send(datagrama);
        System.out.println("Mensaje enviado");
    }

    private static void receiveDatagram(DatagramSocket datagramSocket) throws IOException {
        byte[] mensaje = new byte[100];
        DatagramPacket datagrama = new DatagramPacket(mensaje, mensaje.length);
        datagramSocket.receive(datagrama);
        System.out.println("Mensaje recibido: " + new String(datagrama.getData(), 0, datagrama.getLength()));
    }

    private void tirarDado(){
        //random
    }

    public static synchronized int generarPuerto(){
        puerto++;
        return 0;
    }
}
