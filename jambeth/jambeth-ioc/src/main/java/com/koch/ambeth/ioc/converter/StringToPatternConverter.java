package com.koch.ambeth.ioc.converter;

/*-
 * #%L
 * jambeth-ioc
 * %%
 * Copyright (C) 2017 Koch Software Development
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

import com.koch.ambeth.util.IDedicatedConverter;

import java.util.regex.Pattern;

public class StringToPatternConverter implements IDedicatedConverter {
    public static final Pattern splitPattern = Pattern.compile(";");

    @Override
    public Object convertValueToType(Class<?> expectedType, Class<?> sourceType, Object value, Object additionalInformation) {
        if (Pattern.class.equals(expectedType)) {
            return Pattern.compile((String) value);
        } else if (Pattern[].class.equals(expectedType)) {
            var split = splitPattern.split((String) value);
            var patterns = new Pattern[split.length];
            for (int a = split.length; a-- > 0; ) {
                patterns[a] = Pattern.compile(split[a]);
            }
            return patterns;
        } else {
            if (Pattern.class.equals(sourceType)) {
                return ((Pattern) value).pattern();
            } else {
                var sb = new StringBuilder();
                var patterns = (Pattern[]) value;
                for (var pattern : patterns) {
                    if (!sb.isEmpty()) {
                        sb.append(splitPattern.pattern());
                    }
                    sb.append(pattern.pattern());
                }
                return sb.toString();
            }
        }
    }
}
