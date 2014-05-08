package de.osthus.ant.task.readdotclasspath;

import java.io.ByteArrayInputStream;

import org.junit.Assert;
import org.junit.Test;

import de.osthus.ant.task.createclasspath.entity.Attribute;
import de.osthus.ant.task.createclasspath.entity.Classpath;
import de.osthus.ant.task.createclasspath.entity.ClasspathEntry;
import de.osthus.ant.task.createclasspath.entity.ClasspathEntry.Kind;
import de.osthus.ant.task.util.DotClasspathReader;


public class TestDotClasspathReader {

	@Test
	public void testSerializeDotClasspath() {		
		Classpath originalDummy = creatDummyClasspath();		
		String serializedDummy = DotClasspathReader.serializeDotClasspath(originalDummy);		
		Classpath deserializedDummy = DotClasspathReader.readDotClasspath(new ByteArrayInputStream(serializedDummy.getBytes()));
		Assert.assertEquals(originalDummy.getClasspathEntries().size(), deserializedDummy.getClasspathEntries().size()); 
	}
	
	@Test
	public void testLoadSampleClasspathFile() {
		Classpath classpath = DotClasspathReader.readDotClasspath(Thread.currentThread().getContextClassLoader().getResourceAsStream("sample-1.classpath"));		
		Assert.assertNotNull(classpath);
		Assert.assertEquals(classpath.getClasspathEntries().size(), 8);
	}
	
	
	private Classpath creatDummyClasspath() {
		Classpath dummy = new Classpath();
		
		ClasspathEntry cpe1 = new ClasspathEntry();
		cpe1.setPath("/foo/bar/test");
		cpe1.setCombineaccessrules(true);
		cpe1.setExported(true);
		cpe1.setKind(Kind.output);
		
		Attribute a1 = new Attribute();
		a1.setName("foo");
		a1.setValue("bar");
		
		cpe1.getAttributes().add(a1);
		
		dummy.getClasspathEntries().add(cpe1);
		
		return dummy;
	}
}
