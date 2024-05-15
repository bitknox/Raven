import matplotlib.pyplot as plt
from collections import OrderedDict, defaultdict
from data import data


def addlabels(data: data, indices, ticks, y_lim):
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

    if y_lim is None:
        y_lim = max_val
    max_val = min(max_val, y_lim)

    for x, i in enumerate(indices):
        font = white_font
        va = "top"
        yi = min(y_lim, data.times[i]) / 2
        if data.times[i] < 0.1 * max_val:
            yi = (
                data.times[i] + data.errors_hi_95p[i] + 0.01 * max_val
            )  # place the text slightly above the 95 percentile mark
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

        text_box = plt.text(
            ticks[x],
            yi,
            text,
            fontdict=annotation_font,
            multialignment="center",
            ha="center",
            va=va,
        )

        if data.times[i] < 10:
            text = "{:0.2f} s".format(data.times[i])
        elif data.times[i] < 100:
            text = "{:0.1f} s".format(data.times[i])
        else:
            text = "{:.0f} s".format(data.times[i])

        if data.times[i] > 60:
            text = "{:0.0f} min\n {:0.0f} s".format(
                data.times[i] / 60, data.times[i] % 60
            )

        plt.annotate(
            text,
            xycoords=text_box,
            xy=(0.5, 1.3),
            ha="center",
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
        fontsize=14,
        verticalalignment="top",
        horizontalalignment="center",
        bbox=props,
    )


def draw_plot(indices, data, path, id, y_lim, line_plot, x_label):
    if not line_plot:
        draw_bars(indices, data, y_lim)
    else:
        draw_line(indices, data, y_lim)

    if x_label:
        plt.xlabel(x_label)

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


def setup_plot(data, width, padding, groups_set):
    num_groups = len(groups_set)
    _, ax = plt.subplots(figsize=(width, 5))

    ax.margins(padding / width, 0.1)

    plt.suptitle(data.title, fontsize=20, y=1)
    if num_groups == 1:
        plt.title(next(iter(groups_set)), fontsize=15, weight="bold")

    plt.grid(axis="y", which="major", linewidth=1, alpha=0.3, linestyle="dashed")
    plt.grid(
        color="gray",
        linestyle="dashed",
        linewidth=1,
        alpha=0.3,
        axis="y",
        which="minor",
    )
    plt.tick_params(axis="x", rotation=30)
    ax.minorticks_on()
    ax.set_axisbelow(True)
    plt.ylabel("Join time (s)")

    return ax


def draw_bars(indices, data, y_lim):
    relative_bar_width = 0.9
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
    ax = setup_plot(data, width, padding, groups_set)

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
    plt.ylim((0, y_lim))

    handles, labels = plt.gca().get_legend_handles_labels()
    by_label = OrderedDict(
        zip(labels, handles)
    )  # this is done to avoid duplicate entries in legend
    ax.legend(
        by_label.values(), by_label.keys(), loc="upper left", ncols=1, fontsize=10
    )


def draw_line(indices, data: data, y_lim):
    import re

    groups_set = get_unique_groups(indices, data)

    if len(groups_set) != len(indices):
        raise Exception(
            "When drawing line plots, all data-points must have a unique group"
        )
    ax = setup_plot(data, 8, 0.4, groups_set)

    if y_lim is None:
        ax.margins(None, 0.15)
    matches = [(i, re.match("[0-9]+", data.groups[i])) for i in indices]

    if not all(match for _, match in matches):
        plt.plot([data.groups[i] for i in indices], [data.times[i] for i in indices])
        # plt.xticks(range(len(indices)), [data.groups[i] for i in indices])
    else:
        numbers = [
            float(data.groups[i][match.span()[0] : match.span()[1]])
            for i, match in matches
        ]
        plt.plot(numbers, [data.times[i] for i in indices])

    plt.ylim((0, y_lim))


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
        plt.ylim((0, y_lim))
        write_labels(test["labels"])

        plt.savefig(
            path + "/" + test["name"] + " " + id + ".png",
            bbox_inches="tight",
        )
        plt.clf()
