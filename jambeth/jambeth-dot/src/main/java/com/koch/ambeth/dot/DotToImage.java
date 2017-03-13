package com.koch.ambeth.dot;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;

public class DotToImage implements IDotToImage
{
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Override
	public void writeImageFile(File dotFile)
	{
		try
		{
			String targetType = "png";

			Pattern fileNamePattern = Pattern.compile("(.*)\\.dot");

			Matcher fileNameMatcher = fileNamePattern.matcher(dotFile.getName());

			File targetFile;
			if (fileNameMatcher.matches())
			{
				targetFile = new File(dotFile.getParentFile(), fileNameMatcher.group(1) + "." + targetType);
			}
			else
			{
				targetFile = new File(dotFile.getParentFile(), dotFile.getName() + "." + targetType);
			}
			File exeDir = new File("C:/dev/graphviz-2.38/bin");
			// File exeFile = new File(exeDir, "fdp.exe");
			// File exeFile = new File(exeDir, "neato.exe");
			File exeFile = new File(exeDir, "fdp.exe");
			// File exeFile = new File(exeDir, "neato.exe");

			String esc = "\"\"";
			ProcessBuilder pb = new ProcessBuilder("cmd", "/c", //
					exeFile.getPath() + " -T" + targetType + " " + esc + dotFile.getPath() + esc + " > " + esc + targetFile.getPath() + esc);
			pb.directory(targetFile.getParentFile());
			Process mvn = pb.start();
			StringBuilder sb = new StringBuilder();
			BufferedReader is = new BufferedReader(new InputStreamReader(mvn.getInputStream()));
			{
				String line;
				while ((line = is.readLine()) != null)
				{
					if (!line.startsWith("[ERROR"))
					{
						continue;
					}
					if (sb.length() > 0)
					{
						sb.append('\n');
					}
					sb.append(line);
				}
				is = new BufferedReader(new InputStreamReader(mvn.getErrorStream()));
				while ((line = is.readLine()) != null)
				{
					if (sb.length() > 0)
					{
						sb.append('\n');
					}
					sb.append(line);
				}
			}
			mvn.waitFor();

			if (sb.length() > 0)
			{
				BufferedReader reader = new BufferedReader(new FileReader(dotFile));
				try
				{
					StringBuilder fileOutputSb = new StringBuilder();
					int lineIndex = 1;
					String line;
					while ((line = reader.readLine()) != null)
					{
						if (lineIndex > 1)
						{
							fileOutputSb.append('\n');
						}
						fileOutputSb.append(lineIndex + "\t ").append(line);
						lineIndex++;
					}

					log.info(sb + "\n" + fileOutputSb);
				}
				finally
				{
					reader.close();
				}
			}
		}
		catch (Throwable e)
		{
			throw RuntimeExceptionUtil.mask(e);
		}

	}
}
