// Sequential program

import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Vector;
import java.lang.Math;

public class CloudDataComplex {

	static Vector <Float> [][][] advection; 					// in-plane regular grid of wind vectors, that evolve over time
	static float [][][] convection; 									// vertical air movement strength, that evolves over time
	static int [][][] classification; 								// cloud type per grid point, evolving over time
	static int dimx, dimy, dimt; 											// data dimensions
	static float aveX,aveY;														// prevailing wind
	static long results [];														// runtimes

	public static void main(String [] args){
		String filename = "CreatedFile_20000000_UNIX.txt";
		readData(filename);																			// reading in data file
		System.gc();																	  				// disabling garbage collection
		results = new long[15];

		for (int i = 0;i<15;i++){
			long now = System.currentTimeMillis(); 								// time before execution
			prevailingWind();
			// System.out.println("finished prev wind");
			classifyAll();																				// classification of all cells
			long after = System.currentTimeMillis();  						// time after exectution
			long difference = after - now;
			System.out.println("Run: " + i + " Program took " +
													difference +
													" milliseconds");
			results[i] = difference;
		}
		writeTestData("Sequential_Complex_Huge_Unix.txt",results);               // method to write test results to text file
		// writeData("ComplexTest.txt");																							 // writing to output file
	}

	// method to store cloud classifications
	public static void classifyAll(){
		for(int cell = 0; cell < dim(); cell++){
			int[] coOrds = locate(cell);
			int t = coOrds[0];
			int x = coOrds[1];
			int y = coOrds[2];
			classification[t][x][y] = classify(convection[t][x][y],localAverage(coOrds));
		}
	}

	// overall number of elements in the timeline grids
	static int dim(){
		return dimt*dimx*dimy;
	}

	// Method to sum the x- and y- components of advection
	public static void prevailingWind(){
		float sumX=0;
		float sumY=0;
		for(int t = 0; t < dimt; t++){
			for(int x = 0; x < dimx; x++){
				for(int y = 0; y < dimy; y++){
					// updating the x and y totals for calculating the average.
					sumX += advection[t][x][y].get(0);
					sumY += advection[t][x][y].get(1);
				}
			}
		}

		// calulating the averages for x and y respectively.
		aveX = sumX/dim();
		aveY = sumY/dim();

	}



	// convert linear position into 3D location in simulation grid
	public static int[] locate(int pos)
	{
		int[] ind = new int[3];
		ind[0] = (int) pos / (dimx*dimy); // t
		ind[1] = (pos % (dimx*dimy)) / dimy; // x
		ind[2] = pos % (dimy); // y
		return ind;
	}

	// read cloud simulation data from file
	static void readData(String fileName){
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

			classification = new int[dimt][dimx][dimy];
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

	// used to classify a cell as 0, 1 or 2.
	public static int classify(double lift, float[] localWindAverage){
		double lenWind = Math.sqrt(localWindAverage[0]*localWindAverage[0] + localWindAverage[1]*localWindAverage[1]);
		if (Math.abs(lift)>lenWind)
			return 0;
		else if (0.2<lenWind)
			return 1;
		else
			return 2;
	}

	// calculates the local wind average of the observation with the 8 surrounding observations
	public static float[] localAverage(int[] ind){
		float sum[] = new float[2];
		int divisor = 0;

		for (int xInd=ind[1]-1; xInd<ind[1]+2; xInd++){
			for (int yInd=ind[2]-1; yInd<ind[2]+2;yInd++){
				if (xInd<0 || xInd>dimx-1){
					break;
				}
				if (yInd<0 || yInd>dimy-1){
					continue;
				}
				else{
					divisor+=1;
					sum[0] += Math.exp(Math.log(Math.exp(Math.log(advection[ind[0]][xInd][yInd].get(0)))));
					sum[1] += Math.exp(Math.log(Math.exp(Math.log(advection[ind[0]][xInd][yInd].get(1)))));
				}
			}

		}
		if (divisor!=0){
			sum[0] = sum[0]/divisor;
			sum[1] = sum[1]/divisor;
		}
		return sum;
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


	// method to write results from tests to a file
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

}
