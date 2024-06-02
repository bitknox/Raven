import matplotlib.pyplot as plt
import matplotlib
from collections import OrderedDict, defaultdict
from data import data
import math


def addlabels(data: data, indices, ticks, y_lim):
    white_font = {
        "family": "DejaVu Sans",
        "size": 16,
        "color": "white",
        "weight": None,
    }
    black_font = {
        "family": "DejaVu Sans",
        "size": 16,
        "color": "black",
        "weight": None,
    }

    max_val = 0
    min_val = math.inf

    for i in indices:
        max_val = max(max_val, data.times[i] + data.errors_hi[i])
        min_val = min(min_val, data.times[i] + data.errors_hi[i])

    if y_lim[1] is None:
        y_lim[1] = max_val
    max_val = min(max_val, y_lim[1])

    if y_lim[0] is None:
        y_lim[0] = min_val
    min_val = max(min_val, y_lim[0])

    for x, i in enumerate(indices):
        va, font, yi = data.scale.place_label(
            min_val,
            max_val,
            data.times[i],
            data.errors_hi[i],
            y_lim,
            white_font,
            black_font,
        )

        difference = data.times[i] / data.times[data.group_members[data.groups[i]][0]]
        if i == data.group_members[data.groups[i]][0]:
            if len(data.group_members[data.groups[i]]) > 1:
                text = "Ref"
            else:
                text = ""
        elif difference < 1:
            text = "-" + str(round((1 - difference) * 100)) + "%"
        elif difference > 1:
            text = "+" + str(round((difference - 1) * 100)) + "%"
        else:
            text = "±0%"

        if data.detailed:
            writeDifferenceAndValue(
                ticks[x],
                yi,
                text,
                font,
                0,
                va,
                data.times[i] * data.unit[1] / 1000,  # convert time to seconds
            )
        else:
            writeDifference(ticks[x], yi, text, font, 30, va)


def writeDifference(tick, yi, text, font, rotation, va):
    return plt.text(
        tick,
        yi,
        text,
        rotation=rotation,
        fontdict=font,
        multialignment="center",
        ha="center",
        va=va,
    )


def writeDifferenceAndValue(tick, yi, text, font, rotation, va, time):
    annotation_font = {i: font[i] for i in font}
    annotation_font["size"] -= 2

    text_box = writeDifference(tick, yi, text, annotation_font, rotation, va)

    if time < 10:
        text = "{:0.2f} s".format(time)
    elif time < 100:
        text = "{:0.1f} s".format(time)
    else:
        text = "{:.0f} s".format(time)

    if time > 60:
        text = "{:0.0f} min\n {:0.0f} s".format(time // 60, time % 60)

    plt.annotate(
        text,
        xycoords=text_box,
        xy=(0.5, 1.3),
        ha="center",
        rotation=rotation,
        color=font["color"],
        weight=font["weight"],
        size=font["size"],
        linespacing=0.9,
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
        fontsize=18,
        verticalalignment="top",
        horizontalalignment="center",
        bbox=props,
    )


def draw_plot(
    indices, data, path, id, y_lim, line_plot, x_label, y_label, legend_placement
):
    matplotlib.rcParams.update({"font.size": 20})
    if not line_plot:
        draw_bars(indices, data, y_lim, legend_placement, y_label)
    else:
        draw_line(indices, data, y_lim, y_label)

    if x_label:
        plt.xlabel(x_label, fontsize=25)

    # plt.locator_params(axis="y", nbins=6)
    plt.locator_params(axis="x", nbins=10)

    illeagal = ["\n", "\\", "/", ":", "*", "?", '"', "<", ">", "|"]
    for char in illeagal:
        data.title = data.title.replace(char, "")
    plt.savefig(
        path + "/" + data.title + " " + id + ".png",
        bbox_inches="tight",
    )
    plt.clf()


def get_unique_groups(indices, data):
    groups_set = set()
    for index in indices:
        groups_set.add(data.groups[index])
    return groups_set


def setup_plot(data, width, padding, groups_set, y_label):
    num_groups = len(groups_set)
    _, ax = plt.subplots(figsize=(width, 5.5))

    ax.margins(padding / width, 0.1)

    plt.suptitle(data.title, fontsize=30, y=1.08)
    if num_groups == 1:
        plt.title(next(iter(groups_set)), fontsize=25, weight="bold")

    plt.grid(axis="y", which="major", linewidth=1.5, alpha=1, linestyle="solid")
    plt.tick_params(axis="x", rotation=30)
    ax.minorticks_on()
    ax.set_axisbelow(True)
    ax.tick_params("y", length=10, width=2, which="major")
    ax.tick_params("y", length=5, width=1.5, which="minor")
    ax.tick_params("x", length=0, width=0, which="minor")
    ax.tick_params("x", length=5, width=2, which="major")
    if y_label:
        plt.ylabel(y_label + " " + data.scale.y_label.format(data.unit[0]), fontsize=25)
    plt.yscale(data.scale.name)

    return ax


def draw_bars(indices, data, y_lim, legend_placement, y_label):
    relative_bar_width = 0.95
    group_gap = 1
    non_group_gap = 1.25
    absolute_bar_width = 1.25  # determines the width of the graph and therefore also the width of the bars in the image
    padding = 0.4  # padding at the left and right edge of the plot

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

    width = absolute_bar_width * (ticks[-1] + relative_bar_width) + 2 * padding
    groups_set = get_unique_groups(indices, data)
    num_groups = len(groups_set)
    ax = setup_plot(data, width, padding, groups_set, y_label)

    plt.bar(
        ticks,
        relevant_times,
        color=relevant_colours,
        label=relevant_names,
        width=relative_bar_width,
    )

    addlabels(data, indices, ticks, y_lim)

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
    plt.ylim(y_lim)

    handles, labels = plt.gca().get_legend_handles_labels()
    by_label = OrderedDict(
        zip(labels, handles)
    )  # this is done to avoid duplicate entries in legend
    ax.legend(
        by_label.values(), by_label.keys(), loc=legend_placement, ncols=1, fontsize=15
    )


def draw_line(indices, data: data, y_lim, y_label):
    import re

    groups_set = get_unique_groups(indices, data)

    if len(groups_set) != len(indices):
        raise Exception(
            "When drawing line plots, all data-points must have a unique group"
        )
    ax = setup_plot(data, 8, 0.4, groups_set, y_label)

    if y_lim[1] is None:
        ax.margins(None, 0.15)
    matches = [(i, re.match("[0-9.]+", data.groups[i])) for i in indices]

    if not all(match for _, match in matches):
        plt.plot([data.groups[i] for i in indices], [data.times[i] for i in indices])
        # plt.xticks(range(len(indices)), [data.groups[i] for i in indices])
    else:
        numbers = [
            float(data.groups[i][match.span()[0] : match.span()[1]])
            for i, match in matches
        ]
        plt.plot(numbers, [data.times[i] for i in indices], color="darkred")

    plt.ylim(y_lim)


def draw_sub_plots(data, path, id, y_lim):
    for i in range(len(data)):
        _, ax = plt.subplots()
        plt.plot(data.times[i], linestyle="dotted")
        plt.ylabel("Join time ({0})".format(data.unit[0]), fontsize=25)
        plt.xlabel("Iteration", fontsize=25)
        plt.locator_params(axis="x", nbins=10, tight=True)
        plt.tick_params(axis="x", rotation=30)

        ax.margins(x=0)
        plt.title("Join Times for " + data.names[i])
        plt.ylim(y_lim)
        write_labels(data.labels[i])

        illeagal = ["\n", "\\", "/", ":", "*", "?", '"', "<", ">", "|"]
        for char in illeagal:
            data.names[i] = data.names[i].replace(char, "")

        plt.savefig(
            path + "/" + data.names[i] + " " + id + ".png",
            bbox_inches="tight",
        )
        plt.clf()
