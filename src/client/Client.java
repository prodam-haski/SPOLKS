package client;

import server.UDP.UDPClient;

import java.io.*;

public class Client {

    public static void main(String[] args) throws IOException {

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        menu();

        try {
            while (true) {
                if (bufferedReader.ready()) {
                    String mode = bufferedReader.readLine();
                    switch (mode) {
                        case "1":
                            TCProtocol clientTCP = new TCProtocol();
                            Thread threadTCP = new Thread(clientTCP);
                            threadTCP.start();
                            break;
                        case "2":
                            UDProtocol clientUDP = new UDProtocol();
                            Thread threadUDP = new Thread(clientUDP);
                            threadUDP.start();
                            break;
                        case "3":
                            break;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static void menu() {
        System.out.println("1 - TCP client");
        System.out.println("2 - UDP client");
        System.out.println("3 - exit");
    }


}