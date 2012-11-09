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
 * This parses the "Photo Gallery Account" links
 */
public class ParsePhotoLinks implements Parser {
    
    private String itemsXmlStr;

    @Override
    public void setContent(String content) {
        this.itemsXmlStr = content;
    }
    @Override
    public List<Item> parseXML() {
        // Log.d(LOG_MODULE, "parseXML: " + itemsXmlStr);
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

        private boolean inH2 = false, inTable = false, inItemTag = false, done = false;
        private String currTitle, currLink, currImgUrl;

        public void startDocument() throws SAXException {
            itemList = new LinkedList();  // Create a Linked List
            itemTitleSet = new HashSet(); // Create a HashSet for titles.
        }

        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            if (done) return;
            if (!inH2 && localName.equals("h2")) {
                //Log.d(LOG_MODULE, "in h2");
                inH2 = true;
                return;
            }
            if (inH2 && !inTable && localName.equals("table")) {
                //Log.d(LOG_MODULE, "in table");
                inTable = true;
                return;
            }
            if (inH2 && inTable && !inItemTag && localName.equals("a")) {
                //Log.d(LOG_MODULE, "in item");
                currLink = attributes.getValue("href");
                //Log.dLOG_MODULE, "currLink: " + currLink);
                inItemTag = true;
                return;
            }
            if (inH2 && inTable && inItemTag && localName.equals("img")) {
                //Log.dLOG_MODULE, "in img");
                currImgUrl = attributes.getValue("src");
                //Log.dLOG_MODULE, "currImgUrl " + currImgUrl);
                return;
            }
        }

        public void characters(char[] ch, int start, int length) throws SAXException {
            if (done) return;
            if (inH2 && inTable && inItemTag) {
                String currData = new String(ch, start, length);
                currData += " Galleries";
                currTitle = currData;
                //Log.d(LOG_MODULE, "currTitle: " + currTitle); 
                return;
            }
        }

        public void endElement(String uri, String localName, String qName) throws SAXException {
            if (done) return;
            if (inH2 && inTable && inItemTag && localName.equals("a")) {
                //Log.d(LOG_MODULE, "ending item");
                /* Don't need to remove duplicates
                if (!itemTitleSet.contains(currTitle)) {
                    Item newItem = new Item(currTitle, currLink, null, currImg);
                    itemTitleSet.add(currTitle);
                    itemList.add(newItem);
                }
                */
                //Log.d(LOG_MODULE, "creating item: " + currTitle);
                Item newItem = new Item(currTitle, currLink, null, currImgUrl);
                itemList.add(newItem);
                inItemTag = false;
                return;
            }
            if (inH2 && inTable && !inItemTag && localName.equals("table")) {
                //Log.d(LOG_MODULE, "ening table");
                inTable = false;
                inH2 = false;  // end inH2 here.
                done = true;   // done here so we only show one (latest) set of links
                return;
            }
            /*
            if (inH2 && !inTable && !inItemTag && localName.equals("h2")) {
                // Log.d(LOG_MODULE, "ending h2");
                inH2 = false;
                done = true;
                return;
            }
            */
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
