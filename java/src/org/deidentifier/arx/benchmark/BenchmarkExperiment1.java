/*
 * Benchmark of ARX's Heuristic Algorithms
 * Copyright 2021 by Thierry Meurers and contributors
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
import org.deidentifier.arx.benchmark.AbstractBenchmark.PrivacyModel;
import org.deidentifier.arx.benchmark.BenchmarkSetup.BenchmarkDataset;

/**
 * Benchmark class defining tests for low-dimensional datasets with global
 * transformation. The runtime required for finding an optimal solution is
 * measured.
 * 
 * @author Thierry Meurers
 */
public class BenchmarkExperiment1 extends AbstractBenchmark {

    /**
     * Constructor
     * 
     * @param fileName
     */
    BenchmarkExperiment1(String fileName) {
        super(fileName, true, false);
    }

    /**
     * Entry point.
     * 
     * @param args
     *            the arguments
     */
    public static void main(String args[]) throws IOException {
        new BenchmarkExperiment1("results/Experiment1_kAnon.csv").start();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void generateTestConfigurations(List<TestConfiguration> testConfigs) {

        // Definition of properties that will be varied for the Benchmark
        BenchmarkDataset[] datasets = new BenchmarkDataset[] { BenchmarkDataset.ADULT,
                                                               BenchmarkDataset.ATUS,
                                                               BenchmarkDataset.IHIS };

        AnonymizationAlgorithm[] algorithms = new AnonymizationAlgorithm[] { AnonymizationAlgorithm.OPTIMAL,
                                                                              AnonymizationAlgorithm.BEST_EFFORT_GENETIC,
                                                                              AnonymizationAlgorithm.BEST_EFFORT_BOTTOM_UP,
                                                                              AnonymizationAlgorithm.BEST_EFFORT_TOP_DOWN };

        PrivacyModel privacyModel = PrivacyModel.K_ANONYMITY;

        // Number of testruns
        int testRuns = 6;

        // iterate through all possible configuration permutations
        for (int testRun = 0; testRun < testRuns; testRun++) {
            for (BenchmarkDataset dataset : datasets) {

                for (AnonymizationAlgorithm algorithm : algorithms) {
                    
                    TestConfiguration testConfig = new TestConfiguration();

                    // Meta info(s)
                    testConfig.testRunNumber = testRun;
                    
                    // Experiment settings
                    testConfig.algorithm = algorithm;
                    testConfig.dataset = dataset;
                    testConfig.privacyModel = privacyModel;
                    testConfig.supression = 1d;
                    testConfig.limitByOptimalLoss = true;
                    testConfig.timeLimit = 300000;
                    testConfig.weightedQids = false;

                    // GA parameters
                    testConfig.eliteFraction = 0.2;
                    testConfig.crossoverFraction = 0.4;
                    testConfig.productionFraction = 0.2;
                    testConfig.mutationProbability = 0.05;
                    testConfig.immigrationFraction = 0.2;
                    testConfig.immigrationInterval = 10;
                    testConfig.subpopulationSize = 50;

                    // GA variant
                    testConfig.useTriangle = true;
                    testConfig.dualPopulation = true;

                    // dont log first run (warm up)
                    if (testRun == 0) testConfig.writeToFile = false;
                    
                    // add finished config
                    testConfigs.add(testConfig);

                }
            }
        }

    }

}
