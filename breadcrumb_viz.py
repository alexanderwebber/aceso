import pandas as pd
from pandas import DataFrame
import matplotlib.pyplot as plt
from mpl_toolkits.mplot3d import Axes3D

df = pd.read_csv('breadcrumbs.csv')
print(df.head())

three_d = plt.figure().gca(projection='3d')
three_d.scatter(df['X2'], df['Y2'], df['Z2'])
plt.show()
