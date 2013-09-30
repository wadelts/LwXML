package lw.XML;

import java.util.Properties;

/**
  * Encapsulates a TAG/value pair.
  * @author Liam Wade
  * @version 1.0 20/09/2002
  */
public class LwXMLTagValue {
	private String pathToName = null;	// the path to the TAG, including the TAG's name e.g Applic.Auditing.AuditKeys.KeyName
	private String value = null;		// the value retrieved for the TAG
	private Properties attributes = null;	// the list of attribute names and values for the TAG

  /**
    * Will create a new exception with the given reason.
    *
	* @param pathToName the path to the TAG, including the TAG's name e.g Applic.Auditing.AuditKeys.KeyName
	* @param the value retrieved for the TAG
    */
	public LwXMLTagValue(String pathToName, String value) {
		checkNullArgument(pathToName);

		this.pathToName = pathToName;
		this.value = value;
	}

	/**
	  * Return the Name for the TAG.
	  * @return the Name for the TAG
	  */
	public String getTagName() {

		int i = pathToName.lastIndexOf('/');  // returns -1 if no match
		if (i >= 0 && i < (pathToName.length()-1)) { // then has both path and name, so extract name
			return pathToName.substring(i + 1);
		}
		else {
			return pathToName;
		}
	}

	/**
	  * Return the Path to the TAG, but not the TAG's name itslef.
	  * @return the Path to the TAG, null if no path was supplied
	  */
	public String getPathToParent() {

		int i = pathToName.lastIndexOf('/');  // returns -1 if no match
		if (i > 0) { // then has a path, so extract path
			return pathToName.substring(0, i);
		}
		else {
			return null;
		}
	}

	/**
	  * Return the Path to the TAG, including the TAG name.
	  * @return the Path to the TAG, null if no path was supplied
	  */
	public String getPathToName() {
		return pathToName;
	}

	/**
	  * Return the Path to the TAG, excluding the TAG name, and excluding first element of path.
	  * @return the shortened Path to the Name (excl), null, if path only had one element
	  */
	public String getPathToParentLessFirstElement() {
		String path = getPathToParent();

		if (path == null) {
			return path;
		}

		int i = path.indexOf('/');  // returns -1 if no match

		if (i > 0 && path.length() >= 3) { // then has at least two elements in the path (and is valid), so extract first element in path
			return path.substring(i+1);
		}
		else { // "remove" only element
			return null;
		}
	}

	/**
	  * Return the Path to the TAG, including the TAG name, but excluding first element of path.
	  * @return the shortened Path to the Name (incl), the name only, if path only had one element
	  */
	public String getPathToNameLessFirstElement() {
		int i = pathToName.indexOf('/');  // returns -1 if no match

		if (i > 0 && pathToName.length() >= 3) { // then has a path (and is valid), so extract first element in path
			return pathToName.substring(i+1);
		}
		else {
			return pathToName;
		}
	}

	/**
	  *
	  * Return the value for the TAG.
	  * @return the value for the TAG
	  */
	public String getTagValue() {
		return value;
	}

	/**
	  *
	  * Return the Value for named aattribute for the TAG.
	  *
	  * @param newAttributesSet the new set for the TAG
	  * @return the a String containing the attribute's value, null if not found
	  */
	public String getAttributeValue(String attrName) {
		if (attributes == null) {
			return null;
		}

		return attributes.getProperty(attrName);
	}

	/**
	  *
	  * Return the attribute Name/Value pairs for the TAG.
	  *
	  * @return the a Properties object with the attribute Name/Value pairs
	  */
	public Properties getAttributeSet() {
		return attributes;
	}

  /**
    * Set list of attribute names and values for the TAG
    *
	* @param newAttributesSet the new set for the TAG
    */
	public void setAttributeSet(Properties newAttributesSet) {
		this.attributes = newAttributesSet;
	}

	/**
	 * @param o the object to be checked for null.
	 * 
	 * @throws IllegalArgumentException if o is null
	 */
	private void checkNullArgument(Object o) {
		if ((o == null)) throw new IllegalArgumentException("[" + Thread.currentThread().getName() + "]: Null value received.");
	}
}