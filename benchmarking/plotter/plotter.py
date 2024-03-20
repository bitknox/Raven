import json
import matplotlib.pyplot as plt
from matplotlib.pyplot import rcParams
import sys


def write_labels(ax, labels):
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


colours = ["darkred", "darkgreen", "darkblue", "darkorange", "indigo", "dimgray"]

file = open(sys.argv[1], "r")

data = json.loads(file.read())
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

_, ax = plt.subplots()

ax.grid(axis="y", which="major", linewidth=1, alpha=0.3, linestyle="dashed")
plt.grid(
    color="gray", linestyle="dashed", linewidth=1, alpha=0.3, axis="y", which="minor"
)
ax.minorticks_on()
ax.set_axisbelow(True)

plt.bar(names, times, color=colours)
plt.ylabel("Join time (ms)")
plt.title("Average Join Times")
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

plt.savefig(sys.argv[2] + "/big " + sys.argv[3] + ".png", bbox_inches="tight")
plt.clf()

for test in data:
    fig, ax = plt.subplots()
    plt.plot(test["times"], linestyle="dotted")
    plt.ylabel("Join time (ms)")
    plt.xlabel("Iteration")
    plt.locator_params(axis="x", nbins=10, tight=True)
    plt.tick_params(axis="x", rotation=30)
    ax.margins(x=0)
    plt.title("Join Times for " + test["name"])
    plt.ylim(bottom=0)
    write_labels(ax, test["labels"])

    plt.savefig(
        sys.argv[2] + "/" + test["name"] + " " + sys.argv[3] + ".png",
        bbox_inches="tight",
    )
    plt.clf()
