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
import java.util.HashMap;
import java.util.Map;

import static com.teamcity.enums.TCParam.*;


public class TCNavigator {
    public static final String DATE_PATTERN = "yyyyMMdd'T'HHmmssZ";
    public static final String METHOD_CONTENT = "method-content";
    public static final String CLASS_NAME = "class-name";
    public static final String METHOD_NAME = "method-name";
    public static final String PARAMETERS = "parameters";
    public static final String STACK_TRACE = "stack-trace";
    public static final String TEST_FAILED = "class-failed";
    public static final String TEST_SKIPPED = "class-skipped";
    public static final String TEST_PASSED = "class-passed";
    public static final String CHILDREN = "children";
    public static final String ARTIFACTS = "artifacts";
    public static final String FILE = "file";
    public static final String NAME = "name";
    public static final String TC_REST_PROJECTS = "app/rest/projects/";
    private Document document;
    private final RESTInvoker restInvoker;

    private static final String buildType = "buildType";
    private boolean personalBuild = false;

    private HashMap<String, String> filterProperties = new HashMap<>();


    public TCNavigator(RESTInvoker restInvoker) {
        this.restInvoker = restInvoker;
    }

    public void addFilterProperty(String key, String value) {
        filterProperties.put(key, value);
    }

    public void setPersonalBuild(boolean personalBuild) {
        this.personalBuild = personalBuild;
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
        String projectResponse = restInvoker.getDataFromServer(TC_REST_PROJECTS + projectName);
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

    public boolean openBuildByIndex(int index) {
        NodeList nodeList = document.getElementsByTagName("builds");
        Node buildsNode = nodeList.item(0);
        NamedNodeMap nodeMap = buildsNode.getAttributes();
        String b_href = nodeMap.getNamedItem("href").getNodeValue();
        String builds_href = b_href/* + "?count=" + index+1*/;
        if (personalBuild)
            builds_href += "?locator=personal:true";
        String response = restInvoker.getDataFromServer(builds_href);
        parseXMLString(response);

        b_href = getElementHRefByTag("build", index);
        if (b_href == null)
            return false;

        response = restInvoker.getDataFromServer(b_href);
        parseXMLString(response);

        return true;
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

    public TCResults getTestNGResultsForBuild() {
        LocalDateTime startDateTime = getBuildStartDate();
        LocalDateTime finishDateTime = getBuildFinishDate();
        HashMap<String, String> properties = getBuildProperties();

        TCResults tcResults = new TCResults();
        if (filterByProperties(properties))
            return tcResults;

        String a_href = getElementHRefByTag(ARTIFACTS, 0);
        String artifactsResponse = restInvoker.getDataFromServer(a_href);
        parseXMLString(artifactsResponse);

        //here
        String c_href = getElementHRefByTag(CHILDREN, 0);
        if (c_href == null)
            return tcResults;
        String contentResponse = restInvoker.getDataFromServer(c_href);
        parseXMLString(contentResponse);

        c_href = getElementHRefByTag(CHILDREN, 0);
        contentResponse = restInvoker.getDataFromServer(c_href);
        parseXMLString(contentResponse);

        c_href = getElementHRefByTag(CHILDREN, 0);
        contentResponse = restInvoker.getDataFromServer(c_href);
        parseXMLString(contentResponse);

        String i_href = getNodeHRefByTagAndProperty(FILE, NAME, "index.html");
        String indexResponse = restInvoker.getDataFromServer(i_href);
        parseXMLString(indexResponse);

        c_href = getElementHRefByTag("content", 0);
        String htmlResponse = restInvoker.getDataFromServer(c_href);
        org.jsoup.nodes.Document htmlDoc = Jsoup.parse(htmlResponse);

        //add results for this build
        addResultsFromType(htmlDoc, tcResults, startDateTime, TEST_FAILED, TCStatus.FAIL);
        addResultsFromType(htmlDoc, tcResults, startDateTime, TEST_SKIPPED, TCStatus.SKIP);
        addResultsFromType(htmlDoc, tcResults, startDateTime, TEST_PASSED, TCStatus.PASS);

        return tcResults;
    }

    private boolean filterByProperties(HashMap<String, String> properties) {
        for (Map.Entry<String, String> filterProperty : filterProperties.entrySet()) {
            if (!properties.get(filterProperty.getKey()).equals(filterProperty.getValue()))
                return true;
        }
        return false;
    }

    private HashMap<String, String> getBuildProperties() {
        HashMap<String, String> buildProperties = new HashMap<>();
        NodeList propertyList = document.getElementsByTagName("property");
        for (int bt = 0; bt < propertyList.getLength(); bt++) {
            Node buildTypeNode = propertyList.item(bt);
            NamedNodeMap nodeMap = buildTypeNode.getAttributes();
            String name = nodeMap.getNamedItem("name").getNodeValue();
            String value = nodeMap.getNamedItem("value").getNodeValue();
            buildProperties.put(name, value);
        }
        return buildProperties;
    }

    private void addResultsFromType(org.jsoup.nodes.Document htmlDoc, TCResults tcResults, LocalDateTime startDateTime,
                                    String elementClass, TCStatus status) {
        String suiteName = getClassNameForStatus(htmlDoc, elementClass);
        Elements elementClasses = htmlDoc.getElementsByClass(suiteName);
        int classNumb = elementClasses.size();
        for (int element=0; element< classNumb; element++) {
            org.jsoup.nodes.Element nodeClass = elementClasses.get(element);
            Elements testsPerClass = nodeClass.getElementsByClass(METHOD_CONTENT);
            Elements nodeClassName = nodeClass.getElementsByClass(CLASS_NAME);
            String className = nodeClassName.get(0).html();
            className = className.replace("com.philips.sapphire.systemintegrationtests.", "...");
            for (int eClass = 0; eClass<testsPerClass.size(); eClass++) {
                org.jsoup.nodes.Element elementTest = testsPerClass.get(eClass);
                Elements methodNameClass = elementTest.getElementsByClass(METHOD_NAME);
                String methodName = methodNameClass.get(0).html();
                Elements parametersClass = elementTest.getElementsByClass(PARAMETERS);
                String parameters = "";
                if (parametersClass.size() > 0)
                    parameters = parametersClass.get(0).html();
                Elements stackTraceClass = elementTest.getElementsByClass(STACK_TRACE);
                String stackTrace = "";
                if (stackTraceClass.size() > 0)
                    stackTrace = stackTraceClass.get(0).html();
                tcResults.addTest(className, methodName, parameters, stackTrace, startDateTime, status);
            }
        }
    }

    private String getClassNameForStatus(org.jsoup.nodes.Document htmlDoc, String elementClass) {
        Elements mainResultPanel = htmlDoc.getElementsByClass("main-panel-root");
        Elements mainResultsElements = mainResultPanel.get(0).getElementsByClass("panel");
        String suiteName = "";
        for (int e=0; e<mainResultsElements.size(); e++)
            if (mainResultsElements.get(e).attr("panel-name").startsWith("suite-"))
                suiteName = mainResultsElements.get(e).attr("panel-name") + "-" + elementClass;
        return suiteName;
    }

    public TCResults getTestNGResultsForBuild(String project, TCParam param, String paramValue, int build) {
        openBuildsForProject(project, param, paramValue);

        if (openBuildByIndex(build))
            return getTestNGResultsForBuild();

        return null;
    }
}
