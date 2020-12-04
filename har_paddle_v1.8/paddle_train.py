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

from paddle_model import *

################################################
import os
from sklearn import metrics
from sklearn.metrics import classification_report



def harset_loader(eval=False, batch=32):
        
    x = np.load(r'G:\Desktop\HarExp\harset\data/db5_acc.npy')
    y = np.load(r'G:\Desktop\HarExp\harset\data/db5_lab.npy')
        
    BOUNDARY = 12000
    x_train = x[:BOUNDARY].reshape(-1, 3, 151)[:,:,:-1]
    x_test = x[BOUNDARY:].reshape(-1, 3, 151)[:,:,:-1]
    y_train = y[:BOUNDARY]
    y_test = y[BOUNDARY:]

    print("Train dataset : ", x_train.shape, y_train.shape)
    print("Test dataset : ", x_test.shape, y_test.shape)
    
    if eval:
        for i in range(0,y_test.shape[0],1):
            yield x_test[i:i+1], y_test[i:i+1]
    else:
        for i in range(0,x_train.shape[0],batch):
            yield x_train[i:i+batch], y_train[i:i+batch]


# for batch_id, data in enumerate(harset_loader()):
#     print(batch_id, data[0].shape, data[1].shape)

#     if batch_id>9:
#         break





# if __name__ == '__main__':

#     # 定义预测过程
#     with fluid.dygraph.guard():
#         model = HarFcn()
        
#     # 加载模型参数
#         # model_dict, _ = fluid.load_dygraph("mnist")
#         # model.load_dict(model_dict)

#     # 灌入数据
#         model.eval()
#         tensor_img = np.random.rand(1,1,3,150).astype(np.float32)
#         result = model(fluid.dygraph.to_variable(tensor_img))
#     #  预测输出取整，即为预测的数字，打印结果
#         print("本次预测是:", result.numpy().shape)
#         print("本次预测是:", result.numpy())


        
if __name__ == '__main__':

    # 通过with语句创建一个dygraph运行的context
    # 动态图下的一些操作需要在guard下进行
    with fluid.dygraph.guard():
        model = HarFcn()
        model.train()

        # fluid.optimizer._LearningRateEpochDecay()
        # optimizer = fluid.optimizer.SGDOptimizer(learning_rate=0.001, parameter_list=model.parameters())
        optimizer = fluid.optimizer.AdamOptimizer(learning_rate=0.001, parameter_list=model.parameters())
        
        EPOCH_NUM = 10
        for epoch_id in range(EPOCH_NUM):
            for batch_id, data in enumerate(harset_loader()):
                #准备数据，格式需要转换成符合框架要求的
                image_data = data[0].reshape(-1,1,3,150).astype('float32')
                label_data = data[1].reshape(-1,1).astype(np.int64)
                # print(image_data.shape, label_data.shape)

                # 将数据转为飞桨动态图格式
                image = fluid.dygraph.to_variable(image_data)
                label = fluid.dygraph.to_variable(label_data)
                
                #前向计算的过程
                predict = model(image)
                
                #计算损失，取一个批次样本损失的平均值
                # loss = fluid.layers.square_error_cost(predict, label)
                loss = fluid.layers.cross_entropy(predict, label)
                avg_loss = fluid.layers.mean(loss)
                
                #每训练了1000批次的数据，打印下当前Loss的情况
                # if batch_id !=0 and batch_id  % 1000 == 0:
                print("epoch: {}, batch: {}, loss is: {}".format(epoch_id, batch_id, avg_loss.numpy()))
                
                #后向传播，更新参数的过程
                avg_loss.backward()
                optimizer.minimize(avg_loss)
                model.clear_gradients()

        # 保存模型
        fluid.save_dygraph(model.state_dict(), 'fallnet')
        # # 获取模型参数和优化器信息
        # model_state, opt_state = fluid.load_dygraph('fallnet')
        # # 加载模型参数
        # mnist.set_dict(model_state)
