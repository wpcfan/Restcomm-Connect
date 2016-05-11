package org.mobicents.servlet.restcomm.autoconfigure;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Map;
import java.util.Scanner;

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
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.xpath.XPathExpression;
import org.xml.sax.SAXException;

public class AutoConfigureScript {

    static// Properties properties = new Properties();
    String fileRestcommConfileFile = null;
    static String listConfigurationFiles = null;
    static String inputFilePathConf = null;
    boolean isAttribute = false;
    String attributeName = null;
    InputStream inputStreamPropFile = null;
    XPathExpression expr;
    DocumentBuilderFactory docFactory = DocumentBuilderFactory
            .newInstance();
    DocumentBuilder docBuilder;
    Document docStandaloneSipXml;
    XPath xpath = XPathFactory.newInstance().newXPath();
    Node node = null;
    NodeList nodeList = null;
    String key = null, value = null;
    String newPath = null;
    static String getRestcommHome = null;
    boolean thereIsMatch = false;


    public static void main(String[] args) {


        Map<String, String> env = System.getenv();
        for (String envName : env.keySet()) {
            if (envName.startsWith("PWD")) {
                getRestcommHome = env.get(envName)
                        .replace("/bin/restcomm", "");
                System.out.println("Restcomm Home : " + getRestcommHome);

            }
        }
        // set config files

        fileRestcommConfileFile = getRestcommHome
                + "/bin/restcomm/restcomm.conf";
        listConfigurationFiles = getRestcommHome
                + "/bin/restcomm/list-config-files.conf";
        inputFilePathConf = getRestcommHome + "bin/restcomm/restcomm-new.conf";

        AutoConfigureScript r = new AutoConfigureScript();
        r.updateXmlFiles();

    }

    public void updateXmlFiles() {

        try (BufferedReader br = new BufferedReader(new FileReader(
                listConfigurationFiles))) {
            String sCurrentFilePath;
            String sCurrentFilePathWithRestcommHome;
            while ((sCurrentFilePath = br.readLine()) != null) {
                if (sCurrentFilePath.startsWith("/")) {
                    sCurrentFilePathWithRestcommHome = getRestcommHome
                            + sCurrentFilePath;
                System.out.println("text :" + sCurrentFilePath);
                    updateXmlConfigurationFiles(sCurrentFilePathWithRestcommHome);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void updateXmlConfigurationFiles(String currentOutputFilePath) {
        Scanner scanner = openFile(currentOutputFilePath);
        ArrayList<String> lists = new ArrayList<String>();
        // put content of file into an arraylist
        while (scanner.hasNextLine()) {
            lists.add(scanner.nextLine());
        }
        // getListSet();
        for (String list : lists) {
            if (list.startsWith("//") && list != null) {
                String[] varString = list.split("==", 2);
                if (varString.length == 2) {
                    key = varString[0];
                    value = varString[1];
                }
                String valueFromRestcommFile = getValueFromRestcommConfFile(value
                        .toLowerCase());
                // System.out.println("file :" + currentOutputFilePath);
                // System.out.println("file :" + value);
                if (thereIsMatch) {
                    getNode(key, valueFromRestcommFile, currentOutputFilePath);
                    // reset if there is a match
                    thereIsMatch = false;
                } else {

                    getNode(key, value, currentOutputFilePath);
                }

            }
        }

    }

    private String getValueFromRestcommConfFile(String confVariable) {
        String value = null;
        try (BufferedReader br = new BufferedReader(new FileReader(
                fileRestcommConfileFile))) {
            String sCurrentLine;
            while ((sCurrentLine = br.readLine()) != null) {
                if (sCurrentLine.toLowerCase().startsWith(confVariable)) {
                    // remove space comments from line
                    String[] rSpace = sCurrentLine.split(" ", 2);
                    // remove tab
                    String[] rTab = rSpace[0].split("\t", 2);
                    String[] var = rTab[0].split("=", 2);
                    value = var[1].replace("'", "").trim();

                    System.out.println(var[0] + " :  : " + value);
                    thereIsMatch = true;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return value;
    }

    private Scanner openFile(String fileOutput) {
        Scanner scanner = null;
        try {
            docBuilder = docFactory.newDocumentBuilder();
            docStandaloneSipXml = docBuilder
.parse(fileOutput);
            scanner = new Scanner(new File(inputFilePathConf));
        } catch (ParserConfigurationException | SAXException | IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return scanner;
    }


    private void getNode(String newPath, String valueFromRestcommFile,
            String currentOutputFilePath) {
        Scanner s = openFile(currentOutputFilePath);
        if (newPath != null) {
            Node updateNode;
                try {
                updateNode = (Node) xpath.compile(newPath).evaluate(
                        docStandaloneSipXml,
                        XPathConstants.NODE);

                // System.out.println("vaueto update:" +
                // updateNode.getNodeValue());
                if (updateNode != null) {
                    updateNode.setTextContent(valueFromRestcommFile);
                }
            // write the content into xml file
            TransformerFactory transformerFactory = TransformerFactory
                    .newInstance();
            Transformer transformer;
            transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(docStandaloneSipXml);
            StreamResult result = new StreamResult(new File(
                        currentOutputFilePath));
            transformer.transform(source, result);
                s.close();

            } catch (XPathExpressionException | TransformerException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
    }}






}




