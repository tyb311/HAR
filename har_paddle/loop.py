# 加载数据和模型
from dataset import *
from models import *


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

    # 训练模型
    model.fit(harset_loader(eval=False),
            epochs=3,
            batch_size=32,
            verbose=1,
            )

        
    from paddle.static import InputSpec
    paddle.jit.save(
        layer=model.network,
        path='fallnet',
        input_spec=[InputSpec(shape=[None,3,151],dtype='float32')]
        )

    # 测试模型分数
    # model.load('fallnet')
    model.evaluate(harset_loader(eval=True), verbose=1)

    # 加载模型参数测试
    # # create network
    # layer = FallNet()

    # # load
    # state_dict = paddle.load('fallnet')

    # # inference
    # layer.set_state_dict(state_dict, use_structured_name=False)
    # layer.eval()
    # x = paddle.randn([1, 3,151], 'float32')
    # pred = layer(x)
    # model = paddle.Model(layer)
    # model.summary((1,3,151))
