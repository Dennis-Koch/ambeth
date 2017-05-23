package com.koch.ambeth.util.io;

import org.junit.Assert;
import org.junit.Test;

import com.koch.ambeth.util.appendable.AppendableStringBuilder;

public class IntegerUtilTest {

	@Test
	public void correctness() {
		IntegerUtil integerUtil = new IntegerUtil();

		int count = 10000000;

		StringBuilder sb = new StringBuilder(128);
		AppendableStringBuilder asb = new AppendableStringBuilder(sb);

		for (int a = count; a-- > 0;) {
			sb.setLength(0);
			int value = (int) (Math.random() * Integer.MAX_VALUE);
			integerUtil.appendInt(value, asb);
			Assert.assertEquals(Integer.toString(value), sb.toString());
		}
	}

	@Test
	public void fasterThanNaiveApproach() {
		IntegerUtil integerUtil = new IntegerUtil();

		int count = 100000000;

		StringBuilder sb = new StringBuilder(128);
		AppendableStringBuilder asb = new AppendableStringBuilder(sb);

		// give JIT a chance to warm-up
		for (int a = count; a-- > 0;) {
			sb.setLength(0);
			integerUtil.appendInt(a, asb);
		}
		for (int a = count; a-- > 0;) {
			sb.setLength(0);
			sb.append(Integer.toString(a));
		}

		long time1 = System.currentTimeMillis();

		for (int a = count; a-- > 0;) {
			sb.setLength(0);
			integerUtil.appendInt(a, asb);
		}
		long time2 = System.currentTimeMillis();

		for (int a = count; a-- > 0;) {
			sb.setLength(0);
			sb.append(Integer.toString(a));
		}
		long time3 = System.currentTimeMillis();

		long spentIntegerUtil = time2 - time1;

		long spentToString = time3 - time2;

		System.out.println(spentIntegerUtil);
		System.out.println(spentToString);

		Assert.assertTrue(
				"" + spentToString + "ms of Integer.toString() is unexpectedly faster than "
						+ spentIntegerUtil + "ms of " + IntegerUtil.class.getSimpleName(),
				spentIntegerUtil < spentToString);
	}
}
