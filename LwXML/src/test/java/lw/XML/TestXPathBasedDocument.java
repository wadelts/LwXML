package lw.XML;

import static org.junit.Assert.*;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.junit.Test;
import org.w3c.dom.Node;

public class TestXPathBasedDocument {

	@Test
	public void testFind() {


		XMLDocument settingsDoc = null;
		String settingsFileName = "file:///C:/Users/wadel/git/LwXML/LwXML/src/test/xml/Applic_FtoFtoF.xml";
		try {
			settingsDoc = XMLDocument.createDocFromFile(settingsFileName, false);
		} catch (XMLException e) {
			fail("Could not load XML doc: " + e);
		}
		
		String  messageProcessingClassName = settingsDoc.getValueForTag("Processing/MessageProcessingClassName");
		assertEquals("Invalid Class Name", "gemha.servers.ProcessMessageForFile", messageProcessingClassName);
	}

}
