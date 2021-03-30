import numpy as np
from numpy.polynomial.polynomial import polyfit
import pandas as pd
import os
import matplotlib.pyplot as plt


PATH = './'

fileNames = os.listdir(PATH)

fileList = [file for file in fileNames if '.csv' in file]

slopeList = []
stepSizeList = []

for file in reversed(sorted(fileList)):
    df = pd.read_csv(PATH + file, sep=',')

    b, m = polyfit(df['time'], df['msd'], 1)

    plt.plot(df['time'], df['msd'], '.')
    plt.plot(df['time'], b + m * df['time'], '-')
    plt.show() 
    slopeList.append(m / (float(file[-7:-4]) ** 2))
    stepSizeList.append(float(file[-7:-4]))

    	
#plt.plot(stepSizeList, slopeList, label="no gel")
#plt.xlabel('Step Size')
#plt.ylabel('MSD Slope')
#plt.title('no gel')
plt.show()

print(slopeList)
