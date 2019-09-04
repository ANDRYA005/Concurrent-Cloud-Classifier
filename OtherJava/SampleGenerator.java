import java.util.Random;
import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import java.io.FileWriter;
import java.io.PrintWriter;


public class SampleGenerator{

  public static void main(String [] args){
    writeData("CreatedFile_45000000_UNIX.txt",20,1500,1500);
  }


  public static void writeData(String fileName, int dimt, int dimx, int dimy){
		 try{
			 FileWriter fileWriter = new FileWriter(fileName);
			 PrintWriter printWriter = new PrintWriter(fileWriter);
			 printWriter.printf("%d %d %d\n", dimt, dimx, dimy);

			 for(int t = 0; t < dimt; t++){
				 for(int x = 0; x < dimx*dimy*3; x++){
						printWriter.printf("%.1f",new Random().nextFloat());
            printWriter.printf(" ");
				 }
				 printWriter.printf("\n\n");
		     }

			 printWriter.close();
		 }
		 catch (IOException e){
			 System.out.println("Unable to open output file "+fileName);
				e.printStackTrace();
		 }
	}

}
