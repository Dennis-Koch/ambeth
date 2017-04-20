package com.koch.ambeth.extscanner;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.koch.ambeth.extscanner.model.ConfigurationEntry;
import com.koch.ambeth.extscanner.model.TypeEntry;
import com.koch.ambeth.ioc.IStartingBean;
import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.ioc.config.Property;
import com.koch.ambeth.log.ILogger;
import com.koch.ambeth.log.LogInstance;
import com.koch.ambeth.util.annotation.ConfigurationConstants;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.collections.IMap;
import com.koch.classbrowser.java.AnnotationInfo;
import com.koch.classbrowser.java.AnnotationParamInfo;
import com.koch.classbrowser.java.FieldDescription;
import com.koch.classbrowser.java.MethodDescription;
import com.koch.classbrowser.java.TypeDescription;

public class ConfigurationUpdater extends AbstractLatexScanner implements IStartingBean {
	public static final String LISTING_START = "%% GENERATED USAGE REFERENCE - DO NOT EDIT";

	public static final String LISTING_END = "%% GENERATED USAGE REFERENCE END";

	public static final Pattern replaceUsageReferencePattern = Pattern.compile(
			"(.*" + Pattern.quote(LISTING_START) + ").*(" + Pattern.quote(LISTING_END) + ".*)",
			Pattern.DOTALL);

	public static final Pattern configurationConstantsAnnotationMatcher =
			Pattern.compile(Pattern.quote(ConfigurationConstants.class.getName()) + "|"
					+ Pattern.quote("De.Osthus.Ambeth.Annotation.ConfigurationConstants"));

	public static final Pattern propertyAnnotationMatcher =
			Pattern.compile(Pattern.quote(Property.class.getName()) + "|"
					+ Pattern.quote("De.Osthus.Ambeth.Config.PropertyAttribute"));

	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	@Autowired
	protected IModel model;

	@Property(name = "target-properties-tex-dir")
	protected String targetPropertiesTexDirPath;

	@Override
	protected void buildModel(IMap<String, TypeDescription> javaTypes,
			IMap<String, TypeDescription> csharpTypes) throws Throwable {
		for (Entry<String, TypeDescription> entry : javaTypes) {
			TypeDescription typeDescr = entry.getValue();
			List<AnnotationInfo> annotations = typeDescr.getAnnotations();
			boolean isConfigurationConstant = false;
			for (AnnotationInfo annotation : annotations) {
				if (configurationConstantsAnnotationMatcher.matcher(annotation.getAnnotationType())
						.matches()) {
					isConfigurationConstant = true;
					break;
				}
			}
			if (isConfigurationConstant) {
				handleConfigurationConstants(typeDescr, true);
			}
		}

		for (Entry<String, TypeDescription> entry : csharpTypes) {
			TypeDescription typeDescr = entry.getValue();
			List<AnnotationInfo> annotations = typeDescr.getAnnotations();
			boolean isConfigurationConstant = false;
			for (AnnotationInfo annotation : annotations) {
				if (configurationConstantsAnnotationMatcher.matcher(annotation.getAnnotationType())
						.matches()) {
					isConfigurationConstant = true;
					break;
				}
			}
			if (isConfigurationConstant) {
				handleConfigurationConstants(typeDescr, false);
			}
		}

		scanForConfigurationUsage(javaTypes, true);
		scanForConfigurationUsage(csharpTypes, false);
	}

	@Override
	protected void handleModel() throws Throwable {
		ArrayList<ConfigurationEntry> configurations = new ArrayList<>(model.allConfigurations());
		Collections.sort(configurations);

		File allPropertiesTexFile = new File(getAllDir(), "all-configurations.tex");

		File targetPropertiesTexDir = new File(targetPropertiesTexDirPath).getCanonicalFile();
		targetPropertiesTexDir.mkdirs();

		log.debug("TargetTexFile: " + allPropertiesTexFile);
		log.debug("ConfigurationTexDir: " + targetPropertiesTexDir);

		String targetExtendableTexDirCP = targetPropertiesTexDir.getPath();
		String targetTexFileCP = allPropertiesTexFile.getParent();
		if (!targetExtendableTexDirCP.startsWith(targetTexFileCP)) {
			throw new IllegalStateException(
					"Path '" + targetExtendableTexDirCP + "' must reside within '" + targetTexFileCP + "'");
		}
		String pathToExtendableTexFile =
				targetExtendableTexDirCP.substring(targetTexFileCP.length() + 1);

		OutputStreamWriter fw = new OutputStreamWriter(new FileOutputStream(allPropertiesTexFile),
				Charset.forName("UTF-8"));
		try {
			fw.append("%---------------------------------------------------------------\n");
			fw.append("% This file is FULLY generated. Please do not edit anything here\n");
			fw.append("% Any changes have to be done to the java class "
					+ ConfigurationUpdater.class.getName() + "\n");
			fw.append("%---------------------------------------------------------------\n");
			fw.append("\\chapter{Ambeth Configuration}\n");
			fw.append("\\label{ambeth:configurations}\n");
			fw.append("\\begin{landscape}\n");
			fw.append(
					"\\begin{longtable}{ l l c c c } \\hline \\textbf{Property} & \\textbf{Default Value} & \\textbf{Mandatory} & \\textbf{Java} & \\textbf{C\\#} \\\\\n");
			fw.append("\t\\endhead\n");
			fw.append("\t\\hline\n");

			ArrayList<String> includes = new ArrayList<>();

			for (ConfigurationEntry configurationEntry : configurations) {
				log.debug("Handling " + configurationEntry.propertyName);

				if (configurationEntry.isMandatory == null) {
					log.warn("Seems like '" + configurationEntry.propertyName + "' is unused?");
					configurationEntry.isMandatory = Boolean.FALSE;
				}

				String texName = buildTexPropertyName(configurationEntry.propertyName);

				String labelName = "configuration:" + texName;
				writeTableRow(configurationEntry, labelName, fw);
				fw.append(" \\\\\n");
				fw.append("\t\\hline\n");

				String expectedConfigurationTexFileName = texName + ".tex";

				includes.add(pathToExtendableTexFile + "/" + texName);

				File expectedConfigurationTexFile =
						new File(targetPropertiesTexDir, expectedConfigurationTexFileName);

				writeToConfigurationTexFile(configurationEntry, labelName, expectedConfigurationTexFile);
			}
			fw.append("\\end{longtable}\n");
			fw.append("\\end{landscape}\n");
			for (int a = 0, size = includes.size(); a < size; a++) {
				// now write all chapter includes which are referenced from the table
				fw.append("\\input{").append(includes.get(a)).append("}\n");
			}
			fw.append("%--END OF GENERATION----------------------------------------------------------\n");
		}
		finally {
			fw.close();
		}
		allPropertiesTexFile.setLastModified(currentTime);
	}

	protected void scanForConfigurationUsage(IMap<String, TypeDescription> types, boolean isJava) {
		for (Entry<String, TypeDescription> javaEntry : types) {
			TypeDescription typeDescr = javaEntry.getValue();
			for (FieldDescription field : typeDescr.getFieldDescriptions()) {
				for (com.koch.classbrowser.java.AnnotationInfo annotation : field.getAnnotations()) {
					processAnnotation(typeDescr, annotation, isJava);
				}
			}
			for (MethodDescription method : typeDescr.getMethodDescriptions()) {
				for (com.koch.classbrowser.java.AnnotationInfo annotation : method.getAnnotations()) {
					processAnnotation(typeDescr, annotation, isJava);
				}
			}
		}
	}

	protected void processAnnotation(TypeDescription typeDescr,
			com.koch.classbrowser.java.AnnotationInfo annotation, boolean isJava) {
		if (!propertyAnnotationMatcher.matcher(annotation.getAnnotationType()).matches()) {
			return;
		}
		String currentValue = null;
		Boolean mandatory = null;
		String defaultValue = null;
		for (AnnotationParamInfo param : annotation.getParameters()) {
			String name = param.getName();
			if ("name".equals(name) || "Name".equals(name)) {
				currentValue = (String) param.getCurrentValue();
				if (Property.DEFAULT_VALUE.equals(currentValue)) {
					currentValue = null;
				}
			}
			else if ("mandatory".equals(name) || "Mandatory".equals(name)) {
				mandatory = param.getCurrentValue() != null
						? Boolean.valueOf((String) param.getCurrentValue()) : null;
				if (mandatory == null) {
					mandatory = param.getDefaultValue() != null
							? Boolean.valueOf((String) param.getDefaultValue()) : null;
				}
			}
			else if ("defaultValue".equals(name) || "DefaultValue".equals(name)) {
				defaultValue = (String) param.getCurrentValue();
			}
		}
		if (currentValue == null) {
			return;
		}
		if (mandatory == null) {
			mandatory = Boolean.TRUE;
		}
		ConfigurationEntry configurationEntry = model.resolveConfiguration(currentValue);
		if (configurationEntry == null) {
			String texName = buildTexPropertyName(currentValue);
			String labelName = "configuration:" + texName;
			configurationEntry =
					new ConfigurationEntry(log, currentValue, labelName, typeDescr.getModuleName());
			model.addConfiguration(currentValue, configurationEntry);
		}
		if (configurationEntry.isMandatory == null) {
			configurationEntry.isMandatory = mandatory;
		}
		else {
			configurationEntry.isMandatory = Boolean
					.valueOf(configurationEntry.isMandatory.booleanValue() || mandatory.booleanValue());
		}
		configurationEntry.setDefaultValue(defaultValue);

		configurationEntry.usedInTypes.add(model.resolveTypeEntry(typeDescr));
		if (isJava) {
			configurationEntry.inJava = true;
		}
		else {
			configurationEntry.inCSharp = true;
		}
	}

	private void handleConfigurationConstants(TypeDescription typeDescr, boolean isJava) {
		for (FieldDescription field : typeDescr.getFieldDescriptions()) {
			ConfigurationEntry configurationEntry = getEnsureConfigurationEntry(typeDescr, field);
			if (configurationEntry == null) {
				continue;
			}
			if (isJava) {
				configurationEntry.inJava = true;
			}
			else {
				configurationEntry.inCSharp = true;
			}
		}
	}

	protected String buildTexPropertyName(String propertyName) {
		String texName = propertyName;
		while (texName.contains(".")) {
			int dotIndex = texName.indexOf('.');
			texName = texName.substring(0, dotIndex) + Character.toUpperCase(texName.charAt(dotIndex + 1))
					+ texName.substring(dotIndex + 2);
		}
		while (texName.contains("-")) {
			int dotIndex = texName.indexOf('-');
			texName = texName.substring(0, dotIndex) + Character.toUpperCase(texName.charAt(dotIndex + 1))
					+ texName.substring(dotIndex + 2);
		}
		while (texName.contains("_")) {
			int dotIndex = texName.indexOf('_');
			texName = texName.substring(0, dotIndex) + Character.toUpperCase(texName.charAt(dotIndex + 1))
					+ texName.substring(dotIndex + 2);
		}
		return Character.toUpperCase(texName.charAt(0)) + texName.substring(1);
	}

	protected ConfigurationEntry getEnsureConfigurationEntry(TypeDescription typeDescr,
			FieldDescription field) {
		if (!"java.lang.String".equals(field.getFieldType())
				&& !"string".equals(field.getFieldType())) {
			// we are only interested in configuration constants (strings)
			return null;
		}
		List<String> modifiers = field.getModifiers();
		if (!modifiers.contains("public") || !modifiers.contains("static")
				|| !modifiers.contains("final")) {
			// only public static final fields can be constants
			return null;
		}
		String valueOfConstant = field.getInitialValue();
		ConfigurationEntry propertyEntry = model.resolveConfiguration(valueOfConstant);
		if (propertyEntry == null) {
			String texName = buildTexPropertyName(valueOfConstant);
			String labelName = "configuration:" + texName;
			propertyEntry =
					new ConfigurationEntry(log, valueOfConstant, labelName, typeDescr.getModuleName());
			model.addConfiguration(valueOfConstant, propertyEntry);
		}
		propertyEntry.contantDefinitions.add(typeDescr.getFullTypeName() + "." + field.getName());
		return propertyEntry;
	}

	protected void writeToConfigurationTexFile(ConfigurationEntry configurationEntry,
			String labelName, File targetFile) throws Exception {
		String targetOpening = getAPI(configurationEntry);
		if (!targetFile.exists()) {
			OutputStreamWriter fw =
					new OutputStreamWriter(new FileOutputStream(targetFile), Charset.forName("UTF-8"));
			try {
				fw.append(targetOpening);
				fw.append("\n\\section{");
				escapeValue(configurationEntry.propertyName, fw);
				fw.append("}");
				fw.append("\n\\label{").append(labelName).append("}");
				fw.append("\n\\ClearAPI");
				fw.append("\n\\TODO");
				fw.append(LISTING_START).append('\n');
				fw.append(usageReferenceString(configurationEntry));
				fw.append(LISTING_END);
				fw.append("\n\\begin{lstlisting}[style=Props,caption={Usage example for \\textit{");
				escapeValue(configurationEntry.propertyName, fw);
				fw.append("}}]");
				fw.append("\n").append(configurationEntry.propertyName).append("=");
				if (configurationEntry.isDefaultValueSpecified()) {
					fw.append(configurationEntry.getDefaultValue());
				}
				fw.append("\n\\end{lstlisting}");
			}
			finally {
				fw.close();
			}
			targetFile.setLastModified(currentTime);
			return;
		}
		StringBuilder sb = readFileFully(targetFile);
		String newContent = writeSetAPI(sb, configurationEntry);
		Matcher matcher = replaceUsageReferencePattern.matcher(newContent);
		if (matcher.matches()) {
			newContent =
					matcher.group(1) + "\n" + usageReferenceString(configurationEntry) + matcher.group(2);
		}
		else {
			log.warn("Could not replace usage reference in '" + targetFile.getPath() + "'");
		}
		if (newContent.contentEquals(sb)) {
			return;
		}
		updateFileFully(targetFile, newContent);
	}

	protected StringBuilder usageReferenceString(ConfigurationEntry configurationEntry) {
		ArrayList<TypeEntry> usedInTypes = new ArrayList<>(configurationEntry.usedInTypes);
		Collections.sort(usedInTypes);

		StringBuilder sb = new StringBuilder();

		if (usedInTypes.size() > 0) {
			sb.append("\\begin{longtable}{ l l } \\hline \\textbf{Used in bean} & \\textbf{Module} \\\n");
			sb.append("\t\\endhead\n");
			sb.append("\t\\hline\n");

			for (TypeEntry usedInType : usedInTypes) {
				sb.append("\t\t");
				sb.append("\\type{").append(usedInType.typeDesc.getFullTypeName()).append('}');
				sb.append(" &\n\t\t");

				if (usedInType.moduleEntry != null) {
					sb.append("\\prettyref{").append(usedInType.moduleEntry.labelName).append('}');
				}
				sb.append(" \\\\\n");
				sb.append("\t\\hline\n");
			}
			sb.append("\\end{longtable}\n");
		}
		return sb;
	}

	protected void writeTableRow(ConfigurationEntry propertyEntry, String labelName,
			OutputStreamWriter fw) throws Exception {
		fw.append("\t\t");

		// property name
		fw.append("\\nameref{").append(labelName).append('}');
		fw.append(" & ");

		// default Value
		escapeValue(propertyEntry.isDefaultValueSpecified() ? propertyEntry.getDefaultValue() : "", fw);
		fw.append(" & ");

		// mandatory
		escapeValue(propertyEntry.isMandatory ? "X" : " ", fw);
		fw.append(" &\n\t\t");

		writeAvailability(propertyEntry, fw);
	}
}
