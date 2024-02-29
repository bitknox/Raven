import json
import matplotlib.pyplot as plt 
from matplotlib.pyplot import rcParams 
import sys

def write_labels(ax,labels):
        text=""
        for label in labels:
                text += label + "\n"
        
        props = dict(boxstyle='round', facecolor='wheat', alpha=0.5)
        plt.text(0.5, 0, text[0:-1], transform=plt.gcf().transFigure, fontsize=14,
        verticalalignment='top', horizontalalignment="center", bbox=props)
        

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

# rcParams.update({'figure.autolayout': True})
plt.bar(names,times,color="darkturquoise")
plt.ylabel("Join time (ms)")
plt.title("Average Join Times")
eb = plt.errorbar(names,times, yerr=[errors_lo,errors_hi], marker=" ", fmt="o", capsize=10, elinewidth=2, color="darkslategray")
eb[-1][0].set_linestyle("--")
# plt.tight_layout()
# plt.show()
plt.savefig(sys.argv[2]+"/big " + sys.argv[3] +".png", bbox_inches="tight")
plt.clf()

for test in data:
        fig, ax = plt.subplots(figsize=(8, 6))
        plt.figure(figsize=(10,6))
        plt.plot(test["times"], linestyle = 'dotted')
        plt.ylabel("Join time (ms)")
        plt.xlabel("Iteration")
        plt.xticks(range(0,len(test["times"])),range(1,len(test["times"])+1))
        plt.title("Join Times for " + test["name"])
        plt.ylim(bottom=0)
        write_labels(ax,test["labels"])
        # plt.autoscale()
        # plt.show()
        plt.savefig(sys.argv[2]+"/"+test["name"] + " " + sys.argv[3] + ".png", bbox_inches="tight")
        plt.clf()
