package com.koch.ambeth.core.plugin;

/*-
 * #%L
 * jambeth-core
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

import com.koch.ambeth.core.config.CoreConfigurationConstants;
import com.koch.ambeth.ioc.IInitializingBean;
import com.koch.ambeth.ioc.config.Property;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;

public class JarURLProvider implements IJarURLProvider, IInitializingBean {
    @Property(name = CoreConfigurationConstants.PluginPaths)
    protected String[] jarPaths;

    @Property(name = CoreConfigurationConstants.PluginPathsRecursiveFlag, defaultValue = "true")
    protected boolean isRecursive;

    protected List<URL> jarURLs;

    @Override
    public void afterPropertiesSet() throws Throwable {
        jarURLs = extractJarURL(jarPaths);
    }

    @Override
    public List<URL> getJarURLs() {
        return jarURLs;
    }

    private ArrayList<URL> extractJarURL(String... pathArray) {
        ArrayList<URL> urls = new ArrayList<>();
        for (String path : pathArray) {
            urls.addAll(buildUrl(path));
        }
        return urls;
    }

    private List<URL> buildUrl(String path) {
        List<URL> urls = new ArrayList<>();
        File dir = new File(path);
        try {
            if (dir.isFile() && path.toLowerCase().endsWith(".jar")) {
                urls.add(dir.toURI().toURL());
            } else if (dir.isDirectory() && !isRecursive) {
                urls.addAll(listJars(dir));
            } else if (dir.isDirectory() && isRecursive) {
                urls.addAll(listAllJars(dir));
            } else {
                throw new IllegalArgumentException("path for scan plugin is not jar file or not exists, path:" + path);
            }
            return urls;
        } catch (MalformedURLException e) {
            throw RuntimeExceptionUtil.mask(e);
        }
    }

    /**
     * find all the jar files in a folder
     *
     * @param dir to find jars folder
     * @return all the jars
     * @throws MalformedURLException
     */
    private List<URL> listAllJars(File dir) throws MalformedURLException {
        final List<URL> files = new ArrayList<>();
        try {
            Files.walkFileTree(dir.toPath(), new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    if (checkJar(file.toFile())) {
                        files.add(file.toUri().toURL());
                    }
                    return super.visitFile(file, attrs);
                }
            });
        } catch (IOException e) {
            throw RuntimeExceptionUtil.mask(e);
        }
        return files;
    }

    private List<URL> listJars(File dir) throws MalformedURLException {
        File[] listFiles = dir.listFiles();
        List<URL> result = new ArrayList<>();
        for (File file : listFiles) {
            if (checkJar(file)) {
                result.add(file.toURI().toURL());
            }
        }
        return result;
    }

    private boolean checkJar(File file) {
        return file.isFile() && file.getName().toLowerCase().endsWith(".jar");
    }
}
