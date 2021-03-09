import matplotlib.pyplot as plt
import pandas as pd
import os
import powerlaw
import numpy as np

PATH = './'

fileNames = os.listdir(PATH)

fileList = [file for file in fileNames if '.csv' in file]

for file in reversed(fileList):
    df = pd.read_csv(PATH + file, sep=',')
    plt.plot(df['time'], df['msd'], label=file[16:20])
    plt.legend(loc="upper left")
    
print(df.head())

plt.show()
