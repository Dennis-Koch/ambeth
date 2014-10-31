package de.osthus.ambeth.extscanner;

import de.osthus.ambeth.collections.IMap;
import de.osthus.ambeth.ioc.IStartingBean;
import de.osthus.classbrowser.java.TypeDescription;

public class ConfigurationScanner extends AbstractLatexScanner implements IStartingBean
{
	@Override
	protected void handle(IMap<String, TypeDescription> javaTypes, IMap<String, TypeDescription> csharpTypes) throws Throwable
	{
	}

	// public static final Pattern csharpConstantPattern = Pattern.compile(" *public *const *String *[^ =]+ *= *\"(.+)\" *; *");
	//
	// @SuppressWarnings("unused")
	// @LogInstance
	// private ILogger log;
	//
	// @Property(name = "properties-tex-file")
	// protected String allPropertiesTexFilePath;
	//
	// @Property(name = "target-properties-tex-dir")
	// protected String targetPropertiesTexDirPath;
	//
	// @Override
	// public void afterStarted() throws Throwable
	// {
	// File allPropertiesTexFile = new File(allPropertiesTexFilePath).getCanonicalFile();
	// allPropertiesTexFile.getParentFile().mkdirs();
	//
	// File targetPropertiesTexDir = new File(targetPropertiesTexDirPath).getCanonicalFile();
	// targetPropertiesTexDir.mkdirs();
	//
	// log.info("TargetTexFile: " + allPropertiesTexFile);
	// log.info("PropertiesTexDir: " + targetPropertiesTexDir);
	//
	// String targetExtendableTexDirCP = targetPropertiesTexDir.getPath();
	// String targetTexFileCP = allPropertiesTexFile.getParent();
	// if (!targetExtendableTexDirCP.startsWith(targetTexFileCP))
	// {
	// throw new IllegalStateException("Path '" + targetExtendableTexDirCP + "' must reside within '" + targetTexFileCP + "'");
	// }
	// String pathToExtendableTexFile = targetExtendableTexDirCP.substring(targetTexFileCP.length() + 1);
	//
	// ClassPool pool = ClassPool.getDefault();
	// IList<String> targetClassNames = scanForClasses(pool);
	//
	// ArrayList<CtClass> clazzes = new ArrayList<CtClass>(targetClassNames.size());
	// HashMap<String, PropertyEntry> nameToPropertyMap = new HashMap<String, PropertyEntry>();
	//
	// HashMap<String, CtField> nameToConfigurationConstantsMap = new HashMap<String, CtField>();
	//
	// final HashMap<String, List<String>> constantUsedInTypesMap = new HashMap<String, List<String>>();
	//
	// for (int a = 0, size = targetClassNames.size(); a < size; a++)
	// {
	// String className = targetClassNames.get(a);
	// if (!className.endsWith("ConfigurationConstants"))
	// {
	// continue;
	// }
	// CtClass cc = pool.get(className);
	// if (cc.isAnnotation() || cc.isEnum())
	// {
	// continue;
	// }
	// clazzes.add(cc);
	// CtField[] fields = cc.getFields();
	//
	// for (CtField field : fields)
	// {
	// if (!field.getType().getName().equals("java.lang.String"))
	// {
	// // we are only interested in property constants (strings)
	// continue;
	// }
	// if ((field.getModifiers() & Modifier.STATIC) == 0)
	// {
	// // only static fields can be constants
	// continue;
	// }
	// if ((field.getModifiers() & Modifier.PUBLIC) == 0)
	// {
	// // only public fields can be constants
	// continue;
	// }
	// nameToConfigurationConstantsMap.put((String) field.getConstantValue(), field);
	// constantUsedInTypesMap.put(field.getDeclaringClass().getName() + "." + field.getName(), new ArrayList<String>());
	// }
	// }
	//
	// for (int a = 0, size = targetClassNames.size(); a < size; a++)
	// {
	// String className = targetClassNames.get(a);
	// CtClass cc = pool.get(className);
	//
	// if (cc.isInterface() || cc.isAnnotation() || cc.isEnum())
	// {
	// continue;
	// }
	// CtField[] fields = cc.getDeclaredFields();
	// for (CtField field : fields)
	// {
	// if ((field.getModifiers() & Modifier.FINAL) != 0)
	// {
	// // final fields can not be target of a property injection
	// continue;
	// }
	// if ((field.getModifiers() & Modifier.STATIC) != 0)
	// {
	// // static fields can not be target of a property injection
	// continue;
	// }
	// Property property = (Property) field.getAnnotation(Property.class);
	// handlePropertyAnnotation(cc, property, nameToPropertyMap);
	// }
	// CtMethod[] methods = cc.getDeclaredMethods();
	// for (CtMethod method : methods)
	// {
	// if (!method.getName().startsWith("set") || method.getAvailableParameterAnnotations().length != 1)
	// {
	// // not a true setter
	// continue;
	// }
	// Property property = (Property) method.getAnnotation(Property.class);
	// handlePropertyAnnotation(cc, property, nameToPropertyMap);
	// }
	// }
	// final ArrayList<File> csharpConfConstants = new ArrayList<File>();
	//
	// applyFileFilterToCSharp(new FileFilter()
	// {
	// @Override
	// public boolean accept(File pathname)
	// {
	// if (!pathname.getName().endsWith("ConfigurationConstants.cs"))
	// {
	// return false;
	// }
	// csharpConfConstants.add(pathname);
	// return true;
	// }
	// });
	//
	// for (File csharpConfConstantFile : csharpConfConstants)
	// {
	// BufferedReader rd = new BufferedReader(new FileReader(csharpConfConstantFile));
	// try
	// {
	// String line;
	// while ((line = rd.readLine()) != null)
	// {
	// Matcher matcher = csharpConstantPattern.matcher(line);
	// if (!matcher.matches())
	// {
	// continue;
	// }
	// String propertyName = matcher.group(1);
	// PropertyEntry propertyEntry = getEnsurePropertyEntry(propertyName, nameToPropertyMap);
	// propertyEntry.inCSharp = true;
	// }
	// }
	// finally
	// {
	// rd.close();
	// }
	// }
	//
	// log.info("Found " + nameToPropertyMap.size() + " properties");
	//
	// String[] propertyNames = nameToPropertyMap.keySet().toArray(String.class);
	// Arrays.sort(propertyNames);
	//
	// FileWriter fw = new FileWriter(allPropertiesTexFile);
	// try
	// {
	// fw.append("%---------------------------------------------------------------\n");
	// fw.append("% This file is FULLY generated. Please do not edit anything here\n");
	// fw.append("% Any changes have to be done to the java class " + ConfigurationScanner.class.getName() + "\n");
	// fw.append("%---------------------------------------------------------------\n");
	// fw.append("\\chapter{Ambeth Configuration}\n");
	// fw.append("\\begin{landscape}\n");
	// fw.append("\\begin{longtable}{ l l c c c } \\hline \\textbf{Property} & \\textbf{Default Value} & \\textbf{Mandatory} & \\textbf{Java} & \\textbf{C\\#} \\\\\n");
	// fw.append("\t\\endhead\n");
	// fw.append("\t\\hline\n");
	//
	// ArrayList<String> includes = new ArrayList<String>();
	//
	// for (String propertyName : propertyNames)
	// {
	// PropertyEntry propertyEntry = nameToPropertyMap.get(propertyName);
	// log.info("Handling " + propertyEntry.propertyName);
	//
	// String texName = buildTexPropertyName(propertyEntry);
	//
	// String labelName = "configuration:" + texName;
	// writeTableRow(propertyEntry, labelName, fw);
	// fw.append(" \\\\\n");
	// fw.append("\t\\hline\n");
	//
	// String expectedConfigurationTexFileName = texName + ".tex";
	//
	// includes.add(pathToExtendableTexFile + "/" + texName);
	//
	// File expectedConfigurationTexFile = new File(targetPropertiesTexDir, expectedConfigurationTexFileName);
	//
	// if (!expectedConfigurationTexFile.exists())
	// {
	// writeEmptyConfigurationTexFile(propertyEntry, labelName, expectedConfigurationTexFile,
	// nameToConfigurationConstantsMap.get(propertyEntry.propertyName));
	// }
	// }
	// fw.append("\\end{longtable}\n");
	// fw.append("\\end{landscape}\n");
	// for (int a = 0, size = includes.size(); a < size; a++)
	// {
	// // now write all chapter includes which are referenced from the table
	// fw.append("\\input{").append(includes.get(a)).append("}\n");
	// }
	// fw.append("%--END OF GENERATION----------------------------------------------------------\n");
	// }
	// finally
	// {
	// fw.close();
	// }
	//
	// for (String propertyName : nameToPropertyMap.keySet())
	// {
	// // remove all "used" properties
	// nameToConfigurationConstantsMap.remove(propertyName);
	// }
	// if (nameToConfigurationConstantsMap.size() > 0)
	// {
	// String[] obsoletePropertyNames = nameToConfigurationConstantsMap.keySet().toArray(String.class);
	// Arrays.sort(obsoletePropertyNames);
	// StringBuilder sb = new StringBuilder();
	// sb.append("There are ").append(obsoletePropertyNames.length).append(" configurations without any usage and were therefore been skipped:");
	// for (String obsoletePropertyName : obsoletePropertyNames)
	// {
	// CtField ctField = nameToConfigurationConstantsMap.get(obsoletePropertyName);
	// sb.append('\n').append(obsoletePropertyName).append(" ==> ").append(ctField.getDeclaringClass().getName()).append(".")
	// .append(ctField.getName());
	// }
	// log.warn(sb.toString());
	// }
	// }
	//
	// protected void handlePropertyAnnotation(CtClass type, Property property, Map<String, PropertyEntry> map)
	// {
	// if (property == null || Property.DEFAULT_VALUE.equals(property.name()))
	// {
	// // not a target of a property injection
	// return;
	// }
	// PropertyEntry propertyEntry = getEnsurePropertyEntry(property.name(), map);
	// propertyEntry.inJava = true;
	// propertyEntry.setDefaultValue(property.defaultValue());
	// propertyEntry.isMandatory |= property.mandatory();
	// propertyEntry.usedBy.add(type);
	// }
	//
	// protected String buildTexPropertyName(PropertyEntry propertyEntry)
	// {
	// String texName = propertyEntry.propertyName;
	// while (texName.contains("."))
	// {
	// int dotIndex = texName.indexOf('.');
	// texName = texName.substring(0, dotIndex) + Character.toUpperCase(texName.charAt(dotIndex + 1)) + texName.substring(dotIndex + 2);
	// }
	// while (texName.contains("-"))
	// {
	// int dotIndex = texName.indexOf('-');
	// texName = texName.substring(0, dotIndex) + Character.toUpperCase(texName.charAt(dotIndex + 1)) + texName.substring(dotIndex + 2);
	// }
	// while (texName.contains("_"))
	// {
	// int dotIndex = texName.indexOf('_');
	// texName = texName.substring(0, dotIndex) + Character.toUpperCase(texName.charAt(dotIndex + 1)) + texName.substring(dotIndex + 2);
	// }
	// return Character.toUpperCase(texName.charAt(0)) + texName.substring(1);
	// }
	//
	// protected PropertyEntry getEnsurePropertyEntry(String propertyName, Map<String, PropertyEntry> map)
	// {
	// PropertyEntry propertyEntry = map.get(propertyName);
	// if (propertyEntry == null)
	// {
	// propertyEntry = new PropertyEntry(propertyName);
	// map.put(propertyName, propertyEntry);
	// }
	// return propertyEntry;
	// }
	//
	// protected void writeEmptyConfigurationTexFile(PropertyEntry propertyEntry, String labelName, File targetFile, CtField ctField) throws Exception
	// {
	// FileWriter fw = new FileWriter(targetFile);
	// try
	// {
	// fw.append("\\section{").append(propertyEntry.propertyName).append("}\n");
	// fw.append("\\label{").append(labelName).append("}\n");
	// if (propertyEntry.inJava)
	// {
	// if (propertyEntry.inCSharp)
	// {
	// fw.append("\\AvailableInJavaAndCsharp{\\TODO}");
	// }
	// else
	// {
	// fw.append("\\AvailableInJavaOnly{\\TODO}");
	// }
	// }
	// else if (propertyEntry.inCSharp)
	// {
	// fw.append("\\AvailableInCsharpOnly{\\TODO}");
	// }
	// if (ctField != null)
	// {
	// fw.append("\n\\type{").append(ctField.getDeclaringClass().getName()).append(".").append(ctField.getName()).append("}");
	// }
	// fw.append("\n\\begin{lstlisting}[style=Props,caption={Usage example for \\textit{").append(propertyEntry.propertyName).append("}}]");
	// fw.append("\n").append(propertyEntry.propertyName).append("=");
	// if (propertyEntry.defaultValueSpecified)
	// {
	// fw.append(propertyEntry.getDefaultValue());
	// }
	// fw.append("\n\\end{lstlisting}");
	// }
	// finally
	// {
	// fw.close();
	// }
	// }
	//
	// protected void writeTableRow(PropertyEntry propertyEntry, String labelName, FileWriter fw) throws Exception
	// {
	// fw.append("\t\t");
	//
	// // property name
	// fw.append("\\nameref{").append(labelName).append('}');
	// fw.append(" & ");
	//
	// // default Value
	// fw.append(propertyEntry.isDefaultValueSpecified() ? propertyEntry.getDefaultValue() : "");
	// fw.append(" & ");
	//
	// // mandatory
	// fw.append(propertyEntry.isMandatory ? "X" : "");
	// fw.append(" & ");
	//
	// // Java
	// fw.append(propertyEntry.inJava ? "X" : " ").append(" & ");
	//
	// // C#
	// fw.append(propertyEntry.inCSharp ? "X" : " ");
	// }
	//
	// protected void writeJavadoc(CtClass type, FileWriter fw) throws IOException
	// {
	// fw.append("\\javadoc{").append(type.getName()).append("}{").append(type.getSimpleName()).append('}');
	// }
}
