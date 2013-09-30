package lw.XML;

/**
  * Encapsulates exceptions resulting from errors returned from XML activity.
  * @author Liam Wade
  * @version 1.0 25/09/2002
  */
public class LwXMLException extends Exception
{
  /**
    * Will create a new exception.
    */
	public LwXMLException() {
	}

  /**
    * Will create a new exception with the given reason.
	* @param reason the text explaining the error
    */
	public LwXMLException(String reason) {
		super(reason);
	}
}