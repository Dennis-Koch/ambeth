package demo.codeanalyzer.common.model;

import java.util.Map.Entry;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.ExecutableElement;

import com.sun.source.tree.AnnotationTree;

import de.osthus.ambeth.collections.IMap;
import de.osthus.ambeth.collections.LinkedHashMap;

/**
 * Code analyzer model for storing details of annotation
 * 
 * @author Deepa Sobhana, Seema Richard
 */
public class AnnotationInfo implements Annotation
{
	private String name;

	protected final LinkedHashMap<String, AnnotationValue> properties = new LinkedHashMap<String, AnnotationValue>();

	public AnnotationInfo(AnnotationMirror annotationMirror)
	{
		name = annotationMirror.getAnnotationType().toString();
		for (Entry<? extends ExecutableElement, ? extends AnnotationValue> entry : annotationMirror.getElementValues().entrySet())
		{
			String key = entry.getKey().getSimpleName().toString();
			AnnotationValue value = entry.getValue();
			properties.put(key, value);
		}
	}

	public AnnotationInfo(String name, AnnotationTree body)
	{
		this.name = name;
	}

	@Override
	public IMap<String, AnnotationValue> getProperties()
	{
		return properties;
	}

	@Override
	public String getType()
	{
		return name;
	}

	@Override
	public String toString()
	{
		StringBuffer buffer = new StringBuffer();
		buffer.append("Annotation Name: " + name);
		buffer.append("\n");
		return buffer.toString();
	}
}
