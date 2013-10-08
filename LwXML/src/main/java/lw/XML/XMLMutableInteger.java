package lw.XML;

/**
  * This class encapsulates the common processing of a database action - e.g. an insert, update or delete
  * @author Liam Wade
  * @version 1.0 10/10/2002
  */
public class XMLMutableInteger {
	/**
	   * constructor.
	   */
	public XMLMutableInteger(int i) {
		this.i = i;
	}

	public int intValue() {
		return i;
	}

	public int add(int increment) {
		i += increment;
		return i;
	}

	private int i = 0;
}

