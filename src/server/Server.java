package server;

import server.TCP.ServerTCP;
import server.UDP.ServerUDP;

import java.io.*;

public class Server {
    static Thread serverTCPThread;
    static Thread serverUDPThread;
    public static void main(String[] args) throws IOException {

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        boolean isTCPRunning = false;
        boolean isUDPRunning = false;

        while (true) {
            try {
                if (br.ready()) {
                    String mode = br.readLine();
                    if (mode.startsWith("UDP")) {
                        isUDPRunning = true;
                        ServerUDP serverUDP = new ServerUDP();
                        serverUDPThread = new Thread(serverUDP);
                        serverUDPThread.start();
                        serverUDPThread.setPriority(Thread.MAX_PRIORITY);
                    }
                    if (mode.startsWith("TCP")) {
                        ServerTCP serverTCP = new ServerTCP();
                        serverTCPThread = new Thread(serverTCP);
                        isTCPRunning = true;
                        serverTCPThread.start();
                        serverTCPThread.setPriority(Thread.MAX_PRIORITY);
                    }
                    if (mode.startsWith("stop TCP") && isTCPRunning) {
                        serverTCPThread.interrupt();
                    }
                    if (mode.startsWith("stop UDP") && isUDPRunning) {
                        serverUDPThread.interrupt();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}