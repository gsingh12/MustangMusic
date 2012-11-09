package org.fuhsd.hhs.music;

import java.util.List;

/**
 * Parser interface used for oarsing response from a RSS feed.
 */
public interface Parser {

    void setContent(String content);
    List<Item> parseXML();

}
