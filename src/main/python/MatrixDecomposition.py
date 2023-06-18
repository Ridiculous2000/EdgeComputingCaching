import numpy as np
from sklearn.decomposition import NMF

# 读取文件，生成矩阵
def read_matrix_from_file(file_path):
    matrix = []
    with open(file_path, 'r') as file:
        for line in file:
            row = [float(x) for x in line.strip().split(',')]
            matrix.append(row)
    return np.array(matrix)

# 写入矩阵到文件，保留三位小数
def write_matrix_to_file(matrix, file_path):
    with open(file_path, 'w') as file:
        for row in matrix:
            formatted_row = [f"{x:.3f}" for x in row]
            line = ', '.join(formatted_row)
            file.write(line + '\n')

# 读取文件生成矩阵
input_file = 'D:\\JavaProject\\EdgeComputingCaching\\src\\AlgorithmicData\\cumulative_request_matrix.txt'
matrix = read_matrix_from_file(input_file)

# 执行 NMF 矩阵分解
K = 10  # 分解出的矩阵的维度
model = NMF(n_components=K, init='random', random_state=0,max_iter=500)
W = model.fit_transform(matrix)
H = model.components_

# 打印分解后的矩阵信息
print("用户矩阵 W:")
print(f"行数: {W.shape[0]}, 列数: {W.shape[1]}")
print("数据矩阵 H:")
print(f"行数: {H.shape[0]}, 列数: {H.shape[1]}")

# 写入分解后的矩阵到文件
user_matrix_file = 'D:\\JavaProject\\EdgeComputingCaching\\src\\AlgorithmicData\\user_matrix.txt'
data_matrix_file = 'D:\\JavaProject\\EdgeComputingCaching\\src\\AlgorithmicData\\data_matrix.txt'
write_matrix_to_file(W, user_matrix_file)
write_matrix_to_file(H, data_matrix_file)
