package org.fuhsd.hhs.music;

import java.io.StringReader;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import android.util.Log;

/**
 * Current not in use.
 */
public class ParseItemList {
    
    // Log Module
    private static final String LOG_MODULE = "ParseItemList";

    private String itemsXmlStr;

    ParseItemList(String itemsXmlStr) {
        this.itemsXmlStr = itemsXmlStr;
    }

    public List<Item> parseXML() {
        try {
            SAXParserFactory spf = SAXParserFactory.newInstance();
            SAXParser sp = spf.newSAXParser();
            XMLReader xr = sp.getXMLReader();
            ParseItemListHandler parseHandler = new ParseItemListHandler();
            
            // Parse:
            // Log.i("com.homestead", "parseXML: itemsXmlStr=" + itemsXmlStr);
            xr.setContentHandler(parseHandler);
            xr.parse(new InputSource(new StringReader(itemsXmlStr)));
            
            // Parsed results
            List<Item> itemList = parseHandler.returnList();
            // Log.d(LOG_MODULE, "parseXML: itemList = " + itemList);
            
            return itemList;
        } catch (Exception ex) {
            Log.e("com.homestead", "ex: " + ex);
            ex.printStackTrace();
            return null;
        }
    }
    
    class ParseItemListHandler extends DefaultHandler {
        
        private LinkedList<Item> itemList;
        private Set<String> itemTitleSet;

        private boolean inItemTag = false, inTitleTag = false, inLinkTag = false, inDateTag = false;
        private String currTitle, currLink, currDate = null;

        public void startDocument() throws SAXException {
            itemList = new LinkedList();  // Create a Linked List
            itemTitleSet = new HashSet(); // Create a HashSet for titles.
        }

        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            if (localName.equals("item")) {
                inItemTag = true;
            } else if (localName.equals("title")) {
                if (inItemTag) inTitleTag = true;
            } else if (localName.equals("link")) {
                if (inItemTag) inLinkTag = true;
            } else if (localName.equals("isodate")) {
                if (inItemTag) inDateTag = true;
            }
        }

        public void characters(char[] ch, int start, int length) throws SAXException {
            String currData = new String(ch, start, length);
            if (inTitleTag) {
                currTitle = currData;
            } else if (inLinkTag) {
                currLink = currData;
            } else  if (inDateTag) {
                currDate = currData;
            }
        }

        public void endElement(String uri, String localName, String qName) throws SAXException {
            if (localName.equals("title")) {
                inTitleTag = false;
            } else if (localName.equals("link")) {
                inLinkTag = false;
            } else if (localName.equals("isodate")) {
                inDateTag = false;
            } else if (localName.equals("item")) {
                if (!itemTitleSet.contains(currTitle)) {
                    Item newItem = new Item(currTitle, currLink, currDate);
                    Log.i("com.homestead", "Created new Item: " + newItem);
                    itemTitleSet.add(currTitle);
                    itemList.add(newItem);
                }
                inItemTag = false;
            }
        }

        public List<Item> returnList() {
            Collections.reverse(itemList);
            Collections.sort(itemList, new SortByDate());
            return itemList;
        }

        public class SortByDate implements Comparator<Item> {
            public SortByDate() { }

            public int compare(Item item1, Item item2) {
                if ((item1.getDate() != null) && (item2.getDate() != null)) {
                    return item1.getDate().compareTo(item2.getDate());
                } else {
                    return -1;
                }
            }
        }
    }
}
