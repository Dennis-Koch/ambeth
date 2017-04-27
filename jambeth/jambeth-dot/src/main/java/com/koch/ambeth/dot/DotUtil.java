package com.koch.ambeth.dot;

import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Consumer;

import com.koch.ambeth.dot.config.DotConfigurationConstants;
import com.koch.ambeth.ioc.config.Property;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.util.exception.RuntimeExceptionUtil;

public class DotUtil implements IDotUtil {

	@LogInstance
	private ILogger log;

	@Property(name = DotConfigurationConstants.GRAPH_VIZ_BIN_DIR,
			defaultValue = "C:/dev/graphviz-2.38/bin")
	private Path graphVizBinDir;

	@Override
	public byte[] writeDotAsPngBytes(final String dot) {
		try {
			DotWriterHelper dotWriterHelper = new DotWriterHelper(log, graphVizBinDir, false);
			java.nio.file.Path tempDot = Files.createTempFile("ambeth-fim-", ".dot");
			try {
				Path fimPng = dotWriterHelper.writeDotFileAndCreateImage(new Consumer<Writer>() {
					@Override
					public void accept(Writer writer) {
						try {
							writer.write(dot);
						}
						catch (Throwable e) {
							throw RuntimeExceptionUtil.mask(e);
						}
					}
				}, tempDot, "fdp");
				byte[] pngBytes = Files.readAllBytes(fimPng);
				Files.delete(fimPng);
				return pngBytes;
			}
			finally {
				Files.delete(tempDot);
			}
		}
		catch (Throwable e) {
			throw RuntimeExceptionUtil.mask(e);
		}
	}
}
