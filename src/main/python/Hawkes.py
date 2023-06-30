import math

import numpy as np
from scipy.optimize import minimize
import numpy as np
from scipy.integrate import quad
from collections import defaultdict

def hawkes_likelihood(parameters, events):
    alpha, beta, mu = parameters
    n = len(events)

    # Compute the intensity function lambda(t) for each event
    intensity = np.zeros(n)
    for i in range(n):
        t = events[i]
        intensity[i] = mu + np.sum(alpha * np.exp(-beta * (t - events[:i])))

    integral_value = 0
    for i in events:
        integral_value += integral_kernel_function(parameters,i,50)

    # Compute the log-likelihood
    log_likelihood = np.sum(np.log(intensity)) - integral_value
    return -log_likelihood


def intensity_function(parameters, events , T):
    alpha, beta, mu = parameters
    n = len(events)
    intensity = mu
    for i in range(len(events)):
        if events[i]<=T:
            intensity+=alpha * np.exp(-beta * (T - events[i]))
    return  intensity


def integral_kernel_function(parameters,ti,T):
    alpha, beta, mu = parameters
    gx = alpha/beta*(math.exp(-beta*ti)-math.exp(-beta*T))
    return gx



def calculate_intensity(events, start_time, end_time, alpha, beta):
    intensity = {}
    for data_id, request_times in events.items():
        intensity[data_id] = sum(alpha * np.exp(-beta * (t - start_time)) for t in request_times if start_time <= t <= end_time)
    return intensity

def find_max_intensity_data(intensity):
    max_data_id = None
    max_intensity = -float('inf')
    for data_id, value in intensity.items():
        if value > max_intensity:
            max_data_id = data_id
            max_intensity = value
    return max_data_id

def read_file(filename):
    user_data = defaultdict(dict)

    with open(filename, 'r') as file:
        for line in file:
            line = line.strip()
            if line:
                parts = line.split(' - ')
                user_id = int(parts[0])
                data_id_and_times = parts[1].split(':')
                data_id = int(data_id_and_times[0])
                request_times = list(map(int, data_id_and_times[1].split(',')))

                user_data[user_id][data_id] = request_times

    return user_data


def compute_intensity(user_data, parameters):
    intensity_dict = {}
    for user_id, data_dict_user in user_data.items():
        intensity_dict_user = {}
        for data_id, request_times in data_dict_user.items():
            intensity_dict_data = {}
            for i in range(1,101):
                intensity_dict_data[i] = intensity_function(parameters, request_times, i)
            intensity_dict_user[data_id] = intensity_dict_data
        intensity_dict[user_id] = intensity_dict_user
    return intensity_dict


def select_data(data_dict):
    selected_data_dict = {}

    for user_id, data_dict_user in data_dict.items():
        selected_data_dict_user = {}
        for time in range(1, 101):
            max_intensity = float('-inf')
            selected_data_id = None
            for data_id, intensity in data_dict_user.items():
                if time in intensity and intensity[time] > max_intensity:
                    max_intensity = intensity[time]
                    selected_data_id = data_id
            selected_data_dict_user[time] = selected_data_id

        selected_data_dict[user_id] = selected_data_dict_user

    return selected_data_dict


def write_data_to_file(data_dict, file_path):
    with open(file_path, 'w') as file:
        for user_id, data_ids in data_dict.items():
            data_ids_str = ','.join(str(data_id) for data_id in data_ids.values())
            file.write(f"{user_id}:{data_ids_str}\n")



# def find_max_intensity_data(user_data_intensity):
# 观测到的样本数据
data = np.array([6,9,10,11,13,15,17,19,21,28,29,31,34,39,40,43,45,51,55,59,63,66,69,71,75,77,78,81,83,85,91,93,94,95,96,100])

# 初始参数值
initial_params = np.array([2, 1,0.3])

values = []
for i in range(1, 101):
    intensity = intensity_function(initial_params, data, i)
    values.append(intensity)
    print(intensity, end='\t')

print("\n", values)
#
# # 示例使用
# filename = 'D:\JavaProject\EdgeComputingCaching\src\AlgorithmicData\history_hawkes_events.txt'  # 替换为你的文件名
# start_time = 0
# end_time = 100
#
# user_data = read_file(filename)
# user_intensity_dict = compute_intensity(user_data,initial_params)
#
# # 调用函数获取用户-数据ID列表
# selected_data_dict = select_data(user_intensity_dict)
#
# # 调用函数将数据写入文本文件
# write_data_to_file(selected_data_dict, 'D:\JavaProject\EdgeComputingCaching\src\AlgorithmicData\hawkes_predicte.txt')