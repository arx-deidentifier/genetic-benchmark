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
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.deidentifier.arx.AttributeType.Hierarchy;
import org.deidentifier.arx.Data;
import org.deidentifier.arx.DataHandle;

/**
 * Setup class for benchmarks
 * @author Fabian Prasser
 */
public class BenchmarkSetup {

    /**
     * Datasets
     */
    public static enum BenchmarkDataset {
        ADULT,ATUS, IHIS, SS13ACS, CREDITCARD, MACH2019
    }

    /**
     * Quality models
     */
    public static enum BenchmarkQualityModel {
        LOSS, NUENTROPY, SSE
	}

    /**
     * Transformation models
     */
    public static enum BenchmarkTransformationModel {
        MULTI_DIMENSIONAL_GENERALIZATION, LOCAL_GENERALIZATION, 
    }

    /** 1 hour*/
    public static final int TIME_LIMIT = 3600000;
    
    /**
     * Configures and returns the dataset
     * @param dataset
     * @param tm
     * @param qis
     * @return
     * @throws IOException
     */
    public static Data getData(BenchmarkDataset dataset, int qis) throws IOException {

        Data data = getProjectedDataset(getData(dataset), Arrays.copyOf(getQuasiIdentifyingAttributes(dataset), qis));
        int num = 0;
        for (String qi : getQuasiIdentifyingAttributes(dataset)) {
            data.getDefinition().setAttributeType(qi, getHierarchy(dataset, qi));
            num++;
            if (num == qis) {
            	break;
            }
        }
        return data;
    }

    /**
     * Returns labels for the paper
     * @param dataset
     * @return
     */
    public static String getDataLabel(BenchmarkDataset dataset) {
        switch (dataset) {
        case ADULT:
            return "US Census";
        case ATUS:
            return "Time Use Survey";
        case IHIS:
            return "Health Interviews";
        case SS13ACS:
            return "Community Survey";
        case CREDITCARD:
            return "Creditcard information";
        case MACH2019:
            return "Machivallianism Test";
        }
        throw new IllegalArgumentException("Unknown dataset: " + dataset);
    };

    /**
     * Returns labels for the paper
     * @param dataset
     * @return
     */
	public static String getDataLabel(BenchmarkQualityModel quality) {
		switch (quality) {
		case LOSS:
			return "Granularity";
		case SSE:
			return "SSE";
		case NUENTROPY:
			return "Non-Uniform Entropy";
		}
		throw new IllegalArgumentException("Unknown quality model: " + quality);
	}
    
    /**
     * Returns the generalization hierarchy for the dataset and attribute
     * @param dataset
     * @param attribute
     * @return
     * @throws IOException
     */
    public static Hierarchy getHierarchy(BenchmarkDataset dataset, String attribute) throws IOException {
        switch (dataset) {
        case ADULT:
            return Hierarchy.create("hierarchies/adult_int_hierarchy_" + attribute + ".csv", Charset.defaultCharset(), ';');
        case ATUS:
            return Hierarchy.create("hierarchies/atus_int_hierarchy_" + attribute + ".csv", Charset.defaultCharset(), ';');
        case IHIS:
            return Hierarchy.create("hierarchies/ihis_int_hierarchy_" + attribute + ".csv", Charset.defaultCharset(), ';');
        case SS13ACS:
            return Hierarchy.create("hierarchies/ss13acs_int_hierarchy_" + attribute + ".csv", Charset.defaultCharset(), ';');
        case CREDITCARD:
            return Hierarchy.create("hierarchies/creditcard_int_hierarchy_" + attribute + ".csv", Charset.defaultCharset(), ';');
        case MACH2019:
            return Hierarchy.create("hierarchies/mach2019_int_hierarchy_" + attribute + ".csv", Charset.defaultCharset(), ';');
        default:
            throw new IllegalArgumentException("Unknown dataset");
        }
    }

    /**
     * Returns the number of columns in the given data set
     * @param dataset
     * @return
     * @throws IOException
     */
    public static int getNumColumns(BenchmarkDataset dataset) throws IOException {
        return getData(dataset).getHandle().getNumColumns();
    }

    /**
     * Returns the number of records in the given data set
     * @param dataset
     * @return
     * @throws IOException
     */
    public static int getNumRecords(BenchmarkDataset dataset) throws IOException {
        return getData(dataset).getHandle().getNumRows();
    }

    /**
     * Returns the quasi-identifiers for the data set
     * @param dataset
     * @return
     */
    public static String[] getQuasiIdentifyingAttributes(BenchmarkDataset dataset) {
        switch (dataset) {
        case ADULT:
            return new String[] { "sex",
                                  "age",
                                  "race",
                                  "marital-status",
                                  "education",
                                  "native-country",
                                  "workclass",
                                  "occupation",
                                  "salary-class" };
        case ATUS:
            return new String[] { "Region",
                                  "Age",
                                  "Sex",
                                  "Race",
                                  "Marital status",
                                  "Citizenship status",
                                  "Birthplace",
                                  "Highest level of school completed",
                                  "Labor force status" };
        case IHIS:
            return new String[] { "YEAR",
                                  "QUARTER",
                                  "REGION",
                                  "PERNUM",
                                  "AGE",
                                  "MARSTAT",
                                  "SEX",
                                  "RACEA",
                                  "EDUC" };
        case SS13ACS:
            return new String[] { "Insurance purchased",
                                  "Workclass",
                                  "Divorced",
                                  "Income",
                                  "Sex",
                                  "Mobility",
                                  "Military service",
                                  "Self-care",
                                  "Grade level",
                                  "Married",
                                  "Education",
                                  "Widowed",
                                  "Cognitive",
                                  "Insurance Medicaid",
                                  "Ambulatory",
                                  "Living with grandchildren",
                                  "Age",
                                  "Insurance employer",
                                  "Citizenship",
                                  "Indian Health Service",
                                  "Independent living",
                                  "Weight",
                                  "Insurance Medicare",
                                  "Hearing",
                                  "Marital status",
                                  "Vision",
                                  "Insurance Veteran's Association",
                                  "Relationship",
                                  "Insurance Tricare",
                                  "Childbirth" };
        case CREDITCARD:
            return new String[] { "LIMIT_BAL",
                                  "SEX",
                                  "EDUCATION",
                                  "MARRIAGE",
                                  "AGE",
                                  "PAY_0",
                                  "PAY_2",
                                  "PAY_3",
                                  "PAY_4",
                                  "PAY_5",
                                  "PAY_6",
                                  "BILL_AMT1",
                                  "BILL_AMT2",
                                  "BILL_AMT3",
                                  "BILL_AMT4",
                                  "BILL_AMT5",
                                  "BILL_AMT6",
                                  "PAY_AMT1",
                                  "PAY_AMT2",
                                  "PAY_AMT3",
                                  "PAY_AMT4",
                                  "PAY_AMT5",
                                  "PAY_AMT6",
                                  "default-payment-next-month" };
        case MACH2019:
            return new String[] { "age",
                                  "familysize",
                                  "gender",
                                  "married",
                                  "race",
                                  "religion",
                                  "Q1A",
                                  "Q2A",
                                  "Q3A",
                                  "Q4A",
                                  "Q5A",
                                  "Q6A",
                                  "Q7A",
                                  "Q8A",
                                  "Q9A",
                                  "Q10A"};
        default:
            throw new RuntimeException("Invalid dataset");
        }
    }

    /**
     * Returns a dataset
     * @param dataset
     * @return
     * @throws IOException
     */
    public static Data getData(BenchmarkDataset dataset) throws IOException {
        String filename = null;
        switch (dataset) {
        case ADULT:
            filename = "adult_int.csv";
            break;
        case ATUS:
            filename = "atus_int.csv";
            break;
        case IHIS:
            filename = "ihis_int.csv";
            break;
        case SS13ACS:
            filename = "ss13acs_int.csv";
            break;
        case CREDITCARD:
            filename = "creditcard_int.csv";
            break;
        case MACH2019:
            filename = "mach2019_int.csv";
            break;
        default:
            throw new RuntimeException("Invalid dataset");
        }
        return Data.create("data/" + filename, Charset.defaultCharset(), ';');
    }

    /**
     * Projects data
     * @param data
     * @param qis
     * @return
     */
    private static Data getProjectedDataset(Data data, String[] qis) {
		DataHandle handle = data.getHandle();
		List<String[]> output = new ArrayList<>();
		output.add(qis);
		for (int i = 0; i < handle.getNumRows(); i++) {
			String[] record = new String[qis.length];
			for (int j = 0; j < qis.length; j++) {
				record[j] = handle.getValue(i, handle.getColumnIndexOf(qis[j]));
			}
			output.add(record);
		}
		return Data.create(output);
	};
}
