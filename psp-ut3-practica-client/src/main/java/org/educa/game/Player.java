package org.educa.game;

public class Player extends Thread {
    private String gameType;
    private boolean host;
    public Player(String name, String gameType) {
        super.setName(name);
        this.gameType = gameType;
    }

    @Override
    public void run() {
        System.out.println("Start player");
        //TODO
    }

    private void creatSocket(){

    }

    private void llamarServidor(){

    }

    private void recibirServidor(){

    }

    private void crearDatagram(){
        //avisar anfitrion
    }

    private void recibirDatagram(){

    }

    private void tirarDado(){
        //random
    }

    private void informarFinPartida(){

    }
}
