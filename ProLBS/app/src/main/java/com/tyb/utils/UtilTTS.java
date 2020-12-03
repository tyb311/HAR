package com.tyb.utils;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.util.Log;
import android.widget.Toast;

import java.util.Locale;

/*
Android从1.6起就提供了speech包，但是仅仅提供tts接口，而实现tts的能力，则需要安装第三方tts引擎。
Android原生系统直接集成了pico引擎（仅支持英文合成），国内厂商也会集成一些tts引擎，
比如我的小米手机集成了“小爱语音引擎”。
具体可以到手机的 设置—语言和输入法—文字转语言（TTS）输出 下看一下支持哪些引擎。
 */


/**
 * 用来初始化TextToSpeech引擎:int result = tts.setLanguage(Locale.CHINESE);
 * status:SUCCESS或ERROR这2个值
 * setLanguage设置语言，帮助文档里面写了有22种
 * TextToSpeech.LANG_MISSING_DATA：表示语言的数据丢失。
 * TextToSpeech.LANG_NOT_SUPPORTED:不支持
 */

public class UtilTTS{
	private TextToSpeech tts;

	public UtilTTS(final Context context){
		tts = new TextToSpeech(context, new TextToSpeech.OnInitListener() {
			@Override
			public void onInit(int status) {
				// status : TextToSpeech.SUCCESS=0 , TextToSpeech.ERROR=-1
				if (status != TextToSpeech.SUCCESS){
					Log.i("TextToSpeech", "TextToSpeech onInit status = " + status);
					Toast.makeText(context, "TTS初始化失败！", Toast.LENGTH_SHORT).show();
				}

				int result = tts.setLanguage(Locale.CHINA);
				if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
					Toast.makeText(context, "TTS不支持中文", Toast.LENGTH_SHORT).show();
					result = tts.setLanguage(Locale.ENGLISH);
					if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
						Toast.makeText(context, "TTS不支持英文", Toast.LENGTH_SHORT).show();
					}else
						Toast.makeText(context, "TTS已设为英文", Toast.LENGTH_SHORT).show();
				}else
					Toast.makeText(context, "TTS已设为中文", Toast.LENGTH_SHORT).show();
			}
		});
	}

	public boolean isEnable =false, isSpeaking=false;

	public void speakForcely(String words){
		if(isSpeaking)return;
		tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
			@Override
			public void onStart(String utteranceId) {
				isSpeaking=true;
			}
			@Override
			public void onDone(String utteranceId) {
				isSpeaking=false;
			}
			@Override
			public void onError(String utteranceId) {
				isSpeaking=false;
			}
		});
		tts.speak(words, TextToSpeech.QUEUE_FLUSH, null,"");
	}

	public void speak(String words){
		if(isEnable ==false || isSpeaking)return;

//        其结果会走setOnUtteranceProgressListener回调。
		tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
			@Override
			public void onStart(String utteranceId) {
				isSpeaking=true;
			}
			@Override
			public void onDone(String utteranceId) {
				isSpeaking=false;
			}
			@Override
			public void onError(String utteranceId) {
				isSpeaking=false;
			}
		});

//        调用speak或synthesizeToFile方法，前者是语音播报，后者是仅语音合成。
		tts.speak(words, TextToSpeech.QUEUE_FLUSH, null,"");
//        tts.synthesizeToFile("青青子衿，悠悠我心", null, file, id);
	}

	// 设置音调，值越大声音越尖（女生），值越小则变成男声,1.0是常规
	public void setPitch(float i){
		tts.setPitch(i);
	}

	public void release(){
		// 4.关闭TTS，回收资源
		if (tts != null) {
			tts.stop();
			tts.shutdown();
		}
	}

}
