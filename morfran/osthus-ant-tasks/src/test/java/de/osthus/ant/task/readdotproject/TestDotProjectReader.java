package de.osthus.ant.task.readdotproject;

import java.io.ByteArrayInputStream;

import org.junit.Assert;
import org.junit.Test;

import de.osthus.ant.task.readdotproject.DotProjectReader;
import de.osthus.ant.task.readdotproject.entity.Project;


public class TestDotProjectReader {

	@Test
	public void testSerializeDotProject() {
		DotProjectReader dpr = new DotProjectReader();		
		Project originalDummy = creatDummyProject();		
		String serializedDummy = dpr.serializeDotProject(originalDummy);		
		Project deserializedDummy = dpr.readDotProject(new ByteArrayInputStream(serializedDummy.getBytes()));		
		Assert.assertEquals(originalDummy.getName(), deserializedDummy.getName()); 
	}
	
	@Test
	public void testLoadSampleProjectFile() {
		DotProjectReader dpr = new DotProjectReader();
		Project project = dpr.readDotProject(Thread.currentThread().getContextClassLoader().getResourceAsStream("sample-1.project"));
		Assert.assertEquals(project.getName(), "jAmbeth.Cache.File");		
	}
	
	private Project creatDummyProject() {
		Project dummy = new Project();		
		dummy.setName("Ambeth.Foo");		
		return dummy;
	}
}
