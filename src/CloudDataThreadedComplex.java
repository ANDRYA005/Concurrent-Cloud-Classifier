// Parallel program

import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Vector;
import java.lang.Math;
import java.util.concurrent.ForkJoinPool;

public class CloudDataThreadedComplex {

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


	public static void main(String [] args){
		String filename = "CreatedFile_1800000_Windows.txt";
		readData(filename);																							// reading in data file
		System.gc();																								// disabling garbage collection
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

				long after = System.currentTimeMillis();  							// time after exectution
				long difference = after - now;													// runtime
				System.out.println("Run: " + i + " Program took " +
														difference +
														" milliseconds");
				results[i] = difference;
				resultsNumThreads[i] = numThreads[0];
			}
			writeTestData("Threaded_"+threshold + "_Extra_Windows_Complex.txt",results);              										// writing test runtimes to file
			// writeNumThreadsData("Threaded_"+threshold + "_Large_NumThreads_Windows.txt",resultsNumThreads);			// writing test threads used to file
		}

		writeData(args[1]);																					// writing to output file
	}

	// method to calculate the prevailing wing and cloud classifcation in parallel
	static Float[] classifyThreaded(int threshold){
		return fjPool.invoke(new SumLocalComplex(dimx,dimy,advection,convection,classification,0,dim(),threshold,numThreads));
	}


	// overall number of elements in the timeline grids
	static int dim(){
		return dimt*dimx*dimy;
	}


	// read cloud simulation data from file
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


	// write classification output to file
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


	// write test runtime results to file
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

	// write number of threads for tests to file
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
