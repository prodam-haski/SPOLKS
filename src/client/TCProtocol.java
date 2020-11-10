package client;

import java.io.*;
import java.net.Socket;

public class TCProtocol implements Runnable {

    private Socket socket;
    private DataInputStream dataInputStream;
    private DataOutputStream dataOutputStream;

    private String answer;

    TCProtocol() throws IOException {
        socket = new Socket("127.0.0.1", 3345);
        dataInputStream = new DataInputStream(socket.getInputStream());
        dataOutputStream = new DataOutputStream(socket.getOutputStream());
    }

    @Override
    public void run() {
        System.out.println("Transmission Control Protocol is active");
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        try {
            answer = dataInputStream.readUTF();
            System.out.println("Client connected to socket.");
            System.out.println(answer);

            if (answer.startsWith("Last connection wasn't stable")) {
                answer = dataInputStream.readUTF();
                System.out.println(answer);
                if (answer.startsWith("Last down")) {
                    answer = dataInputStream.readUTF();
                    download("download" + answer);

                }
            }
            while (!socket.isOutputShutdown()) {
                if (br.ready()) {
                    boolean isSpecialCommand = false;
                    String clientCommand = br.readLine();
                    if (clientCommand.startsWith("upload")) {
                        isSpecialCommand = true;
                        upload(clientCommand);
                    }
                    if (clientCommand.startsWith("download")) {
                        isSpecialCommand = true;
                        download(clientCommand);

                    }
                    if (!isSpecialCommand) {
                        dataOutputStream.writeUTF(clientCommand);
                        dataOutputStream.flush();
                        System.out.println("Client sent message " + clientCommand + " to server.");
                    }
                    System.out.println(dataInputStream.readUTF());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void download(String clientCommand) {
        try {
            String fileName = clientCommand.replace("download", "").trim();
            dataOutputStream.writeUTF(clientCommand);
            answer = dataInputStream.readUTF();
            System.out.println(answer);
            if (answer.equals("Server reply - File found - OK")) {
                long fileSize = dataInputStream.readLong();
                FileOutputStream fout = new FileOutputStream(fileName);
                byte[] buffer = new byte[64 * 1024];
                int count, total = 0;

                long startTimeStamp = System.nanoTime();
                while ((count = dataInputStream.read(buffer)) != -1) {
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
                System.out.println("Speed " + total + " bytes in " + (endTimeStamp - startTimeStamp) / 1000000000 + " seconds");
                dataOutputStream.writeUTF("File was received");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void upload(String clientCommand) {
        String filePath = clientCommand.replace("upload", "").trim();
        File file = new File(filePath);
        try {
            if (!file.exists()) {
                System.out.println("File not found");
                dataOutputStream.writeUTF("File not found");
            } else {
                dataOutputStream.writeUTF("upload");
                dataOutputStream.writeLong(file.length());
                dataOutputStream.writeUTF(file.getName());

                FileInputStream fin = new FileInputStream(file);
                byte[] buffer = new byte[64 * 1024];
                int count;
                int total = 0;
                long startTimeStamp = System.nanoTime();
                while ((count = fin.read(buffer)) != -1) {
                    total += count;
                    dataOutputStream.write(buffer, 0, count);
                }
                long endTimeStamp = System.nanoTime();
                dataOutputStream.flush();
                System.out.println("Speed " + total + " bytes in " + (endTimeStamp - startTimeStamp) / 1000000000 + " seconds");
                fin.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
