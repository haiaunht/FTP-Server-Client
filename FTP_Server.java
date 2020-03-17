package ftp_server;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Hai-Au Bui, Assignment 6 - FTP CSD 322, Professor Abbott Directory
 * "serverFile" will contains all testing file/files received form client
 */
public class FTP_Server {

    public static void main(String[] args) throws Exception {
        final int POOL_OF_20 = 2;

        // create server socket with port 59898, and a pool of 20 connections
        try (ServerSocket serverSocket = new ServerSocket(59898)) {
            System.out.println("The FTP server is running....");
            ExecutorService pool = Executors.newFixedThreadPool(POOL_OF_20);
            while (true) {
                pool.execute(new FTP_Server_Class(serverSocket.accept()));
            }
        } catch (IOException e) {
            System.out.println("Server exception" + e.getMessage());
        }
    }

    private static class FTP_Server_Class implements Runnable {

        private Socket socket;
        final String QUIT = "bye";
        String commands = "";
        File directory = new File("serverFile");

        //constructor Reverser will instanciate a Socket ojbect
        public FTP_Server_Class(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            String userName = "";
            String password = "";
            final String LEGIT_USERNAME = "anonymous";

            System.out.println("Connected to: " + socket.getInetAddress());
            try {
                //create inputStreamReader object to read the input stream
                BufferedReader buffer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                //output stream, and auto flush the output string with true boolean
                PrintWriter output = new PrintWriter(socket.getOutputStream(), true);

                output.println("Username: ");
                while (true) {
                    userName = buffer.readLine();
                    if (userName.equals(LEGIT_USERNAME)) {
                        output.println("Password: ");
                        password = buffer.readLine();
                        output.println("ftp> ");

                        //using while loop to keep prompting client for command 
                        while (!(commands = buffer.readLine()).equals("")) {
                            takeCommandsFromClient(commands);
                        }
                        commands = buffer.readLine();

                    } else {
                        output.println("...huh?...");
                    }
                    //create folder name "serverFile" in my project
                    directory.mkdir();
                }

            } catch (IOException e) {
                System.out.println("From run() " + e.getMessage());
            }
        }

        public void takeCommandsFromClient(String commands) {
            try {
                //output stream, and auto flush the output string with true boolean
                PrintWriter output = new PrintWriter(socket.getOutputStream(), true);
                ArrayList<String> arrayList;

                //while ( true ) {
                arrayList = commandLine(commands);

                switch (arrayList.size()) {
                    case 1:
                        oneCommands(arrayList.get(0));
                        break;
                    case 2:
                        twoCommands(arrayList.get(0), arrayList.get(1));
                        break;
                    case 3:
                        threeCommands(arrayList.get(0), arrayList.get(1), arrayList.get(2));
                        break;
                    default:
                        output.println("Invalid command.");
                }
            } catch (IOException e) {
                System.out.println("From takeCommands() " + e.getMessage());
            }

        }

        //commandLine() method will break the command into parts
        private static ArrayList<String> commandLine(String command) {
            ArrayList listOfCommand = new ArrayList<>();
            String[] breakCommand = command.split("\\s");

            for (String s : breakCommand) {
                listOfCommand.add(s);
            }
            return listOfCommand;
        }

        //apply for dir, help commands and exit server
        private void oneCommands(String str) {
            try {
                PrintWriter output = new PrintWriter(socket.getOutputStream(), true);
                switch (str) {
                    case "dir":
                        File[] listOfMyFiles = directory.listFiles();
                        if (listOfMyFiles.length == 0) {
                            output.println("There is no file in the directory.");
                        } else {
                            String listOfFile = "";
                            for (int i = 0; i < listOfMyFiles.length; i++) {
                                listOfFile += listOfMyFiles[i] + "!";
                            }
                            output.println(listOfFile);
                        }
                        break;
                    case "help":
                        output.println("The list of commands are:!"
                                + "\t+ get <fileName>!\t+ put <fileName>!\t+ dir!\t+ help!"
                                + "\t+ del <fileName>!\t+ rename <from> <to>");
                        break;
                    case "bye":
                        output.println("From the server side: Bye...");
                        try {
                            socket.close();
                        } catch (Exception e) {
                            System.out.println(e.getMessage());
                        }
                        break;
                    default:
                        output.println("...?...");
                        break;
                }
            } catch (IOException e) {
                System.out.println("From oneCommand() " + e.getMessage());
            }

        }

        
        //apply for get <fileName>, put <fileName> and del <fileName>
        private void twoCommands(String command, String file) {
            try {
                PrintWriter output = new PrintWriter(socket.getOutputStream(), true);
                switch (command) {
                    case "get":
                        File fileToSend = new File(directory.getPath() + "\\" + file + ".txt");
                        if (!fileToSend.exists()) {
                            output.println("...?...");

                        } else {
                            //send file to client
                            DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
                            FileInputStream fileInput = new FileInputStream(fileToSend);
                            BufferedReader buffer = new BufferedReader(new InputStreamReader(fileInput));
                                                        
                            String line = buffer.readLine();
                            while (line != null) {
                                dataOutputStream.writeBytes(line);                                
                                line = buffer.readLine();
                            }
                            buffer.close();
                            fileInput.close();
                            output.println(".");
                        }
                        break;
                    case "put":
                        if (file.equals("")) {
                            output.println("...?...!");
                        } else {
                            //receive file from client
                            Scanner readFromFile = new Scanner(socket.getInputStream());
                            PrintWriter fileOutput = new PrintWriter( directory.getPath() + "/receiveFromClient.txt");

                            while (readFromFile.hasNextLine()) {
                                fileOutput.println(readFromFile.nextLine());
                                fileOutput.close();
                                
                                output.println("Send the file to server successfully.!ftp> ");                                
                                break;
                            }
                        }
                        break;
                    case "del":
                        File fileToDeleted = new File( directory.getPath() + "\\" + file + ".txt");
                        if (fileToDeleted.delete()) {
                            output.println("Deleted the file: " + directory.getPath() + "\\" + file + ".txt");
                        } else {
                            output.println("...huh?...!");
                        }
                        break;
                    default:
                        output.println("...?...!");
                        break;
                }
            } catch (IOException e) {
                System.out.println("From twoCommand() " + e.getMessage());
            }
        }

        //apply for rename <fromFileName> <toFileName>
        private void threeCommands(String command, String fileName, String newFileName) {
            try {
                PrintWriter output = new PrintWriter(socket.getOutputStream(), true);
                File fileToRename = new File( directory.getPath() + "\\" + fileName + ".txt");
                File newFileNameFile = new File( directory.getPath() + "\\" + newFileName + ".txt");
                if (command.equals("rename") && fileToRename.exists()) {
                    //TODO, rename file to newFile
                    if (fileToRename.renameTo(newFileNameFile)) {
                        output.println("Rename: " + directory.getPath() + "\\" + fileName + ".txt TO " 
                                + directory.getPath() + "\\" + newFileName + ".txt");
                    } else {
                        output.println("not successful.");
                    }
                } else {
                    output.println("...huh?...");
                }
            } catch (IOException e) {
                System.out.println("From threeCommand() " + e.getMessage());
            }
        }

    }
}
