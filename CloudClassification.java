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
         File fileTest     = new File("FirstTest.txt");
         File fileOutput   = new File("largesample_output.txt");
         int lineNumber    = 0;
         int numTimeSteps  = 0;
         int airLayerXSize = 0;
         int airLayerYSize = 0;

         try{
         Scanner inputStreamTest = new Scanner(fileTest);
         Scanner inputStreamOutput = new Scanner(fileOutput);
             while(inputStreamTest.hasNext()){
               String lineTest = inputStreamTest.nextLine();
               String lineOutput = inputStreamOutput.nextLine();

               if (!(lineTest.equals(lineOutput))){
                 if (lineNumber!=1){
                   System.out.println("Error! Line, " + lineNumber);
                   break;
                 }
               }
               lineNumber++;
             }
             inputStreamTest.close();
             inputStreamOutput.close();
         }
         catch (FileNotFoundException e) {
             e.printStackTrace();
         }
      }
}
