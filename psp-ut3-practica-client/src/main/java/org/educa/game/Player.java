package org.educa.game;

public class Player extends Thread {
    private String gameType;
    private static final int PUERTO_PROPIO=5556;
    public Player(String name, String gameType) {
        super.setName(name);
        this.gameType = gameType;
    }

    @Override
    public void run() {
        System.out.println("Start player");
        System.out.println("Conectando al server");
        comunicacionServidor();
        System.out.println("Creando socket datagram");
        InetSocketAddress addr = new InetSocketAddress("localhost", 5556);
        InetSocketAddress adrToSend = new InetSocketAddress("localhost", 5555);
        try (DatagramSocket datagramSocket = new DatagramSocket(addr)){
            System.out.println("Enviando mensaje");

            sendDatagram(adrToSend, datagramSocket);
            receiveDatagram(datagramSocket);

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
            InetSocketAddress addr = new InetSocketAddress("localhost", 5555);
            clientSocket.connect(addr);
            try (Socket newSocket = serverSocket.accept();
                 InputStream is = newSocket.getInputStream();
                 OutputStream os = newSocket.getOutputStream();
                 // Flujos que manejan caracteres
                 InputStreamReader isr = new InputStreamReader(is);
                 OutputStreamWriter osw = new OutputStreamWriter(os);
                 // Flujos de líneas
                 BufferedReader bReader = new BufferedReader(isr);
                 PrintWriter pWriter = new PrintWriter(osw);) {

                System.out.println("Enviando mensaje");
                String mensaje = "mensaje desde el cliente " + this.getName();
                pWriter.print(mensaje);
                pWriter.flush();
                String receive = bReader.readLine();
                String [] mensajeServer = receive.split(",");
                System.out.println("Mensaje enviado");

            }
            System.out.println("Terminado");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendDatagram(InetSocketAddress adrToSend, DatagramSocket datagramSocket) throws IOException {
        DatagramPacket datagrama = new DatagramPacket(TOKEN.getBytes(), TOKEN.getBytes().length, adrToSend);
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
}
