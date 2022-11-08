// 19 variant

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Server {
    /*
        API is very simple:
            check filename
                -- this will check all lines of file
            who
                -- this will output all possible commands, copyright and so on
            exit
                -- terminates connection with server
     */
    private final static int port = 1044;
    private final ServerSocket serverSocket;
    private Socket currentClient;

    public Server() throws IOException {
        serverSocket = new ServerSocket(port);

        File log = new File("Log.txt");
        log.createNewFile();
        serverSocket.setReuseAddress(true);
    }

    private void insertLog(String input) throws IOException {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
        LocalDateTime now = LocalDateTime.now();
        FileWriter fileWriter = new FileWriter("Log.txt", true);
        fileWriter.write(String.format("%s %20s\n", dtf.format(now), input));
        fileWriter.close();
    }

    private void parseInput(String input) throws IOException {
        // Inserting log
        try {
            insertLog(input);
        }catch (Exception e){
            System.out.println("Error occurred!");
        }

        String[] uCommand = input.split(" ");
        switch (uCommand[0]) {
            case "check":
                if (uCommand[1].equals("")){
                    PrintWriter printWriter = new PrintWriter(currentClient.getOutputStream());
                    printWriter.println("\\~");
                    printWriter.flush();
                    throw new RuntimeException("No name of the file were specified!");
                }else {
                    try {
                        checkQuasiFile(new File(uCommand[1]));
                    } catch (Exception exception) {
                    }

                    PrintWriter printWriter = new PrintWriter(currentClient.getOutputStream());
                    printWriter.println("\\~");
                    printWriter.flush();
                }
                break;
            case "who":
                PrintWriter printWriter = new PrintWriter(currentClient.getOutputStream());
                printWriter.println("Copyright Ihor Kostiuk, 2022");
                printWriter.flush();
                printWriter.println("\n" +
                        "        API is very simple:\n" +
                        "            check filename\n" +
                        "                -- this will check all lines of file\n" +
                        "            who\n" +
                        "                -- this will output all possible commands, copyright and so on\n" +
                        "            exit\n" +
                        "                -- terminates connection with server\n" +
                        "\\~");
                printWriter.flush();
                break;
            case "exit":
                try {
                    printWriter = new PrintWriter(currentClient.getOutputStream());
                    printWriter.println("\\~");
                    printWriter.flush();
                    System.out.println("User disconnected");
                } catch (IOException e) {
                    throw new RuntimeException(e);
                } finally {
                    currentClient.close();
                    currentClient = null;
                }
                break;
            default:
                printWriter = new PrintWriter(currentClient.getOutputStream());
                printWriter.println("\\~");
                printWriter.flush();
        }
    }

    private void checkQuasiFile(File file) throws IOException {

        PrintWriter printWriter = new PrintWriter(currentClient.getOutputStream());
        if(!file.exists()){
            throw new RuntimeException("File you provided doesn't exist!");
        }

        printWriter.println("accepted");
        printWriter.flush();

        file.createNewFile();

        Scanner scannerFile = new Scanner(file);
        Scanner scannerInput = new Scanner(currentClient.getInputStream());


        List<String> list = new ArrayList<>();
        while (scannerFile.hasNextLine()){
            String row = scannerFile.nextLine();
            String serverRow = scannerInput.nextLine();
            if(!checkQuasiRow(row, serverRow)){
                list.add(row); // Adding a row which is not quasiEqual
            }
            if(!scannerFile.hasNextLine()){
                printWriter.println("\\~");
                printWriter.flush();
            }else{
                printWriter.println("accepted");
                printWriter.flush();
            }
        }

        if(list.size() != 0){
            for(String row: list){
                if(!row.equals("")){
                    printWriter.println(row);
                    printWriter.flush();
                }
            }
        }else{
            printWriter.println("Files are quasiequal!");
            printWriter.flush();
        }
    }

    private boolean checkQuasiRow(String userRow, String serverRow){

        int ptr_l = 0;
        int ptr_r = 0;

        String newUserRow = userRow.toLowerCase().trim();
        String newServerRow = serverRow.toLowerCase().trim();

        while(ptr_r < newServerRow.length() && ptr_l < newUserRow.length()){
            if(newUserRow.charAt(ptr_l) == ' ')
                ptr_l++;
            else if(newServerRow.charAt(ptr_r) == ' ')
                ptr_r++;
            else if(newServerRow.charAt(ptr_r) != newUserRow.charAt(ptr_l)){
                return false;
            }else{
                ptr_r++;
                ptr_l++;
            }
        }

        return ptr_r >= newServerRow.length() && ptr_l >= newUserRow.length();
    }

    public void listen() throws IOException {
        if(currentClient == null){
            currentClient = serverSocket.accept();
            System.out.println("User connected.");
            insertLog("user connected");
        }

        Scanner scanner = new Scanner(currentClient.getInputStream());

        try{
            if(scanner.hasNext())
                parseInput(scanner.nextLine());
            else{
                scanner.close();
                insertLog("user disconnected");
                currentClient = null;
            }
        } catch (RuntimeException exception){
            System.out.println(exception.getMessage());
            PrintWriter printWriter = new PrintWriter(currentClient.getOutputStream());
            printWriter.println("\\~");
            printWriter.flush();
        }
    }

    public Socket getCurrentClient() {
        return currentClient;
    }

    public void setCurrentClient(Socket currentClient) {
        this.currentClient = currentClient;
    }
}
