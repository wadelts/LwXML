package lw.XML;

import javax.xml.parsers.*;						// For DocumentBuilderFactory, DocumentBuilder, exceptions etc
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;

import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.w3c.dom.*;							// For Document

import javax.xml.transform.stream.StreamResult;

import java.io.*;
import java.util.StringTokenizer;
import java.util.Properties;
import java.util.Vector;

/**
  * Encapsulates an XML Document, with additional processing services.
  * @author Liam Wade
  * @version 1.0 20/09/2002
  * @version 1.1 30/09/2013 Refactored to use static creation methods.
  */
public class XMLDocument_old2 implements ErrorHandler {

	public static final boolean SCHEMA_VALIDATION_ON = true;	// for use with the validateAgainstSchema attribute below
	public static final boolean SCHEMA_VALIDATION_OFF = false;	// for use with the validateAgainstSchema attribute below

	private Document doc = null;		// the XML document
	private Node currentNode = null;	// the current node within the doc
	private Node previousNode = null;	// the node that was current before currentNode was last set

	private boolean tagNamesCaseSensitive = false;	// should searches be tagNamesCaseSensitive?
	private static final String JAXP_SCHEMA_LANGUAGE = "http://java.sun.com/xml/jaxp/properties/schemaLanguage";
	private static final String W3C_XML_SCHEMA = "http://www.w3.org/2001/XMLSchema";
	private static final String JAXP_SCHEMA_SOURCE = "http://java.sun.com/xml/jaxp/properties/schemaSource";


	/**
	   * Default constructor.
	   */
	public XMLDocument_old2() {
	}

	/**
	   * Default constructor.
	   */
	private XMLDocument_old2(Document doc) {
		assert doc != null;
		this.doc = doc;
	}

	/**
	  *
	  * Create a new document from the XML file supplied.
	  * @param fileName the path and name of the file containing the XML to be parsed and loaded
	  * @param validateAgainstSchema if true, validate the XML against a Schema
	  * @throws XMLException
	  */
	public static XMLDocument_old2 createDocFromFile(String fileName, boolean validateAgainstSchema)
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
	public static XMLDocument_old2 createDocFromFile(String fileName, boolean validateAgainstSchema, String schemaSourceFile, String schemaLanguage)
											throws XMLException {

		checkNullArgument(fileName);
		
		XMLDocument_old2 newLwDoc = null;
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
					throw new XMLException("XMLDocument_old2.createDocFromFile: Failed to Parse: " + x.getMessage());
				}
			}



			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

			Document newDoc = docBuilder.parse(fileName);
			newLwDoc = new XMLDocument_old2(newDoc);

			// set the current node to the first node
			newLwDoc.setCurrentNodeToFirstElement();
		}
		catch(SAXParseException err) {
			throw new XMLException("XMLDocument_old2.createDocFromFile: Parsing error" + ", line " + err.getLineNumber() + ", uri " + err.getSystemId() + "=>" + err.getMessage ());
		}
		catch(SAXException e) {
			throw new XMLException("XMLDocument_old2.createDocFromFile: " + e.getMessage());
		}
		catch (Throwable t) {
			throw new XMLException("XMLDocument_old2.createDocFromFile: Unknown exception " + t.getMessage());
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
	public static XMLDocument_old2 createDoc(String xmlText, boolean validateAgainstSchema)
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
	public static XMLDocument_old2 createDoc(String xmlText, boolean validateAgainstSchema, String schemaSourceFile, String schemaLanguage)
											throws XMLException {

		checkNullArgument(xmlText);
		
		XMLDocument_old2 newLwDoc = null;
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
					throw new XMLException("XMLDocument_old2.createDocFromFile: Failed to Parse: " + x.getMessage());
				}
			}



			DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

			// Note: had to tell getBytes to return "UTF8", otherwise French chars would parse
			// These chars were in the message of an SQL exception from Derby (primary key error),
			// on my French laptop. 
			Document newDoc = docBuilder.parse(new InputSource(new ByteArrayInputStream(xmlText.getBytes("UTF8"))));
			newLwDoc = new XMLDocument_old2(newDoc);

			// set the current node to the first node
			newLwDoc.setCurrentNodeToFirstElement();
		}
		catch(SAXParseException err) {
			throw new XMLException("XMLDocument_old2.createDoc: Parsing error" + ", line " + err.getLineNumber() + ", uri " + err.getSystemId() + "=>" + err.getMessage ());
		}
		catch(SAXException e) {
			throw new XMLException("XMLDocument_old2.createDoc: " + e.getMessage());
		}
		catch (Throwable t) {
			throw new XMLException("XMLDocument_old2.createDoc: Unknown exception " + t.getMessage() + ": xmlText was " + xmlText);
		}

		return newLwDoc;
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
	  *
	  * Get the value for a named attribute from the current node
	  *
	  * @param namespaceURI an optional namespace for the attribute - leave null to ignore
	  * @param attrName the name of the attribute whose value is to be returned
	  * @return the value for the attribute
	  */
	public String getAttributeValue(String namespaceURI, String attrName) {
		return getAttributeValue(currentNode, namespaceURI, attrName);
	}

	/**
	  *
	  * Get the value for a named attribute from the supplied node
	  *
	  * @param requestNode the node for which the attribute value is to be retrieved
	  * @param namespaceURI an optional namespace for the attribute - leave null to ignore
	  * @param attrName the name of the attribute whose value is to be returned
	  * @return the value for the attribute as a String
	  */
	public static String getAttributeValue(Node requestNode, String namespaceURI, String attrName) {
		checkNullArgument(requestNode);
		checkNullArgument(attrName);
		

		NamedNodeMap currentNodeAttributes = requestNode.getAttributes();

		Node attributeNode = null;
		if ( (attributeNode = currentNodeAttributes.getNamedItemNS(namespaceURI, attrName)) == null) {
			return null;
		}
		else {
			return attributeNode.getNodeValue();
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
		if (requestNode == null) {
			return null;
		}

		Properties attributeSet = new Properties();

		NamedNodeMap currentNodeAttributes = requestNode.getAttributes();

		for  (int i = 0; i < currentNodeAttributes.getLength(); i++) {
			Node attributeNode = currentNodeAttributes.item(i);
			attributeSet.setProperty(attributeNode.getNodeName(), attributeNode.getNodeValue());
		}

		return attributeSet;
	}

	/**
	  *
	  * Add a new attribute to the current node
	  *
	  * @param namespaceURI an optional namespace for the attribute - leave null to ignore
	  * @param attrName the name of the new attribute
	  * @param attrValue the value for the new attribute
	  * @return true if added, otherwise false
	  */
	public boolean addAttribute(String namespaceURI, String attrName, String attrValue) {
		if (attrName == null || attrValue == null || currentNode == null) {
			return false;
		}

		Attr newAttr = doc.createAttributeNS(namespaceURI, attrName);

		if (newAttr == null) {
			return false;
		}
		else { // add the newly-created attribute
			newAttr.setValue(attrValue);
			NamedNodeMap currentNodeAttributes = currentNode.getAttributes();
			currentNodeAttributes.setNamedItemNS(newAttr);
			return true;
		}
	}

	/**
	  *
	  * Remove the named attribute from the current node
	  *
	  * @param namespaceURI an optional namespace for the attribute - leave null to ignore
	  * @param attrName the name of the new attribute
	  * @return true if removed, otherwise false
	  */
	public boolean removeAttribute(String namespaceURI, String attrName) {
		if (attrName == null || currentNode == null) {
			return false;
		}

		NamedNodeMap currentNodeAttributes = currentNode.getAttributes();
		Node removedNode = currentNodeAttributes.removeNamedItemNS(namespaceURI, attrName);
		return (removedNode != null);
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
			if ( (parentNodeOfNewTag = findNthElementByPath(startNode, startNode.getNodeName() + "/" + backAlevel, new XMLMutableInteger(1))) == null) { // then aggregate does not exist, so create it...
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
	  * Return the a set of values for the first occurrence of the tags given in nodePaths.
	  * @param searchNode the node from which to commence search, which will to match the first part of the nodePath
	  * @param nodePaths the fully qualified paths to the TAGs, separated by whitespace e.g. "MESSAGE/ORDER/ORDER_NUMBER MESSAGE/ORDER/TELNO"
	  * @return a Properties object of the tags' values, a missing value means the node will not be returned in the Properties - null never returned
	  */
	public Properties getPropertiesForTags(String nodePaths) {
		return getPropertiesForTags(this.currentNode, nodePaths);
	}

	/**
	  *
	  * Return the a set of values for the first occurrence of the tags given in nodePaths.
	  * @param searchNode the node from which to commence search, which will to match the first part of the nodePath
	  * @param nodePaths the fully qualified paths to the TAGs, separated by whitespace e.g. "MESSAGE/ORDER/ORDER_NUMBER MESSAGE/ORDER/TELNO"
	  * @return a Properties object of the tags' values, a missing value means the node will not be returned in the Properties - null never returned
	  */
	public Properties getPropertiesForTags(Node searchNode, String nodePaths) {

		Properties props = new Properties();

		if (searchNode == null || nodePaths == null) {
			return props;
		}

		StringTokenizer t = new StringTokenizer(nodePaths);

		while (t.hasMoreTokens()) {
			String nodePath = t.nextToken();
			String foundValue = getValueForTag(searchNode, nodePath);

			if (foundValue != null) {
				props.setProperty(nodePath, foundValue);
			}
		}

		return props;
	}


	/**
	  *
	  * Return the delimited/concatenated values for the first occurrence of the tags given in nodePaths.
	  * Note: concatenated values would be good for constructing a key
	  * The search will start from the currentNode
	  * @param nodePaths the fully qualified paths to the TAGs, separated by whitespace e.g. "MESSAGE/ORDER/ORDER_NUMBER MESSAGE/ORDER/TELNO"
	  * @param separator if set to "" or null, will return the values concatenated, otherwise the separator will be used to separate values
	  * @return the tag's values, separated by separator (if supplied), null if nothing found, empty strings for missing individual values
	  */
	public String getConcatValuesForTags(String nodePaths, String separator) {
		return getConcatValuesForTags(this.currentNode, nodePaths, separator);
	}

	/**
	  *
	  * Return the concatenated values for the first occurrence of the tags given in nodePaths.
	  * @param searchNode the node from which to commence search, which will to match the first part of the nodePath
	  * @param nodePaths the fully qualified paths to the TAGs, separated by whitespace e.g. "MESSAGE/ORDER/ORDER_NUMBER MESSAGE/ORDER/TELNO"
	  * @param separator if set to "" or null, will return the values concatenated, otherwise the separator will be used to separate values
	  * @return the tag's values, separated by separator (if supplied), null if nothing found, empty strings for missing individual values
	  */
	public String getConcatValuesForTags(Node searchNode, String nodePaths, String separator) {

		if (searchNode == null || nodePaths == null) {
			return "";
		}

		if (separator == null) {
			separator = "";
		}

		String concatValues = "";

		StringTokenizer t = new StringTokenizer(nodePaths);

		while (t.hasMoreTokens()) {
			String nodePath = t.nextToken();
			String foundValue = getValueForTag(searchNode, nodePath);

			if (foundValue == null) {
				foundValue = "";
			}

			concatValues += separator + foundValue;
		}

		// return null, if nothing found
		if (concatValues.length() <= separator.length()) { // then is empty or only has a separator
			concatValues = null;
		}
		else { // remove first separator, was never needed
			if (separator.length() > 0) {
				concatValues = concatValues.substring(separator.length());
			}
		}

		return concatValues;
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

		Node foundNode = findNthElementByPath(searchNode, nodePath, new XMLMutableInteger(1));

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
	  * Return the value for the first occurrence of the given tag.
	  * The search will start from the currentNode
	  * @param nodePath the fully qualified path to the TAG, e.g. MESSAGE/ORDER/ORDER_NUMBER
	  * @return the tag's value, null if not found
	  */
	public String getValueForTag(String nodePath) {
		// Perform the action from the root element, if nodePath begins with a / - signifying "start at root"
		if (nodePath != null && nodePath.length() > 0 && nodePath.startsWith("/")) {
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

		Node foundNode = findNthElementByPath(searchNode, nodePath, new XMLMutableInteger(1));

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
	  * @param searchNode the node from which to commence search, which will to match the first part of the nodePath
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


		if (parentNode.hasChildNodes()) {
			NodeList nodes = parentNode.getChildNodes();
			int i = 0;
			while (i < nodes.getLength()) {
				Node childNode = nodes.item(i++);

				if (childNode.getNodeType() == Node.ELEMENT_NODE) {
					String foundValue = childNode.getTextContent();
					// Only create a new LwXMLTagValue if something found...
					if (foundValue != null) {
						XMLTagValue tv = new XMLTagValue(childNode.getNodeName(), foundValue);
						values.addElement(tv);
					}
				}
			}
		} /* end if (strlen(path) == 0) */

		return values;
	}

	/**
	  *
	  * Set the case sensitivity for searches on TAG names
	  * @param tagNamesCaseSensitive the new value for tagNamesCaseSensitive
	  */
	public void setTagNamesCaseSensitive(boolean tagNamesCaseSensitive) {
		this.tagNamesCaseSensitive = tagNamesCaseSensitive;
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
		XMLMutableInteger numOccurrences = new XMLMutableInteger(nthOccurance);

		Node foundNode = findNthElementByPath(nodePath, numOccurrences);

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
	  *                                        numNodesByPath
	  *  Calls the findNthElementByPath, supplying the currentNode as the position from which to start the search,
	  *  counting the number of occurrences for this PATH.
	  *
	  * Note: the value of currentNode is not affected by this call.
	  *
	  * @param nodePath the path to follow to the required node e.g. MESSAGE/INFOSERVE/ORD.SERIAL_NO or MESSAGE/INFOSERVE/ORD/SERIAL_NO
	  * @return the number of occurrences found
	  */
	public int numNodesByPath(String nodePath) {
		int aHighNum = 10000;

		XMLMutableInteger numOccurrences = new XMLMutableInteger(aHighNum);

		findNthElementByPath(nodePath, numOccurrences);

		return (aHighNum - numOccurrences.intValue());
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
	public Node findNthElementByPath(String nodePath, XMLMutableInteger nthOccurance) {
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
	  *  Finds the ELEMENT whose name is supplied in nodePath, starting
	  *  the search at the supplied currentNode and following the path down the tree,
	  *  as denoted by the nodePath.
	  *  The special node name * will match any ELEMENT node's name.
	  *
	  *  The nodePath takes the form:
	  *			nodeName_1.nodeName_2.nodeName_n.searchNodeName
	  *
	  *  For example:
	  *			MESSAGE.INFOSERVE.ORD.SERIAL_NO
	  *
	  *  The 'nthOccurance' of the searchNodeName in the path will be the one
	  *  returned. If the name is not found at the lowest aggregate first reached,
	  *  the process will "back up" and go down any alternative paths, as
	  *  determined by the 'nodePath'.
	  *
	  *  As a nice side-effect (because 'nthOccurance' is decremented for each
	  *  occurrance found), to count the number of occurances of an element,
	  *  use a high number (say 1000) for 'nthOccurance' - then, when no currentNode
	  *  is returned, subtract the 'nthOccurance' from the high number.
	  *
	  * @param fromNode the node from which to commence search
	  * @param nodePath the path to follow to the required node e.g. MESSAGE/INFOSERVE/ORD/SERIAL_NO
	  * @param nthOccurance the match of the searched-for node to be returned (null will be returned if less than n occurrences exist)
	  * @return the found node, null if not found
	  */
	public Node findNthElementByPath(Node fromNode, String nodePath, XMLMutableInteger nthOccurance) {
		Node retNode = null;
		String searchName;

		/* Need temp path as findNthElementByPath edits the path as it goes down a branch. */
		StringBuilder path = new StringBuilder(nodePath); // StringBuilder is same as StringBuffer, but performs no synchronisation for threads

		/* Check if first/next word matches fromNode  name. */
		if ( (searchName = nextWord(path, "/")) != null ) {
			if (fromNode.getNodeType() == Node.ELEMENT_NODE) { /* Then want to check if right name. */
				String currentNodeName = fromNode.getNodeName();
				if ( (searchName.equals("*")) ||
					 ( tagNamesCaseSensitive && (currentNodeName.compareTo(searchName) == 0) ) ||
					 (!tagNamesCaseSensitive && (currentNodeName.compareToIgnoreCase(searchName) == 0) )
				   ) { /* then found fromNode */
					if (path.length() == 0) { /* then on last (leaf) word. */
						retNode = fromNode ;
					}
					else if (fromNode.hasChildNodes()) {
						NodeList nodes = fromNode.getChildNodes();
						int i = 0;
						while (i < nodes.getLength() && (retNode == null)) {
							Node childNode = nodes.item(i++);
							if (childNode.getNodeType() == Node.ELEMENT_NODE) {
								retNode = findNthElementByPath(childNode, path.toString(), nthOccurance);
							}
						}
					} /* end if (strlen(path) == 0) */
				} /* end if Name matches */
			} /* end if type == ELEMENT_NODE */
		}

		if (retNode != null) { /* then found a node */
			nthOccurance.add(-1);
		}

		if (nthOccurance.intValue() > 0) { /* keep trying until get correct occurance */
			retNode = null;
		}

		return retNode;
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
	  * @return the found node, null if not found
	  */
	public void getAllElementValuesByPath(Node fromNode, String nodePath, Vector<XMLTagValue> results, String originalNodePath) {
		String searchName;

		/* Need temp path as findNthElementByPath edits the path as it goes down a branch. */
		StringBuilder path = new StringBuilder(nodePath); // StringBuilder is same as StringBuffer, but performs no synchronisation for threads

		/* Check if first/next word matches fromNode  name. */
		if ( (searchName = nextWord(path, "/")) != null ) {
			if (fromNode.getNodeType() == Node.ELEMENT_NODE) { /* Then want to check if right name. */
				String currentNodeName = fromNode.getNodeName();
				if ( (searchName.equals("*")) ||
					 ( tagNamesCaseSensitive && (currentNodeName.compareTo(searchName) == 0) ) ||
					 (!tagNamesCaseSensitive && (currentNodeName.compareToIgnoreCase(searchName) == 0) )
				   ) { /* then found fromNode . */
					if (path.length() == 0) { /* then on last (leaf) word. */
						String foundValue =  fromNode.getTextContent();
						if (foundValue != null) {
							XMLTagValue tv = new XMLTagValue(originalNodePath, foundValue);
							Properties attributes = getAttributeValues(fromNode);
							if (attributes.size() > 0) {
								tv.setAttributeSet(attributes);
							}

							results.addElement(tv);
						}
					}
					else if (fromNode.hasChildNodes()) {
						NodeList nodes = fromNode.getChildNodes();
						int i = 0;
						while (i < nodes.getLength()) {
							Node childNode = nodes.item(i++);
							if (childNode.getNodeType() == Node.ELEMENT_NODE) {
								getAllElementValuesByPath(childNode, path.toString(), results, originalNodePath);
							}
						}
					} /* end if (strlen(path) == 0) */
				} /* end if Name matches */
			} /* end if type == ELEMENT_NODE */
		}
	}


	/**
	  *
	  * Return the next word located in the given target and removes that word from the path,
	  * where words are separated by the supplied separator.
	  * Note: made public, as is very useful.
	  *
	  * @param target the path fro which first word is to be stripped
	  * @param separator the separator to use to identify words
	  * @return the first word, or null if none found
	  */
	public static String nextWord(StringBuilder target, String separator) {
		if (target == null || target.length() == 0) {
			return null;
		}

		String retWord = null;

		// Remove any leading / chars (should really only exist at very start of  path, signifying to start at the root
		while (target.length() > 0 && target.charAt(0) == '/') {
			target.deleteCharAt(0);
		}

		int firstSeparatorPos = target.indexOf(separator);

		if (firstSeparatorPos >= 0) { // then found separator
			if (firstSeparatorPos > 0) { // then target does contain a word before the first separator
				// Extract the first "word"
				retWord = target.substring(0, firstSeparatorPos);
			}

			// remove chars from start of target to end of separator
			target.delete(0, (firstSeparatorPos + separator.length()));
		}
		else { // no separator found, so just return the contents of target and empty target out
			retWord = target.toString();
			target.delete(0, target.length());
		}

		return retWord;
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

	//////////////////////////////////////////////////////////////////////
	// Start: SAX Error Handlers for implementing ErrorHandler interface
	// LW 30/09/2013 - Turned off when made createDoc* static
	//////////////////////////////////////////////////////////////////////
	public void warning(SAXParseException e) throws SAXException {
		throw new SAXException("XMLDocument_old2: Parsing Warning encountered Line " + e.getLineNumber() + " (SysID=" + e.getSystemId() + ") :" + e.getMessage());
	}

	public void error(SAXParseException e) throws SAXException {
		throw new SAXException("XMLDocument_old2: Parsing Error encountered Line " + e.getLineNumber() + " (SysID=" + e.getSystemId() + ") :" + e.getMessage());
	}

	public void fatalError(SAXParseException e) throws SAXException {
		throw new SAXException("XMLDocument_old2: Fatal Parsing Error encountered Line " + e.getLineNumber() + " (SysID=" + e.getSystemId() + ") :" + e.getMessage());
	}
	//////////////////////////////////////////////////////////////////////
	// End: SAX Error Handlers for implementing ErrorHandler interface
	//////////////////////////////////////////////////////////////////////

	/**
	 * @param o the object to be checked for null.
	 * 
	 * @throws IllegalArgumentException if o is null
	 */
	private static void checkNullArgument(Object o) {
		if ((o == null)) throw new IllegalArgumentException("[" + Thread.currentThread().getName() + "]: Null value received.");
	}
}