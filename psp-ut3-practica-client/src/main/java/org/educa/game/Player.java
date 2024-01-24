package org.educa.game;

import java.io.*;
import java.net.*;
import java.util.Random;


//TODO cambiar los metodos del programa a inglés
public class Player extends Thread {
    private String gameType;
    private boolean empezar=true;
    private boolean anfitrion;
    private int puerto;
    private String partida;

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
            //Se le indica la dirección IP y el número de puerto del socket stream servidor
            SocketAddress addr = new InetSocketAddress("localhost", 5554);
            clientSocket.connect(addr); //se conecta
            //Se abren las tuberias para la comunicacion
            try (OutputStream os = clientSocket.getOutputStream();
                 PrintWriter pWriter = new PrintWriter(os);
                 InputStream is = clientSocket.getInputStream();
                 BufferedReader reader = new BufferedReader(new InputStreamReader(is))){

                String mensaje;
                //Si el juego esta empezando se manda un mensaje distinto a si esta terminando
                if(empezar){
                    mensaje= "Empezar" +","+ this.getName()+","+this.gameType;//Avisar que esta empezando una partida y el tipo


                    pWriter.println(mensaje);
                    pWriter.flush();
                    //Mientras el server no devuelva nul, recoge su mensaje
                    String mensajeServer = reader.readLine();
                    while(mensajeServer!=null){
                        System.out.println(mensajeServer);
                        anfitrion = Boolean.parseBoolean(mensajeServer); //Si es o no anfitrion
                        puerto = Integer.parseInt(reader.readLine()); //El numero de puerto
                        partida=reader.readLine();
                        //System.out.println("Anfitrion: "+anfitrion +" Puerto: "+ puerto +" Partida: "+ partida);

                        mensajeServer=null; //Una vez tiene lo que necesita, se fuerza la salida del while
                    }
                    //Al salir del while, crea la comunicacion con otro player
                    comunicacionPlayers(this.getName());
                }else{
                    mensaje= "Terminar" +","+ this.partida;//Avisar que esta finalizando una partida y el tipo
                    pWriter.println(mensaje);
                    pWriter.flush();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Metodo que crea el datagrama para la comunicacion entre players
     */
    private void comunicacionPlayers(String nick) {
        String resultado="E";
        int pPropio=puerto;
        int pOtroJugador;

        String [] partes = partida.split(",");

        if(anfitrion) {
            pOtroJugador=Integer.parseInt(partes[4]);
        }else{
            pOtroJugador =Integer.parseInt(partes[2]);
        }

        System.out.println("Creando socket datagram");

        //se establecen ambos puertos, el de enviar y el de recibir mensajes
        InetSocketAddress addr = new InetSocketAddress("localhost", pPropio);
        InetSocketAddress adrToSend = new InetSocketAddress("localhost", pOtroJugador);

        //se crea el datagrama
        try (DatagramSocket datagramSocket = new DatagramSocket(addr)){
            while("E".equalsIgnoreCase(resultado)){ //en caso de ser empate, se repite
                if (!anfitrion) { //si no se es anfitrion, primero se envia el mensaje del resultado de la tirada.
                    sendDatagramInvitado(adrToSend, datagramSocket, tirarDado(), nick);
                    resultado = receiveDatagramInvitado(datagramSocket); //recibe si ha sido empate o no

                    System.out.println("REsultado inv: "+resultado);
                } else { //si se es anfitrion, primero se recibe el resultado del otro jugador
                    resultado = receiveDatagramAnfitrion(datagramSocket);
                    //y despues le dice si ha empatado, ganado o perdido
                    sendDatagramAnfitrion(adrToSend, datagramSocket, resultado, nick);

                    System.out.println("REsultado anf: "+resultado);
                }
                System.out.println("REsultado fin: "+resultado);
            }

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
        System.out.println("El invitado"+ nick+" ha sacado un "+resultado);
    }

    /**
     * Metodo para mandar un mensaje desde el anfitrion
     * @param adrToSend recive en inetSocket que le indica donde se manda
     * @param datagramSocket Recibe el socket
     * @param resultado Recibe su resultado
     * @throws IOException lanza IOException en caso de fallo
     */
    private static void sendDatagramAnfitrion(InetSocketAddress adrToSend, DatagramSocket datagramSocket, String resultado, String nick) throws IOException {
        DatagramPacket datagrama = new DatagramPacket((resultado).getBytes(), (resultado).getBytes().length, adrToSend);
        datagramSocket.send(datagrama);
        System.out.println("Mensaje enviado por en anfitrion "+nick);
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
        System.out.println(cadena);
        String [] nickResultado = cadena.split(","); //se separa el mensaje

        //hace uso del metodo resolucion para devolver quien ha ganado
        return resolucion(tirarDado(),Integer.parseInt(nickResultado[1]));

    }

    /**
     * Metodo para que el invitado reciba un mensaje
     * @param datagramSocket Recibe el socket
     * @return devuelve un boolean que dice si ha empatado o no
     * @throws IOException lanza IOException en caso de fallo
     */
    private String receiveDatagramInvitado(DatagramSocket datagramSocket) throws IOException {
        byte[] mensaje = new byte[100];
        DatagramPacket datagrama = new DatagramPacket(mensaje, mensaje.length);
        datagramSocket.receive(datagrama);
        String cadena = new String(datagrama.getData(), 0, datagrama.getLength()); //casteo de el mensaje del datagrama
        if("V".equalsIgnoreCase(cadena)) {
            System.out.println("Ha ganado el anfitrion");
        } else if ("E".equalsIgnoreCase(cadena)) {
            System.out.println("Ha habido empate");
        }else{
            System.out.println("Ha ganado el invitado");
        }

        return cadena;
    }

    /**
     * Metodo para calcular quien ha ganado
     * @param anfitrion Recibe el resultado del anfitrion
     * @param invitado Recibe el resultado del invitado
     * @return devuelve quien ha ganado
     */
    private static String resolucion(int anfitrion, int invitado){

        System.out.println("El anfitrion ha sacado un "+anfitrion);
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
        return rnd.nextInt(0,2)+1;
    }
}
