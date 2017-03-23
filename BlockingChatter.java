package edu.nyu.cs9053.homework11;

import java.io.*;
import java.nio.charset.Charset;


public class BlockingChatter implements Chatter {
    
    private InputStream chatServerInput;
    
    private OutputStream chatServerOutput;
    
    private InputStream userInput;
    
    private static final Charset UTF8 = Charset.forName("UTF-8");

    public BlockingChatter(InputStream chatServerInput, OutputStream chatServerOutput, InputStream userInput) {
        this.chatServerInput = chatServerInput;
        this.chatServerOutput = chatServerOutput;
        this.userInput = userInput;
    }

    @Override public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                process();
            } catch (IOException ioe) {
                System.out.printf("Exception - %s%n", ioe.getMessage());
                Thread.currentThread().interrupt();
            }
        }
    }

    
    private void process() throws IOException {
        try{
            int availableAmount = chatServerInput.available();
            if(availableAmount != 0){
                byte[] into = new byte[availableAmount];
                int read = chatServerInput.read(into, 0, into.length);
                if(read == -1) {
                    System.out.printf("Stream is closed%n");
                }else{
                    String stringFromServer = new String(into, UTF8);
                    System.out.println(stringFromServer);
                    if(stringFromServer.length() == 24 && stringFromServer.substring(stringFromServer.length() - 5, stringFromServer.length() - 1).equals("java")){
                        byte[] txtSend = FileExtractor.getRandomLine().getBytes(UTF8);
                        chatServerOutput.write(txtSend, 0, txtSend.length);
                        chatServerOutput.flush();
                    }
                }
            }
        }catch(IOException ioe){
            System.out.printf("Failed to read -- %s%n", ioe.getMessage());
        }
        
        try{
            int availableAmount = userInput.available();
            if(availableAmount != 0){
                byte[] into = new byte[availableAmount];
                int read = userInput.read(into, 0, into.length);
                if(read == -1) {
                    System.out.printf("Stream is closed%n");
                }else{
                    chatServerOutput.write(into, 0, into.length);
                    chatServerOutput.flush();
                }
            }
        }catch(IOException ioe){
            System.out.printf("Failed to read -- %s%n", ioe.getMessage());
        }
    }
    
}
