package server.UDP;

import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ServerUDP implements Runnable {

    private DatagramSocket socket;
    private List<UDPClient> udpClients;

    private DatagramPacket receivePacket;
    private DatagramPacket sendPacket;
    private byte[] buf = new byte[64 * 1024];

    public ServerUDP() throws SocketException {
        socket = new DatagramSocket(3300);
        udpClients = new ArrayList<>();
        receivePacket = new DatagramPacket(buf, buf.length);
        sendPacket = new DatagramPacket(buf, buf.length);
    }

    @Override
    public void run() {
        System.out.println("UDP server is running ");
        while (true) {
            try {
                socket.receive(receivePacket);
                UDPClient currentClient = new UDPClient(receivePacket.getAddress(), receivePacket.getPort());
                sendPacket.setAddress(receivePacket.getAddress());
                sendPacket.setPort(receivePacket.getPort());
                boolean formerClient = false;

                if (!udpClients.isEmpty()) {
                    for (UDPClient s : udpClients
                    ) {
                        if (s.equals(currentClient)) {
                            formerClient = true;
                            if (s.isWantDownload()) {
                                download();
                                s.setWantDownload(false);
                            }
                            if (s.isWantUpload()) {
                                upload();
                                s.setWantUpload(false);
                            }
                            break;
                        }
                    }
                }

                String entry = new String(receivePacket.getData(), 0, receivePacket.getLength());
                System.out.println("UDP message - " + entry);

                boolean commandIsExist = false;
                if (entry.startsWith("echo")) {
                    sendPacket.setData(entry.replace("echo", "").getBytes());
                    commandIsExist = true;
                }
                if (entry.startsWith("caps")) {
                    sendPacket.setData(entry.replace("caps", "").toUpperCase().getBytes());
                    commandIsExist = true;
                }
                if (entry.startsWith("time")) {
                    sendPacket.setData(new Date().toString().getBytes());
                    commandIsExist = true;
                }
                if (entry.startsWith("upload")) {
                    currentClient.setWantUpload(true);
                    currentClient.setFileName(entry.replace("upload", "").trim());
                    sendPacket.setData("Ready to receive your file".getBytes());
                    commandIsExist = true;
                }
                if (entry.startsWith("download")) {

                    String fileName = entry.replace("download", "").trim();
                    File file = new File(fileName);
                    if (!file.exists()) {
                        System.out.println("File not found");
                        sendPacket.setData("File not found".getBytes());

                    } else {
                        currentClient.setFileName(fileName);
                        currentClient.setWantDownload(true);
                        sendPacket.setData("File exists. Ready to send".getBytes());
                        commandIsExist = true;

                    }

                }
                if (!commandIsExist) {
                    System.out.println("Undefined command");
                    sendPacket.setData("Undefined command".getBytes());

                }

                if(!formerClient) udpClients.add(currentClient);
                socket.send(sendPacket);
                if(Thread.interrupted())break;

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        System.out.println("UDP server was stopped");
    }

    private void upload() {
    }

    private void download() {
    }

    ;

}
