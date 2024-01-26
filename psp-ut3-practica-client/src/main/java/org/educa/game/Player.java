package org.educa.game;

import java.io.*;
import java.net.*;
import java.util.Random;


public class Player extends Thread {
    private String gameType;
    private boolean start =true;
    private boolean host;
    private int port;
    private String game;

    /**
     *
     * @param name
     * @param gameType
     */
    public Player(String name, String gameType) {
        super.setName(name);
        this.gameType = gameType;

    }

    /**
     *
     */
    @Override
    public void run() {
        System.out.println("Start player");
        System.out.println("Conectando al server");
        communicationServer();
    }

    /**
     * Metodo para conectar a los jugadores con el servidor
     * @param: no recibe nada
     */
    private void communicationServer(){
        try (Socket clientSocket = new Socket()) {
            System.out.println("Estableciendo la conexión");
            //Se le indica la dirección IP y el número de puerto del socket stream servidor
            SocketAddress addr = new InetSocketAddress("localhost", 5554);
            clientSocket.connect(addr); //se conecta
            //Se abren las tuberias para la comunicacion
            try (OutputStream os = clientSocket.getOutputStream();
                 PrintWriter pWriter = new PrintWriter(os);
                 InputStream is = clientSocket.getInputStream();
                 BufferedReader reader = new BufferedReader(new InputStreamReader(is))){

                String message;
                //Si el juego esta empezando se manda un mensaje distinto a si esta terminando
                if(start){
                    message= "Empezar" +","+ this.getName()+","+this.gameType;//Avisar que esta empezando una partida y el tipo
                    pWriter.println(message);
                    pWriter.flush();
                    //Mientras el server no devuelva null, recoge su mensaje
                    String serverMessage = reader.readLine();
                    while(serverMessage!=null) {
                        host = Boolean.parseBoolean(serverMessage); //Si es o no anfitrion
                        port = Integer.parseInt(reader.readLine()); //El numero de puerto
                        game = reader.readLine();

                        serverMessage = null; //Una vez tiene lo que necesita, se fuerza la salida del while
                    }
                    clientSocket.close();
                    //Al salir del while, crea la comunicacion con otro player
                    communicationPlayers(this.getName());
                }else{
                    message= "Terminar" +","+ this.game;//Avisar que esta finalizando una partida y el tipo
                    pWriter.println(message);
                    pWriter.flush();
                    String []parts = game.split(",");
                    System.out.println(parts[0]+" finalizada, datos enviados al Servidor\n");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Metodo que crea el datagrama para la comunicacion entre players
     */
    private void communicationPlayers(String nick) {
        String result="E";
        int ownPort= port;
        int otherPort;

        String [] parts = game.split(",");

        if(host) {
            otherPort=Integer.parseInt(parts[4]);
        }else{
            otherPort =Integer.parseInt(parts[2]);
        }

        System.out.println("Creando socket datagram");

        //se establecen ambos puertos, el de enviar y el de recibir mensajes
        InetSocketAddress addr = new InetSocketAddress("localhost", ownPort);
        InetSocketAddress adrToSend = new InetSocketAddress("localhost", otherPort);

        //se crea el datagrama
        try (DatagramSocket datagramSocket = new DatagramSocket(addr)){
            while("E".equalsIgnoreCase(result)){ //en caso de ser empate, se repite
                if (!host) { //si no se es anfitrion, primero se envia el mensaje del resultado de la tirada.
                    sendDatagramGuest(adrToSend, datagramSocket, rollDice(), nick);
                    result = receiveDatagramGuest(datagramSocket, parts[0]); //recibe si ha sido empate o no
                } else { //si se es anfitrion, primero se recibe el resultado del otro jugador
                    result = receiveDatagramHost(datagramSocket, nick, parts[0]);
                    //y despues le dice si ha empatado, ganado o perdido
                    sendDatagramHost(adrToSend, datagramSocket, result);
                }
            }
            start = false;
            if(host) {
                communicationServer();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Metodo para mandar un mensaje desde el invitado
     * @param adrToSend recive en inetSocket que le indica donde se manda
     * @param datagramSocket Recibe el socket
     * @param result Recibe el resultado del dado
     * @param nick Recibe su nick
     * @throws IOException lanza IOException en caso de fallo
     */
    private static void sendDatagramGuest(InetSocketAddress adrToSend, DatagramSocket datagramSocket, int result, String nick) throws IOException {
        DatagramPacket datagrama = new DatagramPacket((nick+","+ result).getBytes(), (nick+","+ result).getBytes().length, adrToSend);
        datagramSocket.send(datagrama);
    }

    /**
     * Metodo para mandar un mensaje desde el anfitrion
     * @param adrToSend recive en inetSocket que le indica donde se manda
     * @param datagramSocket Recibe el socket
     * @param result Recibe su resultado
     * @throws IOException lanza IOException en caso de fallo
     */
    private static void sendDatagramHost(InetSocketAddress adrToSend, DatagramSocket datagramSocket, String result) throws IOException {
        DatagramPacket datagrama = new DatagramPacket((result).getBytes(), (result).getBytes().length, adrToSend);
        datagramSocket.send(datagrama);
    }

    /**
     * Metodo con el que el anfitrion recibe un mensaje
     * @param datagramSocket Recibe el socket
     * @return devuelve la resolucion del juego
     * @throws IOException lanza IOException en caso de fallo
     */
    private static String receiveDatagramHost(DatagramSocket datagramSocket, String host, String game) throws IOException {
        byte[] message = new byte[100];
        DatagramPacket datagrama = new DatagramPacket(message, message.length);
        datagramSocket.receive(datagrama);
        String received = new String(datagrama.getData(), 0, datagrama.getLength()); //casteo del mensaje del datagrama
        String [] nickResult = received.split(","); //se separa el mensaje

        //hace uso del metodo resolucion para devolver quien ha ganado
        return resolution(rollDice(),Integer.parseInt(nickResult[1]),nickResult[0], host, game);

    }

    /**
     * Metodo para que el invitado reciba un mensaje
     * @param datagramSocket Recibe el socket
     * @return devuelve un string que dice si ha empatado o no
     * @throws IOException lanza IOException en caso de fallo
     */
    private String receiveDatagramGuest(DatagramSocket datagramSocket, String game) throws IOException {
        byte[] message = new byte[100];
        DatagramPacket datagram = new DatagramPacket(message, message.length);
        datagramSocket.receive(datagram);
        String cadena = new String(datagram.getData(), 0, datagram.getLength()); //casteo de el mensaje del datagrama
        if("V".equalsIgnoreCase(cadena)) {
            System.out.println("Ha ganado el anfitrion en la "+game+"\n");
        } else if ("E".equalsIgnoreCase(cadena)) {
            System.out.println("Ha habido empate en la "+game+"\n");
        }else{
            System.out.println("Ha ganado el invitado en la "+game+"\n");
        }

        return cadena;
    }

    /**
     * Metodo para calcular quien ha ganado
     * @param host Recibe el resultado del anfitrion
     * @param guest Recibe el resultado del invitado
     * @param nickHost recibe el nombre del host
     * @param nickGuest recibe el nombre del guest
     * @return devuelve quien ha ganado
     */
    private static String resolution(int host, int guest, String nickGuest, String nickHost, String game){

        System.out.println("El anfitrion "+nickHost+" de la "+game+" ha sacado un "+host+"\n");
        System.out.println("El invitado "+nickGuest+" de la "+game+" ha sacado un "+guest+"\n");
        if(host>guest){
            return "V";
        }else if(host<guest){
            return "D";
        }else{
            return "E";
        }
    }

    /**
     * Metodo para tirar el dado
     * @return devuelve el resultado de la tirada
     */
    private static int rollDice(){
        Random rnd = new Random();
        return rnd.nextInt(0,6)+1;
    }
}
