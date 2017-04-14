package com.gta.scpoa.util;

import java.util.Comparator;

import com.gta.scpoa.entity.ContactInfo;

/**
 * 拼音比较器
 * @author shengping.pan
 *
 */
public class PinyinComparator implements Comparator<ContactInfo> {

	public int compare(ContactInfo o1, ContactInfo o2) {
		if (o1.getSortLetters().equals("@") || o2.getSortLetters().equals("#")) {
			return -1;
		} else if (o1.getSortLetters().equals("#")
				|| o2.getSortLetters().equals("@")) {
			return 1;
		} else {
			return o1.getSortLetters().compareTo(o2.getSortLetters());
		}
	}

}
