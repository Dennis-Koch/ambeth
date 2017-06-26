package de.osthus.ambeth;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.List;
import java.util.Map.Entry;

import com.taskadapter.redmineapi.IssueManager;
import com.taskadapter.redmineapi.RedmineException;
import com.taskadapter.redmineapi.RedmineManager;
import com.taskadapter.redmineapi.RedmineManagerFactory;
import com.taskadapter.redmineapi.bean.Issue;
import com.taskadapter.redmineapi.bean.Project;
import com.taskadapter.redmineapi.bean.Version;

import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.HashMap;
import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.io.AbstractFileVisitor;
import de.osthus.ambeth.ioc.IStartingBean;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.threading.IBackgroundWorkerParamDelegate;
import de.osthus.ambeth.util.IMultithreadingHelper;

public class TexTodos implements IStartingBean
{
	public static final String TODO_MARKER = ": TODO @ line ";

	@Autowired
	protected IMultithreadingHelper multithreadingHelper;

	@Property(name = TexTodosConfigurationConstants.REDMINE_URL, defaultValue = "https://framework.allotrope.org")
	protected String url;

	@Property(name = TexTodosConfigurationConstants.API_KEY)
	protected String apiKey;

	@Property(defaultValue = "ambeth-services")
	protected String projectKey;

	@Property(defaultValue = "Ambeth Reference Manual: TODOs")
	protected String rootSubject;

	private final ThreadLocal<RedmineManager> redmineManagerTL = new ThreadLocal<RedmineManager>();

	@Override
	public void afterStarted() throws Throwable
	{
		RedmineManager mgr = RedmineManagerFactory.createWithApiKey(url, apiKey);

		try
		{
			final Project project = mgr.getProjectManager().getProjectByKey(projectKey);
			IssueManager issueManager = mgr.getIssueManager();
			final Version version = findVersion(mgr, project);

			List<Issue> issues;
			{
				HashMap<String, String> hashMap = new HashMap<String, String>();
				hashMap.put("subject", rootSubject);
				hashMap.put("project_id", "" + project.getId());
				issues = issueManager.getIssues(hashMap);
			}

			final HashMap<String, Issue> titleToIssueMap = new HashMap<String, Issue>();

			{
				HashMap<String, String> hashMap = new HashMap<String, String>();
				hashMap.put("parent_id", "" + issues.get(0).getId());

				hashMap.put("limit", "" + Integer.MAX_VALUE);

				int offset = 0;
				while (true)
				{
					hashMap.put("offset", "" + offset);
					List<Issue> currIssues = issueManager.getIssues(hashMap);

					offset += currIssues.size();
					for (Issue issue : currIssues)
					{
						if (!titleToIssueMap.putIfNotExists(issue.getSubject(), issue))
						{
							Issue existingIssue = titleToIssueMap.get(issue.getSubject());
							if (existingIssue.getId().equals(issue.getId()))
							{
								continue;
							}
							throw new IllegalStateException("Duplicate issue with subject: " + issue.getSubject());
						}
					}
					if (currIssues.isEmpty())
					{
						break;
					}
				}
			}

			final Issue rootIssue = issues.get(0);

			final Path repoPath = Paths.get("C:/dev/ambeth2/source/osthus-ambeth-check");

			final Path manualPath = repoPath.resolve("doc/reference-manual");

			final ArrayList<Path> texFiles = new ArrayList<Path>();

			Files.walkFileTree(manualPath, new AbstractFileVisitor()
			{
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException
				{
					String fileName = file.getFileName().toString();
					if (fileName.toLowerCase().endsWith(".tex"))
					{
						texFiles.add(file);
					}
					return super.visitFile(file, attrs);
				}
			});

			multithreadingHelper.invokeAndWait(texFiles, new IBackgroundWorkerParamDelegate<Path>()
			{
				int count = 0;

				@Override
				public void invoke(Path texFile) throws Throwable
				{
					List<String> allLines = Files.readAllLines(texFile, Charset.forName("UTF-8"));
					for (int a = 0, size = allLines.size(); a < size; a++)
					{
						String line = allLines.get(a);
						if (line.contains("\\TODO"))
						{
							Path repoRelativePath = repoPath.relativize(texFile);
							Path manualRelativePath = manualPath.relativize(texFile);
							String issueTitle = manualRelativePath.toString() + TODO_MARKER + a;

							Issue issue = titleToIssueMap.remove(issueTitle);
							if (issue == null)
							{
								issue = new Issue();
								issue.setSubject(issueTitle);
								issue.setParentId(rootIssue.getId());
								issue.setProject(project);
								issue.setTargetVersion(version);
								issue.setTracker(rootIssue.getTracker());
								try
								{
									issue = getManager().getIssueManager().createIssue(issue);
								}
								catch (Throwable e)
								{
									e.printStackTrace();
									throw RuntimeExceptionUtil.mask(e);
								}
							}
							count++;
							System.out.println(count + " title: \"" + issueTitle + "\" line " + a + ": " + repoRelativePath);
						}
					}
				}
			});

			for (Entry<String, Issue> entry : titleToIssueMap)
			{
				Issue issue = entry.getValue();
				if (!issue.getSubject().contains(TODO_MARKER))
				{
					continue;
				}
				switch (issue.getStatusName())
				{
					case "New":
					case "In Progress":
					{
						issue.setStatusName("Resolved");
						issueManager.update(issue);
						break;
					}
				}
			}
		}
		catch (Exception e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	protected RedmineManager getManager()
	{
		RedmineManager redmineManager = redmineManagerTL.get();
		if (redmineManager == null)
		{
			redmineManager = RedmineManagerFactory.createWithApiKey(url, apiKey);
			redmineManagerTL.set(redmineManager);
		}
		return redmineManager;
	}

	private Version findVersion(RedmineManager mgr, Project project) throws RedmineException
	{
		List<Version> versions = mgr.getProjectManager().getVersions(project.getId());
		for (Version version : versions)
		{
			if (version.getName().equals("Ambeth Roadmap"))
			{
				return version;
			}
		}
		return null;
	}
}
