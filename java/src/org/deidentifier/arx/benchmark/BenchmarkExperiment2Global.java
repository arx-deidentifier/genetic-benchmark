package org.deidentifier.arx.benchmark;

import java.io.IOException;
import java.util.List;

import org.deidentifier.arx.ARXConfiguration.AnonymizationAlgorithm;
import org.deidentifier.arx.benchmark.BenchmarkSetup.BenchmarkDataset;

/**
 * @author Thierry
 *
 *         Used for execute Benchmarks on high dim datasets with a continuously
 *         tracked utility improvement over time.
 *
 */
public class BenchmarkExperiment2Global extends AbstractBenchmark {

    BenchmarkExperiment2Global(String fileName) {
        super(fileName, true, true);
    }

    public static void main(String args[]) throws IOException {
        new BenchmarkExperiment2Global("results/Experiment2_kAnon_final.csv").start();
    }

    @Override
    public void generateTestConfigurations(List<TestConfiguration> testConfigs) {

        
        // Definition of properties that will be varied for the Benchmark
        AnonymizationAlgorithm[] algorithms = new AnonymizationAlgorithm[] { AnonymizationAlgorithm.BEST_EFFORT_BOTTOM_UP,
                                                                             AnonymizationAlgorithm.BEST_EFFORT_GENETIC,
                                                                             AnonymizationAlgorithm.BEST_EFFORT_TOP_DOWN 
                                                                             };

        BenchmarkDataset[] datasets = new BenchmarkDataset[] { BenchmarkDataset.CREDITCARD,
                                                               BenchmarkDataset.MACH2019,
                                                               BenchmarkDataset.SS13ACS };
        
        // AnonymizationAlgorithm[] algorithms = new AnonymizationAlgorithm[] {AnonymizationAlgorithm.BEST_EFFORT_GENETIC };

        // Number of testruns
        int testRuns = 6;

        // iterate through all possible configuration permutations
        for (int testRun = 0; testRun < testRuns; testRun++) {
            for (BenchmarkDataset dataset : datasets) {
                for (AnonymizationAlgorithm algorithm : algorithms) {

                    TestConfiguration testConfig = new TestConfiguration();

                    testConfig.algorithm = algorithm;
                    testConfig.timeLimit = 100000;
                    testConfig.dataset = dataset;
                    testConfig.testRunNumber = testRun;

                    testConfig.crossoverFraction = 0.4;
                    testConfig.mutationProbability = 0.05;

                    testConfig.privacyModel = PrivacyModel.K_ANONYMITY;

                    testConfig.useLocalTransformation = false;

                    if (testRun == 0) {
                        testConfig.writeToFile = false;
                        testConfig.timeLimit = 5000;
                    }
                    testConfigs.add(testConfig);
                }

            }
        }
    }

}
