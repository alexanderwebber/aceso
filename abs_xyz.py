import pandas as pd
from pandas import DataFrame
import matplotlib.pyplot as plt


df = pd.read_csv('abs_xyz.csv')
print(df.head())


plt.plot(df['x'], label = "x")
plt.plot(df['y'], label = "y")
plt.plot(df['z'], label = "z")
plt.legend(loc="upper left")

plt.title("Absolute Value of Component Vectors")

plt.xlabel("Time Step")
plt.ylabel("Displacement")


plt.show()
