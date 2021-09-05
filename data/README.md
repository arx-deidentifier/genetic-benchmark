# Datasets
This directory contains the datasets that were used as input for the experiments. 

They are recoded versions of real-world datasets, in which all values of the datasets have been mapped to randomly chosen integer-values without preserving the mapping. This does not impact the experiments as all relevant statistical properties relevant to anonymization remain unchanged. At the same time, it limits the data to what is necessary to run the experiments.

## ADULT / *Census income*
**Description:** Excerpt of the 1994 US census dataset.  
**Source:** https://archive.ics.uci.edu/ml/datasets/adult  
**Data file:** https://archive.ics.uci.edu/ml/machine-learning-databases/adult/adult.data  
**Attributes used for experiment:**  

| # |Attribute |
|-|-|
1 | sex 
2 | age 
3 | race 
4 | marital-status 
5 | education 
6 | native-country 
7 | workclass 
8 | occupation  
9 | salary-class 

**Number of records**: 30,162  
**Additional information:** For comparability with prior work, e.g. doi.org/10.1197/jamia.M3144, rows containing missing values have been removed.

## ATUS / *Time use*
**Description:** Data from a nationally representative US time diary survey.  
**Source:** https://www.atusdata.org/atus/index.shtml  
**Data file:** Please contact authors for further information on how to obtain the specific dataset.  
**Attributes used for experiment:**  

| # |Attribute 
|-|-|
1 | Region 
2 | Age 
3 | Sex 
4 | Race 
5 | Marital status 
6 | Citizenship status 
7 | Birthplace 
8 | Highest level of school completed 
9 | Labor force status 

**Number of records**: 539,254

## IHIS / *Health interviews*
**Description:** Results from the integrated health interview series collecting data on the health of the US population.  
**Source:** https://nhis.ipums.org/nhis/  
**Data file:** Please contact authors for further information on how to obtain the specific dataset.  
**Attributes used for experiment:**  

| # |Attribute 
|-|-|
1 | YEAR 
2 | QUARTER 
3 | REGION 
4 | PERNUM 
5 | AGE 
6 | MARSTAT 
7 | SEX 
8 | RACEA 
9 | EDUC 

**Number of records**: 1,185,424

## SS13ACS / *Census community*
**Description:** Data from the responses to the American Community Survey (ACS) which captures demographic, social and economic characteristics of people living in the US.
**Source:** https://www.census.gov/programs-surveys/acs  
**Data file:** Please contact authors for further information on how to obtain the specific dataset.  
**Attributes used for experiment:**  

| # |Attribute 
|-|-|
1 | Insurance purchased 
2 | Workclass 
3 | Divorced 
4 | Income 
5 | Sex 
6 | Mobility 
7 | Military service
8 | Self-care 
9 | Grade level
10 | Married 
11 | Education 
12 | Widowed 
13 | Cognitive 
14 | Insurance Medicaid 
15 | Ambulatory 
16 | Living with grandchildren 
17 | Age 
18 | Insurance employer 
19 | Citizenship 
20 | Indian Health Service 
21 | Independent living 
22 | Weight 
23 | Insurance Medicare 
24 | Hearing 
25 | Marital status 
26 | Vision 
27 | Insurance Veteran's Association 
28 | Relationship 
29 | Insurance Tricare 
30 | Childbirth 

**Number of records**: 68,725

## CREDITCARD / *Credit card*
**Description:** Credit card client dataset from Taiwan used to estimate costumers default payments.  
**Source:** https://archive.ics.uci.edu/ml/datasets/default+of+credit+card+clients  
**Data file:** https://archive.ics.uci.edu/ml/machine-learning-databases/00350/default%20of%20credit%20card%20clients.xls  
**Attributes used for experiment:**  

| # |Attribute 
|-|-|
1 | LIMIT_BAL 
2 | SEX 
3 | EDUCATION 
4 | MARRIAGE 
5 | AGE 
6 | PAY_0 
7 | PAY_2 
8 | PAY_3 
9 | PAY_4 
10 | PAY_5 
11 | PAY_6 
12 | BILL_AMT1 
13 | BILL_AMT2 
14 | BILL_AMT3 
15 | BILL_AMT4 
16 | BILL_AMT5 
17 | BILL_AMT6 
18 | PAY_AMT1 
19 | PAY_AMT2 
20 | PAY_AMT3 
21 | PAY_AMT4 
22 | PAY_AMT5 
23 | PAY_AMT6 
24 | default-payment-next-month 

**Number of records**: 30,000

## MACH2019 / *Psychology test* 
**Description:** Answers to a psychological test designed to measure someone's Machiavellianism from the open-source psychometrics project.  
**Source:** https://openpsychometrics.org/_rawdata/  
**Data file:** http://openpsychometrics.org/_rawdata/MACH_data.zip  
**Attributes used for experiment:**  

| # |Attribute
|-|-|
1 | age 
2 | familysize 
3 | gender 
4 | married 
5 | race 
6 | religion 
7 | Q1A 
8 | Q2A 
9 | Q3A 
10 | Q4A 
11 | Q5A 
12 | Q6A 
13 | Q7A 
14 | Q8A
15 | Q9A 
16 | Q10A 

**Number of records**: 73,489
