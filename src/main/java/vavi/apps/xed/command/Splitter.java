/*
 * Copyright (c) 2019 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.apps.xed.command;

import java.util.LinkedList;
import java.util.Queue;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.klab.commons.cli.Binder;

import vavi.apps.xed.App;
import vavi.apps.xed.Command;


/**
 * Splitter.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2019/04/08 umjammer initial version <br>
 */
public class Splitter implements Command, Binder<App> {

    /** xpath for splitting nodes */
    String targetXPath;

    /** max number for splitting node */
    int maxNumber;

    @Override
    public String toString() {
        return "targetXPath: " + targetXPath +
                ", sourceXPath: " + maxNumber;
    }

    @Override
    public void bind(App bean, String[] args, Context context) {
        bean.splitter = new Splitter();
        bean.splitter.targetXPath = args[0];
        bean.splitter.maxNumber = Integer.parseInt(args[1]);
    }

    /**
     * split targetXPath maxNumber
     * <pre>
     * split "/foo/bar/buz" 3
     * </pre>
     * before
     * <pre>
     * &lt;foo&gt;
     *   &lt;bar&gt;
     *     &lt;buz&gt; &lt;!-- buz みたいな繰り返しがある部分 --&gt;
     *     &lt;buz&gt;
     *     &lt;buz&gt;
     *     &lt;buz&gt;
     *     &lt;buz&gt;
     *     &lt;buz&gt;
     *     &lt;buz&gt;
     *     &lt;buz&gt;
     *     &lt;buz&gt;
     *   &lt;/bar&gt;
     * &lt;/foo&gt;
     * </pre>
     * after
     * <pre>
     * &lt;foo&gt;
     *   &lt;bar&gt;
     *   &lt;/bar&gt;
     * &lt;/foo&gt;
     *     &lt;buz&gt;
     *     &lt;buz&gt;
     *     &lt;buz&gt;
     *     &lt;!-- 3 --&gt;
     *     &lt;buz&gt;
     *     &lt;buz&gt;
     *     &lt;buz&gt;
     *     &lt;!-- 6 --&gt;
     *     &lt;buz&gt;
     *     &lt;buz&gt;
     *     &lt;buz&gt;
     *     &lt;!-- 9 --&gt;
     * </pre>
     *
     * @param document target to edit
     */
    @Override
    public void exec(Document document) {
        try {
            Object nodeSet = xPath.evaluate(targetXPath, document, XPathConstants.NODESET);

            NodeList nodeList = (NodeList) nodeSet;
System.err.println("nodeList: " + nodeList.getLength());

            Queue<Node> nodes = new LinkedList<>();

            Node parent = null;

            for (int i = 0; i < nodeList.getLength(); i++) {

                Node node = nodeList.item(i);
                if (parent == null) {
                    parent = node.getParentNode();
                }

                nodes.add(node);

                parent.removeChild(node);
            }

            int size = nodes.size();
outer:
            while (size > 0) {
                int c = 0;
                while (c < maxNumber) {
                    Node node = nodes.poll();
                    if (node == null) {
                        break outer;
                    }
                    parent.appendChild(node);
                    c++;
                    size--;
                }
                parent.appendChild(document.createComment(" ---- x8 ---- split here " + size + " ---- x8 ---- "));
            }

        } catch (XPathExpressionException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
