package de.osthus.ambeth.xml.namehandler;

import java.lang.reflect.Array;
import java.util.Date;
import java.util.Map.Entry;
import java.util.regex.Pattern;

import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.HashSet;
import de.osthus.ambeth.collections.IdentityLinkedMap;
import de.osthus.ambeth.collections.LinkedHashMap;
import de.osthus.ambeth.exception.RuntimeExceptionUtil;
import de.osthus.ambeth.ioc.annotation.Autowired;
import de.osthus.ambeth.log.ILogger;
import de.osthus.ambeth.log.LogInstance;
import de.osthus.ambeth.merge.IProxyHelper;
import de.osthus.ambeth.objectcollector.IThreadLocalObjectCollector;
import de.osthus.ambeth.typeinfo.ITypeInfo;
import de.osthus.ambeth.typeinfo.ITypeInfoItem;
import de.osthus.ambeth.typeinfo.ITypeInfoProvider;
import de.osthus.ambeth.xml.INameBasedHandler;
import de.osthus.ambeth.xml.IReader;
import de.osthus.ambeth.xml.IWriter;
import de.osthus.ambeth.xml.IXmlTypeKey;
import de.osthus.ambeth.xml.IXmlTypeRegistry;
import de.osthus.ambeth.xml.SpecifiedMember;
import de.osthus.ambeth.xml.typehandler.AbstractHandler;

public class ClassNameHandler extends AbstractHandler implements INameBasedHandler
{
	private static final String SPECIFIED_SUFFIX = "Specified";

	private static final SpecifiedMember[] EMPTY_TII = new SpecifiedMember[0];

	@SuppressWarnings("unused")
	@LogInstance
	private ILogger log;

	protected final HashSet<Class<?>> noMemberAttribute = new HashSet<Class<?>>();

	protected final Pattern splitPattern = Pattern.compile(" ");

	@Autowired
	protected IProxyHelper proxyHelper;

	@Autowired
	protected ITypeInfoProvider typeInfoProvider;

	@Autowired
	protected IXmlTypeRegistry xmlTypeRegistry;

	@Override
	public void afterPropertiesSet() throws Throwable
	{
		super.afterPropertiesSet();

		noMemberAttribute.add(Date.class);
		noMemberAttribute.add(Object.class);
	}

	public void writeAsAttribute(Class<?> type, IWriter writer)
	{
		writeAsAttribute(type, xmlDictionary.getClassIdAttribute(), xmlDictionary.getClassNameAttribute(), xmlDictionary.getClassNamespaceAttribute(),
				xmlDictionary.getClassMemberAttribute(), writer);
	}

	public void writeAsAttribute(Class<?> type, String classIdAttribute, String classNameAttribute, String classNamespaceAttribute,
			String classMemberAttribute, IWriter writer)
	{
		Class<?> realType = proxyHelper.getRealType(type);
		int typeId = writer.getIdOfObject(type);
		if (typeId > 0)
		{
			writer.writeAttribute(classIdAttribute, Integer.toString(typeId));
			return;
		}
		typeId = writer.acquireIdForObject(realType);
		writer.writeAttribute(classIdAttribute, Integer.toString(typeId));

		IThreadLocalObjectCollector tlObjectCollector = objectCollector.getCurrent();
		StringBuilder sb = tlObjectCollector.create(StringBuilder.class);
		try
		{
			while (type.isArray())
			{
				sb.append("[]");
				type = type.getComponentType();
			}
			String xmlName, xmlNamespace;
			IXmlTypeKey xmlType = xmlTypeRegistry.getXmlType(type);
			xmlName = xmlType.getName();
			xmlNamespace = xmlType.getNamespace();
			if (sb.length() > 0)
			{
				sb.insert(0, xmlName);
				writer.writeAttribute(classNameAttribute, sb.toString());
			}
			else
			{
				writer.writeAttribute(classNameAttribute, xmlName);
			}
			writer.writeAttribute(classNamespaceAttribute, xmlNamespace);

			if (!realType.isArray() && !realType.isEnum() && !realType.isInterface() && !noMemberAttribute.contains(realType))
			{
				sb.setLength(0);
				ITypeInfo typeInfo = typeInfoProvider.getTypeInfo(type);
				ITypeInfoItem[] members = typeInfo.getMembers();

				LinkedHashMap<String, ITypeInfoItem> potentialSpecifiedMemberMap = new LinkedHashMap<String, ITypeInfoItem>();

				for (int a = 0, size = members.length; a < size; a++)
				{
					ITypeInfoItem member = members[a];
					String name = member.getName();
					if (name.endsWith(SPECIFIED_SUFFIX))
					{
						String realName = name.substring(0, name.length() - SPECIFIED_SUFFIX.length());
						potentialSpecifiedMemberMap.put(realName, member);
					}
				}

				IdentityLinkedMap<ITypeInfoItem, ITypeInfoItem> usedMembers = new IdentityLinkedMap<ITypeInfoItem, ITypeInfoItem>();

				for (int a = 0, size = members.length; a < size; a++)
				{
					ITypeInfoItem member = members[a];
					if (member.isXMLIgnore())
					{
						continue;
					}
					if (!member.canRead() || !member.canWrite())
					{
						continue;
					}
					ITypeInfoItem specifiedMember = potentialSpecifiedMemberMap.get(member.getName());

					usedMembers.put(member, specifiedMember);

				}

				for (ITypeInfoItem specifiedMember : usedMembers.values())
				{
					if (specifiedMember != null)
					{
						usedMembers.remove(specifiedMember);
					}
				}
				ArrayList<SpecifiedMember> membersToWrite = new ArrayList<SpecifiedMember>();
				for (Entry<ITypeInfoItem, ITypeInfoItem> entry : usedMembers)
				{
					ITypeInfoItem member = entry.getKey();
					if (sb.length() > 0)
					{
						sb.append(' ');
					}
					sb.append(member.getXMLName());

					membersToWrite.add(new SpecifiedMember(member, entry.getValue()));
				}
				writer.writeAttribute(classMemberAttribute, sb.toString());
				writer.putMembersOfType(type, membersToWrite.toArray(SpecifiedMember.class));
			}
		}
		finally
		{
			tlObjectCollector.dispose(sb);
		}
	}

	public Class<?> readFromAttribute(IReader reader)
	{
		return readFromAttribute(xmlDictionary.getClassIdAttribute(), xmlDictionary.getClassNameAttribute(), xmlDictionary.getClassNamespaceAttribute(),
				xmlDictionary.getClassMemberAttribute(), reader);
	}

	public Class<?> readFromAttribute(String classIdAttribute, String classNameAttribute, String classNamespaceAttribute, String classMemberAttribute,
			IReader reader)
	{
		String classIdValue = reader.getAttributeValue(classIdAttribute);
		int classId = 0;
		if (classIdValue != null && classIdValue.length() > 0)
		{
			classId = Integer.parseInt(classIdValue);
		}
		Class<?> typeObj = (Class<?>) reader.getObjectById(classId, false);
		if (typeObj != null)
		{
			return typeObj;
		}
		String name = reader.getAttributeValue(classNameAttribute);
		String namespace = reader.getAttributeValue(classNamespaceAttribute);

		IThreadLocalObjectCollector tlObjectCollector = objectCollector.getCurrent();

		String arraySuffix;
		int dimensionCount = 0;
		StringBuilder arraySuffixSB = tlObjectCollector.create(StringBuilder.class);
		try
		{
			while (name.endsWith("[]"))
			{
				dimensionCount++;
				name = name.substring(0, name.length() - 2);
				arraySuffixSB.append("[");
			}
			arraySuffix = arraySuffixSB.toString();
		}
		finally
		{
			tlObjectCollector.dispose(arraySuffixSB);
			arraySuffixSB = null;
		}
		typeObj = xmlTypeRegistry.getType(name, namespace);

		String classMemberValue = reader.getAttributeValue(classMemberAttribute);
		if (classMemberValue != null)
		{
			ITypeInfo typeInfo = typeInfoProvider.getTypeInfo(typeObj);
			String[] memberNames = splitPattern.split(classMemberValue);
			SpecifiedMember[] members = memberNames.length > 0 ? new SpecifiedMember[memberNames.length] : EMPTY_TII;

			StringBuilder sb = new StringBuilder();
			for (int a = memberNames.length; a-- > 0;)
			{
				String memberName = memberNames[a];
				ITypeInfoItem member = typeInfo.getMemberByXmlName(memberName);
				if (member == null)
				{
					throw new IllegalStateException("No member found with xml name '" + memberName + "' on entity '" + typeObj.getName() + "'");
				}
				sb.setLength(0);
				sb.append(memberName).append(SPECIFIED_SUFFIX);
				ITypeInfoItem specifiedMember = typeInfo.getMemberByXmlName(sb.toString());
				members[a] = new SpecifiedMember(member, specifiedMember);
			}
			reader.putMembersOfType(typeObj, members);
		}
		if (dimensionCount > 0)
		{
			StringBuilder sb = tlObjectCollector.create(StringBuilder.class);
			try
			{
				sb.append(arraySuffix);
				sb.append('L');
				sb.append(typeObj.getName());
				sb.append(';');
				return Thread.currentThread().getContextClassLoader().loadClass(sb.toString());
			}
			catch (ClassNotFoundException e)
			{
				throw RuntimeExceptionUtil.mask(e);
			}
			finally
			{
				tlObjectCollector.dispose(sb);
			}
		}
		reader.putObjectWithId(typeObj, classId);
		return typeObj;
	}

	@Override
	public boolean writesCustom(Object obj, Class<?> type, IWriter writer)
	{
		if (!Class.class.equals(type))
		{
			return false;
		}
		Class<?> typeObj = (Class<?>) obj;
		IThreadLocalObjectCollector tlObjectCollector = objectCollector.getCurrent();
		StringBuilder sb = tlObjectCollector.create(StringBuilder.class);
		try
		{
			while (typeObj.isArray())
			{
				sb.append("[]");
				typeObj = typeObj.getComponentType();
			}
			String xmlName, xmlNamespace;
			IXmlTypeKey xmlType = xmlTypeRegistry.getXmlType(typeObj);
			xmlName = xmlType.getName();
			xmlNamespace = xmlType.getNamespace();

			int id = writer.acquireIdForObject(obj);
			writer.writeStartElement(xmlDictionary.getClassElement());
			writer.writeAttribute(xmlDictionary.getIdAttribute(), Integer.toString(id));
			if (sb.length() > 0)
			{
				sb.insert(0, xmlName);
				writer.writeAttribute(xmlDictionary.getClassNameAttribute(), sb.toString());
			}
			else
			{
				writer.writeAttribute(xmlDictionary.getClassNameAttribute(), xmlName);
			}
			writer.writeAttribute(xmlDictionary.getClassNamespaceAttribute(), xmlNamespace);
			writer.writeEndElement();
		}
		finally
		{
			tlObjectCollector.dispose(sb);
		}
		return true;
	}

	@Override
	public Object readObject(Class<?> returnType, String elementName, int id, IReader reader)
	{
		if (!xmlDictionary.getClassElement().equals(elementName))
		{
			throw new IllegalStateException("Element '" + elementName + "' not supported");
		}
		String name = reader.getAttributeValue(xmlDictionary.getClassNameAttribute());
		String namespace = reader.getAttributeValue(xmlDictionary.getClassNamespaceAttribute());

		// IThreadLocalObjectCollector tlObjectCollector = objectCollector.getCurrent();
		// String arraySuffix;
		int dimensionCount = 0;
		// StringBuilder arraySuffixSB = tlObjectCollector.create(StringBuilder.class);
		// try
		// {
		while (name.endsWith("[]"))
		{
			dimensionCount++;
			name = name.substring(0, name.length() - 2);
			// arraySuffixSB.append("[");
		}
		// arraySuffix = arraySuffixSB.toString();
		// }
		// finally
		// {
		// tlObjectCollector.dispose(arraySuffixSB);
		// arraySuffixSB = null;
		// }
		Class<?> typeObj = xmlTypeRegistry.getType(name, namespace);
		if (dimensionCount > 0)
		{
			// try
			// {
			// return Thread.currentThread().getContextClassLoader().loadClass(arraySuffix + "L" + typeObj.getName() + ";");
			// }
			// catch (ClassNotFoundException e)
			// {
			// throw RuntimeExceptionUtil.mask(e);
			// }
			return Array.newInstance(typeObj, 0).getClass();
		}
		return typeObj;
	}
}
