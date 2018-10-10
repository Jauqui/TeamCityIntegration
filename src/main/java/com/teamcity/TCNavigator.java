package com.teamcity;

import com.teamcity.enums.TCParam;
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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static com.teamcity.enums.TCParam.*;


public class TCNavigator {
    public static final String DATE_PATTERN = "yyyyMMdd'T'HHmmssZ";
    private Document document;
    private final RESTInvoker restInvoker;

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

    public TCNavigator openBuildsForProject(String projectName, TCParam param, String paramValue) {
        String projectResponse = restInvoker.getDataFromServer("app/rest/projects/" + projectName);
        parseXMLString(projectResponse);

        NodeList nodeList = document.getElementsByTagName(buildType);
        for (int bt = 0; bt < nodeList.getLength(); bt++) {
            Node buildTypeNode = nodeList.item(bt);
            NamedNodeMap nodeMap = buildTypeNode.getAttributes();
            String bt_param = nodeMap.getNamedItem(param.getParameter()).getNodeValue();
            String bt_href = nodeMap.getNamedItem(HREF.getParameter()).getNodeValue();

            if (bt_param.equals(paramValue)) {
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

    public LocalDateTime getBuildStartDate() {
        String startDateTime = document.getElementsByTagName("startDate").item(0).getChildNodes().item(0).getNodeValue();
        return LocalDateTime.parse(startDateTime, DateTimeFormatter.ofPattern(DATE_PATTERN));
    }

    public LocalDateTime getBuildFinishDate() {
        String startDateTime = document.getElementsByTagName("finishDate").item(0).getChildNodes().item(0).getNodeValue();
        return LocalDateTime.parse(startDateTime, DateTimeFormatter.ofPattern(DATE_PATTERN));
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

    public TCResults getResultsForBuild() {
        LocalDateTime startDateTime = getBuildStartDate();
        LocalDateTime finishDateTime = getBuildFinishDate();
        TCResults tcResults = new TCResults();

        String a_href = getElementHRefByTag("artifacts", 0);
        String artifactsResponse = restInvoker.getDataFromServer(a_href);
        parseXMLString(artifactsResponse);

        //here
        String c_href = getElementHRefByTag("children", 0);
        if (c_href == null)
            return tcResults;
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

        //add results for this build
        addResultsFromType(htmlDoc, tcResults, startDateTime, "suite-Full_System_Test-class-failed", TCStatus.FAIL);
        addResultsFromType(htmlDoc, tcResults, startDateTime, "suite-Full_System_Test-class-skipped", TCStatus.SKIP);
        addResultsFromType(htmlDoc, tcResults, startDateTime, "suite-Full_System_Test-class-passed", TCStatus.PASS);

        return tcResults;
    }

    private void addResultsFromType(org.jsoup.nodes.Document htmlDoc, TCResults tcResults, LocalDateTime startDateTime,
                                    String elementClass, TCStatus status) {
        Elements elementClasses = htmlDoc.getElementsByClass(elementClass);
        int classNumb = elementClasses.size();
        for (int element=0; element< classNumb; element++) {
            org.jsoup.nodes.Element nodeClass = elementClasses.get(element);
            Elements testsPerClass = nodeClass.getElementsByClass("method-content");
            Elements nodeClassName = nodeClass.getElementsByClass("class-name");
            String className = nodeClassName.get(0).html();
            className = className.replace("com.philips.sapphire.systemintegrationtests.", "...");
            for (int eClass = 0; eClass<testsPerClass.size(); eClass++) {
                org.jsoup.nodes.Element elementTest = testsPerClass.get(eClass);
                Elements methodNameClass = elementTest.getElementsByClass("method-name");
                String methodName = methodNameClass.get(0).html();
                Elements parametersClass = elementTest.getElementsByClass("parameters");
                String parameters = "";
                if (parametersClass.size() > 0)
                    parameters = parametersClass.get(0).html();
                Elements stackTraceClass = elementTest.getElementsByClass("stack-trace");
                String stackTrace = "";
                if (stackTraceClass.size() > 0)
                    stackTrace = stackTraceClass.get(0).html();
                tcResults.addTest(className, methodName, parameters, stackTrace, startDateTime, status);
            }
        }
    }

    public TCResults getResultsForBuild(String project, TCParam param, String paramValue, int build) {
        openBuildsForProject(project, param, paramValue);
        openBuildByIndex(build);

        return getResultsForBuild();
    }
}
