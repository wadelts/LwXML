package lw.XML;

/**
  * Encapsulates exceptions resulting from errors returned from XML activity.
  * @author Liam Wade
  * @version 1.0 25/09/2002
  */
public class XMLException extends Exception
{
  /**
    * Will create a new exception.
    */
	public XMLException() {
	}

  /**
    * Will create a new exception with the given reason.
	* @param reason the text explaining the error
    */
	public XMLException(String reason) {
		super(reason);
	}
}