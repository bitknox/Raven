from collections import defaultdict


class data:
    def __init__(
        self,
        names,
        groups,
        times,
        colours,
        labels,
        errors_lo,
        errors_hi,
        errors_lo_95p,
        errors_hi_95p,
        title,
    ):
        self.names = names
        self.groups = {}
        self.group_members = defaultdict(list)
        self.unique_groups = []
        for group in groups:
            if not group in self.unique_groups:
                self.unique_groups.append(group)
        for i, c in enumerate(groups):
            self.groups[i] = c
            self.group_members[c].append(i)
        self.times = times
        self.colours = colours
        self.labels = labels
        self.errors_lo = errors_lo
        self.errors_hi = errors_hi
        self.errors_lo_95p = errors_lo_95p
        self.errors_hi_95p = errors_hi_95p
        self.title = title
        self.detailed = False
        self.unit = ("ms", 1)
        self.scale = ("linear", "({0})")

    def __len__(self):
        return len(self.names)

    def set_detailed(self):
        self.detailed = True

    def set_unit(self, unit):
        self.times = [y * self.unit[1] for y in self.times]
        self.errors_lo = [y * self.unit[1] for y in self.errors_lo]
        self.errors_hi = [y * self.unit[1] for y in self.errors_hi]
        self.errors_lo_95p = [y * self.unit[1] for y in self.errors_lo_95p]
        self.errors_hi_95p = [y * self.unit[1] for y in self.errors_hi_95p]
        self.unit = unit
        self.times = [y / self.unit[1] for y in self.times]
        self.errors_lo = [y / self.unit[1] for y in self.errors_lo]
        self.errors_hi = [y / self.unit[1] for y in self.errors_hi]
        self.errors_lo_95p = [y / self.unit[1] for y in self.errors_lo_95p]
        self.errors_hi_95p = [y / self.unit[1] for y in self.errors_hi_95p]

    def set_scale(self, scale):
        self.scale = scale
