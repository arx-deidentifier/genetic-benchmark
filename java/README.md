Benchmark
====

This directory contains the Java code required to run the anonymization experiments.

Requirements (Recommended)
------
- MS Windows
- Eclipse IDE
- Java 1.8.0 or newer

How to run
------
- Add ARX and SUBFRAME to your build path by including the jar-files contained in the "lib"-folder
- Make sure to include the classes contained in the "ext" folder of the benchmark project in the build path
- Run the individual benchmark classes contained in src\org\deidentifier\arx\benchmark

The benchmark classes refer to the experiments described in the paper as follows:

| Class | Description  |
|-|-|
BenchmarkExperiment1.java | Anonymization of **low-dimensional** datasets using **global generalization**
BenchmarkExperiment2Global.java | Anonymization of **high-dimensional** datasets using **global generalization** 
BenchmarkExperiment2Local.java | Anonymization of **high-dimensional** datasets using **local generalization** 