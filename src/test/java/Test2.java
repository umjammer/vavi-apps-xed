/*
 * Copyright (c) 2013 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;

import org.junit.jupiter.api.Test;


/**
 * Test2.
 *
 * @author <a href="mailto:umjammer@gmail.com">Naohide Sano</a> (umjammer)
 * @version 0.00 2013/07/02 umjammer initial version <br>
 */
public class Test2 {

    @Test
    public void test1() throws Exception {
        String expression = "xpath('/kml/Folder/Placemark/description/text()') | xpath('/kml/Folder/Placemark/updated/text()') | $1";

        String regex1 = "xpath\\s*\\(\\s*'([/@\\[\\]\\(\\)\\w]+)'\\s*\\)";
        Pattern pattern1 = Pattern.compile(regex1);
        Matcher matcher1 = pattern1.matcher(expression);
        StringBuffer sb = new StringBuffer();
        while (matcher1.find()) {
            String replacement = matcher1.group(1);
System.err.println(replacement);
            matcher1.appendReplacement(sb, replacement);
        }
        matcher1.appendTail(sb);
System.err.println(sb);
    }

    @Test
    public void test2() throws Exception {
        String[] parts = "2013/06/21 20:20:48 | $$".split("\\$\\$", -1);
        int i = 0;
        for (String s : parts) {
            System.err.println(i++ + ": [" + s + "]");
        }

        parts = "$$ | 2013/06/21 20:20:48".split("\\$\\$", -1);
        i = 0;
        for (String s : parts) {
            System.err.println(i++ + ": [" + s + "]");
        }

        parts = "$$ | A | $$".split("\\$\\$", -1);
        i = 0;
        for (String s : parts) {
            System.err.println(i++ + ": [" + s + "]");
        }

        parts = "A | $$ | B".split("\\$\\$", -1);
        i = 0;
        for (String s : parts) {
            System.err.println(i++ + ": [" + s + "]");
        }

        parts = "A | B".split("\\$\\$", -1);
        i = 0;
        for (String s : parts) {
            System.err.println(i++ + ": [" + s + "]");
        }
    }

    @Test
    public void test3() throws Exception {
        ScriptEngineManager manager = new ScriptEngineManager();
        List<ScriptEngineFactory> factories = manager.getEngineFactories();
        System.err.println("---- engines ----");
        factories.forEach(System.err::println);
        System.err.println("----");
        ScriptEngine engine = manager.getEngineByName("javascript");

        String pre = "xpath = function(path) { return java.lang.System.getProperty('java.vendor'); };";
        String expression = "xpath('/kml/Folder/Placemark/')";

        Object result = engine.eval(pre + expression);

        System.err.println(result);
    }
}
