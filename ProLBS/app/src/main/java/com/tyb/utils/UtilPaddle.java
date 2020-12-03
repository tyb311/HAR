package com.tyb.utils;

import android.content.Context;
import android.util.Log;

import com.baidu.paddle.lite.MobileConfig;
import com.baidu.paddle.lite.PaddlePredictor;
import com.baidu.paddle.lite.PowerMode;
import com.baidu.paddle.lite.Tensor;

import java.io.InputStream;
import java.util.Date;
import java.util.Vector;

/**
 * 参考我的知乎教程：
 * 百度Paddle-Lite教程之模型转换篇:https://zhuanlan.zhihu.com/p/81194781
 * 百度Paddle-Lite教程之图像分类App开发实战：https://zhuanlan.zhihu.com/p/81887390
 */
public class UtilPaddle {

	private static final String TAG = "Paddle";
	private boolean isLoaded = false;
	private int cpuThreadNum = 1;
	private String cpuPowerMode = "LITE_POWER_LOW";

	private String modelPath = "/models/paddle/model.nb";
	private String labelPath = "labels/fall.txt";

	protected PaddlePredictor paddlePredictor = null;
	protected float inferenceTime = 0;
	// Only for image classification
	protected Vector<String> wordLabels = new Vector<String>();

	protected String top1Result = "";
	protected String top2Result = "";
	protected String top3Result = "";

	public UtilPaddle(Context ctx){
		Log.i("路径：", ctx.getCacheDir()+modelPath);
		isLoaded = loadModel(ctx, modelPath);
		if (!isLoaded) {
			Log.i(TAG, "加载模型失败");
		}else
			Log.i(TAG, "加载模型成功");

		isLoaded = loadLabel(ctx, labelPath);
		if (!isLoaded) {
			Log.i(TAG, "加载标签失败");
		}else
			Log.i(TAG, "加载标签成功");
	}

	protected boolean loadModel(Context ctx, String modelPath) {
		// Load model
		if (modelPath.isEmpty()) {
			return false;
		}
//		// Release model if exists
//		paddlePredictor = null;
//		isLoaded = false;
//		cpuThreadNum = 1;
//		cpuPowerMode = "LITE_POWER_LOW";

		MobileConfig config = new MobileConfig();
//		ctx.getCacheDir()==/data/user/0/com.tyb.prolite/cache==assets
		Log.i("路径：", ctx.getCacheDir()+modelPath);
		config.setModelFromFile(ctx.getCacheDir()+modelPath);
		config.setThreads(cpuThreadNum);
		if (cpuPowerMode.equalsIgnoreCase("LITE_POWER_HIGH")) {
			config.setPowerMode(PowerMode.LITE_POWER_HIGH);
		} else if (cpuPowerMode.equalsIgnoreCase("LITE_POWER_LOW")) {
			config.setPowerMode(PowerMode.LITE_POWER_LOW);
		} else if (cpuPowerMode.equalsIgnoreCase("LITE_POWER_FULL")) {
			config.setPowerMode(PowerMode.LITE_POWER_FULL);
		} else if (cpuPowerMode.equalsIgnoreCase("LITE_POWER_NO_BIND")) {
			config.setPowerMode(PowerMode.LITE_POWER_NO_BIND);
		} else if (cpuPowerMode.equalsIgnoreCase("LITE_POWER_RAND_HIGH")) {
			config.setPowerMode(PowerMode.LITE_POWER_RAND_HIGH);
		} else if (cpuPowerMode.equalsIgnoreCase("LITE_POWER_RAND_LOW")) {
			config.setPowerMode(PowerMode.LITE_POWER_RAND_LOW);
		} else {
			Log.e(TAG, "Unknown cpu power mode!");
			return false;
		}

		paddlePredictor = PaddlePredictor.createPaddlePredictor(config);
		return true;
	}

	protected boolean loadLabel(Context appCtx, String labelPath) {
		wordLabels.clear();
		// Load word labels from file
		try {
			InputStream assetsInputStream = appCtx.getAssets().open(labelPath);
			int available = assetsInputStream.available();
			byte[] lines = new byte[available];
			assetsInputStream.read(lines);
			assetsInputStream.close();
			String words = new String(lines);
			String[] contents = words.split("\n");
			for (String content : contents) {
				int first_space_pos = content.indexOf(" ");
				if (first_space_pos >= 0 && first_space_pos < content.length()) {
					wordLabels.add(content.substring(first_space_pos));
				}
			}
			Log.i(TAG, "Word label size: " + wordLabels.size());
		} catch (Exception e) {
			Log.e(TAG, e.getMessage());
			return false;
		}
		return true;
	}

	private float[] getRandData(long[] shape){
		int channels = (int) shape[1];
		int width = (int) shape[3];
		int height = (int) shape[2];
		float[] inputData = new float[channels * width * height];
		if (channels == 3) {
			int[] channelStride = new int[]{width * height, width * height * 2};
			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++) {
					inputData[y * width + x] = (float) Math.random();
					inputData[y * width + x + channelStride[0]] = (float) Math.random();
					inputData[y * width + x + channelStride[1]] = (float) Math.random();
				}
			}
		} else if (channels == 1) {
			for (int y = 0; y < height; y++) {
				for (int x = 0; x < width; x++) {
					inputData[y * width + x] = (float) Math.random();
				}
			}
		} else {
			Log.i(TAG, "Unsupported channel size " + Integer.toString(channels) + ",  only channel 1 and 3 is " +
					"supported!");
		}
		return inputData;
	}

	public float[] infer(float[] data, long[] shape) {
		// Set input shape, and feed input tensor
		Tensor inputTensor = paddlePredictor.getInput(0);
		inputTensor.resize(shape);
		inputTensor.setData(data);

		// Run inference
		Date start = new Date();
		paddlePredictor.run();
		inferenceTime = (new Date().getTime() - start.getTime());

		// Fetch output tensor
		Tensor outputTensor = paddlePredictor.getOutput(0);
		return outputTensor.getFloatData();
	}

	public int argmax(float[] data){
		int maxId=0;
		float maxValue=data[0];
		for(int i=0;i<data.length;++i){
			if(maxValue<data[i]){
				maxId=i;
				maxValue=data[i];
			}
//            Log.i(TAG, "Value:"+data[i]);
		}
		Log.i(TAG, "最大值:"+maxId);
		top1Result = "Top1: " + wordLabels.get(maxId) + " - " + String.format("%.4f", data[maxId]);
		return maxId;
	}

	public float[] test(){
		long[] shape = new long[]{1,1,3,150};
		float[] data = getRandData(shape);
		return infer(data, shape);
	}

	public void top3(float[] outputTensor, long[] outputShape){
		// Post-process
//		long outputShape[] = outputTensor.shape();
		long outputSize = 1;
		for (long s : outputShape) {
			outputSize *= s;
		}
		int[] max_index = new int[3]; // Top3 indices
		double[] max_num = new double[3]; // Top3 scores
		for (int i = 0; i < outputSize; i++) {
			float tmp = outputTensor[i];
			int tmp_index = i;
			for (int j = 0; j < 3; j++) {
				if (tmp > max_num[j]) {
					tmp_index += max_index[j];
					max_index[j] = tmp_index - max_index[j];
					tmp_index -= max_index[j];
					tmp += max_num[j];
					max_num[j] = tmp - max_num[j];
					tmp -= max_num[j];
				}
			}
		}
		if (wordLabels.size() > 0) {
			top1Result = "Top1: " + wordLabels.get(max_index[0]) + " - " + String.format("%.3f", max_num[0]);
			top2Result = "Top2: " + wordLabels.get(max_index[1]) + " - " + String.format("%.3f", max_num[1]);
			top3Result = "Top3: " + wordLabels.get(max_index[2]) + " - " + String.format("%.3f", max_num[2]);
		}
	}

	public boolean isLoaded() {
		return paddlePredictor != null && isLoaded;
	}

	public float inferenceTime() {
		return inferenceTime;
	}

	public String top1Result() {
		return top1Result;
	}

	public String top2Result() {
		return top2Result;
	}

	public String top3Result() {
		return top3Result;
	}

}
