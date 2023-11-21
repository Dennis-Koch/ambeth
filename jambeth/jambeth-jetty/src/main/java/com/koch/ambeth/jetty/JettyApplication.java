package com.koch.ambeth.jetty;

/*-
 * #%L
 * jambeth-jetty
 * %%
 * Copyright (C) 2023 Koch Softwaredevelopment
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

import jakarta.servlet.ServletContainerInitializer;
import lombok.SneakyThrows;
import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.ee10.servlet.listener.ContainerInitializer;
import org.eclipse.jetty.ee10.webapp.WebAppContext;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.server.handler.ContextHandlerCollection;
import org.eclipse.jetty.util.resource.PathResource;
import org.eclipse.jetty.util.resource.Resource;
import org.eclipse.jetty.util.resource.ResourceFactory;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * This class lets you start an embedded Jetty server. It is not a bean and does not start Ambeth,
 * but when the {@link com.koch.ambeth.server.rest.AmbethServletListener} is on the classpath, then Ambeth is started by Jetty.
 * Inspired from <a href="https://happycoding.io/tutorials/java-server/embedded-jetty/hello-world-embedded-jetty-maven">https://happycoding.io/tutorials</a>
 */
public class JettyApplication implements Closeable {
    public static final String WEBSERVER_PORT_DEFAULT = "8080";
    public static final String WEBSERVER_PORT = "webserver.port";

    private Server jetty;

    public static JettyApplication run() {
        var ambethApp = new JettyApplication();
        ambethApp.doRun();
        return ambethApp;
    }

    @SneakyThrows
    @Override
    public void close() throws IOException {
        if (jetty != null) {
            jetty.stop();
            jetty = null;
        }
    }

    public void doRun() {
        if (jetty != null) {
            throw new IllegalStateException("Tomcat already running");
        }
        startEmeddedJetty();
    }

    @SneakyThrows
    private void startEmeddedJetty() {
        var baseResource = findBaseResource();

        var context = new WebAppContext();
        context.setContextPath("/");
        context.setExtractWAR(false);
        context.setBaseResource(baseResource);
        context.setDescriptor(baseResource.resolve("resources/WEB-INF/web.xml").toString());

        //context.setAttribute("org.eclipse.jetty.server.webapp.ContainerIncludeJarPattern",
                //".*/build/classes/com/koch/ambeth/|.*/target/classes/com/koch/ambeth");

        var server = new Server(Integer.valueOf(System.getProperty(WEBSERVER_PORT, WEBSERVER_PORT_DEFAULT)));

        var connector = new ServerConnector(server);
        server.addConnector(connector);

        var contexts = new ContextHandlerCollection();
        server.setHandler(contexts);
        contexts.addHandler(context);

        //var context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        //context.setContextPath("/");
        //context.setBaseResource(baseResource);
        //contexts.addHandler(context);

        // server.setDumpAfterStart(true);
        server.start();

        jetty = server;

        // Look for annotations in the classes directory (dev server) and in the
        // jar file (live server)
        // webAppContext.setAttribute(
        //      "org.eclipse.jetty.server.webapp.ContainerIncludeJarPattern",
//     ".*/target/classes/|.*\\.jar");
        jetty.start();
    }


    private static Resource findBaseResource() {
        var log = LoggerFactory.getLogger(JettyApplication.class);
        try (var resourceFactory = ResourceFactory.closeable()) {
            try {
                // Look for resource in classpath (this is the best choice when working with a jar/war archive)
                var classLoader = JettyApplication.class.getClassLoader();
                var webXml = classLoader.getResource("META-INF/resources/WEB-INF/web.xml");
                if (webXml != null) {
                    var uri = webXml.toURI().resolve("../..").normalize();
                    log.info("Found WebResourceBase (Using ClassLoader reference) {}", uri);
                    return resourceFactory.newResource(uri);
                }
            } catch (URISyntaxException e) {
                throw new RuntimeException("Bad ClassPath reference for: WEB-INF", e);
            }
            // Look for resource in common file system paths
            try {
                var pwd = Path.of(System.getProperty("user.dir")).toAbsolutePath();
                var targetDir = pwd.resolve("target");
                if (Files.isDirectory(targetDir)) {
                    try (var listing = Files.list(targetDir)) {
                        var embeddedServletServerDir = listing
                                .filter(Files::isDirectory)
                                .filter((path) -> path.getFileName().toString().startsWith("embedded-servlet-server-"))
                                .findFirst()
                                .orElse(null);
                        if (embeddedServletServerDir != null) {
                            log.info("Found WebResourceBase (Using /target/ Path) {}", embeddedServletServerDir);
                            return resourceFactory.newResource(embeddedServletServerDir);
                        }
                    }
                }

                // Try the source path next
                var srcWebapp = pwd.resolve("src/main/webapp/");
                if (Files.exists(srcWebapp)) {
                    log.info("WebResourceBase (Using /src/main/webapp/ Path) {}", srcWebapp);
                    return resourceFactory.newResource(srcWebapp);
                }
            } catch (Throwable t) {
                throw new RuntimeException("Unable to find web resource in file system", t);
            }
            throw new RuntimeException("Unable to find web resource ref");
        }
    }
}
