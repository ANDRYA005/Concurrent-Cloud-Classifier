// Parallel program

import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Vector;
import java.lang.Math;
import java.util.concurrent.ForkJoinPool;

public class CloudDataThreaded {

	static Vector <Float> [][][] advection; 								  // in-plane regular grid of wind vectors, that evolve over time
	static float [][][] convection;													  // vertical air movement strength, that evolves over time
	static Integer [][][] classification; 			 							// cloud type per grid point, evolving over time
	static int dimx, dimy, dimt;   					 									// data dimensions
	static float aveX,aveY;																		// prevailing wind
	static int[] numThreads;																	// storing the number of threads used for one run
	static final ForkJoinPool fjPool = new ForkJoinPool();

	// range of sequential cutoffs to test
	static int [] thresholds = {400,800,1200,1600,2000,2400,2800,3200,3600,4000};

	static long results [];																		// runtimes
	static int[] resultsNumThreads;														// storing the number of threads used for each run

  /** Main method for reading in dataset, performing the necessary operations and writing out the results.
  * @param args args[0] is the filename to read in, args[1] is the filename to write to.
  */
	public static void main(String [] args){
		readData(args[0]);																				// reading in data file
		System.gc();																							// disabling garbage collection
		results = new long[15];
		resultsNumThreads = new int[15];
		numThreads = new int[1];

		// looping over all the test thresholds
		for (int threshold : thresholds){

			// executing 15 tests for comparison
			for (int i = 0;i<15;i++){
				numThreads[0] = 0;
				Float[] prevWind = new Float[2];
				long now = System.currentTimeMillis(); 									// time before execution

				prevWind = classifyThreaded(threshold);									// method to begin parallel execution

				// calculating averages for prevailing wind
				aveX = prevWind[0]/dim();
				aveY = prevWind[1]/dim();

				long after = System.currentTimeMillis();  							// time after execution
				long difference = after - now;													// runtime
				System.out.println("Run: " + i + " Program took " +
														difference +
														" milliseconds");
				results[i] = difference;
				resultsNumThreads[i] = numThreads[0];
			}
		}

		writeData(args[1]);																					// writing to output file
	}

	/** Method to calculate the prevailing wind and cloud classifcation in parallel.
	* @param threshold the sequential cutoff being tested.
	* @return A float array containing the sum of all the x and y components.
	*/
	static Float[] classifyThreaded(int threshold){
		return fjPool.invoke(new SumLocal(dimx,dimy,advection,convection,classification,0,dim(),threshold,numThreads));
	}



	/** Method to calculate overall number of elements in the timeline grids.
	* @return the number of elements.
	*/
	static int dim(){
		return dimt*dimx*dimy;
	}


	/** Method to read cloud simulation data from file.
	* @param fileName the filename to read in.
	*/
	static void readData(String fileName){
		float sumX=0;
		float sumY=0;
		try{
			Scanner sc = new Scanner(new File(fileName), "UTF-8");

			// input grid dimensions and simulation duration in timesteps
			dimt = sc.nextInt();
			dimx = sc.nextInt();
			dimy = sc.nextInt();

			// initialize and load advection (wind direction and strength) and convection
			advection = new Vector[dimt][dimx][dimy];
			convection = new float[dimt][dimx][dimy];
			for(int t = 0; t < dimt; t++)
				for(int x = 0; x < dimx; x++)
					for(int y = 0; y < dimy; y++){
						advection[t][x][y] = new Vector<Float>();
						advection[t][x][y].add(sc.nextFloat());
						advection[t][x][y].add(sc.nextFloat());
						convection[t][x][y] = sc.nextFloat();
					}

			classification = new Integer[dimt][dimx][dimy];
			sc.close();
		}
		catch (IOException e){
			System.out.println("Unable to open input file "+fileName);
			e.printStackTrace();
		}
		catch (java.util.InputMismatchException e){
			System.out.println("Malformed input file "+fileName);
			e.printStackTrace();
		}
	}


	/** Method to write classification output to file.
	* @param fileName the filename to write to.
	*/
	public static void writeData(String fileName){
		 try{
			 FileWriter fileWriter = new FileWriter(fileName);
			 PrintWriter printWriter = new PrintWriter(fileWriter);
			 printWriter.printf("%d %d %d\n", dimt, dimx, dimy);
			 printWriter.printf("%f %f\n", aveX, aveY);

			 for(int t = 0; t < dimt; t++){
				 for(int x = 0; x < dimx; x++){
					for(int y = 0; y < dimy; y++){
						printWriter.printf("%d ", classification[t][x][y]);
					}
				 }
				 printWriter.printf("\n");
		     }

			 printWriter.close();
		 }
		 catch (IOException e){
			 System.out.println("Unable to open output file "+fileName);
				e.printStackTrace();
		 }
	}


// LAST TWO METHODS WERE USED IN THE TESTING PROCESS //
// we decided to keep them for illustrative purposes //


	/** Method to write test runtime results to file.
	* @param fileName the filename to write to.
	* @param results an array containing all the runtime results.
	*/
	public static void writeTestData(String fileName, long [] results){
		 try{
			 FileWriter fileWriter = new FileWriter(fileName);
			 PrintWriter printWriter = new PrintWriter(fileWriter);
			 for (long result : results){
				 printWriter.printf("%d\n", result);
			 }

			 printWriter.close();
		 }
		 catch (IOException e){
			 System.out.println("Unable to open output file "+fileName);
				e.printStackTrace();
		 }
	}


	/** Method to write number of threads for tests to file.
	* @param fileName the filename to write to.
	* @param results an array containing all the number of threads results.
	*/
	public static void writeNumThreadsData(String fileName, int [] results){
		 try{
			 FileWriter fileWriter = new FileWriter(fileName);
			 PrintWriter printWriter = new PrintWriter(fileWriter);
			 for (long result : results){
				 printWriter.printf("%d\n", result);
			 }

			 printWriter.close();
		 }
		 catch (IOException e){
			 System.out.println("Unable to open output file "+fileName);
				e.printStackTrace();
		 }
	}

}
