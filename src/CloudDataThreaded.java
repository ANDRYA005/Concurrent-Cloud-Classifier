// package cloudscapes;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Vector;
import java.lang.Math;
import java.util.concurrent.ForkJoinPool;

public class CloudDataThreaded {

	static Vector <Float> [][][] advection;  // in-plane regular grid of wind vectors, that evolve over time
	static float [][][] convection;					 // vertical air movement strength, that evolves over time
	static Integer [][][] classification; 			 // cloud type per grid point, evolving over time
	static int dimx, dimy, dimt;   					 // data dimensions
	static float aveX,aveY;
	static int[] numThreads;
	static final ForkJoinPool fjPool = new ForkJoinPool();
	// static int [] thresholds = {0,100,200,300,400,500,600,700,800,900,1000};

// NEED THIS!!
	static int [] thresholds = {400,800,1200,1600,2000,2400,2800,3200,3600,4000};

	static long results [];
	static int[] resultsNumThreads;


	public static void main(String [] args){
		String filename = "CreatedFile_200000_Windows.txt";
		readData(filename);
		System.gc();																		// disabling garbage collection
		results = new long[15];
		resultsNumThreads = new int[15];
		numThreads = new int[1];


		// int threshold = 400;

		// NEED THIS!
		for (int threshold : thresholds){
			for (int i = 0;i<15;i++){
				numThreads[0] = 0;
				Float[] prevWind = new Float[2];
				long now = System.currentTimeMillis(); 				// time before execution

				// prevWind = prevailingThreaded();
				prevWind = classifyThreaded(threshold);
				aveX = prevWind[0]/dim();
				aveY = prevWind[1]/dim();
				// classification = classifyThreaded(threshold);

				long after = System.currentTimeMillis();  		// time after exectution
				long difference = after - now;
				System.out.println("Run: " + i + " Program took " +
														difference +
														" milliseconds");
				results[i] = difference;
				resultsNumThreads[i] = numThreads[0];
			}
			writeTestData("Threaded_"+threshold + "_Large_Windows.txt",results);
			writeNumThreadsData("Threaded_"+threshold + "_Large_NumThreads_Windows.txt",resultsNumThreads);
		}

		// writeData("FirstTest.txt");
	}


	static Float[] classifyThreaded(int threshold){
		return fjPool.invoke(new SumLocal(dimx,dimy,advection,convection,classification,0,dim(),threshold,numThreads));
	}

	// static Float[] prevailingThreaded(){
		// return fjPool.invoke(new PrevailingWindThread(advection,0,dim(),dimx,dimy));
	// }

	// public static void classifyAll(){
	// 	for(int cell = 0; cell < dim(); cell++){
	// 		int[] coOrds = locate(cell);
	// 		int t = coOrds[0];
	// 		int x = coOrds[1];
	// 		int y = coOrds[2];
	// 		classification[t][x][y] = classify(convection[t][x][y],localAverage(coOrds));
	// 	}
	// }

	// overall number of elements in the timeline grids
	static int dim(){
		return dimt*dimx*dimy;
	}

	// // convert linear position into 3D location in simulation grid
	// public static int[] locate(int pos)
	// {
	// 	int[] ind = new int[3];
	// 	ind[0] = (int) pos / (dimx*dimy); // t
	// 	ind[1] = (pos % (dimx*dimy)) / dimy; // x
	// 	ind[2] = pos % (dimy); // y
	// 	return ind;
	// }

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

	// // used to classify a cell as 0, 1 or 2.
	// public static int classify(double lift, float[] localWindAverage){
	// 	double lenWind = Math.sqrt(localWindAverage[0]*localWindAverage[0] + localWindAverage[1]*localWindAverage[1]);
	// 	if (Math.abs(lift)>lenWind)
	// 		return 0;
	// 	else if (0.2<lenWind)
	// 		return 1;
	// 	else
	// 		return 2;
	// }

	// // calculates the local wind average of the observation with the 8 surrounding observations
	// public static float[] localAverage(int[] ind){
	// 	// ind[1] is x, ind[2] is y
	// 	float sum[] = new float[2];
	// 	int divisor = 0;
	//
	// 	for (int xInd=ind[1]-1; xInd<ind[1]+2; xInd++){
	// 		for (int yInd=ind[2]-1; yInd<ind[2]+2;yInd++){
	// 			if (xInd<0 || xInd>dimx-1){
	// 				break;
	// 			}
	// 			if (yInd<0 || yInd>dimy-1){
	// 				continue;
	// 			}
	// 			else{
	// 				divisor+=1;
	// 				sum[0] += advection[ind[0]][xInd][yInd].get(0);
	// 				sum[1] += advection[ind[0]][xInd][yInd].get(1);
	// 			}
	// 		}
	//
	// 	}
	// 	if (divisor!=0){
	// 		sum[0] = sum[0]/divisor;
	// 		sum[1] = sum[1]/divisor;
	// 	}
	// 	return sum;
	// }


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
