package com.koch.ambeth.filter;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import com.koch.ambeth.service.merge.model.IObjRef;

@XmlType
@XmlAccessorType(XmlAccessType.FIELD)
public class PagingResponse<T> implements IPagingResponse<T>
{
	@XmlElement(name = "Number")
	protected int number;

	@XmlElement(name = "TotalNumber")
	protected int totalNumber;

	@XmlElement(name = "TotalSize", required = true)
	protected int totalSize;

	protected List<T> result;

	protected List<IObjRef> refResult;

	@Override
	public int getNumber()
	{
		return number;
	}

	public void setNumber(int number)
	{
		this.number = number;
	}

	public PagingResponse<T> withNumber(int number)
	{
		setNumber(number);
		return this;
	}

	@Override
	public int getSize()
	{
		if (refResult != null)
		{
			return refResult.size();
		}
		else if (result != null)
		{
			return result.size();
		}
		return 0;
	}

	@Override
	public int getTotalNumber()
	{
		return totalNumber;
	}

	public void setTotalNumber(int totalNumber)
	{
		this.totalNumber = totalNumber;
	}

	public PagingResponse<T> withTotalNumber(int totalNumber)
	{
		setTotalNumber(totalNumber);
		return this;
	}

	@Override
	public int getTotalSize()
	{
		return totalSize;
	}

	public void setTotalSize(int totalSize)
	{
		this.totalSize = totalSize;
	}

	public PagingResponse<T> withTotalSize(int totalSize)
	{
		setTotalSize(totalSize);
		return this;
	}

	@Override
	public List<T> getResult()
	{
		return result;
	}

	@Override
	public void setResult(List<T> result)
	{
		this.result = result;
	}

	public IPagingResponse<T> withResult(List<T> result)
	{
		setResult(result);
		return this;
	}

	@Override
	public List<IObjRef> getRefResult()
	{
		return refResult;
	}

	@Override
	public void setRefResult(List<IObjRef> refResult)
	{
		this.refResult = refResult;
	}

	public IPagingResponse<T> withRefResult(List<IObjRef> refResult)
	{
		setRefResult(refResult);
		return this;
	}
}
