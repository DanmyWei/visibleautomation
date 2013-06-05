package createrecorderplugin.parser;

import java.util.Stack;
import org.xml.sax.helpers.DefaultHandler;

public class Parser extends DefaultHandler {
	protected Stack<String> mTokenStack;
	
	protected boolean compareTag(String tag) {
		String[] tags = tag.split("\\.");
		if (tags.length != mTokenStack.size()) {
			return false;
		}
		int itag = 0;
		for (String stackTag : mTokenStack) {
			if (!stackTag.equals(tags[itag++])) {
				return false;
			}
		}
		return true;
	}
	
	@Override
	public void startDocument() {
		mTokenStack = new Stack<String>();
	}

	@Override
	public void endElement(String uri, String localName, String qName)  {
		mTokenStack.pop();
	}

}