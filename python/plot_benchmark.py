"""
Benchmark of ARX's Heuristic Algorithms
 Copyright 2020 by Thierry Meurers and contributors

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
"""

import pandas as pd
import itertools
import numpy as np
import matplotlib.pyplot as plt
from scipy import interpolate

algotihm_style_cfgs = {"OPTIMAL": {"color": "grey", "hatch": "", "label": "Optimal"},
                       "BEST_EFFORT_BOTTOM_UP": {"color": "plum", "color2": "orchid", "hatch": "",
                                                 "label": "Bottom-Up"},
                       "BEST_EFFORT_GENETIC": {"color": "palegoldenrod", "color2": "goldenrod", "hatch": "",
                                               "label": "Genetic"},
                       "BEST_EFFORT_TOP_DOWN": {"color": "skyblue", "color2": "deepskyblue", "hatch": "",
                                                "label": "Top-Down"}}

dataset_aliases = {"ADULT": "Census income", "CUP": "Data mining", "FARS": "Crash statistics", "ATUS": "Time use",
                   "IHIS": "Health interviews", "SS13ACS": "Census community", "CREDITCARD": "Credit card",
                   "MACH2019": "Psychology test"}

limits_for_ex2 = {"Distinguishability": {"x": 10, "y": {"SS13ACS": 0.8, "CREDITCARD": 0.51, "MACH2019": 0.6}},
                  "Population Uniqueness": {"x": 100, "y": {"SS13ACS": 1, "CREDITCARD": 0.7, "MACH2019": 0.8}}}

y_ticks_for_ex1 = {"Distinguishability": {"ADULT": [0.4, 5], "CUP": [6, 5], "FARS": [1.2, 5], "ATUS": [6, 5],
                                        "IHIS": [50, 6]},
                   "Population Uniqueness": {"ADULT": [1.5, 4], "CUP": [25, 6], "FARS": [10, 6], "ATUS": [12, 5],
                                             "IHIS": [50, 6]}}

y_ticks_for_ex2 = {"Distinguishability": {"SS13ACS": [80, 3], "CREDITCARD": [40, 3], "MACH2019": [60, 3]},
                  "Population Uniqueness":  {"SS13ACS": [100, 3], "CREDITCARD": [60, 3], "MACH2019": [80, 3]}}

costum_dataset_order_ld = ["ADULT", "ATUS", "IHIS"]
costum_dataset_order_hd = ['SS13ACS', 'CREDITCARD', 'MACH2019']
costum_algorithm_order_ld = ["OPTIMAL", "BEST_EFFORT_BOTTOM_UP", "BEST_EFFORT_GENETIC", "BEST_EFFORT_TOP_DOWN"]
costum_algorithm_order_hd = ["BEST_EFFORT_BOTTOM_UP", "BEST_EFFORT_GENETIC", "BEST_EFFORT_TOP_DOWN"]


def load_and_calc_avg(file_name, descriptive_column_names):
    df_all = pd.read_csv(file_name, sep=";", skiprows=0)

    # transform ms to s
    df_all['time'] = df_all['time'] / 1000
    df_all['timeLimit'] = df_all['timeLimit'] / 1000

    all_lists = [list(df_all[column_name].unique()) for column_name in descriptive_column_names]
    all_permutations = list(itertools.product(*all_lists))

    df_res = pd.DataFrame(columns=descriptive_column_names + ['time_avg', 'time_std', 'utility_avg', 'utility_std'])

    for permutation in all_permutations:
        df_temp = df_all
        result_line = {}
        for column_name, target_value in zip(descriptive_column_names, permutation):
            df_temp = df_temp[df_temp[column_name] == target_value]
            result_line[column_name] = target_value

        result_line['time_avg'] = np.mean(df_temp['time'])
        result_line['time_std'] = np.std(df_temp['time'])
        result_line['utility_avg'] = np.mean(df_temp['externalUtility'])
        result_line['utility_std'] = np.std(df_temp['externalUtility'])

        df_res = df_res.append(result_line, ignore_index=True)

    return df_res


def load_and_create_traces(file_name, descriptive_column_names, end_point=0):
    df_all = pd.read_csv(file_name, sep=";", skiprows=0)

    # transform ms to s
    df_all['time'] = df_all['time'] / 1000

    all_lists = [list(df_all[column_name].unique()) for column_name in descriptive_column_names]
    all_permutations = list(itertools.product(*all_lists))

    df_res = pd.DataFrame(columns=descriptive_column_names  + ['utility_trace', 'time_trace'])

    for permutation in all_permutations:

        df_temp = df_all
        result_line = {}
        for column_name, target_value in zip( descriptive_column_names, permutation):
            df_temp = df_temp[df_temp[column_name] == target_value]
            result_line[column_name] = target_value

        result_line['utility_trace'] = df_temp['externalUtility'].tolist()
        result_line['time_trace'] = df_temp['time'].tolist()

        print(type(result_line['utility_trace']))
        print(permutation)

        if end_point != 0:
            result_line['utility_trace'] = np.append(result_line['utility_trace'], result_line['utility_trace'][-1])
            result_line['time_trace'] = np.append(result_line['time_trace'], end_point)

        df_res = df_res.append(result_line, ignore_index=True)

    return df_res


def draw_experiment1(input_file, output_file, figure_title="", costum_dataset_order=[], costum_algorithm_order=[]):
    descriptive_column_names = ['algorithm', 'dataset']
    df = load_and_calc_avg(input_file, descriptive_column_names)

    # get name of all datasets, timeLimits and algorithms
    if costum_dataset_order:
        datasets = costum_dataset_order
    else:
        datasets = df['dataset'].unique()
    if costum_algorithm_order:
        algorithms = costum_algorithm_order
    else:
        algorithms = df['algorithm'].unique()

    #prepare figure
    fig, axs = plt.subplots(nrows=1, ncols=len(datasets), figsize=(5, 2), dpi=100)

    #matplotlib.rcParams['hatch.linewidth'] = 3

    for i, dataset in enumerate(datasets):
        for j, algorithm in enumerate(algorithms):
            temp_time = df[(df['dataset'] == dataset) & (df['algorithm'] == algorithm)]['time_avg'].values[0]
            temp_std = df[(df['dataset'] == dataset) & (df['algorithm'] == algorithm)]['time_std'].values[0]
            style_cfg = algotihm_style_cfgs[algorithm]
            if algorithm == "BEST_EFFORT_GENETIC":
                print("%s = %.2f (+- %.2f)" % (dataset, temp_time,temp_std))

            axs[i].bar(j, temp_time, color=style_cfg['color'], label=style_cfg['label'], ecolor="dimgray", yerr=temp_std, capsize=3.5, hatch=style_cfg['hatch'], edgecolor="black")
        axs[i].set_xticks([])
        y_tick_config = y_ticks_for_ex1[figure_title][dataset]
        axs[i].set_yticks(np.linspace(0, y_tick_config[0], y_tick_config[1], True))
        axs[i].set_ylim(0, y_tick_config[0])
        axs[i].set_title(dataset_aliases[dataset], fontsize="9")

    #fig.text(0.06, 0.5, 'Time (s)', va='center', rotation='vertical')
    axs[0].set_ylabel("Time (s)")

    handles, labels = axs[0].get_legend_handles_labels()
    fig.legend(handles, labels, loc='lower center', ncol=4, bbox_to_anchor=(0.5,-0.0), prop={"size": 9})
    fig.suptitle(figure_title)

    plt.subplots_adjust(top=0.78)
    plt.subplots_adjust(wspace=0.4)
    plt.subplots_adjust(bottom=0.2)

    plt.show()
    fig.savefig(output_file)


draw_experiment1("results\\Experiment1_kAnon.csv", "figures\\ex1_kAnon.svg", "Distinguishability", costum_dataset_order=costum_dataset_order_ld, costum_algorithm_order=costum_algorithm_order_ld)
draw_experiment1("results\\Experiment1_popUnique.csv", "figures\\ex1_PopUnique.svg", "Population Uniqueness", costum_dataset_order=costum_dataset_order_ld, costum_algorithm_order=costum_algorithm_order_ld)


def draw_experiment2(input_file, output_file, figure_title="", costum_dataset_order=[], costum_algorithm_order=[]):

    def avg_utility_traces(time_traces, utility_traces, x):
        new_utility_traces = []
        for time_trace, utility_trace in zip(time_traces, utility_traces):
            # remove faulty values
            utility_trace = [0 if x > 1.0 else x for x in utility_trace]

            # make convex
            for i in range(len(utility_trace)-1):
                if utility_trace[i+1] < utility_trace[i]:
                    utility_trace[i + 1] = utility_trace[i]

            # convert back to array
            utility_trace = np.array(utility_trace)

            # cut to plotted range
            utility_trace = utility_trace[time_trace < x_max]
            time_trace = time_trace[time_trace < x_max]

            # add 0 to the beginning
            time_trace = np.insert(time_trace, 0, 0.0, axis=0)
            utility_trace = np.insert(utility_trace, 0, 0.0, axis=0)

            # add max value to the end
            time_trace = np.append(time_trace, x_max)
            utility_trace = np.append(utility_trace, max(utility_trace))

            # interpolate
            f = interpolate.interp1d(time_trace, utility_trace)
            new_utility_traces.append(f(x))

        sum_array = np.mean(np.array(new_utility_traces), axis=0)
        sum_array *= 100

        return sum_array

    x_max = limits_for_ex2[figure_title]["x"]
    df = load_and_create_traces(input_file, ['algorithm', 'batchNumber', 'dataset'],  end_point=x_max)

    # get name of all datasets, timeLimits and algorithms
    if costum_dataset_order:
        datasets = costum_dataset_order
    else:
        datasets = df['dataset'].unique()
    if costum_algorithm_order:
        algorithms = costum_algorithm_order
    else:
        algorithms = df['algorithm'].unique()

    fig, axs = plt.subplots(nrows=len(datasets), ncols=1, figsize=(4, 4), dpi=100)

    for i, dataset in enumerate(datasets):
        for j, algorithm in enumerate(algorithms):
            df_temp = df[(df['dataset'] == dataset) & (df['algorithm'] == algorithm)]
            time_traces = df_temp['time_trace'].tolist()
            utility_traces = df_temp['utility_trace'].tolist()
            x = np.linspace(0, x_max, num=100, endpoint=True)
            avg_utility_trace = avg_utility_traces(time_traces, utility_traces, x)
            style_cfg = algotihm_style_cfgs[algorithm]
            #axs[i].step(x, avg_utility_trace, color=style_cfg['color2'], label=style_cfg['label'], linewidth=1.0)
            axs[i].plot(x, avg_utility_trace, color=style_cfg['color2'], label=style_cfg['label'], linewidth=1.5)

        axs[i].set_xlim(0, x_max)
        y_tick_config = y_ticks_for_ex2[figure_title][dataset]
        axs[i].set_ylim(0, y_tick_config[0])
        axs[i].set_yticks(np.linspace(0, y_tick_config[0], y_tick_config[1], True))
        axs[i].set_title(dataset_aliases[dataset], fontsize="9")

    axs[1].set_ylabel("Quality (%)")
    axs[2].set_xlabel("Time (s)")

    handles, labels = axs[0].get_legend_handles_labels()
    fig.legend(handles, labels, loc='lower center', ncol=4, bbox_to_anchor=(0.5,-0.0))
    fig.suptitle(figure_title)

    plt.subplots_adjust(hspace=0.8)
    plt.subplots_adjust(bottom=0.2)
    plt.show()
    fig.savefig(output_file)


draw_experiment2("results\\Experiment2_kAnon.csv", "figures\\ex2_kAnon.svg", figure_title="Distinguishability", costum_algorithm_order=costum_algorithm_order_hd, costum_dataset_order=costum_dataset_order_hd)
draw_experiment2("results\\Experiment2_popUnique.csv", "figures\\ex2_popUnique.svg", figure_title="Population Uniqueness",costum_algorithm_order=costum_algorithm_order_hd,costum_dataset_order=costum_dataset_order_hd)


def draw_experiment3(input_file, output_file, figure_title="", costum_dataset_order=[],  costum_algorithm_order=[]):
    descriptive_column_names = ['algorithm', 'dataset', 'timeLimit']
    df = load_and_calc_avg(input_file, descriptive_column_names)

    df["utility_avg"] *= 100
    df["utility_std"] *= 100

    # get name of all datasets, timeLimits and algorithms
    if costum_dataset_order:
        datasets = costum_dataset_order
    else:
        datasets = df['dataset'].unique()
    if costum_algorithm_order:
        algorithms = costum_algorithm_order
    else:
        algorithms = df['algorithm'].unique()

    time_limits = ["%d" % time_limit for time_limit in df['timeLimit'].unique()]

    #prepare figure
    fig, axs = plt.subplots(nrows=1, ncols=len(datasets), figsize=(7, 2.5), dpi=100, sharey=True)

    x = np.arange(len(time_limits))  # the label locations
    width = 0.2  # the width of the bars
    offset = (width * len(algorithms) / -2) + width / 2

    for i, dataset in enumerate(datasets):
        for j, algorithm in enumerate(algorithms):
            temp_res = df[(df['dataset'] == dataset) & (df['algorithm'] == algorithm)]['utility_avg']
            temp_std = df[(df['dataset'] == dataset) & (df['algorithm'] == algorithm)]['utility_std']
            style_cfg = algotihm_style_cfgs[algorithm]
            if algorithm == "BEST_EFFORT_GENETIC":
                for limit, res, std in zip([5, 10, 20], temp_res, temp_std):
                    print("%s (Time Limit: %d) = %.2f (+- %.2f)" % (dataset, limit, res, std))
            axs[i].bar(x + offset + width * j, temp_res,  width=width, color=style_cfg['color'], ecolor="dimgray", yerr=temp_std, capsize=2.5, label=style_cfg['label'], hatch=style_cfg['hatch'], edgecolor="black")

        axs[i].set_xticks(x)

        axs[i].set_xticklabels(time_limits)
        axs[i].set_title(dataset_aliases[dataset], fontsize="9")
        axs[i].set_ylim([0, 100])

    axs[0].set_ylabel("Quality (%)")
    axs[1].set_xlabel("Time limit per iteration (s)")

    #fig.text(0.4, 0.175, 'Time limit per iteration (s)', va='center')

    handles, labels = axs[0].get_legend_handles_labels()
    fig.legend(handles, labels, loc='lower center', ncol=4, bbox_to_anchor=(0.5,-0.0))
    fig.suptitle(figure_title)

    plt.subplots_adjust(top=0.78)
    plt.subplots_adjust(wspace=0.06)
    plt.subplots_adjust(bottom=0.3)
    plt.show()
    fig.savefig(output_file)

draw_experiment3("results\\Experiment3_kAnon.csv", "figures\\ex3_kAnon.svg", "Distinguishability", costum_dataset_order=costum_dataset_order_hd)
draw_experiment3("results\\Experiment3_PopUnique.csv",  "figures\\ex3_PopUnique.svg", "Population Uniqueness", costum_dataset_order=costum_dataset_order_hd)