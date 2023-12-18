package com.koch.ambeth.xml.util;

/*-
 * #%L
 * jambeth-xml
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

import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.objectcollector.IObjectCollector;
import jakarta.servlet.ServletContext;
import javassist.ClassPool;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ClasspathScannerServletContext implements IClasspathScannerServletContext {
    @Autowired
    protected IObjectCollector objectCollector;

    @Autowired
    protected ServletContext servletContext;

    @Override
    public List<URL> buildUrlsFromClasspath(ClassPool pool) {
        ArrayList<URL> urls = new ArrayList<>();

        Set<String> libJars = servletContext.getResourcePaths("/WEB-INF/lib");
        for (String jar : libJars) {
            try {
                urls.add(servletContext.getResource(jar));
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
        }

        String classes = "/WEB-INF/classes";
        Set<String> classesSet = servletContext.getResourcePaths(classes);
        for (String jar : classesSet) {
            try {
                URL url = servletContext.getResource(jar);
                String urlString = url.toString();
                int index = urlString.lastIndexOf(classes);
                urlString = urlString.substring(0, index + classes.length());
                urls.add(new URL(urlString));
                break;
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
        }
        return urls;
    }

    @Override
    public String lookupExistingPath(String path) throws Throwable {
        String tempPath = path;
        while (true) {
            Matcher matcher = ClasspathScanner.subPathPattern.matcher(tempPath);
            if (!matcher.matches()) {
                throw new IllegalStateException(buildPatternFailMessage(ClasspathScanner.subPathPattern, tempPath));
            }
            tempPath = matcher.group(2);
            try {
                String realPath = servletContext.getRealPath(tempPath);
                // path has been handled correctly. check if it really exists
                File pathFile = new File(realPath);
                if (!pathFile.exists()) {
                    // if (log.isWarnEnabled())
                    // {
                    // log.warn("Path '" + tempPath + "' does not exist!");
                    // }
                    throw new IllegalStateException("Path '" + realPath + "' does not exist!");
                }
                return realPath;
            } catch (Throwable e) {
                if (matcher.group(1) == null || matcher.group(1).length() == 0) {
                    // no prefix path anymore to potentially recover from this failure
                    throw e;
                }
                continue;
            }
        }
    }

    protected String buildPatternFailMessage(Pattern pattern, String value) {
        return "Matcher should have matched: Pattern: '" + pattern.pattern() + "'. Value '" + value + "'";
    }
}
