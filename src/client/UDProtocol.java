package client;

import java.io.*;
import java.net.*;
import java.util.Arrays;

public class UDProtocol implements Runnable {

    private InetAddress inetAddress;
    private int port;

    private String finalString;

    public UDProtocol() throws UnknownHostException {
        inetAddress = InetAddress.getByName("localhost");
        port = 6600;
    }

    @Override
    public void run() {
        System.out.println("User Datagram Protocol is active");
        DatagramSocket sock;
        BufferedReader cin = new BufferedReader(new InputStreamReader(System.in));

        try {
            sock = new DatagramSocket();

            while (true) {
                System.out.println("Введите сообщение серверу: ");
                String clientCommand = cin.readLine();
                byte[] b = clientCommand.getBytes();

                if (clientCommand.startsWith("upload") || clientCommand.startsWith("download")) {
                    if (clientCommand.startsWith("upload")) {
                        String filePath = clientCommand.replace("upload", "").trim();
                        File file = new File(filePath);

                        if (!file.exists()) System.out.println("File not found");
                        else {
                            byte[] buf = new byte[63 * 1024];
                            DatagramPacket filePacket = new DatagramPacket(buf, buf.length, inetAddress, port);
                            clientCommand += " " + file.length();
                            DatagramPacket dp = new DatagramPacket(b, b.length, inetAddress, port);
                            sock.send(dp);

                            long time = 0;
                            FileInputStream fileInputStream = new FileInputStream(file);
                            for (int i = 0, count = 0; count < file.length(); ) {
                                buf = new byte[63 * 1024];
                                fileInputStream.read(buf);
                                filePacket.setData(buf);
                                count += buf.length;

                                long startTimeStamp = System.nanoTime();
                                sock.send(filePacket);
                                long endTimeStamp = System.nanoTime();
                                time += endTimeStamp - startTimeStamp;
                                if (count >= file.length()) {
                                    fileInputStream.close();
                                    break;
                                }
                            }
                            System.out.println(file.length() + " bytes in " + time / 1000000000 + " seconds");
                        }
                    }
                    if (clientCommand.startsWith("download")) {
                        DatagramPacket dp = new DatagramPacket(b, b.length, inetAddress, port);
                        sock.send(dp);
                        byte[] buf = new byte[4096];
                        DatagramPacket filePacket = new DatagramPacket(buf, buf.length);

                        sock.receive(filePacket);
                        String response = Arrays.toString(filePacket.getData());
                        String fileName = clientCommand.replace("download", "").trim();
                        if (response.startsWith("File found")) {
                            try {
                                FileOutputStream fout = new FileOutputStream(fileName);
                                sock.receive(filePacket);
                                fout.write(filePacket.getData());
                                fout.flush();
                                fout.close();
                                System.out.println("File was received");
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                } else {
                    DatagramPacket dp = new DatagramPacket(b, b.length, inetAddress, port);
                    sock.send(dp);
                }

                byte[] buffer = new byte[65536];
                DatagramPacket reply = new DatagramPacket(buffer, buffer.length);

                sock.receive(reply);
                byte[] data = reply.getData();
                String answer = new String(data, 0, reply.getLength());

                System.out.println("Сервер: " + reply.getAddress().getHostAddress() + ", порт: " + reply.getPort() + ", получил: " + answer);
            }
        } catch (IOException e) {
            System.err.println("IOException " + e);
        }
    }

    public void request(String clientCommand) {
        DatagramSocket sock;
        byte[] b = clientCommand.getBytes();
        try {
            sock = new DatagramSocket();
            if (clientCommand.startsWith("upload") || clientCommand.startsWith("download")) {
                if (clientCommand.startsWith("upload")) {
                    String filePath = clientCommand.replace("upload", "").trim();
                    File file = new File(filePath);
                    if (!file.exists()) {
                        finalString = "File not found";
                        return;
                    } else {
                        byte[] buf = new byte[63 * 1024];
                        DatagramPacket filePacket = new DatagramPacket(buf, buf.length, inetAddress, port);
                        clientCommand += " " + file.length();
                        DatagramPacket dp = new DatagramPacket(b, b.length, inetAddress, port);
                        sock.send(dp);

                        long time = 0;
                        FileInputStream fileInputStream = new FileInputStream(file);
                        for (int i = 0, count = 0; count < file.length(); ) {
                            buf = new byte[63 * 1024];
                            fileInputStream.read(buf);
                            filePacket.setData(buf);
                            count += buf.length;

                            long startTimeStamp = System.nanoTime();
                            sock.send(filePacket);
                            long endTimeStamp = System.nanoTime();
                            time += endTimeStamp - startTimeStamp;
                            if (count >= file.length()) {
                                fileInputStream.close();
                                break;
                            }
                        }
                        finalString = file.length() + " bytes in " + time / 1000000000 + " seconds";
                    }
                }
                if (clientCommand.startsWith("download")) {
                    DatagramPacket dp = new DatagramPacket(b, b.length, inetAddress, port);
                    sock.send(dp);
                    byte[] buf = new byte[4096];
                    DatagramPacket filePacket = new DatagramPacket(buf, buf.length);

                    sock.receive(filePacket);
                    String response = Arrays.toString(filePacket.getData());
                    String fileName = clientCommand.replace("download", "").trim();
                    if (response.startsWith("File found")) {
                        try {
                            FileOutputStream fout = new FileOutputStream(fileName);
                            sock.receive(filePacket);
                            fout.write(filePacket.getData());
                            fout.flush();
                            fout.close();
                            finalString = "File was received";
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            } else {
                DatagramPacket dp = new DatagramPacket(b, b.length, inetAddress, port);
                sock.send(dp);
            }
            byte[] buffer = new byte[65536];
            DatagramPacket reply = new DatagramPacket(buffer, buffer.length);

            sock.receive(reply);
            byte[] data = reply.getData();
            String answer = new String(data, 0, reply.getLength());
            finalString = "Сервер: " + reply.getAddress().getHostAddress() + ", порт: " + reply.getPort() + ", получил: " + answer;

        } catch (IOException e) {
            finalString = "Failure";
        }
    }

    public String getFinalString() {
        return finalString;
    }
}
