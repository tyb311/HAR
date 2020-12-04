import numpy as np


def one_hot(x):
    return np.eye(5)[x]

# x = np.random.randint(low=0, high=5, size=(17,))
# y = one_hot(x)
# print(x.shape)
# print(y.shape)


import torch, torchvision
from torch import nn
from torch.nn import functional as F

# 参数列表

# input_size：x的特征维度
# hidden_size：隐藏层的特征维度
# num_layers：lstm隐层的层数，默认为1
# bias：False则bih=0和bhh=0. 默认为True
# batch_first：True则输入输出的数据格式为 (batch, seq, feature)
# dropout：除最后一层，每一层的输出都进行dropout，默认为: 0
# bidirectional：True则为双向lstm默认为False
# 输入：input, (h0, c0)
# 输出：output, (hn,cn)
# 输入数据格式：
# input(seq_len, batch, input_size)
# h0(num_layers * num_directions, batch, hidden_size)
# c0(num_layers * num_directions, batch, hidden_size)

# 输出数据格式：
# output(seq_len, batch, hidden_size * num_directions)
# hn(num_layers * num_directions, batch, hidden_size)
# cn(num_layers * num_directions, batch, hidden_size)

# x = torch.rand(7,3,151)
# net = nn.LSTM(input_size=151, hidden_size=128, num_layers=1, batch_first=True)
# y,_ = net(x)
# print(y.shape)


import paddle.fluid as fluid
import paddle.fluid.dygraph.base as base
import numpy

D = 151
T = 1#sum(lod[0])

input = numpy.random.rand(T, 3 * D).astype('float32')
hidden_input = numpy.random.rand(T, D).astype('float32')

with fluid.dygraph.guard():
    gru = fluid.dygraph.GRUUnit(size=D * 3)
    h,r,g = gru(base.to_variable(input), base.to_variable(hidden_input))

    print(h.shape,r.shape,g.shape)