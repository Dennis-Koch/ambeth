package com.koch.ambeth.util.format;

/*-
 * #%L
 * jambeth-util
 * %%
 * Copyright (C) 2017 Koch Softwaredevelopment
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 * #L%
 */

import java.text.DecimalFormat;
import java.text.FieldPosition;

public class PaddingDecimalFormat extends DecimalFormat {
	private static final long serialVersionUID = -7737645664654567172L;

	private final int leadingWidth;

	private final String decimalSeparator;

	public PaddingDecimalFormat(String pattern) {
		super(pattern);
		int leadingWidth = 0;
		for (int a = 0, size = pattern.length(); a < size; a++) {
			char oneChar = pattern.charAt(a);
			leadingWidth++;
			if (oneChar != '#' && oneChar != '0') {
				break;
			}
		}
		this.leadingWidth = leadingWidth;
		decimalSeparator = "" + getDecimalFormatSymbols().getDecimalSeparator();
	}

	@Override
	public StringBuffer format(double number, StringBuffer result, FieldPosition fieldPosition) {
		StringBuffer sb = super.format(number, result, fieldPosition);
		int indexOf = sb.indexOf(decimalSeparator);
		int leadingWidth = indexOf < this.leadingWidth ? this.leadingWidth : indexOf;
		while (indexOf < leadingWidth) {
			sb.insert(0, ' ');
			indexOf++;
		}
		return sb;
	}

	@Override
	public StringBuffer format(long number, StringBuffer result, FieldPosition fieldPosition) {
		StringBuffer sb = super.format(number, result, fieldPosition);
		while (sb.length() < leadingWidth) {
			sb.insert(0, ' ');
		}
		return sb;
	}
}
