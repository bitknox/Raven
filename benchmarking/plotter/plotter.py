import json
import matplotlib.pyplot as plt 
import sys

file = open(sys.argv[1],"r")

data = json.loads(file.read())
file.close()

font = {'family' : 'DejaVu Sans',
        'weight' : 'bold',
        'size'   : 15}

plt.rc('font', **font)

names = [test["name"] for test in data]
times = [sum(test["times"])/test["iterations"] for test in data]
errors_lo = [times[i]-min(data[i]["times"]) for i in range(len(data))]
errors_hi = [max(data[i]["times"]) - times[i] for i in range(len(data))]


plt.bar(names,times,color="darkturquoise")
plt.ylabel("Join time (ms)")
eb = plt.errorbar(names,times, yerr=[errors_lo,errors_hi], marker=" ", fmt="o", capsize=10, elinewidth=2, color="darkslategray")
eb[-1][0].set_linestyle("--")
plt.tight_layout()

plt.savefig(sys.argv[2])
# plt.show()