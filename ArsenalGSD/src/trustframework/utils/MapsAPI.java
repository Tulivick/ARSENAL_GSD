/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package trustframework.utils;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 *
 * @author Guilherme
 */
public class MapsAPI {
    public static String getCountry(String location) throws MalformedURLException, IOException, XPathExpressionException{
        URL mapsUrl = new URL("https://maps.googleapis.com/maps/api/geocode/xml?address="+location.trim().replace(" ", "+"));
        InputStream openStream = mapsUrl.openStream();
        XPathFactory factory = XPathFactory.newInstance();
        XPath xPath = factory.newXPath();
        NodeList nodes = (NodeList) xPath.evaluate("/GeocodeResponse/result/address_component[type/text()='country']/long_name", new InputSource(openStream), XPathConstants.NODESET);
        return nodes.getLength()>0?nodes.item(0).getTextContent():null;
    }
}
