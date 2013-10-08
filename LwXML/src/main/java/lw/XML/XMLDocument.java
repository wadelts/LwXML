package lw.XML;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Properties;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * Encapsulates an XML Document, with additional processing services.
 * @author Liam Wade
 * @version 1.0 20/09/2002
 * @version 1.1 30/09/2013 Refactored to use static creation methods.
 * @version 1.2 07/10/2013 Refactored to use xpath.
 */
public class XMLDocument {
	public static final boolean SCHEMA_VALIDATION_ON = true;	// for use with the validateAgainstSchema attribute below
	public static final boolean SCHEMA_VALIDATION_OFF = false;	// for use with the validateAgainstSchema attribute below

	private Document doc = null;		// the XML document
	private Node currentNode = null;	// the current node within the doc
	private Node previousNode = null;	// the node that was current before currentNode was last set

	private static final String JAXP_SCHEMA_LANGUAGE = "http://java.sun.com/xml/jaxp/properties/schemaLanguage";
	private static final String W3C_XML_SCHEMA = "http://www.w3.org/2001/XMLSchema";
	private static final String JAXP_SCHEMA_SOURCE = "http://java.sun.com/xml/jaxp/properties/schemaSource";

	private final XPath xpath;
	/**
	   * Default constructor.
	   */
	public XMLDocument() {
		// Get new XPath instance for running queries.
		xpath = XPathFactory.newInstance().newXPath();
	}

	/**
	   * Default constructor.
	   */
	private XMLDocument(Document doc) {
		assert doc != null;
		this.doc = doc;
		setCurrentNodeToFirstElement();
		
		// Get new XPath instance for running queries.
		xpath = XPathFactory.newInstance().newXPath();
	}

	/**
	  *
	  * Create a new document from the XML file supplied.
	  * @param fileName the path and name of the file containing the XML to be parsed and loaded
	  * @param validateAgainstSchema if true, validate the XML against a Schema
	  * @throws XMLException
	  */
	public static XMLDocument createDocFromFile(String fileName, boolean validateAgainstSchema)
											throws XMLException {
		return createDocFromFile(fileName, validateAgainstSchema, null, null);
	}

	/**
	  *
	  * Create a new document from the XML file supplied.
	  * @param fileName the path and name of the file containing the XML to be parsed and loaded
	  * @param validateAgainstSchema if true, validate the XML against a Schema
	  * @param schemaSourceFile nominate a schema definition file against which to validate, ignored if null
	  * @param schemaLanguage can overwrite the default Schema language of JAXP_SCHEMA_LANGUAGE below, ignored if null
	  * @throws XMLException
	  */
	public static XMLDocument createDocFromFile(String fileName, boolean validateAgainstSchema, String schemaSourceFile, String schemaLanguage)
											throws XMLException {

		checkNullArgument(fileName);
		
		XMLDocument newLwDoc = null;
		try {
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();

			// Turn on Schema-based validation...
			if (validateAgainstSchema) {
				docFactory.setNamespaceAware(true);
				docFactory.setValidating(true);
				try {
					if (schemaLanguage == null) { // then use default - some language always required
						schemaLanguage = W3C_XML_SCHEMA;
					}
					docFactory.setAttribute(JAXP_SCHEMA_LANGUAGE, schemaLanguage);

					if (schemaSourceFile != null) { // then
						docFactory.setAttribute(JAXP_SCHEMA_SOURCE, new File(schemaSourceFile));
					}
				}
				catch (IllegalArgumentException x) {
					// Happens if the parser does not support JAXP 1.2
					throw new XMLException("XMLDocument.createDocFromFile: Failed to Parse: " + x.getMessage());
				}
			}



			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

			Document newDoc = docBuilder.parse(fileName);
			newLwDoc = new XMLDocument(newDoc);
		}
		catch(SAXParseException err) {
			throw new XMLException("XMLDocument.createDocFromFile: Parsing error" + ", line " + err.getLineNumber() + ", uri " + err.getSystemId() + "=>" + err.getMessage ());
		}
		catch(SAXException e) {
			throw new XMLException("XMLDocument.createDocFromFile: " + e.getMessage());
		}
		catch (Throwable t) {
			throw new XMLException("XMLDocument.createDocFromFile: Unknown exception " + t.getMessage());
		}

		return newLwDoc;
	}

	/**
	  *
	  * Create a new document from the XML text String supplied.
	  * @param xmlText the actual XML
	  * @param validateAgainstSchema if true, validate the XML against a Schema
	  * @throws XMLException
	  */
	public static XMLDocument createDoc(String xmlText, boolean validateAgainstSchema)
											throws XMLException {
		return createDoc(xmlText, validateAgainstSchema, null, null);
	}

	/**
	  *
	  * Create a new document from the XML text String supplied.
	  * @param xmlText the actual XML
	  * @param validateAgainstSchema if true, validate the XML against a Schema
	  * @param schemaSourceFile nominate a schema definition file against which to validate, ignored if null
	  * @param schemaLanguage can overwrite the default Schema language of JAXP_SCHEMA_LANGUAGE below, ignored if null
	  * @throws XMLException
	  */
	public static XMLDocument createDoc(String xmlText, boolean validateAgainstSchema, String schemaSourceFile, String schemaLanguage)
											throws XMLException {

		checkNullArgument(xmlText);
		
		XMLDocument newLwDoc = null;
		try {
			DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();

			// Turn on Schema-based validation...
			if (validateAgainstSchema) {
				docFactory.setNamespaceAware(true);
				docFactory.setValidating(true);
				try {
					if (schemaLanguage == null) { // then use default - some language always required
						schemaLanguage = W3C_XML_SCHEMA;
					}
					docFactory.setAttribute(JAXP_SCHEMA_LANGUAGE, schemaLanguage);

					if (schemaSourceFile != null) { // then
						docFactory.setAttribute(JAXP_SCHEMA_SOURCE, new File(schemaSourceFile));
					}
				}
				catch (IllegalArgumentException x) {
					// Happens if the parser does not support JAXP 1.2
					throw new XMLException("XMLDocument.createDocFromFile: Failed to Parse: " + x.getMessage());
				}
			}



			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

			// Note: had to tell getBytes to return "UTF8", otherwise French chars would parse
			// These chars were in the message of an SQL exception from Derby (primary key error),
			// on my French laptop. 
			Document newDoc = docBuilder.parse(new InputSource(new ByteArrayInputStream(xmlText.getBytes("UTF8"))));
			newLwDoc = new XMLDocument(newDoc);

			// set the current node to the first node
			newLwDoc.setCurrentNodeToFirstElement();
		}
		catch(SAXParseException err) {
			throw new XMLException("XMLDocument.createDoc: Parsing error" + ", line " + err.getLineNumber() + ", uri " + err.getSystemId() + "=>" + err.getMessage ());
		}
		catch(SAXException e) {
			throw new XMLException("XMLDocument.createDoc: " + e.getMessage());
		}
		catch (Throwable t) {
			throw new XMLException("XMLDocument.createDoc: Unknown exception " + t.getMessage() + ": xmlText was " + xmlText);
		}

		return newLwDoc;
	}

	/**
	  *
	  * Import a copy of the children of the parentForeignNode to this document and add as children to the currentNode,
	  * leaving the old node in the foreign doc.
	  *
	  * @param parentForeignNode the node whose children are to be copied into this doc
	  * @param deep true if to bring all children as well
	  * @return the first child Node created and added, otherwise null
	  */
	public Node importNodesChildren(Node parentForeignNode, boolean deep) {
		if (parentForeignNode == null || currentNode == null) {
			return null;
		}

		Node firstNode = null;


		if (parentForeignNode.hasChildNodes()) {
			NodeList nodes = parentForeignNode.getChildNodes();
			int i = 0;
			Node newNode = null;
			while (i < nodes.getLength()) {
				Node childNode = nodes.item(i++);

				if ( (newNode = this.importNode(childNode, deep)) != null) {
					if (firstNode == null) {
						firstNode = newNode;
					}
				}
			}
		} /* end if (strlen(path) == 0) */

		return firstNode;
	}

	/**
	  *
	  * Import a copy of the given foreignNode to this document and add as a child to the currentNode,
	  * leaving the old node in the foreign doc.
	  *
	  * @param foreignNode the node to copy into this doc
	  * @param deep true if to bring all children as well
	  * @return the Node of the tag that was created, otherwise null
	  */
	public Node importNode(Node foreignNode, boolean deep) {
		if (foreignNode == null || currentNode == null) {
			return null;
		}

		Node newNode = null;

		if ( (newNode = doc.importNode(foreignNode, deep)) != null) {
			currentNode.appendChild(newNode);
		}

		return newNode;
	}

	/**
	  *
	  * Add a new TAG, which will contain an aggregate or simple text, creating parent aggregates if they don't exist
	  * e.g add DELV/ORD/ORDER_NUMBER to current Node (which could be, for example, MESSAGE)
	  * @param namespaceURI an optional namespace for the element - leave null to ignore
	  * @param nodePath the fully qualified path, from after the currentNode, EXCLUDING the name of the currentNode itself
	  * @param textValue set the text Value of the new node to this value, if not textValue null
	  * @return the Node of the tag thet was created, otherwise null
	  */
	public Node addElement(String namespaceURI, String nodePath, String textValue) {
		// Perform the action from the root element, if nodePath begins with a / - signifying "start at root"
		if (nodePath != null && nodePath.length() > 0 && nodePath.startsWith("/")) {
			return addElement(doc.getDocumentElement(), namespaceURI, nodePath, textValue);
		}
		else {
			return addElement(this.currentNode, namespaceURI, nodePath, textValue);
		}
	}

	/**
	  *
	  * Add a new TAG, which will contain an aggregate or simple text, creating parent aggregates if they don't exist
	  * e.g add DELV/ORD/ORDER_NUMBER to current Node (which could be, for example, MESSAGE)
	  * @param startNode the node in which to add the TAG, and it's parent(s), if they don't already exist
	  * @param namespaceURI an optional namespace for the element - leave null to ignore
	  * @param nodePath the fully qualified path, from after the startNode, EXCLUDING the name of the startNode
	  * @param textValue set the text Value of the new node to this value, if not textValue null
	  * @return the Node of the tag thet was created, otherwise null
	  */
	public Node addElement(Node startNode, String namespaceURI, String nodePath, String textValue) {
		if (startNode == null || nodePath == null) {
			return null;
		}

		
		XMLTagValue tv = new XMLTagValue(nodePath, textValue);
		Node parentNodeOfNewTag = startNode;  // default, for when nodePath just contains a TAG name (i.e no path)

		if (nodePath.contains("/")) { // then is path + tag name, so will make sure parent exists, and set that in parentNodeOfNewTag
			// get path only
			String backAlevel = tv.getPathToParent();
			if ( (parentNodeOfNewTag = findNthElementByPath(startNode, startNode.getNodeName() + "/" + backAlevel, 1)) == null) { // then aggregate does not exist, so create it...
				parentNodeOfNewTag = addElement(startNode, namespaceURI, backAlevel, null);
			}
		}

		// if got here, time to add the tag to the parentNodeOfNewTag
		Node newNode = doc.createElementNS(namespaceURI, tv.getTagName());
		if (textValue != null) {
			newNode.setTextContent(tv.getTagValue());
		}
		parentNodeOfNewTag.appendChild(newNode);

		return newNode;
	}

	/**
	  *
	  * Return the current Node.
	  * @return the current Node
	  */
	public Node getCurrentNode() {
		return currentNode;
	}

	/**
	  *
	  * Return the current Node's name.
	  * @return the name of the current Node, null if currentNode not set
	  */
	public String getCurrentNodeName() {
		if (currentNode != null) {
			return currentNode.getNodeName();
		}
		else {
			return null;
		}
	}

	/**
	  *
	  * Set the currentNode
	  * @param currentNode the node that will become the current node
	  */
	public void setCurrentNode(Node newCurrentNode) {
		previousNode = this.currentNode;
		this.currentNode = newCurrentNode;
	}

	/**
	  *
	  * Set the currentNode back to the value saved in previousNode, if previousNode is not null
	  */
	public void restoreCurrentNode() {
		if (previousNode != null) {
			this.currentNode = previousNode;
			previousNode = null;
		}
	}

	/**
	  *                          setCurrentNodeByPath
	  *  Calls the findNthElementByPath, supplying the currentNode as the position from which to start the search,
	  *  or supplying the root node as the starting position if the nodePath begins with "/", then
	  *  sets the currentNode to the found node, relegating currentNode to the previousNode holder.
	  *
	  *  The 'nthOccurance' of the searchNodeName in the path will be the one
	  *  returned. If the name is not found at the lowest aggregate first reached,
	  *  the process will "back up" and go down any alternative paths, as
	  *  determined by the 'nodePath'.
	  *
	  * @param nodePath the path to follow to the required node e.g. MESSAGE/INFOSERVE/ORD/SERIAL_NO or /MESSAGE/INFOSERVE/ORD/SERIAL_NO
	  * @param nthOccurance the match of the searched-for node to be returned (null will be returned if less than n occurrences exist)
	  * @return true if found node, otherwise false, and the original currentNode stands
	  */
	public boolean setCurrentNodeByPath(String nodePath, int nthOccurance) {

		Node foundNode = findNthElementByPath(nodePath, nthOccurance);

		if (foundNode == null) {
			return false;
		}
		else {
			previousNode = currentNode;
			currentNode = foundNode;

			return true;
		}
	}

	/**
	  *
	  * Set the currentNode to be the parent of the current one
	  */
	public void setCurrentNodeToParentOfCurrent() {
		if (this.currentNode != null) {
			previousNode = this.currentNode;
			this.currentNode = currentNode.getParentNode();
		}
	}

	/**
	  *
	  * Set the Text Content of the current node
	  *
	  * Note: if the currentNode has children, these will all be removed
	  *
	  * @param textContent the new text value. null or "" will remove the current Text (or children)
	  */
	public void setTextContentForCurrentNode(String textContent) {
		if (currentNode == null) {
			return;
		}

		currentNode.setTextContent(textContent);
	}

	/**
	  *                          findNthElementByPath
	  *
	  * Just call findNthElementByPath for current node, or from root, if nodePath begins with a "/"
	  *
	  * @param currentNode the node from which to commence search
	  * @param nodePath the path to follow to the required node e.g. MESSAGE/INFOSERVE/ORD/SERIAL_NO or MESSAGE/INFOSERVE/ORD/SERIAL_NO
	  * @param nthOccurance the match of the searched-for node to be returned (null will be returned if less than n occurrences exist)
	  * @return the found node, null if not found
	  */
	public Node findNthElementByPath(String nodePath, int nthOccurance) {
		// Perform the action from the root element, if nodePath begins with a / - signifying "start at root"
		if (nodePath != null && nodePath.length() > 0 && nodePath.startsWith("/")) {
			return findNthElementByPath(doc.getDocumentElement(), nodePath, nthOccurance);
		}
		else {
			return findNthElementByPath(this.currentNode, nodePath, nthOccurance);
		}
	}

	/**
	  *                          findNthElementByPath
	  *  Finds the nth ELEMENT whose name is supplied in nodePath, starting
	  *  the search at the supplied fromNode and following the path down the tree,
	  *  as denoted by the nodePath.
	  *  
	  *  Note:
	  *  	To allow searching for nth child of the WHOLE SET of a particular element,
	  *  	we need to surround the child and repeating-parent elements in brackets and split out the search path.
	  *  	For example, searching for the 3rd COLUMNS element with /MESSAGE/FILE_REQUEST/TABLE/ROW/COLUMNS
	  *  	will only return node(s) if any ROW element has three COLUMNS children.
	  *  	If, in fact, it is the ROWS element that repeats, each having a single child COLUMNS element,
	  *  	then the search path must be /MESSAGE/FILE_REQUEST/TABLE/(ROW/COLUMNS) and we must submit two
	  *  	queries via xpath (a single query based on this path will produce nothing.)
	  *  	So, we find the node of the element BEFORE the brackets (here it is /MESSAGE/FILE_REQUEST/TABLE)
	  *  	and then submit a search for the nth COLUMNS element in the set of all COLUMNS children of ALL
	  *  	ROWS with the path set to (ROW/COLUMNS) .
	  *
	  * @param fromNode the node from which to commence search
	  * @param nodePath the path to follow to the required node e.g. MESSAGE/INFOSERVE/ORD/SERIAL_NO
	  * @param nthOccurrence the match of the searched-for node to be returned (null will be returned if less than n occurrences exist)
	  * @return the found node, null if not found
	  */
	public Node findNthElementByPath(Node fromNode, String nodePath, int nthOccurrence) {
		if (nodePath == null) return null;
		
		XPathExpression xpathExpr;
		Node foundNode = null;
		String[] splitPath = splitRepeatingElements(nodePath);
		try {
			if (splitPath.length > 1) { //then found bracket(s), so must go to node BEFORE first bracket
				xpathExpr = xpath.compile(splitPath[0]);
				fromNode = (Node) xpathExpr.evaluate(fromNode, XPathConstants.NODE);
				nodePath = splitPath[1];
			}
			xpathExpr = xpath.compile(nodePath + "[position()=" + nthOccurrence + "]");
			foundNode = (Node) xpathExpr.evaluate(fromNode, XPathConstants.NODE);
		} catch (XPathExpressionException e) {
			// Will just return null.
		}

		return foundNode;
	}

	/**
	  *  Splits out a path based on left brackets (assuming there is a matching closing bracket)
	  *
	  * @param nodePath the path to follow to the required node e.g. MESSAGE/INFOSERVE/ORD/SERIAL_NO
	  * @return the path split into first part and second part ( it being surrounded by brackets)
	  */
	private String [] splitRepeatingElements(String nodePath) {
		String[] splitPath;
		// First, just return if nodePath itself if it is totally surrounded by brackets
		if (nodePath.startsWith("(") ) {
			splitPath = new String[1];
			splitPath[0] = nodePath;
			return splitPath;
		}
		
		splitPath = nodePath.split("\\(");
		if (splitPath.length > 1) { //then found bracket(s)
			// Remove final "/" from first part...
			if (splitPath[0].endsWith("/")) {
				splitPath[0] = splitPath[0].substring(0, splitPath[0].length()-1);
			}
			
			// Add back in bracket to all but first element
			for (int i=1; i < splitPath.length; i++) {
				splitPath[i] = "(" + splitPath[i];
			}
		}
		
		return splitPath;
	}

	/**
	  *                          numElements
	  *  Finds the number of nodes found whose name is supplied in nodePath, starting
	  *  the search at the root element, if nodePath begins with a / - signifying "start at root".
	  *
	  * @param nodePath the path to follow to the required node e.g. MESSAGE/INFOSERVE/ORD/SERIAL_NO
	  * @return the number of nodes found to match
	  */
	public int numElements(String nodePath) {
		// Perform the action from the root element, if nodePath begins with a / - signifying "start at root"
		if (nodePath != null && nodePath.length() > 0 && nodePath.startsWith("/")) {
			return numElements(doc.getDocumentElement(), nodePath);
		}
		else {
			return numElements(this.currentNode, nodePath);
		}
	}

	/**
	  *                          numElements
	  *  Finds the number of nodes found whose name is supplied in nodePath, starting
	  *  the search at the supplied fromNode and following the path down the tree,
	  *  as denoted by the nodePath.
	  *
	  * @param fromNode the node from which to commence search
	  * @param nodePath the path to follow to the required node e.g. MESSAGE/INFOSERVE/ORD/SERIAL_NO
	  * @return the number of nodes found to match
	  */
	public int numElements(Node fromNode, String nodePath) {
		XPathExpression xpathExpr;
		NodeList foundNodeList = null;
		try {
			xpathExpr = xpath.compile(nodePath);
			foundNodeList = (NodeList) xpathExpr.evaluate(fromNode, XPathConstants.NODESET);
		} catch (XPathExpressionException e) {
			// Will just return 0.
		}

		return (foundNodeList == null ? 0 : foundNodeList.getLength());
	}

	/**
	/**
	  *
	  * Return the value for the first occurrence of the given tag.
	  * The search will start from the currentNode
	  * @param nodePath the fully qualified path to the TAG, e.g. MESSAGE/ORDER/ORDER_NUMBER
	  * @return the tag's value, null if not found
	  */
	public String getValueForTag(String nodePath) {
		// Perform the action from the root element, if nodePath begins with a / - signifying "start at root"
		if (nodePath != null && nodePath.startsWith("/")) {
			return getValueForTag(doc.getDocumentElement(), nodePath);
		}
		else {
			return getValueForTag(this.currentNode, nodePath);
		}
	}

	/**
	  *
	  * Return the value for the first occurrence of the given tag.
	  * @param searchNode the node from which to commence search, which will to match the first part of the nodePath
	  * @param nodePath the fully qualified path to the TAG, e.g. MESSAGE/ORDER/ORDER_NUMBER
	  * @return the tag's value, null if not found
	  */
	public String getValueForTag(Node searchNode, String nodePath) {

		if (searchNode == null || nodePath == null) {
			return null;
		}

		Node foundNode = findNthElementByPath(searchNode, nodePath, 1);

		String foundValue = null;

		if (foundNode != null) {
			foundValue =  foundNode.getTextContent();
		}

		return foundValue;
	}

	/**
	  *
	  * Return the values for ALL occurrences of the given path.
	  * @param searchNode the node from which to commence search, which will to match the first part of the nodePath
	  * @param nodePath the fully qualified path to the TAG, e.g. MESSAGE/ORDER/ORDER_NUMBER
	  * @return the a Vector of LwXMLTagValue's representing the values for all TAGs matching nodePath, an empty Vector if no matches
	  */
	public Vector<XMLTagValue> getValuesForTag(String nodePath) {
		// Perform the action from the root element, if nodePath begins with a / - signifying "start at root"
		if (nodePath != null && nodePath.length() > 0 && nodePath.startsWith("/")) {
			return getValuesForTag(doc.getDocumentElement(), nodePath);
		}
		else {
			return getValuesForTag(this.currentNode, nodePath);
		}
	}

	/**
	  *
	  * Return the values for ALL occurrences of the given path.
	  * @param searchNode the node from which to commence search, which will match on the first part of the nodePath
	  * @param nodePath the fully qualified path to the TAG, e.g. MESSAGE/ORDER/ORDER_NUMBER
	  * @return the a Vector of Strings representing the values for all TAGs matching nodePath, but only for the first set encountered
	  */
	public Vector<XMLTagValue> getValuesForTag(Node searchNode, String nodePath) {

		if (searchNode == null || nodePath == null) {
			return null;
		}

		Vector<XMLTagValue> values = new Vector<XMLTagValue>();

		getAllElementValuesByPath(searchNode, nodePath, values, nodePath);


		return values;
	}

	/**
	  * Get the tag Names and Text Values for all ELEMENT children of the current node
	  *
	  * @return the a Vector of LwXMLTagValue's representing the values for all child TAGs
	  */
	public Vector<XMLTagValue> getValuesForTagsChildren() {
		return getValuesForTagsChildren(this.currentNode);
	}

	/**
	  *
	  * Get the tag Names and Values for all ELEMENT children of this node
	  *
	  * @return the a Vector of LwXMLTagValue's representing the values for all child TAGs
	  */
	public static Vector<XMLTagValue> getValuesForTagsChildren(Node parentNode) {
		if (parentNode == null) {
			return null;
		}

		Vector<XMLTagValue> values = new Vector<XMLTagValue>();

		XPath xp = XPathFactory.newInstance().newXPath();
		XPathExpression xpathExpr;
		NodeList elements;
		try {
			xpathExpr = xp.compile("*");
			elements = (NodeList) xpathExpr.evaluate(parentNode, XPathConstants.NODESET); 
		} catch (XPathExpressionException e) {
			return values; 
		}

		for(int i=0; i < elements.getLength(); ++i) {
		    String name = elements.item(i).getNodeName();
		    String value = elements.item(i).getTextContent();
			if ( value != null ) {
				XMLTagValue tv = new XMLTagValue(name, value);
				values.addElement(tv);
			}
		}
		
		return values;
	}

	/**
	  *
	  * Return the value for the first occurrence of the given tag, plus it's attributes.
	  *
	  * The search will start from the currentNode
	  * @param nodePath the fully qualified path to the TAG, e.g. MESSAGE/ORDER/ORDER_NUMBER
	  * @return the tag's value and it's attribute set in an LwXMLTagValue object
	  */
	public XMLTagValue getValueForTagPlusAttributes(String nodePath) {
		// Perform the action from the root element, if nodePath begins with a / - signifying "start at root"
		if (nodePath != null && nodePath.length() > 0 && nodePath.startsWith("/")) {
			return getValueForTagPlusAttributes(doc.getDocumentElement(), nodePath);
		}
		else {
			return getValueForTagPlusAttributes(this.currentNode, nodePath);
		}
	}

	/**
	  *
	  * Return the value for the first occurrence of the given tag, plus it's attributes.
	  *
	  * The search will start from the supplied searchNode
	  * @param searchNode the node from which to commence search, which will to match the first part of the nodePath
	  * @param nodePath the fully qualified path to the TAG, e.g. MESSAGE/ORDER/ORDER_NUMBER
	  * @return the tag's value and it's attribute set in an LwXMLTagValue object, null if nothing found
	  */
	public XMLTagValue getValueForTagPlusAttributes(Node searchNode, String nodePath) {
		if (searchNode == null || nodePath == null) {
			return null;
		}

		Node foundNode = findNthElementByPath(searchNode, nodePath, 1);

		String foundValue = null;
		Properties attributes = null;

		XMLTagValue tv = null;

		if (foundNode != null) {
			foundValue =  foundNode.getTextContent();
			attributes = getAttributeValues(foundNode);
		}

		// Only create a new LwXMLTagValue if something found...
		if (foundValue != null || (attributes != null && attributes.size() > 0) ) {
			tv = new XMLTagValue(nodePath, foundValue);
			tv.setAttributeSet(attributes);
		}

		return tv;
	}

	/**
	  *
	  * Return the value for the first occurrence of the given tag, plus it's attributes.
	  *
	  * The search will start from the supplied searchNode
	  * @param searchNode the node from which to commence search, which will to match the first part of the nodePath
	  * @param nodePath the fully qualified path to the TAG, e.g. MESSAGE/ORDER/ORDER_NUMBER
	  * @return the tag's value and it's attribute set in an LwXMLTagValue object, null if nothing found
	  */
	public XMLTagValue getValueForTagPlusAttributesForCurrentNode() {
		Node foundNode = getCurrentNode();

		String foundValue = null;
		Properties attributes = null;

		XMLTagValue tv = null;

		if (foundNode != null) {
			foundValue =  foundNode.getTextContent();
			attributes = getAttributeValues(foundNode);
		}

		// Only create a new LwXMLTagValue if something found...
		if (foundValue != null || (attributes != null && attributes.size() > 0) ) {
			tv = new XMLTagValue(foundNode.getNodeName(), foundValue);
			tv.setAttributeSet(attributes);
		}

		return tv;
	}

	/**
	  *                          getAllElementValuesByPath
	  *  Finds the values for all ELEMENTs whose name matches nodePath, starting
	  *  the search at the supplied topNode and following the path down the tree,
	  *  as denoted by the nodePath.
	  *  The entire tree will be searched.
	  * Attribute name/value pairs are also collected.
	  *  The special node name * will match any ELEMENT node's name.
	  *
	  *  The nodePath takes the form:
	  *			nodeName_1.nodeName_2.nodeName_n.searchNodeName
	  *
	  *  For example:
	  *			MESSAGE.INFOSERVE.ORD.SERIAL_NO
	  *
	  *
	  * @param fromNode the node from which to commence search
	  * @param nodePath the path to follow to the required node (which will sometimes be a partial path of the originalNodePath during recursion) e.g. MESSAGE/INFOSERVE/ORD/SERIAL_NO
	  * @param results the set of values will be collected in this Vector
	  * @param originalNodePath the path to follow to the required node, which will not be altered during processing e.g. MESSAGE/INFOSERVE/ORD/SERIAL_NO
	  */
	public void getAllElementValuesByPath(Node fromNode, String nodePath, Vector<XMLTagValue> results, String originalNodePath) {

		// Ensure nodePath begins with "//"
		if ( nodePath.startsWith("//")) {
			// Perfect, do nothing
		} else if ( nodePath.startsWith("/")) {
			nodePath = "/" + nodePath;
		} else {
			nodePath = "//" + nodePath;
		}
		
		nodePath = "." + nodePath; // "." tells the XML search engine to execute the search relative to the current node reference.

		XPathExpression xpathExpr;
		NodeList nodeList = null;
		try {
			xpathExpr = xpath.compile(nodePath);
			nodeList = (NodeList) xpathExpr.evaluate(fromNode, XPathConstants.NODESET);
		} catch (XPathExpressionException e) {
			// Just add nothing to results
			return;
		}

		if (nodeList != null) {
			for (int i = 0; i < nodeList.getLength(); i++) {
			    String value = nodeList.item(i).getTextContent();
			    if (value != null && value.length() > 0) {
					XMLTagValue tv = new XMLTagValue(originalNodePath, value);
					Properties attributes = getAttributeValues(nodeList.item(i));
					if (attributes.size() > 0) {
						tv.setAttributeSet(attributes);
					}

					results.addElement(tv);
			    }
			}
		}
	}

	/**
	  *
	  * Get attributes for the current node
	  *
	  * @return the a Properties object with the values for all attributes of the currentNode, an empty set if no attributes exist
	  */
	public Properties getAttributeValues() {
		return getAttributeValues(currentNode);
	}

	/**
	  *
	  * Get attributes for the current node
	  *
	  * @param requestNode the node for which attributes are to be retrieved
	  * @return the a Properties object with the values for all attributes of the currentNode, an empty set if no attributes exist
	  */
	public static Properties getAttributeValues(Node requestNode) {
		Properties attributeSet = new Properties();

		if (requestNode == null) {
			return attributeSet;
		}
		
		XPath xp = XPathFactory.newInstance().newXPath();
		XPathExpression NodeListExp;
		NodeList attributeList = null;
		try {
			NodeListExp = xp.compile("@*");
			attributeList = (NodeList) NodeListExp.evaluate(requestNode, XPathConstants.NODESET);
		} catch (XPathExpressionException e) {
			// Just return empty set
			return attributeSet;
		}

		int length = attributeList.getLength();
		for( int i=0; i < length; i++) {
		    Attr attr = (Attr) attributeList.item(i);
		    String name = attr.getName();
		    String value = attr.getValue();
			attributeSet.setProperty(name, value);
		}

		return attributeSet;
	}

	/**
	  *
	  * Output the XML to a file.
	  *
	  * @param fileName the file to which to send the XML
	  */
	public boolean toFile(String fileName, boolean append) {
		//////////////////////////////////////////////////////////////////////////
		// Output the resulting XML...
		//////////////////////////////////////////////////////////////////////////
		Transformer transformer = null;

		try {
			transformer = TransformerFactory.newInstance().newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "no"); // doesn't add indents to new tags, onlt carriage returns!
		}
		catch(TransformerConfigurationException e) {
			return false;
		}

		FileWriter f = null;
		//initialize StreamResult with File object to save to file
		try {
			f = new FileWriter(fileName, append);
		}
		catch(IOException e) {
			return false;
		}

		StreamResult result = new StreamResult(f);
		DOMSource source = new DOMSource(doc);

		try {
			transformer.transform(source, result);
			f.close();
		}
		catch(TransformerException e) {
			return false;
		}
		catch(IOException e) {
			return false;
		}

		try {
			f.close();
		}
		catch(IOException e) {
			return false;
		}

		return true;
	}

	/**
	  *
	  * Place the XML in a String.
	  *
	  * @return the XML as a String
	  */
	public String toString() {
		//////////////////////////////////////////////////////////////////////////
		// Output the resulting XML...
		//////////////////////////////////////////////////////////////////////////
		Transformer transformer = null;

		try {
			transformer = TransformerFactory.newInstance().newTransformer();
			transformer.setOutputProperty(OutputKeys.INDENT, "no"); // doesn't add indents to new tags, onlt carriage returns!
		}
		catch(TransformerConfigurationException e) {
			return null;
		}

		//initialize StreamResult with File object to save to file
		StreamResult result = new StreamResult(new StringWriter());
		DOMSource source = new DOMSource(doc);

		try {
			transformer.transform(source, result);
		}
		catch(TransformerException e) {
			return null;
		}

		String xmlString = result.getWriter().toString();
		return xmlString;
	}

	/**
	  *
	  * Set the currentNode to the first ELEMENT node. e.g ignore comment nodes
	  */
	public void setCurrentNodeToFirstElement() {
		// set the current node to the first node
		previousNode = this.currentNode;
		this.currentNode = doc.getDocumentElement();
	}

	/**
	 * Get the path to a node from the root of the node
	 * @param searchNode the node whose path from root we want extracted
	 * @param txtPath the path as it builds up traversing nodes - supply "" to return path excluding name of searchNode
	 * 
	 * @returns the /-separated path to the node, from the root of the doc (neither end of the string will be /)
	 */
	public static String pathFromRoot(Node searchNode, String txtPath) {
		
		if (searchNode == null || txtPath == null) return "";
		
		Node parent = searchNode.getParentNode();
		if (parent == null || parent == searchNode.getOwnerDocument()) {
			return txtPath;
		} else {
			if ("".equals(txtPath)) {
				txtPath = parent.getNodeName();
			} else {
				txtPath = parent.getNodeName() + "/" + txtPath;
			}
			return pathFromRoot(parent, txtPath);
		}
	}

	/**
	 * @param o the object to be checked for null.
	 * 
	 * @throws IllegalArgumentException if o is null
	 */
	private static void checkNullArgument(Object o) {
		if ((o == null)) throw new IllegalArgumentException("[" + Thread.currentThread().getName() + "]: Null value received.");
	}
}
