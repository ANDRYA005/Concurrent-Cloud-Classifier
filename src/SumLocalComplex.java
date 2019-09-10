// Class that extends RecursiveTask


import java.util.concurrent.RecursiveTask;
import java.util.Vector;
import java.lang.Math;

public class SumLocalComplex extends RecursiveTask<Float[]>  {
	  // int lo; // arguments
	  // int hi;
	  // int[] arr;
		Vector <Float> [][][] advection;   // in-plane regular grid of wind vectors, that evolve over time
		float [][][] convection;					 // vertical air movement strength, that evolves over time
		Integer [][][] classification;		 // storing classified clouds
		int hi;
		int low;
		int dimx;
		int dimy;
		static int SEQUENTIAL_CUTOFF;
		int[] numThreads;									 // storing the number of threads used


		// Constructor
		SumLocalComplex(int dimx,int dimy,Vector<Float>[][][] advection,float [][][] convection,Integer [][][] classification,int low,int hi,int threshold, int[] numThreads){
			this.dimx = dimx;
			this.dimy = dimy;
			this.advection = advection;
			this.convection = convection;
			this.classification = classification;
			this.low = low;
			this.hi = hi;
			this.SEQUENTIAL_CUTOFF = threshold;
			this.numThreads = numThreads;
		}

	 // Pararallizes both the cloud classification and prevailing wind calculations
	 protected Float[] compute(){
		 	numThreads[0]++;
			if((hi-low) < SEQUENTIAL_CUTOFF) {
					classifyAll();												// classifying clouds sequentially
					return prevailingWind();							// calculating prevailing wind sequentially
			}
			else{
				SumLocalComplex left = new SumLocalComplex(dimx, dimy, advection, convection, classification,low,(hi+low)/2,SEQUENTIAL_CUTOFF, numThreads);
				SumLocalComplex right = new SumLocalComplex(dimx, dimy, advection, convection, classification,(hi+low)/2,hi,SEQUENTIAL_CUTOFF,numThreads);
				left.fork();
				Float[] rightAns = right.compute();
				Float[] leftAns  = left.join();
				Float[] ans = new Float[2];							// storing the sum of the x- and y- component of advection for the prevailing wind calculation
				ans[0] = rightAns[0] + leftAns[0];
				ans[1] = rightAns[1] + leftAns[1];
				return ans;
			}
	 }

	 // Method to sum the x- and y- components of advection
	 public Float[] prevailingWind(){
		 Float [] prevailingWindArr = new Float[2];
		 prevailingWindArr[0] = (float)0;
		 prevailingWindArr[1] = (float)0;
		 for(int cell = low; cell < hi; cell++){
				 int[] coOrds = locate(cell);
				 int t = coOrds[0];
				 int x = coOrds[1];
				 int y = coOrds[2];
				 prevailingWindArr[0] += advection[t][x][y].get(0);
				 prevailingWindArr[1] += advection[t][x][y].get(1);
			}
			return prevailingWindArr;
	 }


	 // method to store cloud classifications
	 public void classifyAll(){

		 for(int cell = low; cell < hi; cell++){
			 int[] coOrds = locate(cell);
			 int t = coOrds[0];
			 int x = coOrds[1];
			 int y = coOrds[2];

			 classification[t][x][y] = classify(convection[t][x][y],localAverage(coOrds));
		 }
	 }

	 // convert linear position into 3D location in simulation grid
	 public int[] locate(int pos)
	 {
		 int[] ind = new int[3];
		 ind[0] = (int) pos / (dimx*dimy); // t
		 ind[1] = (pos % (dimx*dimy)) / dimy; // x
		 ind[2] = pos % (dimy); // y
		 return ind;
	 }

	 // used to classify a cell as 0, 1 or 2.
	 public Integer classify(double lift, float[] localWindAverage){
	 	double lenWind = Math.sqrt(localWindAverage[0]*localWindAverage[0] + localWindAverage[1]*localWindAverage[1]);
	 	if (Math.abs(lift)>lenWind)
	 		return 0;
	 	else if (0.2<lenWind)
	 		return 1;
	 	else
	 		return 2;
	 }

	 // calculates the local wind average of the observation with the 8 surrounding observations
	 public float[] localAverage(int[] ind){
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
}
