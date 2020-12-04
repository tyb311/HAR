# 加载评价库
################################################
from sklearn import metrics
from sklearn.metrics import classification_report
from sklearn.model_selection import train_test_split
from sklearn.preprocessing import OneHotEncoder

import numpy as np
import os
# os.chdir('pdkeras')


def harset_loader(eval=False, batch=32):
    '''
    数据加载生成器
    输入：eval,False即训练模式，True即测试模式
    输入：batch，训练批次大小
    返回：数据生成器，例如(data, label)
    '''
    x = np.load('db5_acc.npy')
    y = np.load('db5_lab.npy')
    # dataset = np.concatenate([x,y], axis=1)
    # print(dataset.shape)
    # np.save('harset', dataset)

    # 取前
    x = x.reshape(-1, 3, 151)#[:32,:,:150]
    # y = y[:32]

    x_train, x_test, y_train, y_test = train_test_split(x, y, stratify=y, test_size=0.3)

    one_hot = OneHotEncoder()
    y_train = one_hot.fit_transform(y_train).A
    # y_test = one_hot.fit_transform(y_test).A

    y_train = y_train.astype(np.int64)
    y_test = y_test.astype(np.int64)
    # print(type(y_train), type(y_test))
    # print("Train dataset : ", x_train.shape, y_train.shape)
    # print("Test dataset : ", x_test.shape, y_test.shape)
    if eval:
        for i in range(0,y_test.shape[0],1):
            yield x_test[i:i+1], y_test[i:i+1]
    else:
        for i in range(0,x_train.shape[0],batch):
            yield x_train[i:i+batch], y_train[i:i+batch]


# 主函数
if __name__ == '__main__':
    train()
    test()
        
    

    # for batch_id, data in enumerate(harset_loader(eval=True)):
    #     print(batch_id, data[0].shape, data[1].shape)

    #     if batch_id>9:
    #         break