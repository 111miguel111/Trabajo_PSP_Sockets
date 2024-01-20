package org.educa.game;

import java.io.*;
import java.net.*;
import java.util.Random;

public class Player extends Thread {
    private String gameType;
    private boolean anfitrion;
    private int puerto;
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
                String mensajeServer = reader.readLine();
                while(mensajeServer!=null){
                    System.out.println(mensajeServer);
                    anfitrion = Boolean.parseBoolean(mensajeServer);
                    puerto = Integer.parseInt(reader.readLine());
                    System.out.println("El puerto es: " + puerto);
                    System.out.println(anfitrion);
                    mensajeServer=null;
                }

                //crearDatagrama(puerto,anfitrion);

                System.out.println();

            }
            System.out.println("Terminado");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void crearDatagrama(int puerto, boolean anfitrion) {

        //el if aqui?
        System.out.println("Creando socket datagram");
        InetSocketAddress addr = new InetSocketAddress("localhost", 5556);
        InetSocketAddress adrToSend = new InetSocketAddress("localhost", 5555);
        try (DatagramSocket datagramSocket = new DatagramSocket(addr)){
            System.out.println("Enviando mensaje");

            if(anfitrion) {
                sendDatagram(adrToSend, datagramSocket, puerto);
            }else{
                receiveDatagram(datagramSocket);
            }

            System.out.println("Terminado");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private static void sendDatagram(InetSocketAddress adrToSend, DatagramSocket datagramSocket, int puerto) throws IOException {
        DatagramPacket datagrama = new DatagramPacket(String.valueOf(puerto).getBytes(), String.valueOf(puerto).getBytes().length, adrToSend);
        datagramSocket.send(datagrama);
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

    /*private int random(){
        Random rnd = new Random();

    }*/

}
