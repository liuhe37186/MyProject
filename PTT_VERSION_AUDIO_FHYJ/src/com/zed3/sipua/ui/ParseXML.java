package com.zed3.sipua.ui;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.zed3.sipua.R;

public class ParseXML extends DefaultHandler {
	List<Map<String, Object>> list;
	Map<String, Object> map;
	private StringBuffer buffer = new StringBuffer();

	public void SetData(List<Map<String, Object>> listData) {
		list = listData;
	}

	@Override
	public void startDocument() throws SAXException {
		super.startDocument();
	}

	@Override
	public void endDocument() throws SAXException {
		super.endDocument();
	}

	@Override
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {
		// 保存开始标记
		if (localName.equalsIgnoreCase("contacts")) {
			map = new HashMap<String, Object>();
		}
		super.startElement(uri, localName, qName, attributes);
	}

	@Override
	public void endElement(String uri, String localName, String qName)
			throws SAXException {
		// 保存结束标记
		if (localName.equalsIgnoreCase("contacts")) {
			if (map != null) {
				map.put("img", R.drawable.icon_contact);
				list.add(map);
				buffer.setLength(0);
			}
		} else if (localName.equalsIgnoreCase("name")) {
			if (map != null)
				map.put("title", buffer.toString().trim());
			buffer.setLength(0);
		} else if (localName.equalsIgnoreCase("phone")) {
			if (map != null)
				map.put("info", buffer.toString().trim());
			buffer.setLength(0);
		}
		super.endElement(uri, localName, qName);
	}

	@Override
	public void characters(char[] ch, int start, int length)
			throws SAXException {
		buffer.append(ch, start, length);
		super.characters(ch, start, length);
	}
}
