import json
import matplotlib.pyplot as plt
from matplotlib.pyplot import rcParams
import argparse
from collections import defaultdict
import plotterutil


def addlabels(x, y, indices, groups, group_members):
    white_font = {
        "family": "DejaVu Sans",
        "size": 12,
        "color": "white",
        "weight": None,
    }
    black_font = {
        "family": "DejaVu Sans",
        "size": 12,
        "color": "black",
        "weight": None,
    }

    max_val = 0

    for i in indices:
        max_val = max(max_val, y[i])

    for x, i in enumerate(indices):
        font = white_font
        va = "top"
        yi = y[i] / 2
        if y[i] < 0.1 * max_val:
            yi = y[i] * 1.25
            font = black_font
            va = "bottom"

        difference = y[i] / y[group_members[groups[i]][0]]
        if i == group_members[groups[i]][0]:
            text = "(Reference)"
        elif difference < 1:
            text = "(-" + str(round((1 - difference) * 100)) + "%)"
        else:
            text = "(+" + str(round((difference - 1) * 100)) + "%)"

        annotation_font = {i: font[i] for i in font}
        annotation_font["size"] -= 2

        text = plt.text(
            x,
            yi,
            text,
            fontdict=annotation_font,
            multialignment="center",
            ha="center",
            va=va,
        )  # custom properties
        text = plt.annotate(
            "{:0.2f}s".format(y[i]),
            xycoords=text,
            xy=(0.5, 1.1),
            ha="center",
            color=font["color"],
            weight=font["weight"],
            size=font["size"],
        )


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


def draw_plot(indices, data, path, id, y_lim, groups, group_members):
    _, ax = plt.subplots(figsize=(2 * len(indices), 5))

    ax.grid(axis="y", which="major", linewidth=1, alpha=0.3, linestyle="dashed")
    plt.grid(
        color="gray",
        linestyle="dashed",
        linewidth=1,
        alpha=0.3,
        axis="y",
        which="minor",
    )
    ax.minorticks_on()
    ax.set_axisbelow(True)
    plt.tick_params(axis="x", rotation=30)

    relevant_names = [data.names[i] for i in indices]
    relevant_times = [data.times[i] for i in indices]
    relevant_colours = [data.colours[i] for i in indices]

    plt.bar(
        relevant_names,
        relevant_times,
        color=relevant_colours,
    )
    plt.ylabel("Join time (s)")

    addlabels(data.names, data.times, indices, groups, group_members)

    plt.title(data.title)

    plt.errorbar(
        relevant_names,
        relevant_times,
        yerr=[
            [data.errors_lo[i] for i in indices],
            [data.errors_hi[i] for i in indices],
        ],
        marker=" ",
        fmt="o",
        capsize=5,
        elinewidth=0,
        color="black",
    )
    plt.errorbar(
        relevant_names,
        relevant_times,
        yerr=[
            [data.errors_lo_95p[i] for i in indices],
            [data.errors_hi_95p[i] for i in indices],
        ],
        marker=" ",
        fmt="o",
        capsize=10,
        elinewidth=2,
        color="black",
    )

    plt.ylim(bottom=0, top=y_lim)

    plt.savefig(
        path + "/" + data.title + " " + id + ".png",
        bbox_inches="tight",
    )
    plt.clf()


def draw_sub_plots(data, path, id, y_lim):
    for test in data:
        _, ax = plt.subplots()
        plt.plot(test["times"], linestyle="dotted")
        plt.ylabel("Join time (s)")
        plt.xlabel("Iteration")
        plt.locator_params(axis="x", nbins=10, tight=True)
        plt.tick_params(axis="x", rotation=30)
        ax.margins(x=0)
        plt.title("Join Times for " + test["name"])
        plt.ylim(bottom=0, top=y_lim)
        write_labels(test["labels"])

        plt.savefig(
            path + "/" + test["name"] + " " + id + ".png",
            bbox_inches="tight",
        )
        plt.clf()
