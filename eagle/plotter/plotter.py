import argparse
from plotterutil import *
from reader import read_json
import os

parser = argparse.ArgumentParser(
    description="plots results of benchmarks as a bar chart showing average running times, as well as separate charts showing the progressing of running time of each experiment over time.",
)
parser.add_argument(
    "-i", "--input", help="a path to the result JSON file to plot", required=True
)
parser.add_argument(
    "-o",
    "--output",
    help="a path to the folder the images should be placed in",
    default=None,
)
parser.add_argument(
    "-id",
    "--identifier",
    help="an identifier added to the end of the file to prevent overriding",
    default="",
)
parser.add_argument(
    "-sp",
    "--sub-plots",
    action="store_true",
    help="whether a separate plot should be made for each experiment showing how the running time evolved over time",
)
parser.add_argument(
    "-ylim", "--y-limit", help="the top y-limit of the produced plots", type=float
)
parser.add_argument(
    "-sg",
    "--split-groups",
    action="store_true",
    help="generate separate bar-charts for the different groups. If not given, one bar-chart will be generated containing all groups.",
)
parser.add_argument(
    "-l",
    "--line-plot",
    action="store_true",
    help="When this argument is given, the plotter will generate a line plot instead of bars. This option requires all data-points to have a unique group. If possible, it will use any number present in the group name of each data-point as the x-axis location of the points.",
)
parser.add_argument(
    "-xl",
    "--x-label",
    help="label for the x-axis. If no label is given the x-axis will not be labelled",
)
args = parser.parse_args()

if args.line_plot and args.split_groups:
    print("WARNING: when drawing a line plot, splitting groups does not work")
    args.split_groups = False

if args.output is None:
    args.output = os.path.dirname(args.input)

file = open(args.input, "r")

data = read_json(file)

if args.y_limit == None:
    y_lim = None
else:
    y_lim = float(args.y_limit)

# CALL DRAW FUNCTIONS

if args.split_groups:
    for group in data.unique_groups:
        indices = [i for i in range(len(data)) if data.groups[i] == group]
        draw_plot(
            indices,
            data,
            args.output,
            args.identifier + " " + group,
            y_lim,
            args.line_plot,
            args.x_label,
        )
else:
    indices = [member for g in data.unique_groups for member in data.group_members[g]]
    draw_plot(
        indices, data, args.output, args.identifier, y_lim, args.line_plot, args.x_label
    )
if args.sub_plots:
    draw_sub_plots(data, args.output, args.identifier, y_lim)
