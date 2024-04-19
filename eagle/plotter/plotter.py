import json
import matplotlib.pyplot as plt
from matplotlib.pyplot import rcParams
import sys
import argparse

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
args = parser.parse_args()


def addlabels(x, y):
    font = {"family": "DejaVu Sans", "size": 12, "color": "white"}
    for i in range(len(x)):
        plt.text(
            i, y[i] / 2, str(int(y[i])) + "ms", ha="center", va="bottom", fontdict=font
        )
        difference = y[i] / y[0]
        if i == 0:
            text = "(Ref)"
        elif difference < 1:
            text = "(-" + str(int((1 - difference) * 100)) + "%)"
        else:
            text = "(+" + str(int((difference - 1) * 100)) + "%)"

        plt.text(i, y[i] / 2, text, ha="center", va="top", fontdict=font)


def write_labels(labels):
    text = ""
    for label in labels:
        text += label + "\n"

    props = dict(boxstyle="round", facecolor="wheat", alpha=0.5)
    plt.text(
        0.5,
        -0.05,
        text[0:-1],
        transform=plt.gcf().transFigure,
        fontsize=14,
        verticalalignment="top",
        horizontalalignment="center",
        bbox=props,
    )


file = open(args.input, "r")

experiment = json.loads(file.read())
data = experiment["data"]
file.close()

font = {"family": "DejaVu Sans", "weight": "bold", "size": 15}

plt.rc("font", **font)

thrown_away = 1

for test in data:
    test["times"] = test["times"][thrown_away:]
    test["iterations"] -= thrown_away
    test["sorted times"] = [time for time in test["times"]]
    test["sorted times"].sort()

names = [test["name"] for test in data]
# NOTE: ignores 'thrown_away' entries in all time lists to account for cold starts
times = [sum(test["times"]) / (test["iterations"]) for test in data]
errors_lo = [times[i] - data[i]["sorted times"][0] for i in range(len(data))]
errors_hi = [data[i]["sorted times"][-1] - times[i] for i in range(len(data))]

percentile = 5
index = []
for i in range(len(data)):
    index = int(percentile * data[i]["iterations"] / 100)
print(index)

errors_lo_95p = [times[i] - data[i]["sorted times"][index] for i in range(len(data))]
errors_hi_95p = [
    data[i]["sorted times"][-index - 1] - times[i] for i in range(len(data))
]

_, ax = plt.subplots(figsize=(2 * len(data), 5))

ax.grid(axis="y", which="major", linewidth=1, alpha=0.3, linestyle="dashed")
plt.grid(
    color="gray", linestyle="dashed", linewidth=1, alpha=0.3, axis="y", which="minor"
)
ax.minorticks_on()
ax.set_axisbelow(True)
plt.tick_params(axis="x", rotation=30)

plt.bar(names, times, color=[test["colour"] for test in data])
plt.ylabel("Join time (ms)")

addlabels(names, times)

plt.title(experiment["title"])

plt.errorbar(
    names,
    times,
    yerr=[errors_lo, errors_hi],
    marker=" ",
    fmt="o",
    capsize=5,
    elinewidth=0,
    color="black",
)
eb = plt.errorbar(
    names,
    times,
    yerr=[errors_lo_95p, errors_hi_95p],
    marker=" ",
    fmt="o",
    capsize=10,
    elinewidth=2,
    color="black",
)

if args.y_limit == None:
    y_lim = None
else:
    y_lim = int(args.y_limit)

plt.ylim(bottom=0, top=y_lim)

plt.savefig(
    args.output + "/" + experiment["title"] + " " + args.identifier + ".png",
    bbox_inches="tight",
)
plt.clf()

if not args.sub_plots:
    exit(0)


for test in data:
    fig, ax = plt.subplots()
    plt.plot(test["times"], linestyle="dotted")
    plt.ylabel("Join time (ms)")
    plt.xlabel("Iteration")
    plt.locator_params(axis="x", nbins=10, tight=True)
    plt.tick_params(axis="x", rotation=30)
    ax.margins(x=0)
    plt.title("Join Times for " + test["name"])
    plt.ylim(bottom=0, top=y_lim)
    write_labels(test["labels"])

    plt.savefig(
        args.output + "/" + test["name"] + " " + args.identifier + ".png",
        bbox_inches="tight",
    )
    plt.clf()