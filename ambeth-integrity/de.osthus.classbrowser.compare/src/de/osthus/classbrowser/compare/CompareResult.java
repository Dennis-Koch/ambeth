/**
 * 
 */
package de.osthus.classbrowser.compare;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import de.osthus.classbrowser.java.TypeDescription;

/**
 * Holds the compare result.
 * 
 * @author juergen.panser
 */
public class CompareResult {

	// ---- VARIABLES ----------------------------------------------------------

	private String fullTypeName;

	private TypeDescription javaType;

	private TypeDescription csharpType;

	private List<CompareError> errors;

	// ---- CONSTRUCTORS -------------------------------------------------------

	/**
	 * @param fullTypeName
	 *            Full type name; mandatory
	 */
	public CompareResult(String fullTypeName) {
		this(fullTypeName, null);
	}

	/**
	 * @param fullTypeName
	 *            Full type name; mandatory
	 * @param compareErrors
	 *            List of errors; optional
	 */
	public CompareResult(String fullTypeName, List<CompareError> compareErrors) {
		if (StringUtils.isBlank(fullTypeName)) {
			throw new IllegalArgumentException("Full type name has to be set!");
		}
		setFullTypeName(fullTypeName);
		setErrors(compareErrors);
	}

	// ---- GETTER/SETTER METHODS ----------------------------------------------

	public String getFullTypeName() {
		return fullTypeName;
	}

	private void setFullTypeName(String fullTypeName) {
		this.fullTypeName = fullTypeName;
	}

	public TypeDescription getJavaType() {
		return javaType;
	}

	public void setJavaType(TypeDescription javaType) {
		this.javaType = javaType;
	}

	public TypeDescription getCsharpType() {
		return csharpType;
	}

	public void setCsharpType(TypeDescription csharpType) {
		this.csharpType = csharpType;
	}

	/**
	 * @return List of errors; never null
	 */
	public List<CompareError> getErrors() {
		return errors;
	}

	public void setErrors(List<CompareError> errors) {
		if (errors == null) {
			this.errors = new ArrayList<CompareError>();
		}
		else {
			this.errors = errors;
		}
	}

	// ---- METHODS ------------------------------------------------------------

	/**
	 * @param status
	 *            Compare status; mandatory
	 * @param additionalInformation
	 *            Additional information; optional
	 */
	public void addError(CompareStatus status, String additionalInformation) {
		CompareError error = new CompareError(status, additionalInformation);
		getErrors().add(error);
	}

	public void addError(CompareError... error) {
		if (error != null && error.length > 0) {
			for (CompareError compareError : error) {
				getErrors().add(compareError);
			}
		}
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE).append("full type name", this.fullTypeName).toString();
	}

}
