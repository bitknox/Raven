class data:
    def __init__(
        self,
        names,
        times,
        colours,
        errors_lo,
        errors_hi,
        errors_lo_95p,
        errors_hi_95p,
        title,
    ):
        self.names = names
        self.times = times
        self.colours = colours
        self.errors_lo = errors_lo
        self.errors_hi = errors_hi
        self.errors_lo_95p = errors_lo_95p
        self.errors_hi_95p = errors_hi_95p
        self.title = title

    def __len__(self):
        return len(self.names)
