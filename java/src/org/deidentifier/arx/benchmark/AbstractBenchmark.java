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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import org.deidentifier.arx.ARXAnonymizer;
import org.deidentifier.arx.ARXConfiguration;
import org.deidentifier.arx.ARXConfiguration.AnonymizationAlgorithm;
import org.deidentifier.arx.ARXLattice;
import org.deidentifier.arx.ARXLattice.ARXNode;
import org.deidentifier.arx.ARXResult;
import org.deidentifier.arx.Data;
import org.deidentifier.arx.DataHandle;
import org.deidentifier.arx.algorithm.AbstractAlgorithm;
import org.deidentifier.arx.algorithm.AbstractAlgorithm.TimeUtilityTuple;
import org.deidentifier.arx.algorithm.GeneticAlgorithm;
import org.deidentifier.arx.benchmark.BenchmarkSetup.BenchmarkDataset;
import org.deidentifier.arx.criteria.KAnonymity;
import org.deidentifier.arx.criteria.PopulationUniqueness;
import org.deidentifier.arx.criteria.PrivacyCriterion;
import org.deidentifier.arx.exceptions.RollbackRequiredException;
import org.deidentifier.arx.metric.Metric;
import org.deidentifier.arx.metric.Metric.AggregateFunction;
import org.deidentifier.arx.risk.RiskModelPopulationUniqueness.PopulationUniquenessModel;
import org.deidentifier.arx.ARXPopulationModel;
import org.deidentifier.arx.ARXPopulationModel.Region;

import de.linearbits.subframe.Benchmark;

/**
 * Abstract class used to define a benchmark that systematically performs and logs 
 * an arbitrary number of anonymization runs with varying parameters.
 * 
 * @author Thierry Meurers
 *
 */
public abstract class AbstractBenchmark {
    
    /**
     * Privacy models available for testing
     */
    protected enum PrivacyModel {
        K_ANONYMITY,
        POPULATION_UNIQUENESS
        }

    /**
     * @author Thierry
     *
     * Class describing the executed anonymization run
     */
    class TestConfiguration{

        // Meta Infos
        /** Number of run */
        int testRunNumber = -1;
        
        /** Enable/disable logging */
        boolean writeToFile = true;
        
        /** Tuned parameter */
        String tuningParameter = "none";
        
        /** Tuning value */
        Number tuningValue;
        
        // Anonymization requirements and metrics
        /** gsFactor */
        double                 gsFactor                      = 0.5d;
        
        /** Aggregation function */
        AggregateFunction      aggregateFunction             = AggregateFunction.ARITHMETIC_MEAN;
        
        /** Privacy model */
        PrivacyModel           privacyModel                  = PrivacyModel.K_ANONYMITY;
        
        /** k for k-anonymity */
        int                    k                             = 5;
        
        /** allowed supression */
        double                 supression                    = 1d;
        
        /** flag indicating the transformation model */
        boolean                useLocalTransformation        = false;
        
        /** max. number of iterations for local transformation */
        int                    localTransformationIterations = 0;

        /** Used algorithm */
        AnonymizationAlgorithm algorithm;

        // GA specific settings
        /** Population size */
        int                    subpopulationSize             = 100;
        
        /** Number of GA iterations */
        int                    gaIterations                  = Integer.MAX_VALUE;

        /** Elite fraction */
        double                 eliteFraction                 = 0.2;

        /** Crossover fraction */
        double                 crossoverFraction             = 0.2;

        /** Production fraction */
        double                 productionFraction            = 0.2d;
        
        /** Mutation probability */
        double                 mutationProbability           = 0.2;

        /** Immigration interval */
        int                    immigrationInterval           = 10;
        
        /** Immigration Fraction */
        double                 immigrationFraction           = 0.2;
        
        /** Use triangle pattern for initialization of 1st population */
        public Boolean         useTriangle                   = true;

        /** Use two populations */
        public Boolean         dualPopulation                = true;
        
        // Limits
        /** Time limit */
        int                    timeLimit                     = Integer.MAX_VALUE;
        
        /** Step limit */
        int                    stepLimit                     = Integer.MAX_VALUE;
        
        /** Stop when optimal solution found */
        boolean                limitByOptimalLoss            = false;

        // Input configuration
        /** Benchmark dataset */
        BenchmarkDataset       dataset;
        
        /** Number of attributes included in anonymization */
        int                    qids                          = 0;

        /** Assign qids with ascending weights */
        boolean                weightedQids                  = false;
        
        /** Generates hash of dataset configuration */
        Integer hashInputConfig() {
            return (int) (dataset.hashCode() + qids);
        }
        
        /** 
         * Generates hash of input config.
         * used to find testConfig in the optimal loss hashmap
         * */
        Integer hashObjective() {
            return (int) (hashInputConfig() + k + supression);
        }
        
        /**
         * Outputs string to track the progress.
         */
        @Override
        public String toString() {
            String output = String.format("%s | %s | TimeLimit=%d | TuningParameter=%s ("+String.valueOf(tuningValue)+") | RunNumber=%d" , algorithm, dataset, timeLimit, tuningParameter, testRunNumber);
            return output;
        }
    }
    
    /**
     * Column-names for log file
     */
    private static final Benchmark   BENCHMARK     = new Benchmark(new String[] { "algorithm",
                                                                                  "dataset",
                                                                                  "privacyModel",
                                                                                  "k",
                                                                                  "qids",
                                                                                  "weighted",
                                                                                  "iterations",
                                                                                  "eliteFraction",
                                                                                  "crossoverFraction",
                                                                                  "productionFraction",
                                                                                  "mutationProbability",
                                                                                  "immigrationFraction",
                                                                                  "immigrationInterval",
                                                                                  "subpopulationSize",
                                                                                  "trianglePattern",
                                                                                  "dualPopulation",
                                                                                  "timeLimit",
                                                                                  "stepLimit",
                                                                                  "limitByOptimalLoss",
                                                                                  "batchNumber",
                                                                                  "time",
                                                                                  "externalUtility",
                                                                                  "internalUtility",
                                                                                  "tuningParameter",
                                                                                  "tuningValue"
                                                                                  });
    
    /**
     * log file handle
     */
    private File                     file;
    
    /**
     * Stores the optimal solution (/loss) to avoid re-caclulating them
     */
    private HashMap<Integer, Double> optimalLossHM = new HashMap<Integer, Double>();
    
    /**
     * Enable / Disable console output
     */
    private boolean                  verbose;

    /**
     * Enable / Disable progress (utility improvement) tracking
     */
    private boolean                  writeAllTrackedOptimums;
    
    /**
     * Constructor.
     * 
     * @param fileName
     *            Name of log file
     * @param verbose
     *            If true the current testrun configuration will be printed to
     *            console
     * @param writeAllTrackedOptimums
     *            If true, the progress (utility improvement) will be tracked
     *            continuously and be written to the log file (not usable for
     *            local transformation)
     */
    AbstractBenchmark(String fileName, boolean verbose, boolean writeAllTrackedOptimums){
        file = new File(fileName);
        this.verbose = verbose;
        this.writeAllTrackedOptimums = writeAllTrackedOptimums;
    }
    
    /**
     * Calculates the external utility of a list of transformations (contained in a TimeUtilityTuple) by searching the result's lattice.
     * 
     * @param result ARX result (contains the lattice)
     * @param tuTuples List of TimeUtilityTuples
     */
    private void calculateUtilityForTransformation(ARXResult result,
                                                   List<TimeUtilityTuple> tuTuples) {
        for (TimeUtilityTuple tuTuple : tuTuples) {

            int[] transformation = tuTuple.getTransfomration().getGeneralization();

            ARXLattice lattice = result.getLattice();
            ARXNode node = lattice.getBottom();

            // Search
            while (!Arrays.equals(node.getTransformation(), transformation)) {

                // Successors
                node.expand();
                ARXNode[] successors = node.getSuccessors().clone();

                // Not found
                if (successors.length == 0) {
                    tuTuple.setExternalUtility(0);
                }

                // Sort according to distance
                Arrays.sort(successors, new Comparator<ARXNode>() {

                    @Override
                    public int compare(ARXNode o1, ARXNode o2) {
                        return Integer.compare(getDistance(o1.getTransformation(), transformation),
                                               getDistance(o2.getTransformation(), transformation));

                    }

                    /**
                     * Calculate distance
                     * 
                     * @param current
                     * @param target
                     * @return
                     */
                    private int getDistance(int[] current, int[] target) {
                        int distance = 0;
                        for (int i = 0; i < current.length; i++) {
                            if (current[i] > target[i]) {
                                return Integer.MAX_VALUE;
                            } else {
                                distance += target[i] - current[i];
                            }
                        }
                        return distance;
                    }
                });

                // Take closest node
                node = successors[0];
            }

            // Done
            tuTuple.setExternalUtility(result.getOutput(node, false)
                                             .getStatistics()
                                             .getQualityStatistics()
                                             .getGranularity()
                                             .getArithmeticMean());
        }

    }
    
    /**
     * Method that executed a single anonymization process / test config.
     * 
     * @param testConfiguration config to be executed
     * @throws IOException
     */
    private void executeTest(TestConfiguration testConfiguration) throws IOException {
        
        // reset lossLimit to avoid side effects
        AbstractAlgorithm.lossLimit = -1;
        
        // Copy benchmark config to arx config
        ARXConfiguration arxConfiguration = ARXConfiguration.create();
        arxConfiguration.setQualityModel(Metric.createLossMetric(testConfiguration.gsFactor, testConfiguration.aggregateFunction));
        arxConfiguration.addPrivacyModel(instantiatePrivacyCriterion(testConfiguration));  
        arxConfiguration.setSuppressionLimit(testConfiguration.supression);
        arxConfiguration.setAlgorithm(testConfiguration.algorithm);
        arxConfiguration.setGeneticAlgorithmIterations(testConfiguration.gaIterations);
        arxConfiguration.setGeneticAlgorithmSubpopulationSize(testConfiguration.subpopulationSize);
        arxConfiguration.setGeneticAlgorithmEliteFraction(testConfiguration.eliteFraction);
        arxConfiguration.setGeneticAlgorithmCrossoverFraction(testConfiguration.crossoverFraction);
        arxConfiguration.setGeneticAlgorithmMutationProbability(testConfiguration.mutationProbability);
        arxConfiguration.setGeneticAlgorithmImmigrationFraction(testConfiguration.immigrationFraction);
        arxConfiguration.setGeneticAlgorithmImmigrationInterval(testConfiguration.immigrationInterval);
        arxConfiguration.setGeneticAlgorithmProductionFraction(testConfiguration.productionFraction);
        arxConfiguration.setHeuristicSearchStepLimit(testConfiguration.stepLimit);
        arxConfiguration.setHeuristicSearchTimeLimit(testConfiguration.timeLimit);

        GeneticAlgorithm.useTriangle = testConfiguration.useTriangle;
        GeneticAlgorithm.dualPopulation = testConfiguration.dualPopulation;
        
        if (testConfiguration.weightedQids) {
            setAttributeWeights(arxConfiguration, testConfiguration);
        }
        
        // find and set optimum as stop limit
        if (testConfiguration.limitByOptimalLoss) {
            findAndSetOptimum(testConfiguration);
        }      
        // load data
        Data input = getInputData(testConfiguration);
        
        // Init and start anonymizer
        ARXAnonymizer anonymizer = new ARXAnonymizer();
        long time = System.currentTimeMillis();
        ARXResult result = anonymizer.anonymize(input, arxConfiguration);
        
        DataHandle output = result.getOutput();
        if (testConfiguration.useLocalTransformation && result.isResultAvailable()) {
            try {
                result.optimizeIterativeFast(output, 1d / (double) testConfiguration.localTransformationIterations);
            } catch (RollbackRequiredException e) {
                e.printStackTrace();
                throw new RuntimeException(e);
            }
        }
        
        time = System.currentTimeMillis() - time;
        
        // write result
        if (testConfiguration.writeToFile) {
            
            if (!writeAllTrackedOptimums) {
            // write just the final result
                
                Double externalUtility = 0d;
                if (result.isResultAvailable()) {
                    externalUtility = output
                            .getStatistics()
                            .getQualityStatistics()
                            .getGranularity()
                            .getArithmeticMean();
                }
                BENCHMARK.addRun(String.valueOf(testConfiguration.algorithm),
                                 String.valueOf(testConfiguration.dataset),
                                 String.valueOf(testConfiguration.privacyModel),
                                 String.valueOf(testConfiguration.k),
                                 String.valueOf(testConfiguration.qids),
                                 String.valueOf(testConfiguration.weightedQids),
                                 String.valueOf(testConfiguration.gaIterations),
                                 String.valueOf(testConfiguration.eliteFraction),
                                 String.valueOf(testConfiguration.crossoverFraction),
                                 String.valueOf(testConfiguration.productionFraction),
                                 String.valueOf(testConfiguration.mutationProbability),
                                 String.valueOf(testConfiguration.immigrationFraction),
                                 String.valueOf(testConfiguration.immigrationInterval),
                                 String.valueOf(testConfiguration.subpopulationSize),
                                 String.valueOf(testConfiguration.useTriangle),
                                 String.valueOf(testConfiguration.dualPopulation),
                                 String.valueOf(testConfiguration.timeLimit),
                                 String.valueOf(testConfiguration.stepLimit),
                                 String.valueOf(testConfiguration.limitByOptimalLoss),
                                 String.valueOf(testConfiguration.testRunNumber),
                                 String.valueOf(time),
                                 String.valueOf(externalUtility),
                                 String.valueOf(0),
                                 String.valueOf(testConfiguration.tuningParameter),
                                 String.valueOf(testConfiguration.tuningValue));
                BENCHMARK.getResults().write(file);
            } else {
            // write all tracked optimums (does not work for local transformation)
                List<TimeUtilityTuple> trackedOptimums = AbstractAlgorithm.getTrackedOptimums();
                calculateUtilityForTransformation(result, trackedOptimums);
                for (TimeUtilityTuple trackedOptimum : trackedOptimums) {
                    BENCHMARK.addRun(String.valueOf(testConfiguration.algorithm),
                                     String.valueOf(testConfiguration.dataset),
                                     String.valueOf(testConfiguration.privacyModel),
                                     String.valueOf(testConfiguration.k),
                                     String.valueOf(testConfiguration.qids),
                                     String.valueOf(testConfiguration.weightedQids),
                                     String.valueOf(testConfiguration.gaIterations),
                                     String.valueOf(testConfiguration.eliteFraction),
                                     String.valueOf(testConfiguration.crossoverFraction),
                                     String.valueOf(testConfiguration.productionFraction),
                                     String.valueOf(testConfiguration.mutationProbability),
                                     String.valueOf(testConfiguration.immigrationFraction),
                                     String.valueOf(testConfiguration.immigrationInterval),
                                     String.valueOf(testConfiguration.subpopulationSize),
                                     String.valueOf(testConfiguration.useTriangle),
                                     String.valueOf(testConfiguration.dualPopulation),
                                     String.valueOf(testConfiguration.timeLimit),
                                     String.valueOf(testConfiguration.stepLimit),
                                     String.valueOf(testConfiguration.limitByOptimalLoss),
                                     String.valueOf(testConfiguration.testRunNumber),
                                     String.valueOf(trackedOptimum.getTime()),
                                     String.valueOf(trackedOptimum.getExternalUtility()),
                                     String.valueOf(trackedOptimum.getInternalUtility()),
                                     String.valueOf(testConfiguration.tuningParameter),
                                     String.valueOf(testConfiguration.tuningValue));
                    BENCHMARK.getResults().write(file);

                }
            }
        }
        AbstractAlgorithm.getTrackedOptimums().clear();
    }

    /**
     * Assigned weights to each qid in ascending order ranging from 0.0 to 1.0.
     * 
     * @param arxConfiguration
     * @param testConfiguration
     */
    private void setAttributeWeights(ARXConfiguration arxConfiguration, TestConfiguration testConfiguration) {
        String [] qis = BenchmarkSetup.getQuasiIdentifyingAttributes(testConfiguration.dataset);
        double stepSize = 1d / (qis.length - 1);
        for(int i = 0; i < qis.length; i++) {
            double weight = Math.min(1, i * stepSize);
            arxConfiguration.setAttributeWeight(qis[i], weight);
        }
    }
    
    /**
     * Method used to get the optimal solution for a given configuration. The
     * solution is only calculated once (using the OPTIMAL algorithm) and
     * thereafter stored in a hashMap
     * 
     * @param testConfiguration config describing the problem
     * @throws IOException
     */
    private void findAndSetOptimum(TestConfiguration testConfiguration) throws IOException {
        
        Integer key = testConfiguration.hashObjective();
        
        // check if config contained in hashMap
        if(!optimalLossHM.containsKey(key)) {

            // Copy reuiqred informations to ARXconfig
            ARXConfiguration config = ARXConfiguration.create();
            config.setQualityModel(Metric.createLossMetric(testConfiguration.gsFactor, testConfiguration.aggregateFunction));
            config.addPrivacyModel(instantiatePrivacyCriterion(testConfiguration));
            config.setSuppressionLimit(testConfiguration.supression);
            config.setAlgorithm(AnonymizationAlgorithm.OPTIMAL);
            
            // Load data and start anonymization
            ARXAnonymizer anonymizer = new ARXAnonymizer();
            ARXResult result = anonymizer.anonymize(getInputData(testConfiguration), config);
            
            // Store solution in hashMap
            optimalLossHM.put(key, Double.valueOf(result.getGlobalOptimum().getHighestScore().toString()));
            
            if (verbose) {
                System.out.println("Created new Optimum (" +
                                   String.valueOf(testConfiguration.dataset) + " , qids=" +
                                   testConfiguration.qids + ") with key " + key);
            }
        }
        
        // return solution from hashMap
        AbstractAlgorithm.lossLimit = optimalLossHM.get(key);                         
    }
    
    /**
     * This method is called by the Benchmark's start method. It requires the
     * benchmark implementation to generate an arbitrary number of
     * testConfigurations that will be processed during the Benchmark
     * 
     * @param testConfigurations
     *            List to store the TestConfigurations
     */
    public abstract void generateTestConfigurations(List<TestConfiguration> testConfigurations);
    
    /**
     * Simple method to load the input data using the BenchmarkSetup class.
     * 
     * @param benchConfig configuration containing the dataset and qid information
     * @return Input for the anonmyzation process
     * @throws IOException
     */
    private Data getInputData(TestConfiguration benchConfig) throws IOException {

        int qids = benchConfig.qids;
        if (qids == 0) {
            qids = BenchmarkSetup.getQuasiIdentifyingAttributes(benchConfig.dataset).length;
        }
        return BenchmarkSetup.getData(benchConfig.dataset, qids);
    }
    
    /**
     * Used to instantiate the defined privacy criterion / model
     * 
     * @param testConfiguration
     * @return
     */
    private PrivacyCriterion instantiatePrivacyCriterion(TestConfiguration testConfiguration) {
        
        switch(testConfiguration.privacyModel) {
        case K_ANONYMITY:
            return (new KAnonymity(testConfiguration.k));
        case POPULATION_UNIQUENESS:
            return(new PopulationUniqueness(0.01, PopulationUniquenessModel.PITMAN, ARXPopulationModel.create(Region.USA)));
        default:
            throw new RuntimeException("Unknown Privacy Model");
        }
    }
    
    /**
     * Calls the generateTestConfigurations method and executes all provided
     * TestConfigurations.
     * 
     * @throws IOException
     */
    public void start() throws IOException {
        List<TestConfiguration> testConfigurations = new ArrayList<TestConfiguration>();
        generateTestConfigurations(testConfigurations); 
        
        for(int i = 0; i < testConfigurations.size(); i++) {
            TestConfiguration testConfiguration = testConfigurations.get(i);
            if(verbose) {
                System.out.println(java.time.LocalTime.now() + " - (" + (i+1) +"/" + testConfigurations.size()+ ") " + testConfiguration);
            }
            executeTest(testConfiguration);
        }
    }   
}