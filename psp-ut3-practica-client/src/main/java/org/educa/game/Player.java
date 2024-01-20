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
            SocketAddress addr = new InetSocketAddress("localhost", 5554);
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

                crearDatagrama(puerto,anfitrion, this.getName());

                boolean partida=false;
                /*while(!partida){

                }*/
                System.out.println();

            }
            System.out.println("Terminado");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     *
     * @param puerto
     * @param anfitrion
     */
    private static void crearDatagrama(int puerto, boolean anfitrion, String nick) {
        boolean empate=false;
        String resultado="V";
        int pAnfitrion;
        int pInvitado;

        if(anfitrion) {
            pAnfitrion =puerto;
            pInvitado=puerto+1;
        }else{
            pAnfitrion=puerto;
            pInvitado =puerto-1;
        }

        System.out.println("Creando socket datagram");
        InetSocketAddress addr = new InetSocketAddress("localhost", pAnfitrion);
        InetSocketAddress adrToSend = new InetSocketAddress("localhost", pInvitado);

        try (DatagramSocket datagramSocket = new DatagramSocket(addr)){
            System.out.println("Enviando mensaje");
            do {
                if (!anfitrion) {
                    sendDatagramInvitado(adrToSend, datagramSocket, tirarDado(), nick);
                    empate = receiveDatagramInvitado(datagramSocket);
                } else {
                    resultado = receiveDatagramAnfitrion(datagramSocket);
                    sendDatagramAnfitrion(adrToSend, datagramSocket, resultado);

                }

            }while(empate || "E".equalsIgnoreCase(resultado));

            System.out.println("Terminado");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private static void sendDatagramInvitado(InetSocketAddress adrToSend, DatagramSocket datagramSocket, int resultado, String nick) throws IOException {
        DatagramPacket datagrama = new DatagramPacket((nick+","+ resultado).getBytes(), (nick+","+ resultado).getBytes().length, adrToSend);
        datagramSocket.send(datagrama);
        System.out.println("Mensaje enviado");
    }

    private static void sendDatagramAnfitrion(InetSocketAddress adrToSend, DatagramSocket datagramSocket, String resultado) throws IOException {
        DatagramPacket datagrama = new DatagramPacket((resultado).getBytes(), (resultado).getBytes().length, adrToSend);
        datagramSocket.send(datagrama);
        System.out.println("Mensaje enviado");
    }

    private static String receiveDatagramAnfitrion(DatagramSocket datagramSocket) throws IOException {
        byte[] mensaje = new byte[100];
        DatagramPacket datagrama = new DatagramPacket(mensaje, mensaje.length);
        datagramSocket.receive(datagrama);
        String cadena = new String(datagrama.getData(), 0, datagrama.getLength()); //casteo de el mensaje del datagrama
        String [] nickResultado = cadena.split(",");
        System.out.println("El "+ nickResultado[0]+" ha sacado un "+nickResultado[1]);

        return resolucion(tirarDado(),Integer.parseInt(nickResultado[1]));

    }

    private static boolean receiveDatagramInvitado(DatagramSocket datagramSocket) throws IOException {
        byte[] mensaje = new byte[100];
        DatagramPacket datagrama = new DatagramPacket(mensaje, mensaje.length);
        datagramSocket.receive(datagrama);
        String cadena = new String(datagrama.getData(), 0, datagrama.getLength()); //casteo de el mensaje del datagrama
        System.out.println("El resultado es "+cadena);

        if("E".equalsIgnoreCase(cadena)){
            return true;
        }else{
            return false;
        }

    }

    private static String resolucion(int anfitrion, int invitado){
        String resultado="";
        if(anfitrion>invitado){
            resultado = "V";
        }else if(anfitrion<invitado){
            resultado = "D";
        }else{
            resultado = "E";
        }

        return resultado;
    }

    private static int tirarDado(){
        Random rnd = new Random();
        return rnd.nextInt(0,6)+1;
    }



}
