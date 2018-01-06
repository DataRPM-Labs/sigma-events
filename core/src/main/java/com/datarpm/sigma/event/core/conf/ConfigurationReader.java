/*******************************************************************************
 * Copyright 2017 DataRPM
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package com.datarpm.sigma.event.core.conf;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ConfigurationReader {
  public static ConfigurationReader INSTANCE = new ConfigurationReader();

  private Map<String, Configuration> configurationMap;

  private ConfigurationReader() {
    configurationMap = new ConcurrentHashMap<>();
  }

  public Configuration readConfiguration(String siteXMLFilePath) throws IOException {
    Configuration configuration = configurationMap.get(siteXMLFilePath);
    if (configuration != null) {
      return configuration;
    }

    synchronized (INSTANCE) {
      configuration = configurationMap.get(siteXMLFilePath);
      if (configuration != null) {
        return configuration;
      }

      configuration = parse(siteXMLFilePath);
      configurationMap.put(siteXMLFilePath, configuration);
    }

    return configuration;
  }

  private Configuration parse(String siteXMLFilePath) throws IOException {
    URL resourceURL = configFilePath(siteXMLFilePath);
    if (resourceURL == null) {
      return new Configuration(new Properties());
    }
    try {
      DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
      factory.setNamespaceAware(true);
      DocumentBuilder documentBuilder = factory.newDocumentBuilder();
      Document document = documentBuilder.parse(resourceURL.toURI().toString());

      XPathFactory xpathFactory = XPathFactory.newInstance();
      XPath xpath = xpathFactory.newXPath();
      XPathExpression expression = xpath.compile("/configuration/property");

      Properties properties = new Properties();
      NodeList nodes = (NodeList) expression.evaluate(document, XPathConstants.NODESET);
      for (int index = 0; index < nodes.getLength(); index++) {
        Node node = nodes.item(index);
        NodeList childNodes = node.getChildNodes();
        String name = null;
        String value = null;
        for (int childIndex = 0; childIndex < childNodes.getLength(); childIndex++) {
          Node childNode = childNodes.item(childIndex);
          String nodeName = childNode.getNodeName();
          String textContent = childNode.getTextContent();
          if ("name".equals(nodeName)) {
            name = textContent;
          } else if ("value".equals(nodeName)) {
            value = textContent;
          }
        }
        properties.setProperty(name, value);
      }
      return new Configuration(properties);
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }

  private URL configFilePath(String siteXMLFilePath) throws MalformedURLException {

    String configDir = System.getenv("EVENTENGINE_CONF_DIR");
    if (configDir != null && !configDir.trim().isEmpty()) {
      File f = new File(configDir + File.separator + siteXMLFilePath);
      if (f.exists()) {
        return f.toURI().toURL();
      }
    }

    String eventEngineHome = System.getenv("EVENTENGINE_HOME");
    if (eventEngineHome != null && !eventEngineHome.trim().isEmpty()) {
      File f = new File(
          eventEngineHome + File.separator + "conf" + File.separator + siteXMLFilePath);
      if (f.exists()) {
        return f.toURI().toURL();
      }
    }

    return ConfigurationReader.class.getResource("/" + siteXMLFilePath);
  }

}
