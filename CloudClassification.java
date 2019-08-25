/** CloudClassification for classifying clouds based on advection and convection.
 * @author ANDRYA005
*/

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.io.IOException;
import java.io.BufferedWriter;
import java.io.FileWriter;


public class CloudClassification {

     /** Main method for reading in dataset.
     * @param args <data file name> <output file name>.
     */
     public static void main(String[] args) {
         File file         = new File(args[0]);
         int lineNumber    = 0;
         int numTimeSteps  = 0;
         int airLayerXSize = 0;
         int airLayerYSize = 0;

         try{
         Scanner inputStream = new Scanner(file);
             while(inputStream.hasNext()){
               String line = inputStream.nextLine();
               System.out.println(line);
               if (lineNumber == 0){
                 String[] values = line.split(" ");
                 numTimeSteps  = Integer.valueOf(values[0]);
                 airLayerXSize = Integer.valueOf(values[1]);
                 airLayerYSize = Integer.valueOf(values[2]);
                 break;
               }

               String[] values    = line.split(" ");
               String[] newValues = new String[3];
             }
             inputStream.close();
         }
         catch (FileNotFoundException e) {
             e.printStackTrace();
         }
      }
}
