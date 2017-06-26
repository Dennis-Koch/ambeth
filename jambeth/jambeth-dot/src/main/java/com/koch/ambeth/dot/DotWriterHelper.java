package com.koch.ambeth.dot;

import java.io.Writer;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;

import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;
import com.koch.ambeth.util.io.StringBuilderWriter;
import com.koch.ambeth.util.process.ProcessUtil;

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
			Files.write(dotFile, text.getBytes(utf8));
		} catch (Exception e) {
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
		} catch (Exception e) {
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
		} else {
			pb = new ProcessBuilder(exeFile.toString(), "-T", imageType, "-o",
					targetFile.toString(), dotFile.toString());
		}
		try {
			Process mvn = pb.start();
			try {
				ProcessUtil.waitForTermination(mvn, System.out, System.err);
			} finally {
				mvn.destroyForcibly();
			}
		} catch (Exception e) {
			throw RuntimeExceptionUtil.mask(e);
		}
	}
}
