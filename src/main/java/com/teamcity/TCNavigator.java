package com.teamcity;

import com.teamcity.enums.TCStatus;
import org.jsoup.Jsoup;
import org.jsoup.select.Elements;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;


public class TCNavigator {
    private Document document;
    private final RESTInvoker restInvoker;

    private static final String ID = "id";
    private static final String HREF = "href";
    private static final String buildType = "buildType";


    public TCNavigator(RESTInvoker restInvoker) {
        this.restInvoker = restInvoker;
    }

    private void parseXMLString(String xmlContent) {
        InputStream inputStream = new ByteArrayInputStream(xmlContent.getBytes(Charset.forName("UTF-8")));

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            document = builder.parse(inputStream);
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            document.normalize();
        }
    }

    public TCNavigator openBuildsForProjectById(String path, String id) {
        String projectResponse = restInvoker.getDataFromServer(path);
        parseXMLString(projectResponse);

        NodeList nodeList = document.getElementsByTagName(buildType);
        for (int bt = 0; bt < nodeList.getLength(); bt++) {
            Node buildTypeNode = nodeList.item(bt);
            NamedNodeMap nodeMap = buildTypeNode.getAttributes();
            String bt_id = nodeMap.getNamedItem(ID).getNodeValue();
            String bt_href = nodeMap.getNamedItem(HREF).getNodeValue();

            if (bt_id.equals(id)) {
                String buildResponse = restInvoker.getDataFromServer(bt_href);
                parseXMLString(buildResponse);
                return this;
            }
        }

        return null;
    }

    public void openBuildByIndex(int index) {
        NodeList nodeList = document.getElementsByTagName("builds");
        Node buildsNode = nodeList.item(0);
        NamedNodeMap nodeMap = buildsNode.getAttributes();
        String b_href = nodeMap.getNamedItem("href").getNodeValue();
        String response = restInvoker.getDataFromServer(b_href + "?count=" + index+1);
        parseXMLString(response);

        b_href = getElementHRefByTag("build", index);
        response = restInvoker.getDataFromServer(b_href);
        parseXMLString(response);
    }

    private String getElementHRefByTag(String tag, int index) {
        NodeList nodeList = document.getElementsByTagName(tag);

        Node buildNode = nodeList.item(index);
        if (buildNode == null)
            return null; //Nothing here
        NamedNodeMap nodeMap = buildNode.getAttributes();
        return nodeMap.getNamedItem("href").getNodeValue();
    }

    public String getBuildStartDate() {
        return document.getElementsByTagName("startDate").item(0).getChildNodes().item(0).getNodeValue();
    }

    private String getNodeHRefByTagAndProperty(String tag, String propertyName, String propertyValue) {
        NodeList nodeList = document.getElementsByTagName(tag);
        for (int f=0; f<nodeList.getLength(); f++) {
            Node indexNode = nodeList.item(f);
            NamedNodeMap map = indexNode.getAttributes();
            String name = map.getNamedItem(propertyName).getNodeValue();
            if (name.equals(propertyValue)) {
                return map.getNamedItem("href").getNodeValue();
            }
        }

        return null;
    }

    public TCResult getResultsForBuild() {
        String startDateTime = getBuildStartDate();
        TCResult tcResult = new TCResult();

        String a_href = getElementHRefByTag("artifacts", 0);
        String artifactsResponse = restInvoker.getDataFromServer(a_href);
        parseXMLString(artifactsResponse);

        //here
        String c_href = getElementHRefByTag("children", 0);
        if (c_href == null)
            return tcResult;
        String contentResponse = restInvoker.getDataFromServer(c_href);
        parseXMLString(contentResponse);

        c_href = getElementHRefByTag("children", 0);
        contentResponse = restInvoker.getDataFromServer(c_href);
        parseXMLString(contentResponse);

        c_href = getElementHRefByTag("children", 0);
        contentResponse = restInvoker.getDataFromServer(c_href);
        parseXMLString(contentResponse);

        String i_href = getNodeHRefByTagAndProperty("file", "name", "index.html");
        String indexResponse = restInvoker.getDataFromServer(i_href);
        parseXMLString(indexResponse);

        c_href = getElementHRefByTag("content", 0);
        String htmlResponse = restInvoker.getDataFromServer(c_href);
        org.jsoup.nodes.Document htmlDoc = Jsoup.parse(htmlResponse);

        Elements failClasses = htmlDoc.getElementsByClass("suite-Full_System_Test-class-failed");
        int failClassNumb = failClasses.size();
        for (int fail=0; fail< failClassNumb; fail++) {
            org.jsoup.nodes.Element failClass = failClasses.get(fail);
            Elements failingTestsPerClass = failClass.getElementsByClass("method-content");
            Elements failingClassName = failClass.getElementsByClass("class-name");
            String className = failingClassName.get(0).html();
            for (int fClass = 0; fClass<failingTestsPerClass.size(); fClass++) {
                org.jsoup.nodes.Element failingTest = failingTestsPerClass.get(fClass);
                Elements failingMethodNameClass = failingTest.getElementsByClass("method-name");
                String methodName = failingMethodNameClass.get(0).html();
                Elements failingParametersClass = failingTest.getElementsByClass("parameters");
                String parameters = "";
                if (failingParametersClass.size() > 0)
                    parameters = failingParametersClass.get(0).html();
                Elements failingStackTraceClass = failingTest.getElementsByClass("stack-trace");
                String stackTrace = failingStackTraceClass.get(0).html();
                tcResult.addTest(className, methodName, parameters, stackTrace, startDateTime, TCStatus.FAIL);
            }
        }
        Elements skipClasses = htmlDoc.getElementsByClass("suite-Full_System_Test-class-skipped");
        int skipClassNumb = skipClasses.size();
        for (int skip=0; skip< skipClassNumb; skip++) {
            org.jsoup.nodes.Element skipClass = skipClasses.get(skip);
            Elements skipTestsPerClass = skipClass.getElementsByClass("method-content");
            Elements skipClassName = skipClass.getElementsByClass("class-name");
            String className = skipClassName.get(0).html();
            for (int sClass = 0; sClass<skipTestsPerClass.size(); sClass++) {
                org.jsoup.nodes.Element skipTest = skipTestsPerClass.get(sClass);
                Elements skipMethodNameClass = skipTest.getElementsByClass("method-name");
                String methodName = skipMethodNameClass.get(0).html();
                Elements skipParametersClass = skipTest.getElementsByClass("parameters");
                String parameters = "";
                if (skipParametersClass.size() > 0)
                    parameters = skipParametersClass.get(0).html();
                Elements skipStackTraceClass = skipTest.getElementsByClass("stack-trace");
                String stackTrace = "";
                if (skipStackTraceClass.size() > 0) {
                    stackTrace = skipStackTraceClass.get(0).html();
                }
                tcResult.addTest(className, methodName, parameters, stackTrace, startDateTime, TCStatus.SKIP);
            }
        }
        Elements passClasses = htmlDoc.getElementsByClass("suite-Full_System_Test-class-passed");
        int passClassNumb = passClasses.size();
        for (int pass=0; pass< passClassNumb; pass++) {
            org.jsoup.nodes.Element passClass = passClasses.get(pass);
            Elements passTestsPerClass = passClass.getElementsByClass("method-content");
            Elements passClassName = passClass.getElementsByClass("class-name");String className = passClassName.get(0).html();
            for (int pClass = 0; pClass<passTestsPerClass.size(); pClass++) {
                org.jsoup.nodes.Element passTest = passTestsPerClass.get(pClass);
                Elements passMethodNameClass = passTest.getElementsByClass("method-name");
                String methodName = passMethodNameClass.get(0).html();
                Elements passParametersClass = passTest.getElementsByClass("parameters");
                String parameters = "";
                if (passParametersClass.size() > 0)
                    parameters = passParametersClass.get(0).html();
                Elements passStackTraceClass = passTest.getElementsByClass("stack-trace");
                String stackTrace = "";
                if (passStackTraceClass.size() > 0) {
                    stackTrace = passStackTraceClass.get(0).html();
                }
                tcResult.addTest(className, methodName, parameters, stackTrace, startDateTime, TCStatus.PASS);
            }
        }

        return tcResult;
    }
}
