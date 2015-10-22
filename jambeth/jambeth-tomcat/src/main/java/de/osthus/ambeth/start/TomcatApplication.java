package de.osthus.ambeth.start;

import java.io.File;
import java.util.StringTokenizer;

import javax.servlet.ServletException;

import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.WebResourceRoot;
import org.apache.catalina.startup.Tomcat;
import org.apache.catalina.webresources.DirResourceSet;
import org.apache.catalina.webresources.FileResourceSet;
import org.apache.catalina.webresources.StandardRoot;

import de.osthus.ambeth.exception.RuntimeExceptionUtil;

/**
 * This class lets you start an embedded Tomcat server. It is not a bean and does not start Ambeth, but when the {@link AmbethServletListener} is on the
 * classpath, then Ambeth is started by Tomcat.
 */
public class TomcatApplication
{
	public static final String WEBSERVER_PORT_DEFAULT = "8080";
	public static final String WEBSERVER_PORT = "webserver.port";

	public static final String APP_CONTEXT_ROOT_DEFAULT = "";
	public static final String APP_CONTEXT_ROOT = "app.context.root";

	public static TomcatApplication run()
	{
		TomcatApplication ambethApp = new TomcatApplication();
		ambethApp.doRun();
		return ambethApp;
	}

	public void doRun()
	{
		try
		{
			startEmeddedTomcat();
		}
		catch (Exception e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	private void startEmeddedTomcat() throws ServletException, LifecycleException
	{
		String webappDirLocation = "src/main/webapp/";
		Tomcat tomcat = new Tomcat();
		tomcat.setPort(Integer.valueOf(System.getProperty(WEBSERVER_PORT, WEBSERVER_PORT_DEFAULT)));

		Context ctx = tomcat.addWebapp(System.getProperty(APP_CONTEXT_ROOT, APP_CONTEXT_ROOT_DEFAULT), new File(webappDirLocation).getAbsolutePath());
		WebResourceRoot resources = prepareTomcatResources(ctx);

		ctx.setResources(resources);

		tomcat.start();
		tomcat.getServer().await();
	}

	private WebResourceRoot prepareTomcatResources(Context context)
	{
		WebResourceRoot resources = new StandardRoot(context);

		String cp = System.getProperty("java.class.path");
		StringTokenizer st = new StringTokenizer(cp, ";");
		while (st.hasMoreElements())
		{
			String pathElement = st.nextToken();
			File pe = new File(pathElement);
			if (pe.isFile())
			{
				resources.addPreResources(new FileResourceSet(resources, "/WEB-INF/lib/" + pe.getName(), pe.getAbsolutePath(), "/"));
			}
			else
			{
				resources.addPreResources(new DirResourceSet(resources, "/WEB-INF/classes", pe.getAbsolutePath(), "/"));
			}
		}
		return resources;
	}
}
