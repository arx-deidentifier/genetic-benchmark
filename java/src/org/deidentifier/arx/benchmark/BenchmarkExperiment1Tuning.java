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
import org.deidentifier.arx.benchmark.AbstractBenchmark.PrivacyModel;
import org.deidentifier.arx.benchmark.BenchmarkExperiment2LocalTuning.TuningParameter;
import org.deidentifier.arx.benchmark.BenchmarkSetup.BenchmarkDataset;

/**
 * Benchmark class defining tests for low-dimensional datasets with global
 * transformation. The runtime required for finding an optimal solution is
 * measured.
 * 
 * @author Thierry Meurers
 */
public class BenchmarkExperiment1Tuning extends AbstractBenchmark {

    /**
     * @author Thierry Meurers
     *
     *         Parameters that are varied during the optimization procedure.
     */
    enum TuningParameter {
                          ELITE_FRACTION("eliteFraction", new Double[] { 0.2, 0.4 }),
                          CROSSOVER_FRACTION("crossOverFraction", new Double[] { 0.2, 0.6 }),
                          PRODUCTION_FRACTION("productionFraction", new Double[] { 0.2, 0.4, 0.6 }),
                          MUTATION_PROBABILITY("mutationProbability", new Double[] { 0.025, 0.1 }),
                          IMMIGRATION_FRACTION("immigrationFraction", new Double[] { 0.4, 0.6, 0.8 }),
                          IMMIGRATION_INTERVAL("immigrationInterval", new Integer[] { 5, 20 }),
                          SUBPOPULATION_SIZE("subpopulationSize", new Integer[] { 50, 200 }),
                          GA_IMPLEMENTATION("gaImplementation", new Integer[] { 1, 2 });

        final Number[] tuningValues;
        final String   parameterCaption;

        TuningParameter(String parameterCaption, Number[] tuningValues) {
            this.tuningValues = tuningValues;
            this.parameterCaption = parameterCaption;
        }
    }

    /**
     * Constructor
     * 
     * @param fileName
     */
    BenchmarkExperiment1Tuning(String fileName) {
        super(fileName, true, false);
    }

    /**
     * Entry point.
     * 
     * @param args
     *            the arguments
     */
    public static void main(String args[]) throws IOException {
        new BenchmarkExperiment1Tuning("results/ParameterTuning/Experiment1_popUnique_tuning.csv").start();
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

        AnonymizationAlgorithm algorithm = AnonymizationAlgorithm.BEST_EFFORT_GENETIC;

        TuningParameter[] tuningParameters = new TuningParameter[] { TuningParameter.CROSSOVER_FRACTION,
                                                                     TuningParameter.ELITE_FRACTION,
                                                                     TuningParameter.PRODUCTION_FRACTION,
                                                                     TuningParameter.MUTATION_PROBABILITY,
                                                                     TuningParameter.IMMIGRATION_FRACTION,
                                                                     TuningParameter.IMMIGRATION_INTERVAL,
                                                                     TuningParameter.SUBPOPULATION_SIZE,
                                                                     TuningParameter.GA_IMPLEMENTATION };

        PrivacyModel privacyModel = PrivacyModel.POPULATION_UNIQUENESS;

        // Number of testruns
        int testRuns = 6;

        // iterate through all possible configuration permutations
        for (int testRun = 0; testRun < testRuns; testRun++) {
            for (BenchmarkDataset dataset : datasets) {
                for (TuningParameter parameter : tuningParameters) {
                    for (Number value : parameter.tuningValues) {

                        TestConfiguration testConfig = new TestConfiguration();

                        // Meta infos
                        testConfig.testRunNumber = testRun;
                        testConfig.tuningParameter = parameter.parameterCaption;
                        testConfig.tuningValue = value;

                        // Experiment settings
                        testConfig.privacyModel = privacyModel;
                        testConfig.supression = 1d;
                        testConfig.weightedQids = false;
                        testConfig.algorithm = algorithm;
                        testConfig.dataset = dataset;
                        testConfig.limitByOptimalLoss = true;
                        testConfig.timeLimit = 300000;

                        // GA parameters (default values)
                        testConfig.eliteFraction = 0.2; // 0.2
                        testConfig.crossoverFraction = 0.4; // 0.4
                        testConfig.productionFraction = 0.8; // 0.8
                        testConfig.mutationProbability = 0.05; // 0.05
                        testConfig.immigrationFraction = 0.2; // 0.2
                        testConfig.immigrationInterval = 10; // 10
                        testConfig.subpopulationSize = 100; // 100

                        // GA variant (default variant)
                        testConfig.useTriangle = true;
                        testConfig.dualPopulation = true;

                        // GA settings - changed for tuning
                        switch (parameter) {
                        case ELITE_FRACTION:
                            testConfig.eliteFraction = (double) value;
                            break;
                        case CROSSOVER_FRACTION:
                            testConfig.crossoverFraction = (double) value;
                            break;
                        case PRODUCTION_FRACTION:
                            testConfig.productionFraction = (double) value;
                            break;
                        case MUTATION_PROBABILITY:
                            testConfig.mutationProbability = (double) value;
                            break;
                        case IMMIGRATION_FRACTION:
                            testConfig.immigrationFraction = (double) value;
                            break;
                        case IMMIGRATION_INTERVAL:
                            testConfig.immigrationInterval = (int) value;
                            break;
                        case SUBPOPULATION_SIZE:
                            testConfig.subpopulationSize = (int) value;
                        case GA_IMPLEMENTATION:
                            int _value = (int) value;
                            switch (_value) {
                            case 0:
                                testConfig.useTriangle = true;
                                testConfig.dualPopulation = true;
                                break;
                            case 1:
                                testConfig.useTriangle = false;
                                testConfig.dualPopulation = true;
                                break;
                            case 2:
                                testConfig.useTriangle = false;
                                testConfig.dualPopulation = false;
                                testConfig.subpopulationSize *= 2;
                                break;
                            }
                        }

                        // dont log first run (warm up)
                        if (testRun == 0) testConfig.writeToFile = false;

                        // add finished config
                        testConfigs.add(testConfig);

                    }
                }
            }
        }
    }

}
