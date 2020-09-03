import matplotlib.pyplot as plt
import pandas as pd

df = pd.read_csv('density_vs_time.csv', sep=',')

df.plot(x = 'Position', y = 'XY')

df.plot(x = 'Position', y = 'XZ')

df.plot(x = 'Position', y = 'YZ')

plt.show()
