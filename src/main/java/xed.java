/*
 * Copyright (c) 2002 by Naohide Sano, All Rights Reserved.
 *
 * Programmed by Naohide Sano
 */

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.jaxen.JaxenException;
import org.jaxen.XPath;
import org.jaxen.dom.DOMXPath;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


/**
 * Command line XML editor.
 * 
 * @author <a href="mailto:vavivavi@yahoo.co.jp">Naohide Sano</a> (vavi)
 * @version 0.00 021110 vavi initial version <br>
 */
public class xed {

    /**
     * xed script in out.
     */
    public static void main(String[] args) throws Exception {
        if (args.length != 3) {
            System.err.println("usage: java xed script in out");
            System.exit(1);
        }

        new xed(args[0], args[1], args[2]);
    }

    /**
     * 
     */
    private xed(String scriptFile, String inFile, String outFile)
        throws JaxenException,
               SAXException,
               IOException,
               TransformerException {

        // XML 文書から DOM を取得
        // System.err.println(parser.isValidating());
        InputSource input = new InputSource(new BufferedReader(new FileReader(inFile)));

        Document document = parser.parse(input);
// System.err.println("---- encoding ----");
// System.err.println(input.getEncoding());

        String publicId = document.getDoctype().getPublicId();
System.err.println("---- public id ----");
System.err.println(publicId);

        String systemId = document.getDoctype().getSystemId();
System.err.println("---- system id ----");
System.err.println(systemId);

// System.err.println(document.getDoctype().getName());
// System.err.println(document.getDoctype().getInternalSubset());

//         NamedNodeMap es = document.getDoctype().getEntities();
//         for (int i = 0; i < es.getLength(); i++) {
// System.err.println(es.item(i));
//         }

//         NamedNodeMap ns = document.getDoctype().getNotations();
//         for (int i = 0; i < ns.getLength(); i++) {
// System.err.println(ns.item(i));
//         }

//         String encoding = document.getDoctype().getEntities().getNamedItem(OutputKeys.ENCODING).getNodeValue();
// System.err.println("---- encoding ----");
// System.err.println(encoding);

        // script
        Document scriptDocument = parser.parse(scriptFile);
System.err.println("---- document node ----");
System.err.println(document.getDocumentElement());

        XPath commandPath = new DOMXPath("/xed/*");
        List<?> commands = commandPath.selectNodes(scriptDocument);
        Iterator<?> i = commands.iterator();
        while (i.hasNext()) {
            Node commandNode = (Node) i.next();
System.err.println("---- command node ----");
System.err.println(commandNode);
            String command = commandNode.getNodeName();
System.err.println("---- command ----");
System.err.println(command);
            String xpath = commandNode.getAttributes().getNamedItem("path").getNodeValue();
System.err.println("---- xpath ----");
System.err.println(xpath);

            if ("add".equals(command)) {
                XPath importPath = new DOMXPath("*");
                Node importNode = (Node) importPath.selectNodes(commandNode).get(0);
System.err.println("---- import node ----");
System.err.println(importNode);
                Node addNode = document.importNode(importNode, true);

                XPath selectPath = new DOMXPath(xpath);
                List<?> results = selectPath.selectNodes(document);
                Iterator<?> j = results.iterator();
                while (j.hasNext()) {
                    Node result = (Node) j.next();
// System.err.println(result);
                    result.appendChild(addNode);
                }
System.err.println("---- result node ----");
System.err.println(results);
            } else if ("delete".equals(command)) {
            } else if ("replace".equals(command)) {
            } else {
System.err.println("unknown command: " + command);
            }
        }

        Writer writer = new BufferedWriter(new FileWriter(outFile));

        DOMSource source = new DOMSource(document);
        StreamResult result = new StreamResult(writer);
        // set encode
        Properties props = new Properties();
//      props.setProperty(OutputKeys.INDENT, "yes");
        props.setProperty(OutputKeys.DOCTYPE_SYSTEM, systemId);
        props.setProperty(OutputKeys.DOCTYPE_PUBLIC, publicId);
        props.setProperty(OutputKeys.ENCODING, "Shift_JIS");
        transformer.setOutputProperties(props);

        transformer.transform(source, result);
    }

    /** */
    private Transformer transformer;

    /** */
    private DocumentBuilder parser;

    /** */
    {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            // xerces depend code: to avoid network access.
//          dbf.setAttribute("http://apache.org/xml/features/nonvalidating/load-external-dtd", Boolean.FALSE);
            parser = dbf.newDocumentBuilder();
            TransformerFactory tf = TransformerFactory.newInstance();
            transformer = tf.newTransformer();
        } catch (Exception e) {
e.printStackTrace(System.err);
            System.exit(1);
        }
    }
}

/* */
