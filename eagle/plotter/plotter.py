import argparse
from plotterutil import *
from reader import read_json
import os
from scale import scale

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
    "-ytop", "--y-top", help="the top y-limit of the produced plots", type=float
)
parser.add_argument(
    "-ybot", "--y-bottom", help="the bottom y-limit of the produced plots", type=float
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
parser.add_argument(
    "--detailed",
    action="store_true",
    help="If given, the plotter will label all bars with their time taken",
)
parser.add_argument("-u", "--unit", help="unit to be used for the y-axis", default="s")
parser.add_argument(
    "-leg", "--legend-placement", help="placement of the legend", default="upper left"
)

parser.add_argument(
    "--scale",
    help="scale to be used for the plot (either log or linear)",
    default="linear",
)
args = parser.parse_args()

if args.line_plot and args.split_groups:
    print("WARNING: when drawing a line plot, splitting groups does not work")
    args.split_groups = False

if args.output is None:
    args.output = os.path.dirname(args.input)

file = open(args.input, "r")

data = read_json(file)

if args.detailed:
    data.set_detailed()

units = {"ms": 1, "s": 1000, "min": 60000, "h": 3600000}


def linear_place(min_val, max_val, y, yerr, y_lim, white_font, black_font):
    font = white_font
    va = "top"
    yi = min(y_lim[1], y) * 0.35
    if y < 0.13 * max_val:
        yi = (
            y + yerr + 0.01 * max_val
        )  # place the text slightly above the 95 percentile mark
        font = black_font
        va = "bottom"
    return (va, font, yi)


def log_place(min_val, max_val, y, yerr, y_lim, white_font, black_font):
    font = white_font
    va = "top"
    yi = math.exp(
        math.log(max(y_lim[0], min(y_lim[1], y)) / min_val) / 2 + math.log(min_val)
    )
    if math.log(y) < math.log(max_val) * 0.15:
        yi = (y + yerr) * 1.1  # place the text slightly above the 95 percentile mark
        font = black_font
        va = "bottom"

    return (va, font, yi)


scales = {
    "linear": scale("linear", "({0})", linear_place),
    "log": scale("log", "({0} - Log Scale)", log_place),
}

if args.unit is not None and args.unit in units:
    data.set_unit((args.unit, units[args.unit]))

if args.scale in scales:
    data.set_scale(scales[args.scale])

if args.y_top == None:
    y_top = None
else:
    y_top = float(args.y_top)
if args.y_bottom == None:
    y_bot = None
else:
    y_bot = float(args.y_bottom)

# CALL DRAW FUNCTIONS

if args.split_groups:
    for group in data.unique_groups:
        indices = [i for i in range(len(data)) if data.groups[i] == group]
        draw_plot(
            indices,
            data,
            args.output,
            args.identifier + " " + group,
            [y_bot, y_top],
            args.line_plot,
            args.x_label,
        )
else:
    indices = [member for g in data.unique_groups for member in data.group_members[g]]
    draw_plot(
        indices,
        data,
        args.output,
        args.identifier,
        [y_bot, y_top],
        args.line_plot,
        args.x_label,
        args.legend_placement,
    )
if args.sub_plots:
    draw_sub_plots(data, args.output, args.identifier, [y_bot, y_top])
