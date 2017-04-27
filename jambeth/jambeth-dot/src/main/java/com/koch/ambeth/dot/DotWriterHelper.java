package com.koch.ambeth.dot;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.function.Consumer;

import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;
import com.koch.ambeth.util.io.StringBuilderWriter;

public class DotWriterHelper {
	private static final Charset utf8 = Charset.forName("UTF-8");

	private Path graphVizBinDir;
	private boolean createImageMap;
	private ILogger log;

	public DotWriterHelper(ILogger log, Path graphVizBinDir, boolean createImageMap) {
		this.log = log;
		this.graphVizBinDir = graphVizBinDir;
		this.createImageMap = createImageMap;
	}

	public void writeDotFile(Consumer<Writer> dotLambda, Path dotFile) {
		StringBuilder sb = new StringBuilder();
		try (StringBuilderWriter writer = new StringBuilderWriter(sb)) {
			dotLambda.accept(writer);
			String text = sb.toString();
			Files.write(dotFile, text.getBytes(utf8), StandardOpenOption.CREATE);
		}
		catch (Throwable e) {
			throw RuntimeExceptionUtil.mask(e);
		}
	}

	public Path writeDotFileAndCreateImage(Consumer<Writer> dotLambda, Path dotFile,
			String algorithm) {
		dotFile = dotFile.toAbsolutePath().normalize();
		writeDotFile(dotLambda, dotFile);
		String fileName = dotFile.getFileName().toString();
		int lastDot = fileName.lastIndexOf('.');
		Path imageFile = dotFile.resolveSibling(fileName.substring(0, lastDot) + ".png");

		writeImageFile(dotFile, imageFile, "png", algorithm);
		return imageFile;
	}

	public void writeImageFile(Path dotFile, Path targetFile, String imageType, String algorithm) {
		Path mapFile = createImageMap ? dotFile.resolveSibling(targetFile + ".map") : null;

		Path exeFile = graphVizBinDir.resolve(algorithm + ".exe");

		if (!Files.exists(exeFile)) {
			if (log.isErrorEnabled()) {
				log.error("No Graphviz installation fould (" + exeFile.toString()
						+ "). Skipping creation of image files.");
			}
			return;
		}

		try {
			Files.createDirectories(targetFile.getParent());
		}
		catch (Throwable e) {
			throw RuntimeExceptionUtil.mask(e);
		}

		ProcessBuilder pb;
		if (createImageMap) {
			pb = new ProcessBuilder( //
					exeFile.toString(), //
					"-T", imageType, "-o", targetFile.toString(), //
					"-T", "cmapx", "-o", mapFile.toString(), //
					dotFile.toString() //
			);
		}
		else {
			pb = new ProcessBuilder(exeFile.toString(), "-T", imageType, "-o", targetFile.toString(),
					dotFile.toString());
		}
		try {
			Process mvn = pb.start();
			try {
				StringBuilder sb = new StringBuilder();
				BufferedReader is = new BufferedReader(new InputStreamReader(mvn.getInputStream()));
				{
					String line;
					while ((line = is.readLine()) != null) {
						if (!line.startsWith("[ERROR")) {
							continue;
						}
						if (sb.length() > 0) {
							sb.append('\n');
						}
						sb.append(line);
					}
					is = new BufferedReader(new InputStreamReader(mvn.getErrorStream()));
					while ((line = is.readLine()) != null) {
						if (sb.length() > 0) {
							sb.append('\n');
						}
						sb.append(line);
					}
				}
				mvn.waitFor();

				if (sb.length() > 0) {
					BufferedReader reader = new BufferedReader(new FileReader(dotFile.toFile()));
					try {
						StringBuilder fileOutputSb = new StringBuilder();
						int lineIndex = 1;
						String line;
						while ((line = reader.readLine()) != null) {
							if (lineIndex > 1) {
								fileOutputSb.append('\n');
							}
							fileOutputSb.append(lineIndex + "\t ").append(line);
							lineIndex++;
						}

						log.info(sb + "\n" + fileOutputSb);
					}
					finally {
						reader.close();
					}
				}
			}
			finally {
				mvn.destroyForcibly();
			}
		}
		catch (Throwable e) {
			throw RuntimeExceptionUtil.mask(e);
		}
	}
}
