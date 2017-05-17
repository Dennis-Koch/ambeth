package com.koch.ambeth.xml;

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
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.util.config.IProperties;
import com.koch.ambeth.xml.postprocess.IPostProcessReader;
import com.koch.ambeth.xml.postprocess.IXmlPostProcessor;
import com.koch.ambeth.xml.postprocess.IXmlPostProcessorRegistry;
import com.koch.ambeth.xml.simple.SimpleXmlReader;

public class CyclicXmlReader extends SimpleXmlReader {
	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected ICyclicXmlDictionary xmlDictionary;

	@Autowired
	protected IXmlPostProcessorRegistry xmlPostProcessorRegistry;

	public CyclicXmlReader() {
		super();
	}

	protected CyclicXmlReader(IProperties props) {
		super(props);
	}

	@Override
	protected SimpleXmlReader createReaderInstance(IProperties props) {
		CyclicXmlReader reader = new CyclicXmlReader(props);
		reader.xmlDictionary = xmlDictionary;
		reader.xmlPostProcessorRegistry = xmlPostProcessorRegistry;
		return reader;
	}

	@Override
	protected Object postProcess(Object object, IPostProcessReader pullParserReader) {
		handlePostProcessingTag(pullParserReader);

		return super.postProcess(object, pullParserReader);
	}

	@Override
	protected void readPrefix(IReader reader) {
		if (!xmlDictionary.getRootElement().equals(reader.getElementName())) {
			throw new IllegalStateException("Invalid root element: " + reader);
		}
		reader.nextTag();
	}

	protected void handlePostProcessingTag(IPostProcessReader pullParserReader) {
		if (!pullParserReader.isStartTag()) {
			return;
		}

		String elementName = pullParserReader.getElementName();
		if (!xmlDictionary.getPostProcessElement().equals(elementName)) {
			throw new IllegalStateException(
					"Only <pp> allowed as second child of root. <" + elementName + "> found.");
		}

		pullParserReader.nextTag();

		while (pullParserReader.isStartTag()) {
			elementName = pullParserReader.getElementName();
			IXmlPostProcessor xmlPostProcessor =
					xmlPostProcessorRegistry.getXmlPostProcessor(elementName);
			if (xmlPostProcessor == null) {
				throw new IllegalStateException("Post processing tag <" + elementName + "> not supported.");
			}

			xmlPostProcessor.processRead(pullParserReader);

			pullParserReader.moveOverElementEnd();
		}
	}
}
