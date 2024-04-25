import json
import matplotlib.pyplot as plt
from plotterutil import *
from data import data


def read_json(file):
    experiment = json.loads(file.read())
    tests = experiment["data"]
    file.close()

    font = {"family": "DejaVu Sans", "weight": "bold", "size": 15}

    plt.rc("font", **font)

    thrown_away = 1

    for test in tests:
        test["times"] = [y / 1000 for y in test["times"]]
        test["times"] = test["times"][thrown_away:]
        test["iterations"] -= thrown_away
        test["sorted times"] = [time for time in test["times"]]
        test["sorted times"].sort()

    names = [test["name"] for test in tests]
    colours = [test["colour"] for test in tests]
    # NOTE: ignores 'thrown_away' entries in all time lists to account for cold starts
    times = [sum(test["times"]) / (test["iterations"]) for test in tests]
    errors_lo = [times[i] - tests[i]["sorted times"][0] for i in range(len(tests))]
    errors_hi = [tests[i]["sorted times"][-1] - times[i] for i in range(len(tests))]

    percentile = 5
    index = []
    for i in range(len(tests)):
        index = int(percentile * tests[i]["iterations"] / 100)

    errors_lo_95p = [
        times[i] - tests[i]["sorted times"][index] for i in range(len(tests))
    ]
    errors_hi_95p = [
        tests[i]["sorted times"][-index - 1] - times[i] for i in range(len(tests))
    ]

    return data(
        names,
        times,
        colours,
        errors_lo,
        errors_hi,
        errors_lo_95p,
        errors_hi_95p,
        experiment["title"],
    )
