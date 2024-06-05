/*
 * Copyright (c) 2013 by Naohide Sano, All Rights Reserved.
 *
 * Programmed by Naohide Sano
 */

package vavi.apps.xed;

import java.io.File;
import java.io.PrintWriter;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;

import org.klab.commons.cli.Argument;
import org.klab.commons.cli.Bound;
import org.klab.commons.cli.Option;
import org.klab.commons.cli.Options;

import vavi.apps.xed.command.Editor;
import vavi.apps.xed.command.Sorter;
import vavi.apps.xed.command.Splitter;
import vavi.xml.util.PrettyPrinter;


/**
 * Command line XML editor.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2013/07/30 umjammer initial version <br>
 */
@Options
public class App {

    /** TODO could be more loosely coupled */
    @Option(option = "s", argName = "sorter target_xpath key_xpath [asc|desc] [datetime|string] [option]", args = 5)
    @Bound(binder = Sorter.class)
    public Sorter sorter;

    /** TODO could be more loosely coupled */
    @Option(option = "e", argName = "editor target_xpath source_xpath dest_expression", args = 3)
    @Bound(binder = Editor.class)
    public Editor editor;

    /** TODO could be more loosely coupled */
    @Option(option = "S", argName = "splitter target_xpath max_number", args = 2)
    @Bound(binder = Splitter.class)
    public Splitter splitter;

    /** */
    @Argument(index = 0)
    private File inFile;

    /** */
    private Command[] commands = {
        sorter, editor, splitter
    };

    /**
     * @param args
     */
    public static void main(String[] args) throws Exception {
        App app = new App();
        Options.Util.bind(args, app);
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        Document document = dbf.newDocumentBuilder().parse(app.inFile);
        for (Command command : app.commands) {
            if (command != null) {
//logger.log(Level.DEBUG, command);
                command.exec(document);
            }
        }
        new PrettyPrinter(new PrintWriter(System.out)).print(document);
    }
}
