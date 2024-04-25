import json
import matplotlib.pyplot as plt
import argparse
from collections import defaultdict
from plotterutil import *
from reader import read_json

parser = argparse.ArgumentParser(
    description="plots results of benchmarks as a bar chart showing average running times, as well as separate charts showing the progressing of running time of each experiment over time.",
)
parser.add_argument("-i", "--input", help="a path to the result JSON file to plot")
parser.add_argument(
    "-o", "--output", help="a path to the folder the images should be placed in"
)
parser.add_argument(
    "-id",
    "--identifier",
    help="an identifier added to the end of the file to prevent overriding",
)
parser.add_argument(
    "-sp",
    "--sub-plots",
    action="store_true",
    help="whether a separate plot should be made for each experiment showing how the running time evolved over time",
)
parser.add_argument("-ylim", "--y-limit", help="the top y-limit of the produced plots")
parser.add_argument(
    "-g",
    "--groups",
    help="a description of the groups given as a string where every character corresponds to some group. Experiments in the same group will be compared to eachother. Any character can be the identifier for a group",
)
parser.add_argument(
    "-sg",
    "--split-groups",
    action="store_true",
    help="generate separate bar-charts for the different groups. If not given, one bar-chart will be generated containing all groups.",
)
args = parser.parse_args()

file = open(args.input, "r")

data = read_json(file)


if args.groups is None:
    args.groups = "a" * len(data)
args.groups = [c for c in args.groups] + ([None] * (len(data) - len(args.groups)))

unique_groups = []
for group in args.groups:
    if not group in unique_groups:
        unique_groups.append(group)

groups = {}
group_members = defaultdict(list)
for i, c in enumerate(args.groups):
    groups[i] = c
    group_members[c].append(i)

if args.y_limit == None:
    y_lim = None
else:
    y_lim = int(args.y_limit)

# CALL DRAW FUNCTIONS

if args.split_groups:
    for group in unique_groups:
        indices = [i for i in range(len(data)) if groups[i] == group]
        draw_plot(
            indices,
            data,
            args.output,
            args.identifier + " " + group,
            y_lim,
            groups,
            group_members,
        )
else:
    indices = [member for g in unique_groups for member in group_members[g]]
    draw_plot(
        indices,
        data,
        args.output,
        args.identifier,
        y_lim,
        groups,
        group_members,
    )
if args.sub_plots:
    draw_sub_plots(data, args.output, args.identifier, y_lim)
