/*
 * Copyright (c) 2013 by Naohide Sano, All Rights Reserved.
 *
 * Programmed by Naohide Sano
 */

import java.io.File;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.klab.commons.cli.Argument;
import org.klab.commons.cli.Binded;
import org.klab.commons.cli.Binder;
import org.klab.commons.cli.Option;
import org.klab.commons.cli.Options;

import vavi.xml.util.PrettyPrinter;


/**
 * Command line XML editor.
 * 
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2013/07/30 umjammer initial version <br>
 */
@Options
public class xed {

    /** */
    private static class Sorter {
        enum Type {
            datetime {
                int compareTo(String o1, String o2, Sorter sorter) {
                    try {
                        DateFormat sdf = new SimpleDateFormat(sorter.option, Locale.ENGLISH); // "http://d.hatena.ne.jp/rudi/20101201/1291214680" TODO locale
                        Date d1 = sdf.parse(o1);
                        Date d2 = sdf.parse(o2);
                        return sorter.ascend ? d1.compareTo(d2) : d2.compareTo(d1);
                    } catch (ParseException e) {
                        throw new IllegalArgumentException(e);
                    }
                }
            },
            string {
                int compareTo(String o1, String o2, Sorter sorter) {
                    return sorter.ascend ? o1.compareTo(o2) : o2.compareTo(o1);
                }
            };
            abstract int compareTo(String o1, String o2, Sorter sorter);
        }
        /** xpath for sorting nodes */
        String targetXPath;
        /** xpath for sort key text */
        String keyXPath;
        /** "asc" | "desc" */
        boolean ascend;
        /** datetime | string */
        Type type;
        /** datetime: date time format @see {@link java.util.DateFormat} */
        String option;
        public String toString() {
            return "targetXPath: " + targetXPath +
                    ", keyXPath: " + keyXPath +
                    ", ascend: " + ascend +
                    ", type: " + type +
                    ", option: " + option;
        }
    }

    /** */
    @Option(option = "s", argName = "sorter target_xpath key_xpath [asc|desc] [datetime|string] [option]", args = 5)
    @Binded(binder = SorterBinder.class)
    private Sorter sorter;
    
    /** */
    public static class SorterBinder implements Binder<xed> {
        public void bind(xed bean, String[] args, Context context) {
            bean.sorter = new Sorter();
            bean.sorter.targetXPath = args[0];
            bean.sorter.keyXPath = args[1];
            bean.sorter.ascend = args.length > 2 ? args[2].equals("desc") ? false : true : true;
            bean.sorter.type = args.length > 3 ? args[3].equals("datetime") ? Sorter.Type.datetime : Sorter.Type.string : Sorter.Type.string;
            bean.sorter.option = args.length > 4 ? args[4] : null;
        }
    }

    /** */
    private static class Editor {
        /** xpath for editing nodes */
        String targetXPath;
        /** xpath for editing node */
        String sourceXPath;
        /** js expression, $$ in string replaced by child nodes of sourceXPath */
        String destinationExpression;
        public String toString() {
            return "targetXPath: " + targetXPath +
                    ", sourceXPath: " + sourceXPath +
                    ", destinationExpression: " + destinationExpression;
        }
    }

    /** */
    @Option(option = "e", argName = "editor target_xpath source_xpath dest_expression", args = 3)
    @Binded(binder = EditorBinder.class)
    private Editor editor;

    /** */
    public static class EditorBinder implements Binder<xed> {
        public void bind(xed bean, String[] args, Context context) {
            bean.editor = new Editor();
            bean.editor.targetXPath = args[0];
            bean.editor.sourceXPath = args[1];
            bean.editor.destinationExpression = args[2];
        }
    }

    /** */
    @Argument(index = 0)
    private File inFile;

    /** */
    private XPath xPath = XPathFactory.newInstance().newXPath();

    /** */
    private DocumentBuilder db;
    
    /* */
    {
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            db = dbf.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * sort targetXPath keyXPath [asc|desc] [datetime|string] [option]
     * <pre>
     * sort "/foo/bar/buz" "/foo/bar/buz/key/text()" asc
     * </pre>
     * before
     * <pre>
     * &lt;foo&gt;
     *   &lt;bar&gt;
     *     &lt;buz&gt;&lt;key&gt;3&lt;/key&gt;&lt;/buz&gt; &lt;!-- buz みたいな繰り返しがある部分 --&gt; 
     *     &lt;buz&gt;&lt;key&gt;1&lt;/key&gt;&lt;/buz&gt; &lt;!-- buz 中に含まれる要素 key の値でソートする --&gt;
     *     &lt;buz&gt;&lt;key&gt;2&lt;/key&gt;&lt;/buz&gt; 
     *     &lt;buz&gt;&lt;key&gt;4&lt;/key&gt;&lt;/buz&gt;
     *   &lt;/bar&gt;
     * &lt;/foo&gt;
     * </pre>
     * after
     * <pre>
     * &lt;foo&gt;
     *   &lt;bar&gt;
     *     &lt;buz&gt;&lt;key&gt;1&lt;/key&gt;&lt;/buz&gt;
     *     &lt;buz&gt;&lt;key&gt;2&lt;/key&gt;&lt;/buz&gt; 
     *     &lt;buz&gt;&lt;key&gt;3&lt;/key&gt;&lt;/buz&gt;
     *     &lt;buz&gt;&lt;key&gt;4&lt;/key&gt;&lt;/buz&gt;
     *   &lt;/bar&gt;
     * &lt;/foo&gt;
     * </pre>
     * 
     * @param document
     */
    void sort(Document document) {
        try {
            Object nodeSet = xPath.evaluate(sorter.targetXPath, document, XPathConstants.NODESET);

            NodeList nodeList = NodeList.class.cast(nodeSet);
//System.err.println("nodeList: " + nodeList.getLength());

            SortedMap<String, Node> nodes = new TreeMap<String, Node>(new Comparator<String>() {
                public int compare(String o1, String o2) {
                    return sorter.type.compareTo(o1, o2, sorter);
                }
            });

            Node parent = null;

            for (int i = 0; i < nodeList.getLength(); i++) {
    
                Node node = nodeList.item(i);
                if (parent == null) {
                    parent = node.getParentNode();
                }

                String key = (String) xPath.evaluate(sorter.keyXPath, node, XPathConstants.STRING); // removeChild してるからうまく行ってる

                nodes.put(key, node);

                parent.removeChild(node);
            }

            for (Node node : nodes.values()) {
                parent.appendChild(node);
            }
            
        } catch (XPathExpressionException e) {
            throw new IllegalArgumentException(e);
        }
    }
    
    /**
     * edit targetXPath sourceXPath destinationExpression
     * <p>
     * functions
     * <ul>
     *  <li>xpath: xpath reference</li>
     *  <li>xpath_sdf: xpath reference with simple date format</li>
     * </ul>
     * <li>$$: source reference</li>
     * </p>
     * <pre>
     * edit "/foo/bar/buz" "/foo/bar/buz/target" "number is $$"
     * </pre>
     * before
     * <pre>
     * &lt;foo&gt;
     *   &lt;bar&gt;
     *     &lt;buz&gt;&lt;target&gt;3&lt;/target&gt;&lt;/buz&gt; &lt;!-- buz みたいな繰り返しがある部分 --&gt; 
     *     &lt;buz&gt;&lt;target&gt;1&lt;/target&gt;&lt;/buz&gt; &lt;!-- buz 中に含まれる要素 target のテキストを編集をする --&gt;
     *     &lt;buz&gt;&lt;target&gt;2&lt;/target&gt;&lt;/buz&gt; 
     *     &lt;buz&gt;&lt;target&gt;4&lt;/target&gt;&lt;/buz&gt; 
     *   &lt;/bar&gt;
     * &lt;/foo&gt;
     * </pre>
     * after
     * <pre>
     * &lt;foo&gt;
     *   &lt;bar&gt;
     *     &lt;buz&gt;&lt;target&gt;number is 3&lt;/target&gt;&lt;/buz&gt;
     *     &lt;buz&gt;&lt;target&gt;number is 1&lt;/target&gt;&lt;/buz&gt;
     *     &lt;buz&gt;&lt;target&gt;number is 2&lt;/target&gt;&lt;/buz&gt; 
     *     &lt;buz&gt;&lt;target&gt;number is 4&lt;/target&gt;&lt;/buz&gt; 
     *   &lt;/bar&gt;
     * &lt;/foo&gt;
     * </pre>
     *
     * @param document
     */
    void edit(Document document) {
        try {
            Object nodeSet = xPath.evaluate(editor.targetXPath, document, XPathConstants.NODESET);

            NodeList nodeList = NodeList.class.cast(nodeSet);
//System.err.println("nodeList: " + nodeList.getLength());

            List<Node> nodes = new ArrayList<Node>();

            Node parent = null;

            for (int i = 0; i < nodeList.getLength(); i++) {
    
                Node node = nodeList.item(i);
                if (parent == null) {
                    parent = node.getParentNode();
                }

                Node sourceNode = (Node) xPath.evaluate(editor.sourceXPath, node, XPathConstants.NODE);

                process_script(editor.destinationExpression, node, sourceNode, document);

                nodes.add(node);

                parent.removeChild(node);
            }

            for (Node node : nodes) {
                parent.appendChild(node);
            }

        } catch (XPathExpressionException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /** replaces $$ */
    private void process_$$(String expression, Node sourceNode, Document document) {
        List<Node> sourceNodes = new ArrayList<Node>();

        // foursquare がアホな出力するから
        for (int j = 0; j < sourceNode.getChildNodes().getLength(); j++) {
            Node childNode = sourceNode.getChildNodes().item(j);  
//System.err.println("i:" + childNode);
            sourceNodes.add(childNode);
        }
        for (int j = 0; j < sourceNode.getChildNodes().getLength(); j++) {
            Node childNode = sourceNode.getChildNodes().item(j);  
            sourceNode.removeChild(childNode);
        }
        
        String[] parts = expression.split("\\$\\$", -1);
//System.err.println("parts:" + parts.length);
        if (parts.length > 1) {
            for (int j = 0; j < parts.length - 1; j++) {
                if (!parts[j].isEmpty()) {
                    sourceNode.appendChild(document.createTextNode(parts[j]));
                }
                for (Node n : sourceNodes) {
//System.err.println("o:" + n);
                    sourceNode.appendChild(n);
                }
            }
        }
    }

    /** */
    public String function_xpath(String xpath, Node node) throws XPathExpressionException {
        String replacement = (String) xPath.evaluate(xpath, node, XPathConstants.STRING);
//System.err.println("replacement: " + replacement);
        return replacement;
    }

    /** */
    public String function_xpath_sdf(String xpath, String format1, String format2, Node node) throws XPathExpressionException {
        String datetime = (String) xPath.evaluate(xpath, node, XPathConstants.STRING);
        String replacement;
        try {
            replacement = new SimpleDateFormat(format2).format(new SimpleDateFormat(format1, Locale.ENGLISH).parse(datetime)); // TODO formats locale
            } catch (ParseException e) {
System.err.println("parse error: " + format1);
            replacement = datetime;
        }
//System.err.println("replacement: " + replacement);
        return replacement;
    }

    /** */
    private void process_script(String expression, Node node, Node sourceNode, Document document) {
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("js");

        Bindings bindings = engine.getBindings(ScriptContext.ENGINE_SCOPE);
        bindings.put("xed", this);
        bindings.put("node", node);

        String prepare = "xpath = function(path) { return xed.function_xpath(path, node); };" +
            "xpath_sdf = function(path, format1, format2) { return xed.function_xpath_sdf(path, format1, format2, node); };";

        try {
            String result = (String) engine.eval(prepare + expression);
            process_$$(result, sourceNode, document);
        } catch (ScriptException e) {
e.printStackTrace(System.err);
            throw new IllegalArgumentException("invalid script: " + expression);
        }
    }

    /**
     * @param args
     */
    public static void main(String[] args) throws Exception {
        xed app = new xed();
        Options.Util.bind(args, app);
        Document document = app.db.parse(app.inFile);
        if (app.sorter != null) {
//System.err.println(app.sorter);
            app.sort(document);
        }
        if (app.editor != null) {
//System.err.println(app.editor);
            app.edit(document);
        }
        new PrettyPrinter(new PrintWriter(System.out)).print(document);
    }
}

/* */
