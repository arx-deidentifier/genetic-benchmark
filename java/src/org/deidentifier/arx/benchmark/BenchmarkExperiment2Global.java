/*
 * Benchmark of ARX's Heuristic Algorithms
 * Copyright 2020 by Thierry Meurers and contributors
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.deidentifier.arx.benchmark;

import java.io.IOException;
import java.util.List;

import org.deidentifier.arx.ARXConfiguration.AnonymizationAlgorithm;
import org.deidentifier.arx.benchmark.BenchmarkSetup.BenchmarkDataset;

/**
 * Benchmark class defining tests for high-dimensional datasets with global transformation.
 * The utility improvement is tracked continuously over time.
 * 
 * @author Thierry Meurers
 */
public class BenchmarkExperiment2Global extends AbstractBenchmark {

    /**
     * Constructor
     * 
     * @param fileName
     */
    BenchmarkExperiment2Global(String fileName) {
        super(fileName, true, true);
    }

    /**
     * Entry point.
     * 
     * @param args the arguments
     */
    public static void main(String args[]) throws IOException {
        new BenchmarkExperiment2Global("results/Experiment2_kAnon_final.csv").start();
    }

    /**
     * {@inheritDoc}
     */
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
