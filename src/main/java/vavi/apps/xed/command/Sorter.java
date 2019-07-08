/*
 * Copyright (c) 2019 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.apps.xed.command;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.klab.commons.cli.Binder;

import vavi.apps.xed.Command;
import vavi.apps.xed.App;


/**
 * Sorter.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2019/04/08 umjammer initial version <br>
 */
public class Sorter implements Command, Binder<App> {

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
    Sorter.Type type;

    /** datetime: author time format @see {@link java.text.DateFormat} */
    String option;

    /* */
    public String toString() {
        return "targetXPath: " + targetXPath +
                ", keyXPath: " + keyXPath +
                ", ascend: " + ascend +
                ", type: " + type +
                ", option: " + option;
    }

    /* */
    public void bind(App bean, String[] args, Context context) {
        bean.sorter = new Sorter();
        bean.sorter.targetXPath = args[0];
        bean.sorter.keyXPath = args[1];
        bean.sorter.ascend = args.length > 2 ? args[2].equals("desc") ? false : true : true;
        bean.sorter.type = args.length > 3 ? args[3].equals("datetime") ? Sorter.Type.datetime : Sorter.Type.string : Sorter.Type.string;
        bean.sorter.option = args.length > 4 ? args[4] : null;
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
    public void exec(Document document) {
        try {
            Object nodeSet = xPath.evaluate(targetXPath, document, XPathConstants.NODESET);

            NodeList nodeList = NodeList.class.cast(nodeSet);
//System.err.println("nodeList: " + nodeList.getLength());

            SortedMap<String, Node> nodes = new TreeMap<>(new Comparator<String>() {
                public int compare(String o1, String o2) {
                    return type.compareTo(o1, o2, Sorter.this);
                }
            });

            Node parent = null;

            for (int i = 0; i < nodeList.getLength(); i++) {

                Node node = nodeList.item(i);
                if (parent == null) {
                    parent = node.getParentNode();
                }

                String key = (String) xPath.evaluate(keyXPath, node, XPathConstants.STRING); // removeChild してるからうまく行ってる

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
}

/* */
