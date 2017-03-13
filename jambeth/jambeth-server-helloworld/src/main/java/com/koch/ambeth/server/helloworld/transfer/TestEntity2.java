package com.koch.ambeth.server.helloworld.transfer;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(namespace = "HelloWorld")
@XmlAccessorType(XmlAccessType.FIELD)
public class TestEntity2 extends AbstractEntity
{
	@XmlElement
	protected int myValue2;

	public void setMyValue2(int myValue2)
	{
		this.myValue2 = myValue2;
	}

	public int getMyValue2()
	{
		return myValue2;
	}
}
