package com.tyb.utils;


import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

public class UtilHar {
	//运动强度等级0,1,2,3,4
//	public static final int[] HAR_LEVEL_NUM = {0,0,1,2,1,2,1,0,0,3,3,3,3,3,3,3,3,4};
	public static final String[] HAR_LEVEL_TTS = {"安静","行走","奔跑","跌倒","报警"};
	public static final String[] HAR_FULL_NAME = {
			"Standing up from sitting",
			"Standing up from laying",
			"Walking",
			"Running",
			"Going upstairs",
			"Jumping",
			"Going downstairs",
			"Lying down from standing",
			"Sitting down",

			"Generic falling forward",
			"Falling rightward",
			"Generic falling backward",
			"Hitting an obstacle in the fall",
			"Falling with protection strategies",
			"Falling backward-sitting-chair",
			"Syncope",
			"Falling leftward",

			"Help!!!"
	};
//    {
//            "StandingUpFS", "StandingUpFL",  "Walking", "Running",
//            "GoingUpS'", "Jumping",  "GoingDownS",  "LyingDownFS",
//            "SittingDown", "FallingForw", "FallingRight", "FallingBack",
//            "HittingObstacle",  "FallingWithPS",  "FallingBackSC","Syncope",
//            "FallingLeft",
//    };



	public String adls = "00000";
	public void update(int value){
		adls = adls.substring(1)+value;
	}
	public boolean isFall(){
		return adls.endsWith("330");
	}
	public boolean isSOS(){
		return adls.endsWith("44");
	}

	//###########################################################
	// 列表排序
	//###########################################################
	class Item {
		private float value;
		private String name;

		public Item(float value, String name) {
			this.value = value;
			this.name = name;
		}

		@Override
		public String toString() {
			return String.format(" %.4f @ %s", value, name);
		}
	}


	public List sortList(float[] values){
		List<Item> list = new LinkedList<>();
		for(int i=0;i<17;++i){
			list.add(new Item(values[i], HAR_FULL_NAME[i]));
		}
		sort(list);
		return list;
	}

	private void sort(List<Item> ItemList) {
		Collections.sort(ItemList, new Comparator<Item>() {
			@Override
			public int compare(Item a, Item b) {
				if(a.value>b.value)
					return -1;
				else
					return 1;
			}
		});
	}

}
