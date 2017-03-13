package com.koch.ambeth.filter;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

@XmlType
@XmlAccessorType(XmlAccessType.FIELD)
public class PagingRequest implements IPagingRequest
{
	@XmlElement(name = "Number", required = true)
	protected int number;

	@XmlElement(name = "Size", required = true)
	protected int size;

	@Override
	public int getNumber()
	{
		return number;
	}

	/**
	 * 0 based paging index.
	 * 
	 * @param number
	 */
	public void setNumber(int number)
	{
		this.number = number;
	}

	/**
	 * 0 based paging index.
	 * 
	 * @param number
	 */
	public PagingRequest withNumber(int number)
	{
		setNumber(number);
		return this;
	}

	@Override
	public int getSize()
	{
		return size;
	}

	/**
	 * @param size
	 *            Max. item count per page
	 */
	public void setSize(int size)
	{
		this.size = size;
	}

	/**
	 * @param size
	 *            Max. item count per page
	 */
	public PagingRequest withSize(int size)
	{
		setSize(size);
		return this;
	}
}
