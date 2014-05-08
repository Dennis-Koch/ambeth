package de.osthus.ant.task;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

public abstract class AbstractOsthusAntTask extends Task {	
	
	public void validateTrue(boolean value, String failMessage) {
		if ( !value ) {
			throw new BuildException(failMessage);
		}
	}
	
	public void validatePropertiesNotNull(String... properties) {
		List<String> missingProperties = new ArrayList<String>();
		
		try {
			Class<? extends Task> clazz;
			Field field;
			Object value;
			for ( String propertyName: properties ) {
				clazz = getClass();
				field = clazz.getDeclaredField(propertyName);
				field.setAccessible(true);
				value = field.get(this);
				if ( value == null ) {
					missingProperties.add(propertyName);
				}
			}
			
			if ( missingProperties.size() > 0 ) {
				throw new BuildException("Task Validation failed. Missing attributes: " + missingProperties);	
			}
		} catch (Exception e) {
			throw new BuildException("Error validating Task: " + e.getMessage(), e);
		} 		
	}
}
