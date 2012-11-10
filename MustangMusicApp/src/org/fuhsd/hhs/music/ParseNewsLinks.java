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
 * This parses the Blasts links
 */
public class ParseNewsLinks implements Parser {
    
    // Log Module
    private static final String LOG_MODULE = "ParseNewsLinks";

    private String itemsXmlStr;

    @Override
    public void setContent(String content) {
        this.itemsXmlStr = content;
    }
    
    @Override
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

        private boolean inHomePage = false, inNews = false, inNewsList = false, done = false;
        
        public void startDocument() throws SAXException {
            itemList = new LinkedList();  // Create a Linked List
            itemTitleSet = new HashSet(); // Create a HashSet for titles.
        }

        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            if (done) return;
            if (inHomePage && inNews && !inNewsList && localName.equals("ul")) {
                //Log.d(LOG_MODULE, "start ul");
                inNewsList = true;
                return;
            }
            if (inHomePage && inNews && inNewsList && !inItemTag && localName.equals("a")) {
                //Log.d(LOG_MODULE, "start a");
                currLink = attributes.getValue("href");
                //Log.d(LOG_MODULE, "currLink="+currLink);
                inItemTag = true;
                return;
            }
        }

        public void characters(char[] ch, int start, int length) throws SAXException {
            String currData = new String(ch, start, length);
            if (!inHomePage && currData.equals("Home")) {
                //Log.d(LOG_MODULE, "in Home now");
                inHomePage = true;
                return;
            }
            if (inHomePage && !inNews && currData.equals("Latest News")) {
                //Log.d(LOG_MODULE, "in NEWS now");
                inNews = true;
                return;
            }
            if (inHomePage && inNews && inNewsList && inItemTag) {
                currTitle = currData;
                //Log.d(LOG_MODULE, "currTitle="+currTitle);
                return;
            }
        }

        public void endElement(String uri, String localName, String qName) throws SAXException {
            if (done) return;
            
            if (inHomePage && inNews && inNewsList && inItemTag && localName.equals("a")) {
                //Log.d(LOG_MODULE, "end ul");
                /*
                if (!itemTitleSet.contains(currTitle)) {
                }
                */
                Item newItem = new Item(currTitle, currLink, currDate);
                // Log.i(LOG_MODULE, "Created new Item: " + newItem);
                itemTitleSet.add(currTitle);
                itemList.add(newItem);
                inItemTag = false;
                return;
            }
            if (inHomePage && inNews && inNewsList && !inItemTag && localName.equals("ul")) {
                //Log.d(LOG_MODULE, "end ul");
                inNewsList = false;
                inNews = false; // End News here too
                inHomePage = false;
                done = true;  // That is it.
                return;
            }
        }

        public List<Item> returnList() {
            //Collections.reverse(itemList);
            //Collections.sort(itemList, new SortByDate());
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
