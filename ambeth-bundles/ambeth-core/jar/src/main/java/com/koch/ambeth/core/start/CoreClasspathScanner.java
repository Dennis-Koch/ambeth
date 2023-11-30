package com.koch.ambeth.core.start;

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
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.config.Property;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.util.IClassLoaderProvider;
import com.koch.ambeth.util.IClasspathScanner;
import com.koch.ambeth.util.ParamChecker;
import com.koch.ambeth.util.StringBuilderUtil;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.collections.HashSet;
import com.koch.ambeth.util.collections.IList;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;
import com.koch.ambeth.util.objectcollector.IObjectCollector;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;
import lombok.SneakyThrows;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CoreClasspathScanner implements IClasspathScanner, IInitializingBean {
    public static final Pattern jarPathPrefixPattern = Pattern.compile("(.+)/([^/]+)");
    public static final Pattern cutDollarPattern = Pattern.compile("([^\\$\\.]+)(?:\\$[\\.]+)?\\.(?:java|class)");
    @Autowired
    protected IClasspathInfo classpathInfo;
    @Autowired
    protected IClassLoaderProvider classLoaderProvider;
    @Autowired
    protected IObjectCollector objectCollector;
    @Property(name = CoreConfigurationConstants.PackageScanPatterns, defaultValue = "com/koch/.*")
    protected String packageFilterPatterns;
    protected Pattern[] packageScanPatterns;
    protected Pattern[] preceedingPackageScanPatterns;
    protected ClassPool classPool;
    @LogInstance
    private ILogger log;

    @Override
    public void afterPropertiesSet() throws Throwable {
        // intended blank
    }

    protected void initializeClassPool(ClassPool classPool) {
        if (log.isDebugEnabled()) {
            log.debug("Initializing ClassPool:");
        }

        for (URL url : getJarURLs()) {
            try {
                String pathList = convertURLToFile(url).toString();
                if (log.isDebugEnabled()) {
                    log.debug("Append URL to path: " + url + " (converted file path: " + pathList + ")");
                }
                classPool.appendPathList(pathList);
            } catch (Exception e) {
                throw RuntimeExceptionUtil.mask(e);
            }
        }
    }

    // Has to be used by getter since it might be needed before afterPropertiesSet() was called.
    protected Pattern[] getPackageScanPatterns() {
        if (packageScanPatterns == null) {
            ParamChecker.assertNotNull(packageFilterPatterns, "packageFilterPatterns");

            String[] split = packageFilterPatterns.split(";");
            ArrayList<Pattern> patterns = new ArrayList<>();
            for (int a = split.length; a-- > 0; ) {
                String packagePattern = split[a];
                String packagePattern1 = packagePattern.replaceAll(Pattern.quote("\\."), Matcher.quoteReplacement("/"));
                patterns.add(Pattern.compile(packagePattern));
                if (!packagePattern1.equals(packagePattern)) {
                    patterns.add(Pattern.compile(packagePattern1));
                }
            }
            packageScanPatterns = patterns.toArray(Pattern.class);
        }
        return packageScanPatterns;
    }

    @Override
    public List<Class<?>> scanClassesAnnotatedWith(Class<?>... annotationTypes) {
        ClassPool pool = getClassPool();
        IList<String> targetClassNames = scanForClasses(pool);
        try {
            List<CtClass> classNamesFound = new ArrayList<>();
            for (int a = 0, size = targetClassNames.size(); a < size; a++) {
                String className = targetClassNames.get(a);
                CtClass cc;
                try {
                    cc = pool.get(className);
                } catch (NotFoundException e) {
                    if (log.isErrorEnabled()) {
                        log.error("Javassist could not load class (but found it in classpath): " + className);
                    }
                    continue;
                }
                for (int b = annotationTypes.length; b-- > 0; ) {
                    Object annotation = cc.getAnnotation(annotationTypes[b]);

                    if (annotation == null) {
                        continue;
                    }
                    classNamesFound.add(cc);
                    break;
                }
            }
            return convertToClasses(classNamesFound);
        } catch (Exception e) {
            throw RuntimeExceptionUtil.mask(e);
        }
    }

    @SneakyThrows
    @Override
    public List<Class<?>> scanClassesImplementing(Class<?>... superTypes) {
        var pool = getClassPool();
        var targetClassNames = scanForClasses(pool);
        var ctSuperTypes = new CtClass[superTypes.length];
        for (int a = superTypes.length; a-- > 0; ) {
            ctSuperTypes[a] = pool.getOrNull(superTypes[a].getName());
            if (ctSuperTypes[a] == null) {
                ctSuperTypes[a] = ClassPool.getDefault().get(superTypes[a].getName());
            }
        }
        var classNamesFound = new ArrayList<CtClass>();
        for (int a = 0, size = targetClassNames.size(); a < size; a++) {
            var className = targetClassNames.get(a);
            var cc = pool.get(className);
            for (int b = ctSuperTypes.length; b-- > 0; ) {
                if (!cc.subtypeOf(ctSuperTypes[b])) {
                    continue;
                }
                classNamesFound.add(cc);
            }
        }
        return convertToClasses(classNamesFound);
    }

    protected ClassPool getClassPool() {
        if (classPool == null) {
            classPool = ClassPool.getDefault();
            initializeClassPool(classPool);
        }
        return classPool;
    }

    @SneakyThrows
    protected List<Class<?>> convertToClasses(List<CtClass> ctClasses) {
        var set = new HashSet<Class<?>>();
        for (int a = 0, size = ctClasses.size(); a < size; a++) {
            var ctClass = ctClasses.get(a);
            set.add(getClassLoader().loadClass(ctClass.getName()));
        }
        var list = new ArrayList<Class<?>>(set.size());
        set.toList(list);
        return list;
    }

    protected ClassLoader getClassLoader() {
        return classLoaderProvider.getClassLoader();
    }

    protected IList<String> scanForClasses(ClassPool pool) {
        var urls = getJarURLs();

        var targetClassNames = new ArrayList<String>();

        for (int a = 0, size = urls.size(); a < size; a++) {
            var url = urls.get(a);
            try {
                var realPathFile = convertURLToFile(url);
                if (realPathFile.toFile().exists()) {
                    if (Files.isDirectory(realPathFile)) {
                        scanDirectory(realPathFile, "", targetClassNames, false);
                    } else {
                        scanFile(realPathFile, targetClassNames);
                    }
                }
            } catch (Throwable e) {
                throw RuntimeExceptionUtil.mask(e, "Error occured while handling URL '" + url.toString() + "'");
            }
        }
        return targetClassNames;
    }

    protected IList<URL> getJarURLs() {
        return classpathInfo.getJarURLs();
    }

    protected Path convertURLToFile(URL url) throws Exception {
        return classpathInfo.openAsFile(url);
    }

    @SneakyThrows
    protected void scanFile(Path file, List<String> targetClassNames) {
        var tlObjectCollector = objectCollector.getCurrent();

        var sb = tlObjectCollector.create(StringBuilder.class);
        try {
            var jarFile = new JarFile(file.toFile());
            try {
                var entries = jarFile.entries();
                while (entries.hasMoreElements()) {
                    var entry = entries.nextElement();
                    if (entry.isDirectory()) {
                        continue;
                    }
                    var entryName = entry.getName();

                    var packageScanPatterns = getPackageScanPatterns();
                    for (int a = packageScanPatterns.length; a-- > 0; ) {
                        var pathMatcher = packageScanPatterns[a].matcher(entryName);
                        if (!pathMatcher.matches()) {
                            continue;
                        }
                        var matcher = jarPathPrefixPattern.matcher(entryName);
                        String path, name;
                        if (!matcher.matches()) {
                            path = "";
                            name = entryName;
                        } else {
                            path = matcher.group(1);
                            name = matcher.group(2);
                        }
                        var cutDollarMatcher = cutDollarPattern.matcher(name);
                        if (!cutDollarMatcher.matches()) {
                            continue;
                        }
                        sb.setLength(0);
                        sb.append(path);
                        if (path.length() > 0) {
                            sb.append('/');
                        }
                        sb.append(cutDollarMatcher.group(1));
                        var className = StringBuilderUtil.replace(sb, '/', '.').toString();
                        targetClassNames.add(className);
                    }
                }
            } finally {
                jarFile.close();
            }
        } finally {
            tlObjectCollector.dispose(sb);
        }
    }

    protected void scanDirectory(Path dir, String relativePath, List<String> targetClassNames, boolean addOnly) {
        var tlObjectCollector = objectCollector.getCurrent();
        var sb = tlObjectCollector.create(StringBuilder.class);
        try {
            var files = dir.toFile().listFiles();
            if (files == null) {
                throw new IllegalStateException("Directory '" + dir.toFile().getAbsolutePath() + "' not accessible");
            }
            sb.append(relativePath);
            if (relativePath.length() > 0) {
                sb.append('/');
            }
            int sbStartLength = sb.length();

            for (int a = 0, size = files.length; a < size; a++) {
                var file = files[a];

                sb.setLength(sbStartLength);
                if (file.isDirectory()) {
                    sb.append(file.getName());
                    var classNamePart = sb.toString();
                    scanDirectory(file.toPath(), classNamePart, targetClassNames, addOnly);
                    continue;
                }
                var cutDollarMatcher = cutDollarPattern.matcher(file.getName());
                if (!cutDollarMatcher.matches()) {
                    continue;
                }
                sb.append(cutDollarMatcher.group(1));
                var className = sb.toString();
                if (addOnly) {
                    targetClassNames.add(className.replace('/', '.'));
                    continue;
                }
                // Matcher matcher = jarPathPrefixPattern.matcher(className);
                //
                // if (!matcher.matches())
                // {
                // continue;
                // }
                // String path = matcher.group(1);
                // String fileName = matcher.group(2);

                var packageScanPatterns = getPackageScanPatterns();
                for (int b = packageScanPatterns.length; b-- > 0; ) {
                    var pathMatcher = packageScanPatterns[b].matcher(className);
                    if (pathMatcher.matches()) {
                        targetClassNames.add(className.replace('/', '.'));
                        break;
                    }
                }
                //
                // for (int b = classPathItems.length; b-- > 0;)
                // {
                // ClassPathItem classPathItem = classPathItems[b];
                // Matcher matcher = classPathItem.getPattern().matcher(className);
                // if (!matcher.matches())
                // {
                // continue;
                // }
                // targetClassNames.add(className);
                // }
            }
        } finally {
            tlObjectCollector.dispose(sb);
        }
    }
}
