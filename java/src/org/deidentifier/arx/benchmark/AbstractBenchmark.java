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

public abstract class AbstractBenchmark {
    
    protected enum PrivacyModel {
        K_ANONYMITY,
        POPULATION_UNIQUENESS
        }

    // Column-names for log file
    private static final Benchmark   BENCHMARK     = new Benchmark(new String[] { "algorithm",
                                                                                  "dataset",
                                                                                  "k",
                                                                                  "qids",
                                                                                  "iterations",
                                                                                  "eliteFraction",
                                                                                  "crossoverFraction",
                                                                                  "mutationProbability",
                                                                                  "timeLimit",
                                                                                  "stepLimit",
                                                                                  "limitByOptimalLoss",
                                                                                  "batchNumber",
                                                                                  "time",
                                                                                  "externalUtility",
                                                                                  "internalUtility" });
    
    // log file handle
    private File                     file;
    
    // hash map to store the data obects (currently nur used)
    private HashMap<Integer, Data>   inputDataHM   = new HashMap<Integer, Data>();
    
    // Stores the optimal solution (/loss) to avoid re-caclulating them
    private HashMap<Integer, Double> optimalLossHM = new HashMap<Integer, Double>();
    
    // Enable / Disable console output
    private boolean                  verbose;
    
    // Enable / Disable progress (utility improvement) tracking
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
    
    /**
     * This method is called by the Benchmarks start method. It requires the
     * benchmark implementation to generate an arbitrary number of
     * testConfigurations that will be processed during the Benchmark
     * 
     * @param testConfigurations
     *            List to store the TestConfigurations
     */
    public abstract void generateTestConfigurations(List<TestConfiguration> testConfigurations);
    
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
        //arxConfiguration.setQualityModel(Metric.createEntropyMetric(true, testConfiguration.aggregateFunction));
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
                    //utility = output.getStatistics().getQualityStatistics().getNonUniformEntropy().getArithmeticMean();
                }
                BENCHMARK.addRun(String.valueOf(testConfiguration.algorithm),
                                 String.valueOf(testConfiguration.dataset),
                                 String.valueOf(testConfiguration.k),
                                 String.valueOf(testConfiguration.qids),
                                 String.valueOf(testConfiguration.gaIterations),
                                 String.valueOf(testConfiguration.eliteFraction),
                                 String.valueOf(testConfiguration.crossoverFraction),
                                 String.valueOf(testConfiguration.mutationProbability),
                                 String.valueOf(testConfiguration.timeLimit),
                                 String.valueOf(testConfiguration.stepLimit),
                                 String.valueOf(testConfiguration.limitByOptimalLoss),
                                 String.valueOf(testConfiguration.testRunNumber),
                                 String.valueOf(time),
                                 String.valueOf(externalUtility),
                                 String.valueOf(0));
                BENCHMARK.getResults().write(file);
            } else {
            // write all tracked optimums (does not work for local transformation)
                List<TimeUtilityTuple> trackedOptimums = AbstractAlgorithm.getTrackedOptimums();
                calculateUtilityForTransformation(result, trackedOptimums);
                for (TimeUtilityTuple trackedOptimum : trackedOptimums) {
                    BENCHMARK.addRun(String.valueOf(testConfiguration.algorithm),
                                     String.valueOf(testConfiguration.dataset),
                                     String.valueOf(testConfiguration.k),
                                     String.valueOf(testConfiguration.qids),
                                     String.valueOf(testConfiguration.gaIterations),
                                     String.valueOf(testConfiguration.eliteFraction),
                                     String.valueOf(testConfiguration.crossoverFraction),
                                     String.valueOf(testConfiguration.mutationProbability),
                                     String.valueOf(testConfiguration.timeLimit),
                                     String.valueOf(testConfiguration.stepLimit),
                                     String.valueOf(testConfiguration.limitByOptimalLoss),
                                     String.valueOf(testConfiguration.testRunNumber),
                                     String.valueOf(trackedOptimum.getTime()),
                                     String.valueOf(trackedOptimum.getExternalUtility()),
                                     String.valueOf(trackedOptimum.getInternalUtility()));
                    BENCHMARK.getResults().write(file);

                }
            }
        }
        AbstractAlgorithm.getTrackedOptimums().clear();
    }

    
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
    
    @Deprecated
    // Broken idea as a Data object is changed when its used for anonymization
    private Data getInputDataFromHashMap(TestConfiguration testConfiguration) throws IOException {
        
        Integer key = testConfiguration.hashInputConfig();
        
        if(!inputDataHM.containsKey(key)) {
            int qids = testConfiguration.qids;
            if (qids == 0) {
                qids = BenchmarkSetup.getQuasiIdentifyingAttributes(testConfiguration.dataset).length;
            }
            System.out.println("Created new InputData (" + String.valueOf(testConfiguration.dataset) + " , qids=" + qids + ") with key " + key);
            inputDataHM.put(key, BenchmarkSetup.getData(testConfiguration.dataset, qids));
        }
        
        return inputDataHM.get(key);
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
     * @author Thierry
     *
     * Class describing the executed / tested anonymization process
     */
    class TestConfiguration{

        int testRunNumber = -1;
        boolean writeToFile = true;
        
        // Anonymization requirements and metrics
        double                 gsFactor                      = 0.5d;
        AggregateFunction      aggregateFunction             = AggregateFunction.ARITHMETIC_MEAN;
        PrivacyModel           privacyModel                  = PrivacyModel.K_ANONYMITY;
        int                    k                             = 5;
        
        double                 supression                    = 1d;
        boolean                useLocalTransformation        = false;
        int                    localTransformationIterations = 0;

        // Used algorithm
        AnonymizationAlgorithm algorithm;

        // GA specific settings
        int                    subpopulationSize             = 100;
        int                    gaIterations                  = Integer.MAX_VALUE;
        double                 eliteFraction                 = 0.2;
        double                 crossoverFraction             = 0.2;
        double                 mutationProbability           = 0.2;
        double                 immigrationFraction           = 0.2;
        double                 productionFraction            = 0.2d;
        int                    immigrationInterval           = 10;

        // Limits
        int                    timeLimit                     = Integer.MAX_VALUE;
        int                    stepLimit                     = Integer.MAX_VALUE;
        boolean                limitByOptimalLoss            = false;

        // Input configuration
        BenchmarkDataset       dataset;
        int                    qids                          = 0;

        Integer hashInputConfig() {
            return (int) (dataset.hashCode() + qids);
        }
        
        // used to find testConfig in the optimal loss hashmap
        Integer hashObjective() {
            return (int) (hashInputConfig() + k + supression);
        }
        
        @Override
        public String toString() {
            String output_raw = "%s | %s | TimeLimit=%d |  |RunNumber=%d";
            String output = String.format("%s | %s | TimeLimit=%d | RunNumber=%d", algorithm, dataset, timeLimit, testRunNumber);
            return output;
        }

    }
    
}
