package server.TCP;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@SuppressWarnings("ALL")
public class ServerTCP implements Runnable {
    private List<Connection> brokenConnections;

    private ServerSocket server;
    private Socket client;
    private Connection connection;

    DataOutputStream out;
    DataInputStream in;

    public ServerTCP() throws IOException {
        brokenConnections = new ArrayList<>();
        server = new ServerSocket(3345);//  стартуем сервер на порту 3345
    }

    @Override
    public void run() {
        System.out.println("TCP server is running ");
        try {
            while (true) {
                client = server.accept();
                connection = new Connection(client.getInetAddress());
                out = new DataOutputStream(client.getOutputStream());
                in = new DataInputStream(client.getInputStream());
                System.out.print("Connection accepted ");

                if (!brokenConnections.isEmpty()) {
                    boolean isBroken = false;
                    for (Connection s : brokenConnections
                    ) {
                        System.out.println(s.getClientAddress());
                        System.out.println("down " + s.isDownloading());
                        System.out.println("up " + s.isUploading());

                        if (client.getInetAddress().equals(s.getClientAddress())) {
                            isBroken = true;
                            out.writeUTF("Last connection wasn't stable. ");
                            out.flush();

                            if (s.isUploading()) {
                                System.out.println(s.getUploadingFileName());
                                out.writeUTF("Last uploading was broken. File " + s.getUploadingFileName());
                                out.flush();
                                brokenConnections.remove(s);
                                if (brokenConnections.isEmpty()) break;
                            }
                            if (s.isDownloading()) {
                                System.out.println(s.getDownloadFileName());
                                out.writeUTF("Last downloading was broken.");
                                out.flush();
                                Thread.sleep(1000);
                                out.writeUTF(s.getDownloadFileName());
                                out.flush();
                                Thread.sleep(1000);

                                download(s, new File(s.getDownloadFileName()));

                                brokenConnections.remove(s);
                                if (brokenConnections.isEmpty()) break;
                            }
                            if (!s.isDownloading() && !s.isUploading()) {
                                out.writeUTF("Files are not lost.");
                                brokenConnections.remove(s);
                                if (brokenConnections.isEmpty()) break;
                            }
                        }

                    }
                    if (!isBroken) {
                        System.out.println("Last connection was stable.");
                        out.writeUTF("Last connection was stable.");
                        out.flush();
                    }
                } else {
                    System.out.println("First connection or last connection was stable.");
                    out.writeUTF("First connection.");
                    out.flush();
                }

                while (!client.isClosed()) {

                    try {
                        String entry = in.readUTF();
                        System.out.println("READ from client message - " + entry);
                        boolean commandIsExist = false;

                        if (entry.startsWith("echo")) {
                            out.writeUTF("Server reply - " + entry.replace("echo", "") + " - OK");
                            out.flush();
                            commandIsExist = true;
                        }
                        if (entry.startsWith("caps")) {
                            out.writeUTF("Server reply - " + entry.replace("caps", "").toUpperCase() + " - OK");
                            out.flush();
                            commandIsExist = true;
                        }
                        if (entry.startsWith("time")) {
                            time();
                            commandIsExist = true;
                        }
                        if (entry.startsWith("quit")) {
                            out.writeUTF("Server reply - OK");
                            closeConnection();
                            commandIsExist = true;
                        }
                        if (entry.startsWith("upload")) {
                            upload();
                            commandIsExist = true;
                        }
                        if (entry.startsWith("download")) {
                            String fileName = entry.replace("download", "").trim();
                            File file = new File(fileName);
                            if (!file.exists()) {
                                System.out.println("File not found");
                                out.writeUTF("Server reply - File not found - OK");
                            } else {
                                download(connection, file);
                                commandIsExist = true;
                            }
                        }
                        if (!commandIsExist) {
                            System.out.println("Undefined command");
                            out.writeUTF("Undefined command");
                            out.flush();
                        }
                        if(Thread.interrupted()){
                            closeConnection();
                            stopServer();
                            return;
                        }

                    } catch (IOException e) {
                        System.out.println("Disconect...");
                        connection.setWasBroken(true);
                        boolean wasBroken = false;
                        for (Connection s : brokenConnections
                        ) {
                            if (connection.equals(s)) {
                                brokenConnections.remove(s);
                                brokenConnections.add(connection);
                                wasBroken = true;
                            }
                        }
                        if (!wasBroken) brokenConnections.add(connection);
                        break;
                    }
                }
            }
        } catch (IOException | InterruptedException e) {
            System.out.println("Disconect...");
        }
    }

    private void closeConnection() throws IOException {
        out.flush();
        in.close();
        out.close();
        client.close();
        System.out.println("Closing connections & channels - DONE.");
    }
    private void stopServer() throws IOException {
        server.close();
    }


    private void time() throws IOException {
        System.out.println("Client wants to know the time");
        Date date = new Date();
        System.out.println(date.toString());
        out.writeUTF("Server reply - " + date.toString() + " - OK");
        out.flush();
    }

    private void upload() {
        connection.setUploading(true);
        try {
            long fileSize = in.readLong();
            String fileName = in.readUTF().replace("upload", "").trim();
            byte[] buffer = new byte[64 * 1024];

            connection.setUploadingFileName(fileName);
            FileOutputStream fout = new FileOutputStream(fileName);
            int count, total = 0;

            long startTimeStamp = System.nanoTime();
            while ((count = in.read(buffer)) != -1) {
                total += count;
                fout.write(buffer, 0, count);
                if (total == fileSize) {
                    break;
                }
            }
            long endTimeStamp = System.nanoTime();
            fout.flush();
            fout.close();
            System.out.println("File was received");
            out.writeUTF("Server reply - File was received by server - OK");
            out.flush();
            connection.setUploading(false);
            System.out.println("Speed " + total + " bytes in " + (endTimeStamp - startTimeStamp) / 1000000000 + " seconds");
        } catch (IOException e) {
            System.out.println("Disconect...");
        }
    }

    private void download(Connection connection, File file) {
        try {
            out.writeUTF("Server reply - File found - OK");

            connection.setDownloading(true);
            connection.setDownloadFileName(file.getName());
            Thread.sleep(1000);

            out.writeLong(file.length());
            FileInputStream fin = new FileInputStream(file);
            byte[] buffer = new byte[64 * 1024];
            int count, total = 0;
            long startTimeStamp = System.nanoTime();
            while ((count = fin.read(buffer)) != -1) {
                total += count;
                out.write(buffer, 0, count);
            }
            long endTimeStamp = System.nanoTime();
            out.flush();
            fin.close();
            String answer = in.readUTF();
            if (answer.equals("File was received")) {
                connection.setDownloading(false);
                out.writeUTF("Server reply - OK");
                System.out.println("Speed " + total + " bytes in " + (endTimeStamp - startTimeStamp) / 1000000000 + " seconds");

            }
        } catch (InterruptedException | IOException e) {
            System.out.println("Disconect...");
        }
    }
}
