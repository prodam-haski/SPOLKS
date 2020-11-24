package server.UDP;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ServerUDP implements Runnable {

    private List<UDPClient> udpClients;
    private byte[] buf = new byte[64 * 1024];
    private DatagramSocket socket;
    private DatagramPacket receivePacket;
    private DatagramPacket sendPacket;

    public ServerUDP() throws SocketException {
        udpClients = new ArrayList<>();
        socket = new DatagramSocket(6600);

    }

    @Override
    public void run() {
        System.out.println("UDP server is running ");
        while (true) {
            try {
                buf = new byte[64 * 1024];
                receivePacket = new DatagramPacket(buf, buf.length);
                sendPacket = new DatagramPacket(buf, buf.length);

                socket.receive(receivePacket);
                System.out.println("Client was connected");

                UDPClient currentClient = new UDPClient(receivePacket.getAddress(), receivePacket.getPort());
                sendPacket.setAddress(receivePacket.getAddress());
                sendPacket.setPort(receivePacket.getPort());

                String entry = new String(receivePacket.getData());
                System.out.println("UDP message - " + entry);

                /*if (!udpClients.isEmpty()) {
                    for (UDPClient s : udpClients
                    ) {
                        System.out.print(s.getAddress());
                        System.out.println(" " + s.getPort());
                        System.out.println(s.isWantDownload());
                        System.out.println(s.isWantUpload());
                        if (s.getAddress().equals(currentClient.getAddress()) && s.getPort() == currentClient.getPort()) {
                            if (s.isWantDownload()) {
                                download(s.getFileName());
                                udpClients.remove(s);
                            }
                            if (s.isWantUpload()) {
                                upload(s.getFileName());
                                udpClients.remove(s);
                            }
                            break;
                        }
                        if (udpClients.isEmpty())break;
                    }
                }*/

                boolean commandIsExist = false;
                if (entry.startsWith("echo")) {
                    sendPacket.setData(entry.replace("echo", "").trim().getBytes());
                    commandIsExist = true;
                }
                if (entry.startsWith("caps")) {
                    sendPacket.setData(entry.replace("caps", "").toUpperCase().trim().getBytes());
                    commandIsExist = true;
                }
                if (entry.startsWith("time")) {
                    sendPacket.setData(new Date().toString().getBytes());
                    commandIsExist = true;
                }
                if (entry.startsWith("upload")) {
                    currentClient.setWantService(true);
                    currentClient.setWantUpload(true);

                    entry= entry.replace("upload", "").trim();
                    String fileName=entry.substring(0,entry.lastIndexOf(" "));
                    int fileSize = Integer.getInteger(entry.substring(entry.lastIndexOf(" ")));
                    currentClient.setFileName(fileName);
                    upload(fileName,fileSize);
                    sendPacket.setData("File was received".getBytes());
                    commandIsExist = true;
                }
                if (entry.startsWith("download")) {
                    commandIsExist = true;
                    String fileName = entry.replace("download", "").trim();
                    File file = new File(fileName);
                    if (!file.exists()) {
                        System.out.println("File not found");
                        sendPacket.setData("File not found".getBytes());

                    } else {
                        currentClient.setWantService(true);
                        currentClient.setFileName(fileName);
                        currentClient.setWantDownload(true);
                        sendPacket.setData("File exists. Ready to send".getBytes());
                        download(fileName, currentClient.getAddress(), currentClient.getPort());
                    }

                }
                if (!commandIsExist) {
                    System.out.println("Undefined command");
                    sendPacket.setData("Undefined command".getBytes());

                }

                //if (currentClient.isWantService()) udpClients.add(currentClient);
                socket.send(sendPacket);
                if (Thread.interrupted()) {
                    socket.close();
                    break;
                }
            } catch (IOException e) {
                System.out.println("UDP server error");
            }

        }
        System.out.println("UDP server was stopped");

    }

    private void upload(String fileName,int fileSize) {
        System.out.println("Uploading...");
        byte[] buf = new byte[63*1024];
        DatagramPacket filePacket = new DatagramPacket(buf, buf.length);
        try {
            FileOutputStream fout = new FileOutputStream(fileName);
            socket.receive(filePacket);
            fout.write(filePacket.getData());
            fout.flush();
            fout.close();
            System.out.println("File was received");

        } catch (IOException e) {
            System.out.println("Uploading error");
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void download(String fileName, InetAddress inetAddress, int port) {
        System.out.println("Downloading...");
        File file = new File(fileName);
        byte[] buf = new byte[63*1024];
        DatagramPacket filePacket = new DatagramPacket(buf, buf.length, inetAddress, port);
        try {
            if (!file.exists()) {
                System.out.println("File not found");
                filePacket.setData("File not found".getBytes());
                socket.send(filePacket);

            } else {
                System.out.println("File found");
                filePacket.setData("File found".getBytes());
                socket.send(filePacket);

                FileInputStream fin = new FileInputStream(file);
                fin.read(buf);
                filePacket.setData(buf);

                long startTimeStamp = System.nanoTime();
                socket.send(filePacket);
                long endTimeStamp = System.nanoTime();

                System.out.println("Time " + (endTimeStamp - startTimeStamp) / 1000000000 + " seconds");

            }
        } catch (IOException e) {
            System.out.println("Downloading error");
        }
    }
}
