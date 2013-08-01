/*
 * Copyright (c) 2013 by Naohide Sano, All rights reserved.
 *
 * Programmed by Naohide Sano
 */

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Test;


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
System.err.println(sb.toString());
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
}

/* */
