package edu.nyu.cs9053.homework11;

import java.io.IOException;
import java.util.Random;
import java.util.ArrayList;
import java.util.List;
import java.nio.charset.Charset;
import java.io.File;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.FileInputStream;


/**
 * User: blangel
 * Date: 11/23/14
 * Time: 4:31 PM
 */
public class FileExtractor {
    
    private static final String FILEPATH = "src/main/resources/Moby Dick.txt";
    
    private static final Random random = new Random();
    
    private static final Charset UTF8 = Charset.forName("UTF-8");

    public static String getRandomLine(){
        List<String> lineList = new ArrayList<>();
        int count = 0;
        try(BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(new FileInputStream(new File(FILEPATH)), UTF8))){
            String lineTxt = null;
            while((lineTxt = bufferedReader.readLine()) != null){
                lineList.add(lineTxt);
                count++;
            }
        }catch(IOException ioe){
            System.out.printf("Failed to read -- %s%n", ioe.getMessage());
        }
        return ("A line from Book Moby Dick.txt: "+ (lineList.get(random.nextInt(count)) == null ? "Fail to read" : lineList.get(random.nextInt(count))));
    }
    
}
