package demo.codeanalyzer.common.model;

import javax.lang.model.element.AnnotationValue;

import de.osthus.ambeth.collections.IMap;

/**
 * Code analyzer model for storing details of annotation
 * 
 * @author Deepa Sobhana, Seema Richard
 */
public interface Annotation
{
	String getType();

	IMap<String, AnnotationValue> getProperties();
}
