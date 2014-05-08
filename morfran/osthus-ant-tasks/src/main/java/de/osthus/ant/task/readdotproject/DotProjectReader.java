package de.osthus.ant.task.readdotproject;

import java.io.InputStream;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.mapper.MapperWrapper;

import de.osthus.ant.task.readdotproject.entity.Project;

public class DotProjectReader {

	public Project readDotProject(InputStream dotClasspathFileInputStream) {
		XStream x = createXStreamInstance();
		return (Project) x.fromXML(dotClasspathFileInputStream);		
	}
	
	public String serializeDotProject(Project project) {
		XStream x = createXStreamInstance();
		return x.toXML(project);		
	}
	
	private XStream createXStreamInstance() {		
		XStream x = new XStream() {
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
		
		x.processAnnotations(Project.class);
		return x;
	}
}
