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

import java.text.FieldPosition;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Pattern;

public class ISO8601DateFormat extends SimpleDateFormat {
	private static final long serialVersionUID = -4034139121118690879L;

	private static final Pattern iso8601_preprocessPattern = Pattern.compile(":(?=[0-9]{2}$)");

	public ISO8601DateFormat() {
		super("yyyy-MM-dd'T'HH:mm:ssZ");
	}

	@Override
	public Date parse(String source, ParsePosition pos) {
		source = iso8601_preprocessPattern.matcher(source).replaceFirst("");
		return super.parse(source, pos);
	}

	@Override
	public StringBuffer format(Date date, StringBuffer toAppendTo, FieldPosition pos) {
		StringBuffer sourceSb = super.format(date, toAppendTo, pos);

		sourceSb.insert(sourceSb.length() - 2, ':');
		return sourceSb;
	}
}
