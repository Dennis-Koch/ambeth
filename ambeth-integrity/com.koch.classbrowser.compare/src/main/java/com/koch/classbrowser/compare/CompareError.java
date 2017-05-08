/**
 * 
 */
package com.koch.classbrowser.compare;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * Holds the compare detail information for a single compare mismatch/error.
 * 
 * @author juergen.panser
 */
public class CompareError
{

	// ---- VARIABLES ----------------------------------------------------------

	private CompareStatus status;

	private String additionalInformation;

	// ---- CONSTRUCTORS -------------------------------------------------------

	/**
	 * Create a new instance.
	 * 
	 * @param status
	 *            CompareStatus; mandatory
	 */
	public CompareError(CompareStatus status)
	{
		this(status, null);
	}

	/**
	 * Create a new instance.
	 * 
	 * @param status
	 *            CompareStatus; mandatory
	 * @param additionalInformation
	 *            Additional information; optional
	 */
	public CompareError(CompareStatus status, String additionalInformation)
	{
		if (status == null)
		{
			throw new IllegalArgumentException("Status has to be set!");
		}
		setStatus(status);
		setAdditionalInformation(additionalInformation);
	}

	// ---- GETTER/SETTER METHODS ----------------------------------------------

	public CompareStatus getStatus()
	{
		return status;
	}

	public void setStatus(CompareStatus status)
	{
		this.status = status;
	}

	public String getAdditionalInformation()
	{
		return additionalInformation;
	}

	public void setAdditionalInformation(String additionalInformation)
	{
		this.additionalInformation = additionalInformation;
	}

	// ---- METHODS ------------------------------------------------------------

	@Override
	public String toString()
	{
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).append("status", this.status)
				.append("additional information", this.additionalInformation).toString();
	}

}
