package com.koch.ambeth.ioc.converter;

/*-
 * #%L
 * jambeth-ioc
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

import com.koch.ambeth.util.IDedicatedConverter;

import java.io.File;
import java.nio.file.Path;
import java.util.regex.Pattern;

public class FileToPathConverter implements IDedicatedConverter {
    protected static final Pattern fileDelimiterPattern = Pattern.compile(";");

    @Override
    public Object convertValueToType(Class<?> expectedType, Class<?> sourceType, Object value, Object additionalInformation) {
        if (Path.class.equals(expectedType)) {
            return ((File) value).toPath();
        }
        if (File.class.equals(expectedType)) {
            return ((Path) value).toFile();
        }
        if (File[].class.equals(expectedType)) {
            Path[] paths = (Path[]) value;
            File[] files = new File[paths.length];
            for (int a = paths.length; a-- > 0; ) {
                files[a] = paths[a].toFile();
            }
            return files;
        }
        if (Path[].class.equals(expectedType)) {
            File[] files = (File[]) value;
            Path[] paths = new Path[files.length];
            for (int a = files.length; a-- > 0; ) {
                paths[a] = files[a].toPath();
            }
            return paths;
        }
        return null;
    }
}
