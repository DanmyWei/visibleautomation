package createrecorderplugin.parser;
import java.io.File;
import java.io.IOException;
import java.util.Stack;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * parse the Project file for the project name
 * @author Matthew
  * Copyright (c) 2013 Visible Automation LLC.  All Rights Reserved.
 *
 */
public class ProjectParser extends Parser {
	protected final String 	NAME_TAG = "projectDescription.name";
	protected String 		mProjectName = null;
	protected boolean		mfProjectName = false;
	
	public ProjectParser(String projectFileName) throws SAXException, ParserConfigurationException, IOException { 
		File projectFile = new File(projectFileName);
		SAXParserFactory factory = SAXParserFactory.newInstance();
		SAXParser parser = factory.newSAXParser();
		parser.parse(projectFile, this);
	}
	
	public ProjectParser(File projectFile) throws SAXException, ParserConfigurationException, IOException { 
		SAXParserFactory factory = SAXParserFactory.newInstance();
		SAXParser parser = factory.newSAXParser();
		parser.parse(projectFile, this);
	}
	
	public String getProjectName() {
		return mProjectName;
	}
	
	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) {
		mTokenStack.push(qName);
		if (compareTag(NAME_TAG)) {
			mfProjectName = true;
		} else {
			mfProjectName = false;
		}
	}
	
	@Override
	public void endElement(String uri, String localName, String qName)  {
		mTokenStack.pop();
		mfProjectName = false;
	}

	@Override
	public void characters(char[] ch, int start, int length) {
		if (mfProjectName) {
			mProjectName = new String(ch, start, length);
		}
	}
}
