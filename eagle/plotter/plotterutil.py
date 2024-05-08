import matplotlib.pyplot as plt
from matplotlib.pyplot import rcParams
from collections import OrderedDict, defaultdict
from data import data


def addlabels(data: data, indices, ticks):
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
        max_val = max(max_val, data.times[i])

    for x, i in enumerate(indices):
        font = white_font
        va = "top"
        yi = data.times[i] / 2
        if data.times[i] < 0.1 * max_val:
            yi = data.times[i] + data.errors_hi_95p[i] * 1.1
            font = black_font
            va = "bottom"

        difference = data.times[i] / data.times[data.group_members[data.groups[i]][0]]
        if i == data.group_members[data.groups[i]][0]:
            if len(data.group_members[data.groups[i]]) > 1:
                text = "(Reference)"
            else:
                text = ""
        elif difference < 1:
            text = "(-" + str(round((1 - difference) * 100)) + "%)"
        elif difference > 1:
            text = "(+" + str(round((difference - 1) * 100)) + "%)"
        else:
            text = "(±0%)"

        annotation_font = {i: font[i] for i in font}
        annotation_font["size"] -= 2
        annotation_font["weight"] = "regular"

        text = plt.text(
            ticks[x],
            yi,
            text,
            fontdict=annotation_font,
            multialignment="center",
            ha="center",
            va=va,
        )  # custom properties
        text = plt.annotate(
            "{:0.2f}s".format(data.times[i]),
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


def draw_plot(indices, data, path, id, y_lim):
    bar_width = 0.8
    group_gap = 1
    non_group_gap = 1.25

    relevant_names = [data.names[i] for i in indices]
    relevant_times = [data.times[i] for i in indices]
    relevant_colours = [data.colours[i] for i in indices]
    group_placements = defaultdict(list)
    ticks = [0]
    group_placements[data.groups[indices[0]]].append(ticks[-1])
    for i in range(1, len(indices)):
        if data.groups[indices[i]] == data.groups[indices[i - 1]]:
            ticks.append(ticks[-1] + group_gap)
        else:
            ticks.append(ticks[-1] + non_group_gap)
        group_placements[data.groups[indices[i]]].append(ticks[-1])

    group_placements = {
        group: sum(group_placements[group]) / len(group_placements[group])
        for group in group_placements
    }
    groups_set = set()
    for index in indices:
        groups_set.add(data.groups[index])
    num_groups = len(groups_set)

    _, ax = plt.subplots(figsize=(2 * len(indices), 5))

    if y_lim is None:
        ax.margins(None, 0.15)

    plt.suptitle(data.title, fontsize=20, y=1)
    if num_groups == 1:
        plt.title(next(iter(groups_set)), fontsize=15, weight="bold")

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

    plt.bar(
        ticks,
        relevant_times,
        color=relevant_colours,
        label=relevant_names,
        width=bar_width,
    )
    plt.ylabel("Join time (s)")

    addlabels(data, indices, ticks)

    plt.errorbar(
        ticks,
        relevant_times,
        yerr=[
            [data.errors_lo[i] for i in indices],
            [data.errors_hi[i] for i in indices],
        ],
        marker=" ",
        fmt=" ",
        capsize=5,
        elinewidth=0,
        color="black",
    )
    plt.errorbar(
        ticks,
        relevant_times,
        yerr=[
            [data.errors_lo_95p[i] for i in indices],
            [data.errors_hi_95p[i] for i in indices],
        ],
        marker=" ",
        fmt=" ",
        capsize=10,
        elinewidth=2,
        color="black",
    )

    if num_groups > 1:
        plt.xticks(
            [group_placements[group] for group in group_placements],
            [group for group in group_placements],
        )
    else:
        plt.xticks([], [])
    plt.ylim(bottom=0, top=y_lim)

    handles, labels = plt.gca().get_legend_handles_labels()
    by_label = OrderedDict(
        zip(labels, handles)
    )  # this is done to avoid duplicate entries in legend
    ax.legend(
        by_label.values(), by_label.keys(), loc="upper left", ncols=1, fontsize=10
    )

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
