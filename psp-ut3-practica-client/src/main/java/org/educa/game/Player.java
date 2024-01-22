package org.educa.game;

import java.io.*;
import java.net.*;
import java.util.Random;

public class Player extends Thread {
    private String gameType;
    private boolean empezar=true;
    private boolean anfitrion;
    private int puerto;
    private String[] infoPartida;

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

    /**
     * Metodo para conectar a los jugadores con el servidor
     * @param: no recibe nada
     */
    private void comunicacionServidor(){
        try (Socket clientSocket = new Socket()) {
            System.out.println("Estableciendo la conexión");
            // Se le indica la dirección IP y el número de puerto del socket stream servidor
            SocketAddress addr = new InetSocketAddress("localhost", 5554);
            clientSocket.connect(addr); //se conecta
            //se abren las tuberias para la comunicacion98
            try (OutputStream os = clientSocket.getOutputStream();
                 PrintWriter pWriter = new PrintWriter(os);
                 InputStream is = clientSocket.getInputStream();
                 BufferedReader reader = new BufferedReader(new InputStreamReader(is))){

                String mensaje;
                //si el juego esta empezando se manda un mensaje distinto a si esta terminando
                if(empezar){
                    mensaje= "Empezar" +","+ this.getName()+","+this.gameType;//avisar que esta empezando una partida y el tipo

                }else{
                    mensaje= "Terminar" +","+ this.infoPartida[0];//avisar que esta finalizando una partida y el tipo

                }

                pWriter.println(mensaje);
                pWriter.flush();

                //System.out.println("Mensaje enviado");

                //TODO ESTO NO DEBERIA ESTAR EN EL IF? SI HA TERMINADO NO SE COMUNICAN DE NUEVO, NO?
                //mientras el server no devuelva null, recoge su mensaje
                String mensajeServer = reader.readLine();
                while(mensajeServer!=null){
                    System.out.println(mensajeServer);
                    anfitrion = Boolean.parseBoolean(mensajeServer); //si es o no anfitrion
                    puerto = Integer.parseInt(reader.readLine()); //el numero de puerto
                    infoPartida= reader.readLine().split(",");
                    System.out.println("El puerto es: " + puerto);
                    System.out.println(anfitrion);
                    System.out.println(infoPartida[0]);
                    System.out.println(infoPartida[1]);
                    System.out.println(infoPartida[2]);
                    System.out.println(infoPartida[3]);
                    System.out.println(infoPartida[4]);
                    mensajeServer=null; //una vez tiene lo que necesita, se fuerza la salida del while
                }

                //al salir del while, crea la comunicacion con otro player
                crearDatagrama(anfitrion, this.getName());

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
     * Metodo que crea el datagrama para la comunicacion entre players
     * @param anfitrion recibe si es o no anfitrion
     */
    private void crearDatagrama(boolean anfitrion, String nick) {
        String result="V";
        int ownPort;
        int sendPort;

        if(anfitrion) {
            ownPort =Integer.parseInt(infoPartida[2]);
            sendPort=Integer.parseInt(infoPartida[4]);
        }else{
            ownPort=Integer.parseInt(infoPartida[4]);
            sendPort =Integer.parseInt(infoPartida[2]);
        }

        System.out.println("Creando socket datagram");

        //se establecen ambos puertos, el de enviar y el de recibir mensajes
        InetSocketAddress addr = new InetSocketAddress("localhost", ownPort);
        InetSocketAddress adrToSend = new InetSocketAddress("localhost", sendPort);

        //se crea el datagrama
        try (DatagramSocket datagramSocket = new DatagramSocket(addr)){
            System.out.println("Enviando mensaje");
            do {
                if (!anfitrion) { //si no se es anfitrion, primero se envia el mensaje del result de la tirada.
                    sendDatagramInvitado(adrToSend, datagramSocket, tirarDado(), nick);
                    receiveDatagramInvitado(datagramSocket); //recibe si ha sido empate o no
                } else { //si se es anfitrion, primero se recibe el result del otro jugador
                    result = receiveDatagramAnfitrion(datagramSocket);
                    //y despues le dice si ha empatado, ganado o perdido
                    sendDatagramAnfitrion(adrToSend, datagramSocket, result);
                }

            }while("E".equalsIgnoreCase(result)); //en caso de ser empate, se repite

            System.out.println("Terminado");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Metodo para mandar un mensaje desde el invitado
     * @param adrToSend recive en inetSocket que le indica donde se manda
     * @param datagramSocket Recibe el socket
     * @param resultado Recibe el resultado del dado
     * @param nick Recibe su nick
     * @throws IOException lanza IOException en caso de fallo
     */
    private static void sendDatagramInvitado(InetSocketAddress adrToSend, DatagramSocket datagramSocket, int resultado, String nick) throws IOException {
        DatagramPacket datagrama = new DatagramPacket((nick+","+ resultado).getBytes(), (nick+","+ resultado).getBytes().length, adrToSend);
        datagramSocket.send(datagrama);
        System.out.println("Mensaje enviado");
    }

    /**
     * Metodo para mandar un mensaje desde el anfitrion
     * @param adrToSend recive en inetSocket que le indica donde se manda
     * @param datagramSocket Recibe el socket
     * @param resultado Recibe su resultado
     * @throws IOException lanza IOException en caso de fallo
     */
    private static void sendDatagramAnfitrion(InetSocketAddress adrToSend, DatagramSocket datagramSocket, String resultado) throws IOException {
        DatagramPacket datagrama = new DatagramPacket((resultado).getBytes(), (resultado).getBytes().length, adrToSend);
        datagramSocket.send(datagrama);
        System.out.println("Mensaje enviado");
    }

    /**
     * Metodo con el que el anfitrion recibe un mensaje
     * @param datagramSocket Recibe el socket
     * @return devuelve la resolucion del juego
     * @throws IOException lanza IOException en caso de fallo
     */
    private static String receiveDatagramAnfitrion(DatagramSocket datagramSocket) throws IOException {
        byte[] mensaje = new byte[100];
        DatagramPacket datagrama = new DatagramPacket(mensaje, mensaje.length);
        datagramSocket.receive(datagrama);
        String cadena = new String(datagrama.getData(), 0, datagrama.getLength()); //casteo del mensaje del datagrama
        String [] nickResultado = cadena.split(","); //se separa el mensaje
        System.out.println("El "+ nickResultado[0]+" ha sacado un "+nickResultado[1]);

        //hace uso del metodo resolucion para devolver quien ha ganado
        return resolucion(tirarDado(),Integer.parseInt(nickResultado[1]));

    }

    /**
     * Metodo para que el invitado reciba un mensaje
     * @param datagramSocket Recibe el socket
     * @return devuelve un boolean que dice si ha empatado o no
     * @throws IOException lanza IOException en caso de fallo
     */
    private static void receiveDatagramInvitado(DatagramSocket datagramSocket) throws IOException {
        byte[] mensaje = new byte[100];
        DatagramPacket datagrama = new DatagramPacket(mensaje, mensaje.length);
        datagramSocket.receive(datagrama);
        String cadena = new String(datagrama.getData(), 0, datagrama.getLength()); //casteo de el mensaje del datagrama
        System.out.println("El resultado es "+cadena); //imprime E, V o D
    }

    /**
     * Metodo para calcular quien ha ganado
     * @param anfitrion Recibe el resultado del anfitrion
     * @param invitado Recibe el resultado del invitado
     * @return devuelve quien ha ganado
     */
    private static String resolucion(int anfitrion, int invitado){
        if(anfitrion>invitado){
            return "V";
        }else if(anfitrion<invitado){
            return "D";
        }else{
            return "E";
        }
    }

    /**
     * Metodo para tirar el dado
     * @return devuelve el resultado de la tirada
     */
    private static int tirarDado(){
        Random rnd = new Random();
        return rnd.nextInt(0,6)+1;
    }



}
