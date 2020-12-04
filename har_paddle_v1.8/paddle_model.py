#加载飞桨和相关类库
import paddle
import paddle.fluid as fluid
from paddle.fluid.dygraph import nn
import paddle.fluid.dygraph as dy
from paddle.fluid import layers

import numpy as np
import os
print(paddle.__version__)


class HarFcn(fluid.dygraph.Layer):
    __name__ = 'harfcn'
    def __init__(self):
        super(HarFcn, self).__init__()

        self.cnn1 = dy.Sequential(
            dy.Conv2D(num_channels=1, num_filters=128, filter_size=3, stride=1, padding=1),
            dy.BatchNorm(num_channels=128),
            dy.Dropout(p=.2),
        )
        self.cnn2 = dy.Sequential(
            dy.Conv2D(num_channels=128, num_filters=128, filter_size=3, stride=1, padding=1),
            dy.BatchNorm(num_channels=128),
            dy.Dropout(p=.2),
        )
        self.cnn3 = dy.Sequential(
            dy.Conv2D(num_channels=128, num_filters=128, filter_size=3, stride=1, padding=1),
            dy.BatchNorm(num_channels=128),
            dy.Dropout(p=.2),
        )

        self.cls = dy.Sequential(
            dy.Linear(input_dim=384, output_dim=128),
            dy.Dropout(p=.2),
            dy.Linear(input_dim=128, output_dim=5),
        )

    # 定义网络结构的前向计算过程
    def forward(self, x):
        x = self.cnn1(x)
        x1 = layers.pool2d(x, pool_size=(3,150), pool_type='avg')
        x = self.cnn2(x)
        x2 = layers.pool2d(x, pool_size=(3,150), pool_type='avg')
        x = self.cnn3(x)
        x3 = layers.pool2d(x, pool_size=(3,150), pool_type='avg')
        # print(x1.shape, x2.shape)

        y = layers.concat([x1,x2,x3], axis=1)
        y = layers.reshape(y, shape=[y.shape[0],-1,])
        # print('y:', y.shape)

        # y = layers.concat([h,y], axis=1)
        # print(x.shape)
        y = self.cls(y)
        y = layers.softmax(y, axis=1)
        return y


if __name__ == '__main__':
    with fluid.dygraph.guard():
        model = HarFcn()
        model.eval()
        x = np.random.rand(3,1,3,150).astype(np.float32)
        x = dy.to_variable(x)
        y = model(x)
        print(y.shape)


from paddle.fluid.dygraph import Linear, to_variable, TracedLayer
# https://www.paddlepaddle.org.cn/documentation/docs/zh/api_cn/dygraph_cn/TracedLayer_cn.html
if __name__ == '__main__':

    exe = fluid.Executor(fluid.CPUPlace())
    # 定义预测过程
    with fluid.dygraph.guard():
        model = HarFcn()
        model.eval()
        # 保存动态图模型     
        # fluid.save_dygraph(model.state_dict(), 'lstmfcn')

        # 保存静态图模型
        image = np.random.rand(1,1,3,150).astype(np.float32)
        image = fluid.dygraph.to_variable(image)

        # class paddle.fluid.dygraph.TracedLayer(program, parameters, feed_names, fetch_names)
        out_dygraph, static_layer = TracedLayer.trace(model, inputs=[image])
        
        # 内部使用Executor运行静态图模型
        out_static_graph = static_layer([image])
        print(out_static_graph[0].shape) # (2, 10)

        # 将静态图模型保存为预测模型
        static_layer.save_inference_model(dirname='lite') 
        print("Saved")

            

# if __name__ == '__main__':

#     exe = fluid.Executor(fluid.CPUPlace())
#     # 定义预测过程
#     with fluid.dygraph.guard():
#         model = HarFcn()
        
#     # 加载模型参数
#         model_dict, _ = fluid.load_dygraph("mnist_demo")
#         model.load_dict(model_dict)

#     # 灌入数据
#         model.eval()
#         tensor_img = np.random.rand(1,1,3,150).astype(np.float32)
#         result = model(fluid.dygraph.to_variable(tensor_img))
#     #  预测输出取整，即为预测的数字，打印结果
#         print("本次预测是:", result.numpy().shape)
#         print("本次预测是:", result.numpy())

#         # print(model.__dict__)
#         # 保存模型
#         fluid.save_dygraph(model.state_dict(), 'mnist_demo')


'''
转换模型
!pip install paddlelite
# https://paddle-lite.readthedocs.io/zh/latest/user_guides/model_optimize_tool.html


!paddle_lite_opt --print_model_ops=true --model_dir=fall --valid_targets=arm



!paddle_lite_opt \
    --model_dir=./fall \
    --optimize_out_type=naive_buffer \
    --optimize_out=lstmfcn \
    --valid_targets=arm \
    --record_tailoring_info =true

WARNING: Logging before InitGoogleLogging() is written to STDERR
I0726 12:23:20.354004   602 cxx_api.cc:251] Load model from file.
I0726 12:23:20.377671   602 optimizer.h:202] == Running pass: lite_quant_dequant_fuse_pass
I0726 12:23:20.378165   602 optimizer.h:219] == Finished running: lite_quant_dequant_fuse_pass
I0726 12:23:20.378181   602 optimizer.h:202] == Running pass: weight_quantization_preprocess_pass
I0726 12:23:20.378237   602 optimizer.h:219] == Finished running: weight_quantization_preprocess_pass
I0726 12:23:20.378247   602 optimizer.h:202] == Running pass: lite_conv_elementwise_fuse_pass
I0726 12:23:20.378563   602 pattern_matcher.cc:108] detected 3 subgraph
I0726 12:23:20.378782   602 optimizer.h:219] == Finished running: lite_conv_elementwise_fuse_pass
I0726 12:23:20.378794   602 optimizer.h:202] == Running pass: lite_conv_bn_fuse_pass
I0726 12:23:20.379238   602 pattern_matcher.cc:108] detected 3 subgraph
I0726 12:23:20.380278   602 optimizer.h:219] == Finished running: lite_conv_bn_fuse_pass
I0726 12:23:20.380304   602 optimizer.h:202] == Running pass: lite_conv_elementwise_fuse_pass
I0726 12:23:20.380762   602 optimizer.h:219] == Finished running: lite_conv_elementwise_fuse_pass
I0726 12:23:20.380786   602 optimizer.h:202] == Running pass: lite_conv_activation_fuse_pass
I0726 12:23:20.381784   602 optimizer.h:219] == Finished running: lite_conv_activation_fuse_pass
I0726 12:23:20.381821   602 optimizer.h:202] == Running pass: lite_var_conv_2d_activation_fuse_pass
I0726 12:23:20.381834   602 optimizer.h:215]    - Skip lite_var_conv_2d_activation_fuse_pass because the target or kernel does not match.
I0726 12:23:20.381844   602 optimizer.h:202] == Running pass: lite_fc_fuse_pass
I0726 12:23:20.382098   602 optimizer.h:219] == Finished running: lite_fc_fuse_pass
I0726 12:23:20.382135   602 optimizer.h:202] == Running pass: lite_shuffle_channel_fuse_pass
I0726 12:23:20.382396   602 optimizer.h:219] == Finished running: lite_shuffle_channel_fuse_pass
I0726 12:23:20.382416   602 optimizer.h:202] == Running pass: lite_transpose_softmax_transpose_fuse_pass
I0726 12:23:20.382529   602 optimizer.h:219] == Finished running: lite_transpose_softmax_transpose_fuse_pass
I0726 12:23:20.382542   602 optimizer.h:202] == Running pass: lite_interpolate_fuse_pass
I0726 12:23:20.382720   602 optimizer.h:219] == Finished running: lite_interpolate_fuse_pass
I0726 12:23:20.382735   602 optimizer.h:202] == Running pass: identity_scale_eliminate_pass
I0726 12:23:20.383025   602 pattern_matcher.cc:108] detected 1 subgraph
I0726 12:23:20.383107   602 optimizer.h:219] == Finished running: identity_scale_eliminate_pass
I0726 12:23:20.383137   602 optimizer.h:202] == Running pass: elementwise_mul_constant_eliminate_pass
I0726 12:23:20.383234   602 optimizer.h:219] == Finished running: elementwise_mul_constant_eliminate_pass
I0726 12:23:20.383249   602 optimizer.h:202] == Running pass: lite_sequence_pool_concat_fuse_pass
I0726 12:23:20.383257   602 optimizer.h:215]    - Skip lite_sequence_pool_concat_fuse_pass because the target or kernel does not match.
I0726 12:23:20.383265   602 optimizer.h:202] == Running pass: __xpu__resnet_fuse_pass
I0726 12:23:20.383275   602 optimizer.h:215]    - Skip __xpu__resnet_fuse_pass because the target or kernel does not match.
I0726 12:23:20.383281   602 optimizer.h:202] == Running pass: __xpu__multi_encoder_fuse_pass
I0726 12:23:20.383289   602 optimizer.h:215]    - Skip __xpu__multi_encoder_fuse_pass because the target or kernel does not match.
I0726 12:23:20.383298   602 optimizer.h:202] == Running pass: __xpu__embedding_with_eltwise_add_fuse_pass
I0726 12:23:20.383306   602 optimizer.h:215]    - Skip __xpu__embedding_with_eltwise_add_fuse_pass because the target or kernel does not match.
I0726 12:23:20.383314   602 optimizer.h:202] == Running pass: __xpu__fc_fuse_pass
I0726 12:23:20.383322   602 optimizer.h:215]    - Skip __xpu__fc_fuse_pass because the target or kernel does not match.
I0726 12:23:20.383330   602 optimizer.h:202] == Running pass: identity_dropout_eliminate_pass
I0726 12:23:20.383339   602 optimizer.h:215]    - Skip identity_dropout_eliminate_pass because the target or kernel does not match.
I0726 12:23:20.383347   602 optimizer.h:202] == Running pass: quantized_op_attributes_inference_pass
I0726 12:23:20.383355   602 optimizer.h:215]    - Skip quantized_op_attributes_inference_pass because the target or kernel does not match.
I0726 12:23:20.383364   602 optimizer.h:202] == Running pass: npu_subgraph_pass
I0726 12:23:20.383373   602 optimizer.h:215]    - Skip npu_subgraph_pass because the target or kernel does not match.
I0726 12:23:20.383379   602 optimizer.h:202] == Running pass: xpu_subgraph_pass
I0726 12:23:20.383388   602 optimizer.h:215]    - Skip xpu_subgraph_pass because the target or kernel does not match.
I0726 12:23:20.383395   602 optimizer.h:202] == Running pass: bm_subgraph_pass
I0726 12:23:20.383404   602 optimizer.h:215]    - Skip bm_subgraph_pass because the target or kernel does not match.
I0726 12:23:20.383420   602 optimizer.h:202] == Running pass: apu_subgraph_pass
I0726 12:23:20.383430   602 optimizer.h:215]    - Skip apu_subgraph_pass because the target or kernel does not match.
I0726 12:23:20.383437   602 optimizer.h:202] == Running pass: rknpu_subgraph_pass
I0726 12:23:20.383445   602 optimizer.h:215]    - Skip rknpu_subgraph_pass because the target or kernel does not match.
I0726 12:23:20.383453   602 optimizer.h:202] == Running pass: static_kernel_pick_pass
I0726 12:23:20.385087   602 optimizer.h:219] == Finished running: static_kernel_pick_pass
I0726 12:23:20.385113   602 optimizer.h:202] == Running pass: variable_place_inference_pass
I0726 12:23:20.385563   602 optimizer.h:219] == Finished running: variable_place_inference_pass
I0726 12:23:20.385584   602 optimizer.h:202] == Running pass: argument_type_display_pass
I0726 12:23:20.385596   602 optimizer.h:219] == Finished running: argument_type_display_pass
I0726 12:23:20.385601   602 optimizer.h:202] == Running pass: type_target_cast_pass
I0726 12:23:20.385733   602 optimizer.h:219] == Finished running: type_target_cast_pass
I0726 12:23:20.385746   602 optimizer.h:202] == Running pass: variable_place_inference_pass
I0726 12:23:20.386023   602 optimizer.h:219] == Finished running: variable_place_inference_pass
I0726 12:23:20.386041   602 optimizer.h:202] == Running pass: argument_type_display_pass
I0726 12:23:20.386054   602 optimizer.h:219] == Finished running: argument_type_display_pass
I0726 12:23:20.386063   602 optimizer.h:202] == Running pass: io_copy_kernel_pick_pass
I0726 12:23:20.386191   602 optimizer.h:219] == Finished running: io_copy_kernel_pick_pass
I0726 12:23:20.386207   602 optimizer.h:202] == Running pass: argument_type_display_pass
I0726 12:23:20.386219   602 optimizer.h:219] == Finished running: argument_type_display_pass
I0726 12:23:20.386229   602 optimizer.h:202] == Running pass: variable_place_inference_pass
I0726 12:23:20.386510   602 optimizer.h:219] == Finished running: variable_place_inference_pass
I0726 12:23:20.386528   602 optimizer.h:202] == Running pass: argument_type_display_pass
I0726 12:23:20.386536   602 optimizer.h:219] == Finished running: argument_type_display_pass
I0726 12:23:20.386543   602 optimizer.h:202] == Running pass: type_precision_cast_pass
I0726 12:23:20.386776   602 optimizer.h:219] == Finished running: type_precision_cast_pass
I0726 12:23:20.386788   602 optimizer.h:202] == Running pass: variable_place_inference_pass
I0726 12:23:20.386973   602 optimizer.h:219] == Finished running: variable_place_inference_pass
I0726 12:23:20.386983   602 optimizer.h:202] == Running pass: argument_type_display_pass
I0726 12:23:20.386991   602 optimizer.h:219] == Finished running: argument_type_display_pass
I0726 12:23:20.386996   602 optimizer.h:202] == Running pass: type_layout_cast_pass
I0726 12:23:20.387240   602 optimizer.h:219] == Finished running: type_layout_cast_pass
I0726 12:23:20.387253   602 optimizer.h:202] == Running pass: argument_type_display_pass
I0726 12:23:20.387260   602 optimizer.h:219] == Finished running: argument_type_display_pass
I0726 12:23:20.387267   602 optimizer.h:202] == Running pass: variable_place_inference_pass
I0726 12:23:20.387459   602 optimizer.h:219] == Finished running: variable_place_inference_pass
I0726 12:23:20.387470   602 optimizer.h:202] == Running pass: argument_type_display_pass
I0726 12:23:20.387477   602 optimizer.h:219] == Finished running: argument_type_display_pass
I0726 12:23:20.387482   602 optimizer.h:202] == Running pass: mlu_subgraph_pass
I0726 12:23:20.387492   602 optimizer.h:215]    - Skip mlu_subgraph_pass because the target or kernel does not match.
I0726 12:23:20.387501   602 optimizer.h:202] == Running pass: runtime_context_assign_pass
I0726 12:23:20.387516   602 optimizer.h:219] == Finished running: runtime_context_assign_pass
I0726 12:23:20.387523   602 optimizer.h:202] == Running pass: argument_type_display_pass
I0726 12:23:20.387531   602 optimizer.h:219] == Finished running: argument_type_display_pass
I0726 12:23:20.387539   602 optimizer.h:202] == Running pass: mlu_postprocess_pass
I0726 12:23:20.387547   602 optimizer.h:215]    - Skip mlu_postprocess_pass because the target or kernel does not match.
I0726 12:23:20.387553   602 optimizer.h:202] == Running pass: memory_optimize_pass
I0726 12:23:20.387697   602 memory_optimize_pass.cc:160] There are 1 types device var.
I0726 12:23:20.387774   602 memory_optimize_pass.cc:209] cluster: t_51
I0726 12:23:20.387785   602 memory_optimize_pass.cc:209] cluster: t_50
I0726 12:23:20.387791   602 memory_optimize_pass.cc:209] cluster: t_49
I0726 12:23:20.387796   602 memory_optimize_pass.cc:209] cluster: t_0
I0726 12:23:20.387802   602 memory_optimize_pass.cc:209] cluster: t_26
I0726 12:23:20.388633   602 optimizer.h:219] == Finished running: memory_optimize_pass
I0726 12:23:20.388697   602 generate_program_pass.h:37] insts.size 31
I0726 12:23:20.455926   602 model_parser.cc:588] Save naive buffer model in 'lstmfcn.nb' successfully
Save the optimized model into :lstmfcnsuccessfully
'''