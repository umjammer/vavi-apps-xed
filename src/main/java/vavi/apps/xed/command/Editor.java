/*
 * Copyright (c) 2019 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.apps.xed.command;

import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.klab.commons.cli.Binder;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import vavi.apps.xed.App;
import vavi.apps.xed.Command;
import vavi.util.Debug;

import static java.lang.System.getLogger;


/**
 * Editor.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2019/04/08 umjammer initial version <br>
 */
public class Editor implements Command, Binder<App> {

    private static final Logger logger = getLogger(Editor.class.getName());

    /** xpath for editing nodes */
    String targetXPath;

    /** xpath for editing node */
    String sourceXPath;

    /** js expression, $$ in string replaced by child nodes of sourceXPath */
    String destinationExpression;

    @Override
    public String toString() {
        return "targetXPath: " + targetXPath +
                ", sourceXPath: " + sourceXPath +
                ", destinationExpression: " + destinationExpression;
    }

    @Override
    public void bind(App bean, String[] args, Context context) {
        bean.editor = new Editor();
        bean.editor.targetXPath = args[0];
        bean.editor.sourceXPath = args[1];
        bean.editor.destinationExpression = args[2];
    }

    /**
     * edit targetXPath sourceXPath destinationExpression
     * <p>
     * functions
     * <ul>
     *  <li>xpath: xpath reference</li>
     *  <li>xpath_sdf: xpath reference with simple author format</li>
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
     * @param document target to edit
     */
    @Override
    public void exec(Document document) {
        try {
            Object nodeSet = xPath.evaluate(targetXPath, document, XPathConstants.NODESET);

            NodeList nodeList = (NodeList) nodeSet;
//logger.log(Level.DEBUG, "nodeList: " + nodeList.getLength());

            List<Node> nodes = new ArrayList<>();

            Node parent = null;

            for (int i = 0; i < nodeList.getLength(); i++) {

                Node node = nodeList.item(i);
                if (parent == null) {
                    parent = node.getParentNode();
                }

                Node sourceNode = (Node) xPath.evaluate(sourceXPath, node, XPathConstants.NODE);

                process_script(destinationExpression, node, sourceNode, document);

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

    /** function for javascript */
    public String function_xpath(String xpath, Node node) throws XPathExpressionException {
        String replacement = (String) xPath.evaluate(xpath, node, XPathConstants.STRING);
//logger.log(Level.DEBUG, "replacement: " + replacement);
        return replacement;
    }

    /** function for javascript */
    public String function_xpath_sdf(String xpath, String format1, String format2, Node node) throws XPathExpressionException {
        String datetime = (String) xPath.evaluate(xpath, node, XPathConstants.STRING);
        String replacement;
        try {
            replacement = new SimpleDateFormat(format2).format(new SimpleDateFormat(format1, Locale.ENGLISH).parse(datetime)); // TODO formats locale
        } catch (ParseException e) {
logger.log(Level.DEBUG, "parse error: " + format1);
            replacement = datetime;
        }
//logger.log(Level.DEBUG, "replacement: " + replacement);
        return replacement;
    }

    /** exec javascript */
    private void process_script(String expression, Node node, Node sourceNode, Document document) {
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("javascript");

        Bindings bindings = engine.getBindings(ScriptContext.ENGINE_SCOPE);
        bindings.put("App", this);
        bindings.put("node", node);

        String prepare = "xpath = function(path) { return App.function_xpath(path, node); };" +
            "xpath_sdf = function(path, format1, format2) { return App.function_xpath_sdf(path, format1, format2, node); };";

        try {
            String result = (String) engine.eval(prepare + expression);
//logger.log(Level.DEBUG, "result: " + result);
            process_$$(result, sourceNode, document);
        } catch (ScriptException e) {
Debug.printStackTrace(java.util.logging.Level.FINE, e);
            throw new IllegalArgumentException("invalid script: " + expression);
        }
    }

    /** replaces $$ */
    private void process_$$(String expression, Node sourceNode, Document document) {
        List<Node> sourceNodes = new ArrayList<>();

        // cause foursquare's weired outputs
        for (int j = 0; j < sourceNode.getChildNodes().getLength(); j++) {
            Node childNode = sourceNode.getChildNodes().item(j);
//logger.log(Level.DEBUG, "i:" + childNode);
            sourceNodes.add(childNode);
        }
        for (int j = 0; j < sourceNode.getChildNodes().getLength(); j++) {
            Node childNode = sourceNode.getChildNodes().item(j);
            sourceNode.removeChild(childNode);
        }

        String[] parts = expression.split("\\$\\$", -1);
//logger.log(Level.DEBUG, "parts:" + parts.length);
        if (parts.length > 1) {
            for (int j = 0; j < parts.length - 1; j++) {
                if (!parts[j].isEmpty()) {
                    sourceNode.appendChild(document.createTextNode(parts[j]));
                }
                for (Node n : sourceNodes) {
//logger.log(Level.DEBUG, "o:" + n);
                    sourceNode.appendChild(n);
                }
            }
        }
    }
}
