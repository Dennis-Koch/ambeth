/**
 * 
 */
package de.osthus.classbrowser.java;

/**
 * @author juergen.panser
 * 
 */
public enum ExitCode {

	// ---- CONSTANTS ----------------------------------------------------------

	SUCCESS(0, "Success"), //
	ERROR(1, "Error");

	// ---- VARIABLES ----------------------------------------------------------

	private int code;
	private String label;

	// ---- CONSTRUCTORS -------------------------------------------------------

	/**
	 * Constructor.
	 * 
	 * @param label
	 *            Label
	 */
	ExitCode(int code, String label) {
		this.code = code;
		this.label = label;
	}

	// ---- GETTER/SETTER METHODS ----------------------------------------------


	/**
	 * Returns the code
	 * 
	 * @return the code
	 */
	public int getCode() {
		return this.code;
	}

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
		return "[" + getCode() + "] " + getLabel();
	}
}
