/*
 * Copyright (c) 2013 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.List;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import net.sf.saxon.dom.NodeOverNodeInfo;
import net.sf.saxon.om.NodeInfo;

import org.w3c.dom.NodeList;

import org.xml.sax.InputSource;

import vavi.xml.util.PrettyPrinter;


/**
 * Test1.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2013/07/02 umjammer initial version <br>
 */
public class Test1 {

    protected XPath xPath;

    {
        System.setProperty(XPathFactory.DEFAULT_PROPERTY_NAME + ":" + XPathFactory.DEFAULT_OBJECT_MODEL_URI, "net.sf.saxon.xpath.XPathFactoryImpl");    
        xPath = XPathFactory.newInstance().newXPath();
System.err.println("SaxonXPathParser: xpath: " + XPathFactory.newInstance().getClass());
    }

    /** */
    public void parse(Reader inputHandler, String xpath) throws IOException {
        try {
            InputSource in = new InputSource(inputHandler);

//System.err.println("xpath: " + xpath);

            Object nodeSet = xPath.evaluate(xpath, in, XPathConstants.NODESET);

            if (nodeSet instanceof List) {

                @SuppressWarnings("unchecked")
                List<NodeInfo> nodeList = (List<NodeInfo>) nodeSet;
//System.err.println("nodeList: " + nodeList.size());
                for (int i = 0; i < nodeList.size(); i++) {
System.out.println("[" + i + "]------------------ ");

                    PrettyPrinter pp = new PrettyPrinter(System.out);
                    pp.print(NodeOverNodeInfo.wrap(nodeList.get(i)));
                }
            } else if (nodeSet instanceof NodeList) {

                NodeList nodeList = (NodeList) nodeSet;
//System.err.println("nodeList: " + nodeList.getLength());
                for (int i = 0; i < nodeList.getLength(); i++) {

                    @SuppressWarnings("unused")
                    String text = nodeList.item(i).getTextContent().trim();
//System.err.println(field.getName() + ": " + text);
                }
            } else {
                throw new IllegalStateException("unsupported type returns: " + nodeSet.getClass().getName());
            }

        } catch (XPathExpressionException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * @param args
     */
    public static void main(String[] args) throws Exception {
        Test1 app = new Test1();
        app.parse(new FileReader("/Users/nsano/Downloads/41XOYOEQ25TBK1GU0APANDOEFC2RK3D5.kml"), "/kml/Folder/Placemark");
    }
}
