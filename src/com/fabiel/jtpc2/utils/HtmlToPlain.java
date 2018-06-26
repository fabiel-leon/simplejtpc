/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.fabiel.jtpc2.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import org.jsoup.helper.StringUtil;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;
import org.jsoup.select.NodeTraversor;
import org.jsoup.select.NodeVisitor;

/**
 * HTML to plain-text. This example program demonstrates the use of jsoup to
 * convert HTML input to lightly-formatted plain-text. That is divergent from
 * the general goal of jsoup's .text() methods, which is to get clean data from
 * a scrape.
 * <p/>
 * Note that this is a fairly simplistic formatter -- for real world use you'll
 * want to embrace and extend.
 *
 * @author Jonathan Hedley, jonathan@hedley.net
 */
public class HtmlToPlain {
//    public static void main(String... args) throws IOException {
//        Validate.isTrue(args.length == 1, "usage: supply url to fetch");
//        String url = args[0];
//
//        // fetch the specified URL and parse to a HTML DOM
//        Document doc = Jsoup.connect(url).get();
//
//        HtmlToPlainText formatter = new HtmlToPlainText();
//        String plainText = formatter.getPlainText(doc);
//        System.out.println(plainText);
//    }

    /**
     * Format an Element to plain-text
     *
     * @param element the root element to format
     * @param url
     * @return formatted text
     */
    public String getPlainText(Element element, boolean url) {
        FormattingVisitor formatter = new FormattingVisitor(url);
        NodeTraversor traversor = new NodeTraversor(formatter);
        traversor.traverse(element); // walk the DOM, and call .head() and .tail() for each node
        return formatter.toString();
    }

    public String getMailText(Document doc, String subject) throws UnsupportedEncodingException {
        String arrayURLAtt[] = new String[]{"href", "src", "action", "data", "cite"};
        for (String att : arrayURLAtt) {
            Elements links = doc.select("[" + att + "]");
            for (Element elem : links) {
                String abs = elem.attr("abs:" + att);
                if (abs.startsWith("http://")
                        || abs.startsWith("https://")) {
                    elem.attr(att, "mailto:app@simplejtpc.appspotmail.com?subject=" + subject + "&body=" + URLEncoder.encode(abs, "UTF-8"));
                }
                if ("img".equals(elem.tagName()) && !"a".equals(elem.parent().tagName())) {
                    elem.wrap("<a href=\"mailto:app@simplejtpc.appspotmail.com?subject=get&body=" + URLEncoder.encode(abs, "UTF-8") + "\"> </a>");
                }
            }
        }
//        Elements links = doc.select("a[href]");
//        for (Element link : links) {
//            link.attr("href", "mailto:app@simplejtpc.appspotmail.com?subject=" + subject + "&body=" + link.attr("abs:href"));
//        }
        return doc.outerHtml();
    }

    // the formatting rules, implemented in a breadth-first DOM traverse
    private class FormattingVisitor implements NodeVisitor {

        private static final int maxWidth = 80;
        private int width = 0;
        private final StringBuilder accum = new StringBuilder(); // holds the accumulated text
        private final boolean url;

        private FormattingVisitor(boolean url) {
            this.url = url;
        }

        // hit when the node is first seen
        @Override
        public void head(Node node, int depth) {
            String name = node.nodeName();
//            System.out.println("name = " + name);
            if (node instanceof TextNode) {
                append(((TextNode) node).getWholeText()); // get whole text returns the text including newlines TextNodes carry all user-readable text in the DOM.
//                System.out.println("nodo texto = " + ((TextNode) node).getWholeText());
            } else if (name.equals("li")) {
                append("\n * ");
            }
        }

        // hit when all of the node's children (if any) have been visited
        @Override
        public void tail(Node node, int depth) {
            String name = node.nodeName();
            if (name.equals("br")) {
                append("\n");
            } else if (StringUtil.in(name, "p", "h1", "h2", "h3", "h4", "h5", "div")) {
                append("\n\n");
            } else if (url && name.equals("a")) {
                append(" " + node.absUrl("href") + " ");
            }
        }

        // appends text to the string builder with a simple word wrap method
        private void append(String text) {
            if (text.startsWith("\n")) {
                width = 0; // reset counter if starts with a newline. only from formats above, not in natural text
            }
            if (text.equals(" ")
                    && (accum.length() == 0 || StringUtil.in(accum.substring(accum.length() - 1), " ", "\n"))) {
                return; // don't accumulate long runs of empty spaces
            }
            if (text.length() + width > maxWidth) { // won't fit, needs to wrap
                String words[] = text.split("\\s+");
                for (int i = 0; i < words.length; i++) {
                    String word = words[i];
                    boolean last = i == words.length - 1;
                    if (!last) // insert a space if not the last word
                    {
                        word = word + " ";
                    }
                    if (word.length() + width > maxWidth) { // wrap and reset counter
                        accum.append("\n").append(word);
                        width = word.length();
                    } else {
                        accum.append(word);
                        width += word.length();
                    }
                }
            } else { // fits as is, without need to wrap text
                accum.append(text);
                width += text.length();
            }
        }

        @Override
        public String toString() {
            return accum.toString();
        }
    }
}
