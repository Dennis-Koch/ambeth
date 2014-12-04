package de.osthus.ambeth.xml;

import java.io.Writer;

import de.osthus.ambeth.appendable.IAppendable;
import de.osthus.ambeth.appendable.WriterAppendable;
import de.osthus.ambeth.collections.HashMap;
import de.osthus.ambeth.collections.IMap;
import de.osthus.ambeth.collections.ISet;
import de.osthus.ambeth.collections.IdentityHashMap;
import de.osthus.ambeth.collections.IdentityHashSet;
import de.osthus.ambeth.typeinfo.ITypeInfoItem;
import de.osthus.ambeth.util.ImmutableTypeSet;
import de.osthus.ambeth.xml.postprocess.IPostProcessWriter;

public class DefaultXmlWriter implements IWriter, IPostProcessWriter
{
	protected final IAppendable appendable;

	protected final ICyclicXmlController xmlController;

	protected final IdentityHashMap<Object, Integer> mutableToIdMap = new IdentityHashMap<Object, Integer>();
	protected final HashMap<Object, Integer> immutableToIdMap = new HashMap<Object, Integer>();
	protected int nextIdMapIndex = 1;

	protected final ISet<Object> substitutedEntities = new IdentityHashSet<Object>();

	protected HashMap<Class<?>, ITypeInfoItem[]> typeToMemberMap = new HashMap<Class<?>, ITypeInfoItem[]>();

	protected boolean isInAttributeState = false;

	protected int beautifierLevel;

	protected boolean beautifierIgnoreLineBreak = true;

	protected int elementContentLevel = -1;

	protected String beautifierLinebreak;

	protected boolean beautifierActive;

	public DefaultXmlWriter(final Writer osw, ICyclicXmlController xmlController)
	{
		this(new WriterAppendable(osw), xmlController);
	}

	public DefaultXmlWriter(IAppendable appendable, ICyclicXmlController xmlController)
	{
		this.appendable = appendable;
		this.xmlController = xmlController;
	}

	public void setBeautifierActive(boolean beautifierActive)
	{
		this.beautifierActive = beautifierActive;
	}

	public boolean isBeautifierActive()
	{
		return beautifierActive;
	}

	public String getBeautifierLinebreak()
	{
		return beautifierLinebreak;
	}

	public void setBeautifierLinebreak(String beautifierLinebreak)
	{
		this.beautifierLinebreak = beautifierLinebreak;
	}

	protected void writeBeautifierTabs(int amount)
	{
		if (beautifierIgnoreLineBreak)
		{
			beautifierIgnoreLineBreak = false;
		}
		else
		{
			write(beautifierLinebreak);
		}
		while (amount-- > 0)
		{
			write('\t');
		}
	}

	@Override
	public void writeEscapedXml(CharSequence unescapedString)
	{
		for (int a = 0, size = unescapedString.length(); a < size; a++)
		{
			char oneChar = unescapedString.charAt(a);
			switch (oneChar)
			{
				case '&':
					appendable.append("&amp;");
					break;
				case '\"':
					appendable.append("&quot;");
					break;
				case '\'':
					appendable.append("&apos;");
					break;
				case '<':
					appendable.append("&lt;");
					break;
				case '>':
					appendable.append("&gt;");
					break;
				default:
					appendable.append(oneChar);
					break;
			}
		}
	}

	@Override
	public void writeAttribute(CharSequence attributeName, Object attributeValue)
	{
		if (attributeValue == null)
		{
			return;
		}
		writeAttribute(attributeName, attributeValue.toString());
	}

	@Override
	public void writeAttribute(CharSequence attributeName, CharSequence attributeValue)
	{
		if (attributeValue == null || attributeValue.length() == 0)
		{
			return;
		}
		checkIfInAttributeState();
		appendable.append(' ').append(attributeName).append("=\"");
		writeEscapedXml(attributeValue);
		appendable.append('\"');
	}

	@Override
	public void writeEndElement()
	{
		checkIfInAttributeState();
		appendable.append("/>");
		isInAttributeState = false;
		if (beautifierActive)
		{
			beautifierLevel--;
		}
	}

	@Override
	public void writeCloseElement(CharSequence elementName)
	{
		if (isInAttributeState)
		{
			writeEndElement();
			isInAttributeState = false;
		}
		else
		{
			if (beautifierActive)
			{
				if (elementContentLevel == beautifierLevel)
				{
					writeBeautifierTabs(beautifierLevel - 1);
				}
				beautifierLevel--;
				elementContentLevel = beautifierLevel;
			}
			appendable.append("</").append(elementName).append('>');
		}
	}

	@Override
	public void write(CharSequence s)
	{
		appendable.append(s);
	}

	@Override
	public void writeOpenElement(CharSequence elementName)
	{
		endTagIfInAttributeState();
		if (beautifierActive)
		{
			writeBeautifierTabs(beautifierLevel);
			appendable.append('<').append(elementName).append('>');
			elementContentLevel = beautifierLevel;
			beautifierLevel++;
		}
		else
		{
			appendable.append('<').append(elementName).append('>');
		}
	}

	@Override
	public void writeStartElement(CharSequence elementName)
	{
		endTagIfInAttributeState();
		if (beautifierActive)
		{
			writeBeautifierTabs(beautifierLevel);
			appendable.append('<').append(elementName);
			elementContentLevel = beautifierLevel;
			beautifierLevel++;
		}
		else
		{
			appendable.append('<').append(elementName);
		}
		isInAttributeState = true;
	}

	@Override
	public void writeStartElementEnd()
	{
		if (!isInAttributeState)
		{
			return;
		}
		checkIfInAttributeState();
		appendable.append('>');
		isInAttributeState = false;
	}

	@Override
	public void writeObject(Object obj)
	{
		xmlController.writeObject(obj, this);
	}

	@Override
	public void writeEmptyElement(CharSequence elementName)
	{
		endTagIfInAttributeState();
		if (beautifierActive)
		{
			elementContentLevel = beautifierLevel - 1;
			writeBeautifierTabs(beautifierLevel);
		}
		appendable.append('<').append(elementName).append("/>");
	}

	@Override
	public void write(char s)
	{
		appendable.append(s);
	}

	@Override
	public int acquireIdForObject(Object obj)
	{
		boolean isImmutableType = ImmutableTypeSet.isImmutableType(obj.getClass());
		IMap<Object, Integer> objectToIdMap = isImmutableType ? immutableToIdMap : mutableToIdMap;

		Integer id = Integer.valueOf(nextIdMapIndex++);
		if (!objectToIdMap.putIfNotExists(obj, id))
		{
			throw new IllegalStateException("There is already a id mapped given object (" + obj + ")");
		}

		return id.intValue();
	}

	@Override
	public int getIdOfObject(Object obj)
	{
		boolean isImmutableType = ImmutableTypeSet.isImmutableType(obj.getClass());
		IMap<Object, Integer> objectToIdMap = isImmutableType ? immutableToIdMap : mutableToIdMap;

		Integer id = objectToIdMap.get(obj);

		return (id == null) ? 0 : id.intValue();
	}

	@Override
	public void putMembersOfType(Class<?> type, ITypeInfoItem[] members)
	{
		if (!typeToMemberMap.putIfNotExists(type, members))
		{
			throw new IllegalStateException("Already mapped type '" + type + "'");
		}
	}

	@Override
	public ITypeInfoItem[] getMembersOfType(Class<?> type)
	{
		return typeToMemberMap.get(type);
	}

	@Override
	public ISet<Object> getSubstitutedEntities()
	{
		return substitutedEntities;
	}

	@Override
	public void addSubstitutedEntity(Object entity)
	{
		substitutedEntities.add(entity);
	}

	@Override
	public IMap<Object, Integer> getMutableToIdMap()
	{
		return mutableToIdMap;
	}

	@Override
	public IMap<Object, Integer> getImmutableToIdMap()
	{
		return immutableToIdMap;
	}

	@Override
	public boolean isInAttributeState()
	{
		return isInAttributeState;
	}

	protected void checkIfInAttributeState()
	{
		if (!isInAttributeState())
		{
			throw new IllegalStateException("There is currently no pending open tag to attribute");
		}
	}

	protected void endTagIfInAttributeState()
	{
		if (isInAttributeState())
		{
			writeStartElementEnd();
		}
	}
}
