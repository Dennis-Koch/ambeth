package de.osthus.ambeth.helloworld.vo;


public class TestEntity3VO extends AbstractEntityVO
{
	protected TestEntityVO test;

	protected String myValue3;

	public TestEntityVO getTest()
	{
		return test;
	}

	public void setTest(TestEntityVO test)
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
