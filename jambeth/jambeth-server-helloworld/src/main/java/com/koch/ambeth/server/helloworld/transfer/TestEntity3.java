package com.koch.ambeth.server.helloworld.transfer;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(namespace = "HelloWorld")
@XmlAccessorType(XmlAccessType.FIELD)
public class TestEntity3 extends AbstractEntity
{
	@XmlElement
	protected TestEntity test;

	@XmlElement
	protected String myValue3;

	public TestEntity getTest()
	{
		return test;
	}

	public void setTest(TestEntity test)
	{
		this.test = test;
	}

	public void setMyValue3(String myValue3)
	{
		this.myValue3 = myValue3;
	}

	public String getMyValue3()
	{
		return myValue3;
	}
}
