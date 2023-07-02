from collections import OrderedDict

import torch
import torch.nn as nn
import numpy as np

# 定义编码器模型
class Encoder(nn.Module):
    def __init__(self, input_size, hidden_size, num_layers):
        super(Encoder, self).__init__()
        self.hidden_size = hidden_size
        self.num_layers = num_layers
        self.lstm = nn.LSTM(input_size, hidden_size, num_layers, batch_first=True)

    def forward(self, x):
        h0 = torch.zeros(self.num_layers, x.size(0), self.hidden_size).to(x.device)
        c0 = torch.zeros(self.num_layers, x.size(0), self.hidden_size).to(x.device)

        _, (hidden_state, cell_state) = self.lstm(x, (h0, c0))

        return hidden_state, cell_state


# 定义解码器模型
class Decoder(nn.Module):
    def __init__(self, input_size, hidden_size, num_layers, output_size, output_sequence_length):
        super(Decoder, self).__init__()
        self.hidden_size = hidden_size
        self.num_layers = num_layers
        self.output_sequence_length = output_sequence_length
        self.lstm = nn.LSTM(input_size, hidden_size, num_layers, batch_first=True)
        self.fc = nn.Linear(hidden_size, output_size)

    def forward(self, x, hidden_state, cell_state):
        out, _ = self.lstm(x, (hidden_state, cell_state))
        out = self.fc(out[:, -self.output_sequence_length:, :])  # 切片操作，保留最后5个时间步的输出

        return out


# 定义编码器-解码器模型
class EncoderDecoder(nn.Module):
    def __init__(self, input_size, hidden_size, num_layers, output_size, output_sequence_length):
        super(EncoderDecoder, self).__init__()
        self.encoder = Encoder(input_size, hidden_size, num_layers)
        self.decoder = Decoder(input_size, hidden_size, num_layers, output_size, output_sequence_length)

    def forward(self, x):
        hidden_state, cell_state = self.encoder(x)
        output = self.decoder(x, hidden_state, cell_state)

        return output


def lstm_predicte(input_data):
    # 定义输入特征数、输出特征数等超参数
    input_size = 100
    hidden_size = 32
    num_layers = 4
    output_size = 100

    # 创建随机输入数据（batch_size为4，时间步长为20）
    batch_size = 5
    input_sequence_length = 50
    output_sequence_length = 50

    # input_data = torch.randn(batch_size, input_sequence_length, input_size)


    # 创建模型实例
    model = EncoderDecoder(input_size, hidden_size, num_layers, output_size, output_sequence_length)

    # 执行前向传播
    output = model(input_data)

    # 输出结果形状（batch_size, 输出时间步长, 输出特征数）
    return output




def read_server_data(filename):
    server_data = {}

    with open(filename, 'r') as file:
        lines = file.readlines()

        # 每50行为一个数据
        for i in range(0, len(lines), 50):
            start_index = i
            end_index = i + 50

            # 提取服务器ID
            server_line = lines[start_index].strip()
            server_id, _ = server_line.split(':')
            server_id = int(server_id)

            # 提取50行数据
            data_lines = lines[start_index:end_index]
            data = []

            # 将每行数据转换为整数列表
            for line in data_lines:
                _, values = line.strip().split(':')
                values = values.split(',')
                values = [int(value) for value in values]
                data.append(values)

            # 将50行数据作为一个二维数组存入字典
            server_data[server_id] = data

    return server_data


# Example usage:
filename = "D:\JavaProject\EdgeComputingCaching\src\AlgorithmicData\server_history.txt"  # Replace with the actual filename
server_data = read_server_data(filename)
server_data_ordered = OrderedDict(server_data)
# 获取键的顺序
keys = list(server_data_ordered.keys())
values = server_data.values()

# 将值（二维数组）转换为 PyTorch 张量
tensor_values = [torch.tensor(arr, dtype=torch.float32) for arr in values]

# 构建一个列表，将每个转换后的张量添加到列表中
tensor_list = [tensor.unsqueeze(0) for tensor in tensor_values]

# 将列表转换为 PyTorch 3D 张量
input_data = torch.cat(tensor_list, dim=0)

output_data = lstm_predicte(input_data)

# 获取数组的形状
batch_size, sequence_length, output_size = output_data.shape

output_dict = dict(zip(keys, output_data))

# print(output_dict)

output_file = "D:\JavaProject\EdgeComputingCaching\src\AlgorithmicData\output.txt"  # 指定输出文件路径

with open(output_file, "w") as file:
    for key, value in output_dict.items():
        file.write(f"{key}:\n")
        value = value.detach().numpy()  # 分离张量并转换为NumPy数组
        for row in value:
            row_str = ",".join(str(element) for element in row)
            file.write(f"{row_str}\n")
        file.write("\n")

input_file = "D:\JavaProject\EdgeComputingCaching\src\AlgorithmicData\output.txt"   # 输入文件路径

data_dict = {}  # 存储数据的字典

with open(input_file, "r") as file:
    lines = file.readlines()
    num_lines = len(lines)
    i = 0
    while i < num_lines:
        line = lines[i].strip()
        if line.endswith(":"):
            server_id = line[:-1]  # 提取服务器ID
            data_dict[server_id] = []  # 创建空数组用于存储数据
            i += 1
            while i < num_lines and lines[i].strip():  # 读取非空行
                row = [float(val) for val in lines[i].strip().split(",")]  # 解析一行数据
                data_dict[server_id].append(row)  # 将一行数据添加到数组中
                i += 1
        i += 1

# print(data_dict)

normalization_data_dict = {}  # 存储处理后的字典

for server_id, data in data_dict.items():
    normalization_data_dict[server_id] = []  # 创建空数组用于存储索引

    for row in data:
        top10_indices = sorted(range(len(row)), key=lambda i: row[i], reverse=True)[:10]  # 找出最大的10个值的索引
        normalization_data_dict[server_id].append(top10_indices)  # 将索引数组添加到输出字典中

# print(normalization_data_dict)


# 定义要保存的文件路径
output_file = "D:\JavaProject\EdgeComputingCaching\src\AlgorithmicData\processed_data.txt"

# 打开文件进行写入
with open(output_file, "w") as file:
    for server_id, indices in normalization_data_dict.items():
        file.write(f"{server_id}:\n")
        for index_list in indices:
            line = ",".join(str(index) for index in index_list)
            file.write(f"{line}\n")
        file.write("\n")




# input_data = torch.tensor([
#     [[1, 0, 0, 1, 0, 0, 0, 0, 0, 1] for _ in range(50)]
#     for _ in range(5)
# ], dtype=torch.float32)