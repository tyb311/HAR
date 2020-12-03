import warnings
warnings.filterwarnings('ignore')

import numpy as np
import os

#加载飞桨和相关类库V2.0rc
import paddle
from paddle import nn
from paddle.nn import Conv1D, BatchNorm1D, Dropout, Flatten
from paddle.nn import PReLU, Linear, LSTM, GRU
print(paddle.__version__)


class Block(nn.Layer):
    def __init__(self, in_channels=3, out_channels=3, kernel_size=3, *args):
        super(Block, self).__init__()
        self.nn = nn.Sequential(
            nn.Conv1D(in_channels=in_channels, out_channels=out_channels, 
                    kernel_size=kernel_size, padding=0, bias_attr=False),
            nn.BatchNorm1D(num_features=out_channels),
            nn.LeakyReLU(.2),
            nn.Dropout(p=.2)
        )
    def forward(self, x):
        return self.nn(x)


class FallNet(nn.Layer):
    def __init__(self, in_channels=3, out_classes=5, hid=64, num=64):
        super(FallNet, self).__init__()
        self.cnn0 = Block(in_channels,hid,7,0)

        self.cnn1 = Block(hid,hid,5,0)
        self.cnn2 = Block(hid,hid,3,0)
        self.cnn3 = Block(hid,hid,1,0)
        self.avg = nn.AdaptiveAvgPool1D(output_size=num)


        # self.rnn0 = nn.LSTM(input_size=145, hidden_size=num, dropout=.2, num_layers=3)
        self.rnn0 = nn.GRU(input_size=145, hidden_size=num, num_layers=1, dropout=0.2)
        self.rnn1 = Block(hid,hid,1,0)
        self.rnn2 = Block(hid,4,3,0)

        self.cls = nn.Sequential(
            nn.Linear(in_features=1016, out_features=128),
            nn.Dropout(p=.2),
            nn.Linear(in_features=128, out_features=out_classes),
            nn.Softmax(axis=1)
        )

    def forward(self, x):#
        batch = x.shape[0]
        x = self.cnn0(x)

        y = self.cnn1(x)
        y1 = self.avg(y)

        y = self.cnn2(y)
        y2 = self.avg(y)

        y = self.cnn3(y)
        y3 = self.avg(y)

        # print('CNN:', y1.shape, y2.shape, y3.shape)

        r,t = self.rnn0(x)

        x = paddle.concat([y1,y2,y3,r], axis=-1)

        x = self.rnn1(x)
        x = self.rnn2(x)
        x = paddle.flatten(x, start_axis=1)

        x = self.cls(x)
        return x


if __name__ == '__main__':
    model = FallNet()
    model = paddle.Model(model)
    # model.summary((1,3,151))

    # x = np.random.rand(1,3,151)
    # y = model.predict(x)
    # print(y.shape)

    from paddle.metric import Accuracy

    model.prepare(
        paddle.optimizer.Adam(0.0001, parameters=model.parameters()),
        paddle.nn.CrossEntropyLoss(),
        Accuracy()
        )


    # model.fit(train_dataset,
    #         epochs=20,
    #         batch_size=32,
    #         verbose=1,
    #         )