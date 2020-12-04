#加载飞桨和相关类库
import paddle
import paddle.fluid as fluid
from paddle.fluid.dygraph import nn
import paddle.fluid.dygraph as dy
from paddle.fluid import layers
import numpy as np
import os
from PIL import Image
print(paddle.__version__)


# 一行代码实现动转静。
# 动静转换的操作非常简单，仅需添加一个装饰器（ @to_static ），框架就会自动将动态图的程序，转换为静态图的program，并使用该program训练、保存为静态图模型以实现推理部署。

# import paddle
# from paddle.static import InputSpec
# from paddle.fluid.dygraph import Layer
# from paddle.jit import to_static


# class SimpleNet(Layer):
#     def __init__(self):
#         super(SimpleNet, self).__init__()
#         self.linear = paddle.nn.Linear(10, 3)

#     @to_static(input_spec=[InputSpec(shape=[None, 10], name='x'), InputSpec(shape=[3], name='y')])
#     def forward(self, x, y):
#         out = self.linear(x)
#         out = out + y
#         return out


# net = SimpleNet()
# paddle.jit.save(net, './simple_net') 

# 定义mnist数据识别网络结构，同房价预测网络
class MNIST(fluid.dygraph.Layer):
    def __init__(self):
        super(MNIST, self).__init__()

        self.cnn = dy.Conv2D(num_channels=3, num_filters=1, filter_size=3, stride=1, padding=1, act='relu')
        
        self.cls = dy.Sequential(
            dy.Linear(input_dim=784, output_dim=128),
            dy.Dropout(p=.2),
            dy.Linear(input_dim=128, output_dim=5),
        )
        # self.cls = dy.Linear(input_dim=784, output_dim=5)

    # 定义网络结构的前向计算过程
    def forward(self, x):
        x = self.cnn(x)

        b = x.shape[0]
        # print(b)
        x = layers.reshape(x, shape=[b,-1,])
        # print(x.shape)
        x = self.cls(x)
        # print(x.shape)
        return layers.softmax(x, axis=1)


if __name__ == '__main__':

    # 定义预测过程
    with fluid.dygraph.guard():
        model = MNIST()
        
    # 加载模型参数
        # model_dict, _ = fluid.load_dygraph("mnist")
        # model.load_dict(model_dict)

    # 灌入数据
        model.eval()
        tensor_img = np.random.rand(1,3,28,28).astype(np.float32)
        result = model(fluid.dygraph.to_variable(tensor_img))
    #  预测输出取整，即为预测的数字，打印结果
        print("本次预测的数字是", result.numpy().astype('int32'))


if __name__ == '__main__':

    x = np.load('harset/db5_acc.npy')
    y = np.load('harset/db5_lab.npy')

    # # 定义飞桨动态图工作环境
    # with fluid.dygraph.guard():
    #     # 声明网络结构
    #     model = MNIST()
    #     # 启动训练模式
    #     model.train()
    #     # 定义数据读取函数，数据读取batch_size设置为16
    #     train_loader = paddle.batch(paddle.dataset.mnist.train(), batch_size=16)
    #     # 定义优化器，使用随机梯度下降SGD优化器，学习率设置为0.001
    #     optimizer = fluid.optimizer.SGDOptimizer(learning_rate=0.001, parameter_list=model.parameters())


    # 通过with语句创建一个dygraph运行的context
    # 动态图下的一些操作需要在guard下进行
    # with fluid.dygraph.guard():
    #     model = MNIST()
    #     model.train()
    #     train_loader = paddle.batch(paddle.dataset.mnist.train(), batch_size=16)
        # optimizer = fluid.optimizer.SGDOptimizer(learning_rate=0.001, parameter_list=model.parameters())
        
        # EPOCH_NUM = 10
        # for epoch_id in range(EPOCH_NUM):
        #     for batch_id, data in enumerate(train_loader()):
        #         #准备数据，格式需要转换成符合框架要求的
        #         image_data = np.array([x[0] for x in data]).astype('float32')
        #         label_data = np.array([x[1] for x in data]).astype('float32').reshape(-1, 1)
        #         # 将数据转为飞桨动态图格式
        #         image = fluid.dygraph.to_variable(image_data)
        #         label = fluid.dygraph.to_variable(label_data)
                
        #         #前向计算的过程
        #         predict = model(image)
                
        #         #计算损失，取一个批次样本损失的平均值
        #         loss = fluid.layers.square_error_cost(predict, label)
        #         avg_loss = fluid.layers.mean(loss)
                
        #         #每训练了1000批次的数据，打印下当前Loss的情况
        #         if batch_id !=0 and batch_id  % 1000 == 0:
        #             print("epoch: {}, batch: {}, loss is: {}".format(epoch_id, batch_id, avg_loss.numpy()))
                
        #         #后向传播，更新参数的过程
        #         avg_loss.backward()
        #         optimizer.minimize(avg_loss)
        #         model.clear_gradients()

        # # 保存模型
        # fluid.save_dygraph(model.state_dict(), 'mnist')
