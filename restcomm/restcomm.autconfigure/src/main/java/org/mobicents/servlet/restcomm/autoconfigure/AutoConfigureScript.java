package org.mobicents.servlet.restcomm.autoconfigure;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.xpath.XPathExpression;
import org.xml.sax.SAXException;

public class AutoConfigureScript {

    static// Properties properties = new Properties();

    InputStream inputStreamPropFile = null;
    XPathExpression expr;
    DocumentBuilderFactory docFactory = DocumentBuilderFactory
            .newInstance();
    DocumentBuilder docBuilder;
    Document xmlFile;
    XPath xpath = XPathFactory.newInstance().newXPath();
    Node node = null;
    NodeList nodeList = null;
    String key = null, value = null;
    String outputFile = null;
    String newPath = null;

    // static String getRestcommHome = null;
    // boolean thereIsMatch = false;
    static String xpathListFile = null;


    public static void main(String[] args) {

        if (args.length == 1) {
            xpathListFile = args[0];
            UpdateConfigurationFile r = new UpdateConfigurationFile();
            r.updateXmlConfigurationFiles();
        } else {

            System.out.println("ERROR : " + "This requires a single  argument");
            System.out.println("USAGE: "
                    + "java -jar ./auto-config.jar XPATH_FILENAME ");
        }

    }

    private void updateXmlConfigurationFiles() {

        try (BufferedReader br = new BufferedReader(new FileReader(
                xpathListFile))) {
            String sCurrentFilePath;
            ArrayList<String> lists = new ArrayList<String>();

            while ((sCurrentFilePath = br.readLine()) != null) {
                lists.add(sCurrentFilePath);

            }

            // getListSet();
            for (String list : lists) {
                if (list != null || list != "") {
                    String[] varString = list.split("==", 2);
                    key = varString[0]; // xpath from config file
                    String[] varString2 = varString[1].split(" /", 2);
                    value = varString2[0]; // xpath value to update
                    outputFile = "/" + varString2[1]; // xml file path ex. //
                                                      // /$RESTCOMM_HOME/.../restcomm.xml

                    if (outputFile.contains(".properties")) {
                        updatePropertiesFile(key, value, outputFile);
                    } else if (outputFile.contains(".xml")) {
                        getNode(key, value, outputFile);
                    } else {
                        System.out
                                .println("ERROR : Your output file must be an XML or .properties file");
                    }

            }
            }
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    private Document openFile(String fileOutput) {
        try {
            docBuilder = docFactory.newDocumentBuilder();
            xmlFile = docBuilder.parse(fileOutput);
        } catch (ParserConfigurationException | SAXException | IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return xmlFile;
    }

    private void getNode(String newPath, String value, String xmlFileToUpdate) {
        Document doc = openFile(xmlFileToUpdate);

        if (newPath != null) {
            Node updateNode;
                try {
                updateNode = (Node) xpath.compile(newPath).evaluate(doc,
                        XPathConstants.NODE);
                // System.out.println("vaueto update:" +
                // updateNode.getNodeValue());
                // Element element = doc.getDocumentElement();
                System.out.println(" newPath :" + newPath);
                if (updateNode != null) {
                    System.out.println(" doc  updateNode:"
                            + updateNode.getNodeName());
                    System.out.println(" doc  updateNode.getNodeType:"
                            + updateNode.getNodeType());
                    if (updateNode.getNodeType() == 1) {
                        System.out.println(" doc  getParentNode():"
                                + updateNode.getParentNode().getNodeName());

                    }
                }

                // if attribute doesn't exist, create it
                if (updateNode == null) {
                    // if missing node is attribute
                    if (newPath.contains("/@")) {
                        // replace path to get only the attribute
                        // ex. //restcomm/mscontrol/media-server/@class the
                        // regex will return class
                        String change = newPath.replaceAll("^(.*?)/@", "");
                        String change2 = "/@" + change;
                        String nPath = newPath.replace(change2, "");
                        Node updateNode2 = (Node) xpath.compile(nPath)
                                .evaluate(doc, XPathConstants.NODE);
                        Element elem = (Element) doc.getElementsByTagName(
                                updateNode2.getNodeName()).item(0);
                        // add attribute
                        elem.setAttribute(change, value);
                    } else {
                        // missing node is an element so create it
                        // TODO create missing tag element

                    }
                } else {
                    if (updateNode.getNodeType() == 2) {
                        String change = "/@" + updateNode.getNodeName();
                        String nPath = newPath.replace(change, "");

                        System.out.println("nPath :  " + nPath);

                        Node updateNode2 = (Node) xpath.compile(nPath)
                                .evaluate(doc, XPathConstants.NODE);
                        Element elem = (Element) doc.getElementsByTagName(
                                updateNode2.getNodeName()).item(0);

                        elem.setAttribute(updateNode.getNodeName(), value);

                    }
                }

                if (updateNode != null) {
                    updateNode.setTextContent(value);
                }
            // write the content into xml file
            TransformerFactory transformerFactory = TransformerFactory
                    .newInstance();
                Transformer transformer = transformerFactory.newTransformer();
                DOMSource source = new DOMSource(doc);
                StreamResult result = new StreamResult(
                        new File(xmlFileToUpdate));
            transformer.transform(source, result);
            } catch (XPathExpressionException | TransformerException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
    }}

    private void updatePropertiesFile(String key, String value,
            String propertiesFileToUpdate) {

        OutputStream output = null;
        try {
            FileInputStream input = new FileInputStream(propertiesFileToUpdate);
            Properties prop = new Properties();
            prop.load(input);
            input.close();
            output = new FileOutputStream(propertiesFileToUpdate);
            // set the properties value
            prop.setProperty(key, value);

            // save properties to project root folder
            prop.store(output, null);
            output.close();

        } catch (IOException io) {
            io.printStackTrace();
        } finally {
            if (output != null) {
                try {
                    output.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }

    }

}




