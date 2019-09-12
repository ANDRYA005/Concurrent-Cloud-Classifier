/** SumLocal class to facilitate parallelism.
 * @author ANDRYA005
*/

import java.util.concurrent.RecursiveTask;
import java.util.Vector;
import java.lang.Math;

public class SumLocal extends RecursiveTask<Float[]>  {
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


		/** Constructor.
		* @param dimx x dimension of the data.
		* @param dimy y dimension of the data
		* @param advection 3D array containing all advection values.
		* @param convection 3D array containing all convection values.
		* @param classification array containing classifications.
		* @param low used to compute sequential cutoff.
		* @param hi used to compute sequential cutoff.
		* @param threshold sequential cutoff.
		* @param numThreads array counting the number of threads.
	  */
		SumLocal(int dimx,int dimy,Vector<Float>[][][] advection,float [][][] convection,Integer [][][] classification,int low,int hi,int threshold, int[] numThreads){
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



	/** Pararallizes both the cloud classification and prevailing wind calculations.
 	* @return A Float containing the sum of all x and y values.
	*/
	 protected Float[] compute(){
		 	numThreads[0]++;
			if((hi-low) < SEQUENTIAL_CUTOFF) {
					classifyAll();												// classifying clouds sequentially
					return prevailingWind();							// calculating prevailing wind sequentially
			}
			else{
				SumLocal left = new SumLocal(dimx, dimy, advection, convection, classification,low,(hi+low)/2,SEQUENTIAL_CUTOFF, numThreads);
				SumLocal right = new SumLocal(dimx, dimy, advection, convection, classification,(hi+low)/2,hi,SEQUENTIAL_CUTOFF,numThreads);
				left.fork();
				Float[] rightAns = right.compute();
				Float[] leftAns  = left.join();
				Float[] ans = new Float[2];							// storing the sum of the x- and y- component of advection for the prevailing wind calculation
				ans[0] = rightAns[0] + leftAns[0];
				ans[1] = rightAns[1] + leftAns[1];
				return ans;
			}
	 }



	/** Method to sum the x- and y- components of advection.
	* @return A Float containing the sum of all x and y values.
 	*/
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



	 /** Method to store cloud classifications.
	 */
	 public void classifyAll(){

		 for(int cell = low; cell < hi; cell++){
			 int[] coOrds = locate(cell);
			 int t = coOrds[0];
			 int x = coOrds[1];
			 int y = coOrds[2];

			 classification[t][x][y] = classify(convection[t][x][y],localAverage(coOrds));
		 }
	 }

	 /** Method to convert linear position into 3D location in simulation grid.
	 * @param pos the linear position of the data point.
	 * @return A Float containing the sum of all x and y values.
	 */
	 public int[] locate(int pos)
	 {
		 int[] ind = new int[3];
		 ind[0] = (int) pos / (dimx*dimy); // t
		 ind[1] = (pos % (dimx*dimy)) / dimy; // x
		 ind[2] = pos % (dimy); // y
		 return ind;
	 }

	 /** Method to used to classify a cell as 0, 1 or 2.
	 * @param lift the convection value.
	 * @param localWindAverage the local wind average at the point.
	 * @return An Integer representing the classification.
	 */
	 public Integer classify(double lift, float[] localWindAverage){
	 	double lenWind = Math.sqrt(localWindAverage[0]*localWindAverage[0] + localWindAverage[1]*localWindAverage[1]);
	 	if (Math.abs(lift)>lenWind)
	 		return 0;
	 	else if (0.2<lenWind)
	 		return 1;
	 	else
	 		return 2;
	 }

	 /** Method to calculate the local wind average of the observation with the 8 surrounding observations
	 * @param ind 3D location of cell.
	 * @return An float array containing the average of the x and y components.
	 */
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
					 sum[0] += advection[ind[0]][xInd][yInd].get(0);
					 sum[1] += advection[ind[0]][xInd][yInd].get(1);
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
