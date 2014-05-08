/**
 * 
 */
package de.osthus.classbrowser.compare;

/**
 * @author juergen.panser
 * 
 */
public enum CompareStatus {

	// ---- CONSTANTS ----------------------------------------------------------

	NOT_COMPARED("Not compared"), //
	EQUAL("Java and C# classes are equal"), //

	NO_MODULENAME_FOUND("No module name found"), //
	MODULENAME_DIFFERS("Module name differs"), //
	MODULENAME_CASE("Wrong module name case"), //

	NO_MATCHING_JAVA_CLASS_FOUND("No matching JAVA class found"), //
	NO_MATCHING_CSHARP_CLASS_FOUND("No matching C# class found"), //
	
	PUBLIC_METHOD_COUNT_DIFFERS("Public method count differs"), //
	PROTECTED_METHOD_COUNT_DIFFERS("Protected method count differs"), //
	METHOD_COUNT_DIFFERS("Method count differs"), //
	
	INTERFACE_METHOD_NOT_FOUND("Interface method not found"), //
	PUBLIC_METHOD_NOT_FOUND("Public method not found"), //
	INTERNAL_METHOD_NOT_FOUND("Internal method not found"), //
	
	METHOD_NAME_CASE("Wrong method name case"), //
	
	PARAMETER_COUNT_DIFFERS("Parameter count differs"), //
	
	WRONG_TYPE("Wrong type"), //
	
	FIELDS_DIFFER("Fields differ"), //
	
	PATTERN_VIOLATION("Pattern has been violated");

	// ---- VARIABLES ----------------------------------------------------------

	private String label;

	// ---- CONSTRUCTORS -------------------------------------------------------

	/**
	 * Constructor.
	 * 
	 * @param label
	 *            Label
	 */
	CompareStatus(String label) {
		this.label = label;
	}

	// ---- GETTER/SETTER METHODS ----------------------------------------------

	/**
	 * Returns the display label
	 * 
	 * @return the label
	 */
	public String getLabel() {
		return this.label;
	}

	// ---- METHODS ------------------------------------------------------------

	@Override
	public String toString() {
		return getLabel();
	}

}
