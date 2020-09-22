# Concurrent Cloud Classifier

Making use of the output of a simple weather simulation to perform an analysis to determine the prevailing wind direction and the types of clouds that might result.

The data corresponds to a single layer of air as it evolves over time. This air layer at a particular time is represented as a regular two-dimensional matrix with each matrix entry consisting of three floating point values. The first two elements represent the x- and ycoordinate components of a wind vector (w) and the third element is a lift value (u) representing the rate at which air shifts upwards or downwards. These terms are sometimes also called advection (w) and convection (u). Each time step will have a new matrix with changing data. Given a set of rules, this data was then used to determine the clouds that might occur and the prevailing wind direction.


## Concurrency 

The *Map Pattern* was applied to assign the type of cloud that is likely to form in that location based on a comparison of the local average wind direction and uplift value. This was suitable because we were able to operate on each location independently without having to combine results. The *Reduce Pattern* was used to calculate the prevailing wind over all of the locations and time steps. This pattern was suitable because we needed to produce a single answer (the sum of all x advection values and y advection values) from an array via addition (an associative operator).

For more details on the implementation and results, see *Report.pdf*


## A few things to note when running the scripts:

1. After running the ```make``` command, all the necessary compilations of source files occurs.

2. Do not call "make run" because it does not allow you to pass command-line arguments in. Rather run the program by calling ```java CloudDataThreaded <input_file> <output_file>``` from the bin directory. You will then see the run times for all of the tests in the terminal.

	* \<input_file> - The data related to the cloud classifications and prevailing winds. See "simplesample_input.txt" for an example.
	* \<output_file> - The true cloud classifications and prevailing winds. See "simplesample_output.txt" for an example.

3. Ensure the input file is in the bin directory.

4. I have included a directory called "Other Java" which contains two classes:

	* CloudClassification.java - was used to compare the produced output to the given output.
	* SampleGenerator.java - used to generate input data files.
