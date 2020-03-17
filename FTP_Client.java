/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ftp_client;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

/**
 * @author HaiAu Bui, Assignment 6 - FTP CSD 322, Professor Abbott Directory
 * "myClientFile" contains testing files/ files received from server
 */
public class FTP_Client {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        File myFiles = new File("myClientFile");
        String readFromBuffer;

        //TODO: after testing with localhost, come back to this
//        if(args.length != 1){
//            System.err.println("Pass the server IP as the sole sommand line argument");
//            return;
//        }
        //use to test local host without turn off firewall
        try (Socket socket = new Socket("localhost", 59898)) {
            //create bufferdReader object to reader the input stream
            BufferedReader buffer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter sendToPrintWritter = new PrintWriter(socket.getOutputStream(), true);
            //creat scanner object to read the client's input
            Scanner scanner = new Scanner(System.in);
            String clientInput = "";
            myFiles.mkdir();

            while (true) {
                readFromBuffer = buffer.readLine();
                
                //display the output stream
                displayOutput( readFromBuffer );

                //prompt for the next command
                clientInput = scanner.nextLine();
                sendToPrintWritter.println(clientInput);

                //receive file from server
                if (clientInput.contains("get")) {
                    Scanner readFromFile = new Scanner(socket.getInputStream());
                    PrintWriter fileOutput = new PrintWriter("myClientFile/receiveFromServer.txt");

                    while (readFromFile.hasNextLine()) {
                        fileOutput.println(readFromFile.nextLine());
                        fileOutput.println();
                        fileOutput.close();
                        break;
                    }

                    if (InvalidFile("myClientFile/receiveFromServer.txt")) {
                        System.out.println("Wrong file");
                        sendToPrintWritter.println("help");
                    } else {
                        System.out.println("Successfully saving file");
                        sendToPrintWritter.println("help");
                    }
                }

                //sending file to server
                if (clientInput.contains("put")) {
                    String[] fileName = clientInput.split("\\s");

                    //if the file is not exist, display message -- or else send it to server
                    if ( fileName.length == 1 || !isFileExist(fileName[fileName.length - 1])) {
                        System.out.println("The file name is not exist!\nftp> ");
                        sendToPrintWritter.println(scanner.nextLine());
                    } else {
                        sendFile(socket, myFiles.getPath() + "\\" + fileName[fileName.length - 1] + ".txt");
                    }
                }

            }

        } catch (IOException e) {
            System.out.println("Client error: " + e.getMessage());
        }         
    }
    

    private static boolean InvalidFile(String file) {
        File check = new File(file);
        boolean validate = false;
        try {
            BufferedReader buffer = new BufferedReader(new FileReader(check));
            String line = buffer.readLine();
            if (line.equals("...?...")) {
                validate = true;
            } else {
                validate = false;
            }
        } catch (IOException e) {
            e.getMessage();
        }
        return validate;
    }

    private static void sendFile(Socket socket, String fileToSend) throws IOException {
        PrintWriter output = new PrintWriter(socket.getOutputStream(), true);
        DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
        FileInputStream fileInput = new FileInputStream(fileToSend);
        BufferedReader buffer = new BufferedReader(new InputStreamReader(fileInput));

        //read file and send via socket
        String line = buffer.readLine();
        while (line != null) {
            dataOutputStream.writeBytes(line);
            line = buffer.readLine();
        }
        buffer.close();
        fileInput.close();
        output.println(".");
    }

    //isFileExists( filename ) method check if the name of file is exist in client's directory
    private static boolean isFileExist(String file) {
        File check = new File("myClientFile\\" + file + ".txt");
        if (check.exists()) {
            return true;
        } else {
            return false;
        }
    }

    //display output with multiple lines from server
    private static void displayOutput(String str) {
        String[] splitByDelimiter = str.split("!");
        for (String s : splitByDelimiter) {
            System.out.println(s);
        }
    }
}
