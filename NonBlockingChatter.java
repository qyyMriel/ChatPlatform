package edu.nyu.cs9053.homework11;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.Channel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.channels.Pipe;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;


/**
 * User: blangel
 * Date: 11/23/14
 * Time: 4:32 PM
 */
public class NonBlockingChatter implements Chatter {
    
    private static final int READ_BUFFER_SIZE = 1024;
    
    private static final Charset UTF8 = Charset.forName("UTF-8");
    
    private Selector selector;
    
    private final ByteBuffer readBuffer;
    
    private final ByteBuffer writeBuffer;
    
    SocketChannel chatServerChannel;
    
    Pipe.SourceChannel userInput;

    public NonBlockingChatter(SocketChannel chatServerChannel,
                              Pipe.SourceChannel userInput) throws IOException{
        this.chatServerChannel = chatServerChannel;
        this.userInput = userInput;
        this.selector = Selector.open();
        chatServerChannel.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
        userInput.register(selector, SelectionKey.OP_READ);
        readBuffer = ByteBuffer.allocate(READ_BUFFER_SIZE);
        writeBuffer = ByteBuffer.allocate(READ_BUFFER_SIZE);
        // TODO
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
    
    private void process() throws IOException{
        int events = selector.select();
        if(events < 1){
            return;
        }
        
        Set<SelectionKey>keys = selector.selectedKeys();
        Iterator<SelectionKey> iterator = keys.iterator();
        while (iterator.hasNext()){
            SelectionKey key = iterator.next();
            
            try {
                 if (key.isReadable()) {
                     if(key.channel() instanceof SocketChannel){
                         SocketChannel client = (SocketChannel) key.channel();
                         readBuffer.clear();
                         int result = client.read(readBuffer);
                         if (result == -1) {
                             key.cancel();
                             continue;
                         }
                         readBuffer.flip();
                         CharsetDecoder decoder = UTF8.newDecoder();
                         CharBuffer charBuffer = decoder.decode(readBuffer);
                         String dataReceived = charBuffer.toString();
                         
//                         if(dataReceived.substring(dataReceived.length() - 5,dataReceived.length() - 1).equals("java")){
//                             writeBuffer.put(FileExtractor.getRandomLine().getBytes());
//                         }
                         
                         System.out.printf("[%s] %s%n", client.getRemoteAddress().toString(), charBuffer.toString());
                         
                     }
                     else if(key.channel() instanceof Pipe.SourceChannel){
                         Pipe.SourceChannel client = (Pipe.SourceChannel) key.channel();
                         int result = client.read(writeBuffer);
                         if (result == -1) {
                             key.cancel();
                             continue;
                         }
                     }
                     else{
                         System.out.println("client Error");
                     }
                } else if (key.isWritable()) {
                    SocketChannel client = (SocketChannel) key.channel();
                    if(client == chatServerChannel){
                        if ((writeBuffer == null) || (writeBuffer.position() == 0)) {
                            continue;
                        }
                        writeBuffer.flip();
                        client.write(writeBuffer);
                        writeBuffer.clear();
                    }
                    
                }
            } finally {
                iterator.remove();
            }
        }
        
    }

}
