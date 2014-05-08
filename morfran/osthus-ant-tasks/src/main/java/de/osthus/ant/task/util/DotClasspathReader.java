package de.osthus.ant.task.util;

import java.io.InputStream;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.mapper.MapperWrapper;

import de.osthus.ant.task.createclasspath.entity.Attribute;
import de.osthus.ant.task.createclasspath.entity.Classpath;
import de.osthus.ant.task.createclasspath.entity.ClasspathEntry;

public class DotClasspathReader {
	
	private static XStream xstream = null;

	public static Classpath readDotClasspath(InputStream dotClasspathFileInputStream) {
		XStream x = getXStreamInstance();
		Classpath cp = (Classpath) x.fromXML(dotClasspathFileInputStream);
		
		for ( ClasspathEntry cpe: cp.getClasspathEntries() ) {
			cpe.standardize();
		}		
		return cp;
	}
	
	public static String serializeDotClasspath(Classpath classpath) {
		XStream x = getXStreamInstance();
		return x.toXML(classpath);		
	}
	
	private static synchronized XStream getXStreamInstance() {
		if ( xstream == null ) {
			xstream = new XStream() {
			    @Override
			    protected MapperWrapper wrapMapper(MapperWrapper next) {
			        return new MapperWrapper(next) {
			            @Override
			            public boolean shouldSerializeMember(@SuppressWarnings("rawtypes") Class definedIn, String fieldName) {
			                if (definedIn == Object.class) {
			                    return false;
			                }
			                return super.shouldSerializeMember(definedIn, fieldName);
			            }
			        };
			    }
			};			
			xstream.processAnnotations(Classpath.class);
			xstream.processAnnotations(ClasspathEntry.class);
			xstream.processAnnotations(Attribute.class);			
		}
		return xstream;
	}
}
