package com.koch.ambeth.log.config;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/*-
 * #%L
 * jambeth-log
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LoggerFactory;
import com.koch.ambeth.log.io.FileUtil;
import com.koch.ambeth.util.collections.HashSet;
import com.koch.ambeth.util.collections.ISet;
import com.koch.ambeth.util.collections.LinkedHashMap;
import com.koch.ambeth.util.collections.LinkedHashSet;
import com.koch.ambeth.util.collections.specialized.PropertyChangeSupport;
import com.koch.ambeth.util.config.IProperties;
import com.koch.ambeth.util.config.UtilConfigurationConstants;
import com.koch.ambeth.util.threading.SensitiveThreadLocal;

public class Properties implements IProperties, Iterable<Entry<String, Object>> {
	private static class WeakPropertyChangeListener extends WeakReference<Properties>
			implements PropertyChangeListener {
		public WeakPropertyChangeListener(Properties referent, ReferenceQueue<? super Properties> q) {
			super(referent, q);
		}

		public WeakPropertyChangeListener(Properties referent) {
			super(referent);
		}

		@Override
		public void propertyChange(PropertyChangeEvent evt) {
			Properties target = get();
			if (target == null) {
				Properties sourceProps = (Properties) evt.getSource();
				sourceProps.removePropertyChangeListener(this);
				return;
			}
			if (target.dictionary.containsKey(evt.getPropertyName())) {
				// change is not propagated to child because it defines already its own value for it
				return;
			}
			PropertyChangeSupport pcs = target.pcs;
			if (pcs != null) {
				pcs.firePropertyChange(target, evt.getPropertyName(), evt.getOldValue(), evt.getNewValue());
			}
		}
	}

	protected static final Pattern commentRegex = Pattern.compile(" *[#;'].*");
	protected static final Pattern propertyRegex =
			Pattern.compile(" *([^= ]+) *(?:=? *(?:(.*)|'(.*)'|\"(.*)\") *)?");

	public static final Pattern dynamicRegex =
			Pattern.compile("(.*)\\$\\{([^\\$\\{\\}]+)\\}(.*)", Pattern.DOTALL);

	public static final Charset CHARSET_UTF_8 = Charset.forName("UTF-8");

	protected static final Properties system = new Properties();
	protected static Properties application;

	protected final LinkedHashMap<String, Object> dictionary = new LinkedHashMap<>();

	protected final IProperties parent;

	static {
		Iterator<Entry<String, String>> iter = System.getenv().entrySet().iterator();
		while (iter.hasNext()) {
			Entry<String, String> entry = iter.next();
			system.put(entry.getKey(), entry.getValue());
		}
		Iterator<Entry<Object, Object>> propsIter = System.getProperties().entrySet().iterator();
		while (propsIter.hasNext()) {
			Entry<Object, Object> entry = propsIter.next();
			system.put((String) entry.getKey(), entry.getValue());
		}
		Properties.application = new Properties(Properties.system);
	}

	public static IProperties getSystem() {
		return system;
	}

	public static Properties getApplication() {
		return application;
	}

	public static void resetApplication() {
		Properties.application = new Properties(Properties.system);
	}

	public static void loadBootstrapPropertyFile() {
		loadBootstrapPropertyFile(Properties.getApplication());
	}

	public static void loadBootstrapPropertyFile(Properties props) {
		System.out.println("Ambeth is looking for environment property '"
				+ UtilConfigurationConstants.BootstrapPropertyFile + "'...");
		String bootstrapPropertyFile =
				props.getString(UtilConfigurationConstants.BootstrapPropertyFile);
		if (bootstrapPropertyFile == null) {
			bootstrapPropertyFile =
					props.getString(UtilConfigurationConstants.BootstrapPropertyFile.toUpperCase());
		}
		if (bootstrapPropertyFile != null) {
			System.out
					.println("  Environment property '" + UtilConfigurationConstants.BootstrapPropertyFile
							+ "' found with value '" + bootstrapPropertyFile + "'");
			props.load(bootstrapPropertyFile, false);
			System.out
					.println("  External property file '" + bootstrapPropertyFile + "' successfully loaded");
		}
		else {
			System.out
					.println("  No Environment property '" + UtilConfigurationConstants.BootstrapPropertyFile
							+ "' found. Skipping search for external bootstrap properties");
		}
	}

	public static String[] deriveArgsFromProperties(IProperties props) {
		ISet<String> keys = props.collectAllPropertyKeys();
		String[] derivedArgs = new String[keys.size()];
		int index = 0;
		for (String propertyKey : keys) {
			Object propertyValue = props.get(propertyKey);
			derivedArgs[index] = propertyKey + '=' + propertyValue;
			index++;
		}
		return derivedArgs;
	}

	// Intentionally not a SensitiveThreadLocal. It can not contain a memory leak, because the HashSet
	// is cleared after each usage
	protected final ThreadLocal<HashSet<String>> cyclicKeyCheckTL =
			new SensitiveThreadLocal<>();
	protected final ThreadLocal<HashSet<String>> unknownListTL =
			new SensitiveThreadLocal<>();

	protected volatile PropertyChangeSupport pcs;

	public Properties() {
		this((IProperties) null);
		// Intended blank
	}

	public Properties(IProperties parent) {
		this.parent = parent;
		if (parent != null) {
			parent.addPropertyChangeListener(new WeakPropertyChangeListener(this));
		}
	}

	public Properties(String filepath) {
		this(filepath, null);
		// Intended blank
	}

	public Properties(String filepath, IProperties parent) {
		this(parent);
		load(filepath);
	}

	public Properties(IProperties dictionary, IProperties parent) {
		this(parent);
		load(dictionary);
	}

	@Override
	public IProperties getParent() {
		return parent;
	}

	@Override
	public Object get(String key) {
		return get(key, this);
	}

	@Override
	public Object get(String key, IProperties initiallyCalledProps) {
		if (initiallyCalledProps == null) {
			initiallyCalledProps = this;
		}
		Object propertyValue = dictionary.get(key);
		if (propertyValue == null && parent != null) {
			return parent.get(key, initiallyCalledProps);
		}
		if (!(propertyValue instanceof String)) {
			return propertyValue;
		}
		return initiallyCalledProps.resolvePropertyParts((String) propertyValue);
	}

	@Override
	public String resolvePropertyParts(String value) {
		if (value == null) {
			return null;
		}

		ThreadLocal<HashSet<String>> unknownListTL = this.unknownListTL;
		HashSet<String> unknownList = unknownListTL.get();
		boolean createdUnkownList = false;
		boolean unkown = false;

		try {
			String currStringValue = value;

			while (true) {
				if (!currStringValue.contains("${")) {
					return currStringValue;
				}
				Matcher matcher = dynamicRegex.matcher(currStringValue);

				String leftFromVariable;
				String variableName;
				String rightFromVariable;
				String additionalRightFromVariable = "";

				do {
					if (!matcher.matches()) {
						return currStringValue;
					}
					leftFromVariable = matcher.group(1);
					variableName = matcher.group(2);
					rightFromVariable = matcher.group(3);

					unkown = false;
					if (unknownList != null && unknownList.contains(variableName)) {
						unkown = true;
						// this steps makes the string "smaller" so we need to keep the right part of the
						// removed part
						additionalRightFromVariable =
								"${" + variableName + "}" + rightFromVariable + additionalRightFromVariable;
						matcher.region(matcher.start(1), matcher.end(1));
					}
				}
				while (unkown);

				ThreadLocal<HashSet<String>> cyclicKeyCheckTL = this.cyclicKeyCheckTL;
				HashSet<String> cyclicKeyCheck = cyclicKeyCheckTL.get();
				boolean created = false, added = false;
				if (cyclicKeyCheck == null) {
					cyclicKeyCheck = new HashSet<>();
					cyclicKeyCheckTL.set(cyclicKeyCheck);
					created = true;
				}
				try {
					if (!cyclicKeyCheck.add(variableName)) {
						throw new IllegalArgumentException(
								"Cycle detected on dynamic property resolution with name: '" + variableName
										+ "'. This is not supported");
					}
					added = true;

					String resolvedVariable = getString(variableName);
					if (resolvedVariable == null) {
						if (leftFromVariable.length() == 0 && rightFromVariable.length() == 0) {
							return "${" + variableName + "}";
						}
						// add to unknown list
						if (unknownList == null) {
							unknownList = new HashSet<>();
							unknownListTL.set(unknownList);
							createdUnkownList = true;
						}

						unknownList.add(variableName);

						resolvedVariable = "${" + variableName + "}";
					}
					currStringValue =
							leftFromVariable + resolvedVariable + rightFromVariable + additionalRightFromVariable;
				}
				finally {
					if (added) {
						cyclicKeyCheck.remove(variableName);
					}
					if (created) {
						cyclicKeyCheckTL.remove();
					}
				}
			}
		}
		finally {
			if (createdUnkownList) {
				unknownListTL.remove();
			}
		}
	}

	public void fillWithCommandLineArgs(String[] args) {
		StringBuilder sb = new StringBuilder();
		for (int a = args.length; a-- > 0;) {
			String arg = args[a];
			if (sb.length() > 0) {
				sb.append('\n');
			}
			sb.append(arg);
		}
		handleContent(sb.toString(), true);
	}

	public void put(String key, Object value) {
		putProperty(key, value);
	}

	public boolean putIfUndefined(String key, Object value) {
		if (get(key) != null) {
			return false;
		}
		putProperty(key, value);
		return true;
	}

	@Override
	public String getString(String key) {
		Object value = get(key);
		if (value == null) {
			return null;
		}
		return value.toString();
	}

	@Override
	public String getString(String key, String defaultValue) {
		Object value = get(key);
		if (value != null && value instanceof String) {
			return (String) value;
		}
		return defaultValue;
	}

	public void putString(String key, String value) {
		putProperty(key, value);
	}

	@Override
	public Iterator<Entry<String, Object>> iterator() {
		return dictionary.iterator();
	}

	@Override
	public ISet<String> collectAllPropertyKeys() {
		LinkedHashSet<String> allPropertiesSet = new LinkedHashSet<>();
		collectAllPropertyKeys(allPropertiesSet);
		return allPropertiesSet;
	}

	@Override
	public void collectAllPropertyKeys(Set<String> allPropertiesSet) {
		if (parent != null) {
			parent.collectAllPropertyKeys(allPropertiesSet);
		}
		for (Entry<String, Object> entry : dictionary) {
			allPropertiesSet.add(entry.getKey());
		}
	}

	public void load(IProperties sourceProperties) {
		ISet<String> propertyKeys = sourceProperties.collectAllPropertyKeys();
		for (String key : propertyKeys) {
			Object value = sourceProperties.get(key);

			put(key, value);
		}
	}

	public void load(java.util.Properties sourceProperties) {
		Iterator<Entry<Object, Object>> iter = sourceProperties.entrySet().iterator();
		while (iter.hasNext()) {
			Entry<Object, Object> entry = iter.next();
			Object key = entry.getKey();
			Object value = entry.getValue();

			put((String) key, value);
		}
	}

	public void load(InputStream stream) {
		load(stream, true);
	}

	public void load(InputStream stream, boolean overwriteParentExisting) {
		InputStreamReader isr = null;
		try {
			isr = new InputStreamReader(stream, CHARSET_UTF_8);
			load(isr, overwriteParentExisting);
		}
		finally {
			if (stream != null) {
				try {
					stream.close();
				}
				catch (IOException e) {
				}
				finally {
					stream = null;
				}
			}
			if (isr != null) {
				try {
					isr.close();
				}
				catch (IOException e) {
				}
				finally {
					isr = null;
				}
			}
		}
	}

	public void load(String filepathSrc) {
		load(filepathSrc, true);
	}

	public void load(String filepathSrc, boolean overwriteParentExisting) {
		String[] filepaths = FileUtil.splitConfigFileNames(filepathSrc);

		InputStream[] fileStreams = FileUtil.openFileStreams(filepaths);

		for (InputStream stream : fileStreams) {
			load(stream, overwriteParentExisting);
		}
	}

	private void load(InputStreamReader inputStreamReader, boolean overwriteParentExisting) {
		StringBuilder fileData = new StringBuilder();
		String text = "";
		BufferedReader br = null;
		try {
			br = new BufferedReader(inputStreamReader);
			while (null != (text = br.readLine())) {
				text = text.trim();
				fileData.append(text).append("\n");
			}
			text = fileData.toString();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		finally {
			if (fileData != null) {
				fileData.setLength(0);
				fileData = null;
			}
			if (inputStreamReader != null) {
				try {
					inputStreamReader.close();
				}
				catch (IOException e) {
				}
				finally {
					inputStreamReader = null;
				}
			}
			if (br != null) {
				try {
					br.close();
				}
				catch (IOException e) {
				}
				finally {
					br = null;
				}
			}
		}
		handleContent(text, overwriteParentExisting);
	}

	protected void handleContent(String content, boolean overwriteParentExisting) {
		content = content.replace("\r", "");
		String[] records = content.split("\n");
		for (String record : records) {
			if (Properties.commentRegex.matcher(record).matches()) {
				continue;
			}
			Matcher matcher = Properties.propertyRegex.matcher(record);
			if (!matcher.matches()) {
				continue;
			}
			String key = matcher.group(1);
			Object value;
			if (matcher.groupCount() > 2) {
				String stringValue = matcher.group(2);
				if (stringValue == null || stringValue.isEmpty()) {
					stringValue = matcher.group(3);
				}
				if (stringValue == null || stringValue.isEmpty()) {
					stringValue = matcher.group(4);
				}
				value = stringValue;
			}
			else {
				value = matcher.group(2);
			}
			if (!overwriteParentExisting && get(key) != null) {
				continue;
			}
			if (value == null) {
				value = "";
			}
			putProperty(key, value);
		}
	}

	protected void putProperty(String key, Object value) {
		Object oldValue = dictionary.put(key, value);
		if (pcs != null && !Objects.equals(oldValue, value)) {
			ILogger logger = LoggerFactory.getLogger(getClass(), this);
			if (logger.isInfoEnabled()) {
				logger.info("Updated property '" + key + "' to value '" + value + "'");
			}
			pcs.firePropertyChange(this, key, oldValue, value);
		}
	}

	@Override
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		if (pcs == null) {
			synchronized (this) {
				if (pcs == null) {
					pcs = new PropertyChangeSupport();
				}
			}
		}
		pcs.addPropertyChangeListener(listener);
	}

	@Override
	public void removePropertyChangeListener(PropertyChangeListener listener) {
		if (pcs == null) {
			return;
		}
		pcs.removePropertyChangeListener(listener);
	}
}
