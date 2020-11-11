package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;

public class UDProtocol implements Runnable {

    private DatagramSocket socket;
    private InetAddress inetAddress;
    private int port = 6600;

    private DatagramPacket receivePacket;
    private DatagramPacket sendPacket;

    private byte[] buf = new byte[64 * 1024];

    UDProtocol() throws UnknownHostException, SocketException {
        socket = new DatagramSocket();
        inetAddress = InetAddress.getByName("192.168.100.3");
        sendPacket = new DatagramPacket(buf, buf.length, inetAddress, port);
        sendPacket.setAddress(inetAddress);
        sendPacket.setPort(port);
    }

    @Override
    public void run() {
        System.out.println("User Datagram Protocol is active");
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        try {
            if (bufferedReader.ready()) {
                String clientCommand = bufferedReader.readLine();
                sendPacket.setData(clientCommand.getBytes());
                socket.send(sendPacket);
                socket.receive(receivePacket);
                System.out.println(receivePacket.getData().toString());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
