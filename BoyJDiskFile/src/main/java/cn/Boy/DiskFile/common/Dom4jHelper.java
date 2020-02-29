package cn.Boy.DiskFile.common;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.XPath;
import org.dom4j.io.SAXReader;
import org.dom4j.io.SAXValidator;
import org.dom4j.io.XPP3Reader;
import org.dom4j.util.XMLErrorHandler;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * 描述:Dom4j解析工具类
 * **/
@Component("Dom4jHelper")
@Scope("prototype")
public class Dom4jHelper {

    private Document document = null;
    private XMLErrorHandler errorHandler = null;
    private SAXValidator validator = null;
    private String encoding = "UTF-8";
    private String xsd = null;
    private Map<String, String> params = null;
    public static final String  SAX_PARSE_MODEL = "SAX";
    public static final String  XPP3_PARSE_MODEL = "XPP3";

    public Dom4jHelper(){
        this.params = new HashMap<String, String>();
    }

    public void parseXML(File file){
        parseXML(file,XPP3_PARSE_MODEL);
    }

    public void parseXML(File file,String mode){
        try {
            if(mode==null){
                throw new RuntimeException("xml parse model is null!");
            }
            if(mode.equals(SAX_PARSE_MODEL)){
                SAXReader reader = new SAXReader();
                //reader.getDocumentFactory().setXPathNamespaceURIs(params);
                this.document = reader.read(file);
            }else if(mode.equals(XPP3_PARSE_MODEL)){
                XPP3Reader reader = new XPP3Reader();
                this.document = reader.read(file);
                //reader.getDocumentFactory().setXPathNamespaceURIs(params);
            }
            this.document.setXMLEncoding(this.encoding);

            if(this.xsd!=null){
                this.validateXMLFormat();
            }
        } catch (DocumentException e) {
            throw new RuntimeException("dom4j load document error:",e);
        }catch(RuntimeException e){
            throw e;
        }catch (Exception e) {
            throw new RuntimeException("dom4j load document error:",e);
        }
    }

    public void parseXML(String text){
        parseXML(text,XPP3_PARSE_MODEL);
    }

    public void parseXML(String text,String mode){
        try {
            if(mode==null){
                throw new RuntimeException("xml parse model is null!");
            }

            if(mode.equals(SAX_PARSE_MODEL)){
                SAXReader reader = new SAXReader();
                //reader.getDocumentFactory().setXPathNamespaceURIs(params);
                this.document = reader.read(new StringReader(text));
            }else if(mode.equals(XPP3_PARSE_MODEL)){
                XPP3Reader reader = new XPP3Reader();
                //reader.getDocumentFactory().setXPathNamespaceURIs(params);
                this.document = reader.read(new StringReader(text));
            }
            this.document.setXMLEncoding(this.encoding);

            if(this.xsd!=null){
                this.validateXMLFormat();
            }
        } catch (DocumentException e) {
            throw new RuntimeException("dom4j load document error:",e);
        }catch(RuntimeException e){
            throw e;
        }catch (Exception e) {
            throw new RuntimeException("dom4j load document error:",e);
        }
    }

    /**
     * 描述:验证XML格式方法
     * **/
    private void validateXMLFormat(){
        InputStream is = null;
        try{
            if(this.validator==null){
                this.errorHandler = new XMLErrorHandler();
                SAXParserFactory factory = SAXParserFactory.newInstance();
                factory.setValidating(true);
                factory.setNamespaceAware(true);
                SAXParser parser = factory.newSAXParser();
                parser.setProperty(
                        "http://java.sun.com/xml/jaxp/properties/schemaLanguage",
                        "http://www.w3.org/2001/XMLSchema");

                is = this.getClass().getClassLoader().getResourceAsStream(xsd);
                parser.setProperty("http://java.sun.com/xml/jaxp/properties/schemaSource",is);

                this.validator = new SAXValidator(parser.getXMLReader());
                this.validator.setErrorHandler(this.errorHandler);
                this.validator.validate(this.document);
            }

            if (this.errorHandler.getErrors().hasContent()) {
                throw new RuntimeException("xml validate format error:"+this.errorHandler.getErrors().getStringValue());
            }
        }catch(RuntimeException e){
            throw e;
        }catch(Exception e){
            throw new RuntimeException("xml validate format error:",e);
        }finally{
            if(is!=null){
                try {
                    is.close();
                } catch (IOException e) {
                    throw new RuntimeException("schema file stream close error:",e);
                }
            }
        }
    }

    /**
     * 描述:查找元素方法
     * @param	nodePath:xPath路径
     * **/
    public Node findSingleElement(String nodePath){
        Node node = this.document.selectSingleNode(nodePath);
        if(node==null){
            return null;
        }else{
            return node;
        }
    }

    /**
     * 描述:查找元素的文本值方法
     * @param	nodePath:xPath路径
     * **/
    public String findSingleElementText(String nodePath){
        Node node = this.document.selectSingleNode(nodePath);
        if(node==null){
            return null;
        }else{
            return node.getText();
        }
    }

    /**
     * 描述:查找元素的属性值方法
     * @param	nodePath:xPath路径
     * @param	attributeName:属性名称
     * **/
    public String findElementAttributeText(String nodePath,String attributeName){
        Node node = document.selectSingleNode(nodePath);
        if(node==null){
            return null;
        }else{
            return node.valueOf("@"+attributeName);
        }
    }

    /**
     * 描述:查找元素的属性值方法
     * @param	nodePath:xPath路径
     * @param	attributeName:属性名称
     * **/
    public String findElementAttributeText(Node node,String attributeName){
        if(node==null){
            return null;
        }else{
            return node.valueOf("@"+attributeName);
        }
    }

    /**
     * 描述:查找元素方法
     * @param	nodePath:xPath路径
     * **/
    public List<Node> findElement(String nodePath){
        List<Node> elements =  this.document.selectNodes(nodePath);
        return elements;
    }

    /**
     * 描述:查找元素的文本值方法
     * @param	nodePath:xPath路径
     * **/
    public String[] findElementsText(String nodePath){
        String[] texts = null;
        List elements =  this.document.selectNodes(nodePath);
        if(elements!=null){
            int size = elements.size();
            if(size>0){
                texts = new String[size];
                for(int i=0;i<size;i++){
                    texts[i] = ((Element)elements.get(i)).getText();
                }
            }

        }
        return texts;
    }

    /**
     * 描述:查找元素的文本值方法
     * @param	nodePath:		xPath路径
     * @param	childNodesPath:	xPath子路径
     * **/
    public String[][] findElementsText(String nodePath,String[] childNodesPath){
        String[][] texts = null;
        List elements = this.document.selectNodes(nodePath);
        List childElements = null;
        if(elements!=null){
            int size = elements.size();
            int childSize = 0;
            if(size>0){
                texts = new String[size][];
                for(int i=0;i<childNodesPath.length;i++){
                    childElements = ((Element) elements.get(i)).selectNodes(childNodesPath[i]);
                    if(childElements!=null&&childElements.size()>0){
                        texts[i] = new String[childSize];
                        for(int j=0;j<childSize;j++){
                            texts[i][j] = ((Element) childElements.get(j)).getText();
                        }
                    }

                }
            }
        }
        return texts;
    }

    /**
     * 描述 :增加节点方法
     * @param	nodePath:	节点路径
     * @param	nodeName:	新增节点名称
     * @param	text:		文本值
     * **/
    public void  addNode(String nodePath,String nodeName,String text){
        List<Element> elements = this.document.selectNodes(nodePath);
        Element element = null;
        if(elements!=null&&elements.size()>0){
            element = elements.get(0);
            Element channelElement = element.addElement(nodeName);
            channelElement.setText(text);
        }
    }

    /**
     * 描述:设置节点值方法
     * @param	nodePath:	节点路径
     * @param	text:		文本值
     * **/
    public void  setNodeText(String nodePath,String text){
        Node node = this.document.selectSingleNode(nodePath);
        if(node!=null){
            node.setText(text);
        }
    }

    /**
     * 描述:增加节点属性方法
     * @param	nodePath:	节点路径
     * @param	attributeName:	属性名称
     * @param	attributeValue:	属性值
     * **/
    public void  setNodeAttbiute(String nodePath,String attributeName,String attributeValue){
        List<Element> elements = this.document.selectNodes(nodePath);
        Element element = null;
        Attribute attribute = null;
        if(elements!=null&&elements.size()>0){
            element = elements.get(0);
            attribute =  element.attribute(attributeName);
            if(attribute!=null){
                attribute.setValue(attributeValue);
                List<Attribute> attributes =  new ArrayList<Attribute>();
                attributes.add(attribute);
                element.setAttributes(attributes);
            }
        }
    }

    /**
     * 描述:XML节点转换为文本方法
     * @param	nodePath:xPath路径
     * **/
    public String convertXMLNodeForText(String nodePath){
        Node node = this.document.selectSingleNode(nodePath);
        if(node!=null){
            return node.asXML();
        }
        return null;
    }

    /**
     * 描述:查找元素方法
     * @param	nodePath:xPath路径
     * **/
    public Node findSingleElementByXPath(String parentPath,String xpath){
        Node parentNode = this.document.selectSingleNode(parentPath);
        XPath xpathObj=document.createXPath(xpath);
        xpathObj.setNamespaceURIs(this.params);
        Node node = xpathObj.selectSingleNode(parentNode);
        if(node==null){
            return null;
        }else{
            return node;
        }
    }

    /**
     * 描述:查找元素的文本值方法(使用XPatch方式)
     * @param	nodePath:xPath路径
     * **/
    public String findSingleElementTextByXPath(String parentPath,String xpath){
        Node node = this.findSingleElementByXPath(parentPath,xpath);
        if(node==null){
            return null;
        }else{
            return node.getText();
        }
    }

    /**
     * 描述:查找元素的属性值方法(使用XPatch方式)
     * @param	nodePath:xPath路径
     * @param	attributeName:属性名称
     * **/
    public String  findElementAttributeTextByXPath(String parentPath,String xpath,String attributeName){
        Node node = this.findSingleElementByXPath(parentPath,xpath);
        if(node==null){
            return null;
        }else{
            return node.valueOf("@"+attributeName);
        }
    }

    /**
     * 描述:查找元素方法(使用XPatch方式)
     * @param	nodePath:xPath路径
     * **/
    public List<Element> findElementByXPath(String parentPatch,String xpatch){
        Node parentNode = this.document.selectSingleNode(parentPatch);
        XPath xpath=document.createXPath(xpatch);
        xpath.setNamespaceURIs(this.params);
        List<Element> elements = xpath.selectNodes(parentNode);
        return elements;
    }

    /**
     * 描述:查找元素的文本值方法(使用XPatch方式)
     * @param	nodePath:xPath路径
     * **/
    public String[]  findElementsTextByXPath(String parentPath,String xpath){
        String[] texts = null;
        List elements = this.findElementByXPath(parentPath, xpath);
        if(elements!=null){
            int size = elements.size();
            if(size>0){
                texts = new String[size];
                for(int i=0;i<size;i++){
                    texts[i] = ((Element)elements.get(i)).getText();
                }
            }
        }
        return texts;
    }

    /**
     * 描述 :增加节点方法
     * @param	nodePath:	节点路径
     * @param	nodeName:	新增节点名称
     * @param	text:		文本值
     * **/
    public void  addNodeByXPath(String parentPath,String xpath,String newNodeName,String text){
        Node parentNode = this.document.selectSingleNode(parentPath);
        XPath xpathObj=document.createXPath(xpath);
        xpathObj.setNamespaceURIs(this.params);
        List<Element> elements = xpathObj.selectNodes(parentNode);

        Element element = null;
        if(elements!=null&&elements.size()>0){
            element = elements.get(0);
            Element channelElement = element.addElement(newNodeName);
            channelElement.setText(text);
        }
    }

    /**
     * 描述 :增加节点方法
     * @param	nodePath:	节点路径
     * @param	nodeName:	新增节点名称
     * @param	attributes:	属性集合
     * @param	text:		文本值
     * **/
    public void  addNodeByXPath(String parentPath,String xpath,String newNodeName,Map<String,String> attributes,String text){
        Node parentNode = this.document.selectSingleNode(parentPath);
        XPath xpathObj=document.createXPath(xpath);
        xpathObj.setNamespaceURIs(this.params);
        List<Element> elements = xpathObj.selectNodes(parentNode);

        Element element = null;
        Iterator<?> iterator = attributes.keySet().iterator();

        String key = null;
        if(elements!=null&&elements.size()>0){
            element = elements.get(0);
            Element channelElement = element.addElement(newNodeName);
            channelElement.setText(text);

            while(iterator.hasNext()){
                key = (String)iterator.next();
                channelElement.addAttribute(key, attributes.get(key));
            }
        }
    }

    /**
     * 描述:设置节点值方法
     * @param	nodePath:	节点路径
     * @param	text:		文本值
     * **/
    public void	 setNodeTextByXPath(String parentPath,String xpath,String text){
        Node node = this.findSingleElementByXPath(parentPath, xpath);
        if(node!=null){
            node.setText(text);
        }
    }

    /**
     * 描述 :删除节点方法
     * @param	nodePath:	节点路径
     * @param	nodeName:	新增节点名称
     * @param	text:		文本值
     * **/
    public void removeNodeByXPath(String parentPath,String xpath){
        Node parentNode = this.document.selectSingleNode(parentPath);
        XPath xpathObj=document.createXPath(xpath);
        xpathObj.setNamespaceURIs(this.params);
        Node node = xpathObj.selectSingleNode(parentNode);
        if(node!=null){
            node.detach();
        }
    }


    /**
     * 描述:增加节点属性方法
     * @param	nodePath:	节点路径
     * @param	attributeName:	属性名称
     * @param	attributeValue:	属性值
     * **/
    public void	 setNodeAttbiuteByXPath(String parentPath,String xpath,String attributeName,String attributeValue){
        Node parentNode = this.document.selectSingleNode(parentPath);
        XPath xpathObj=document.createXPath(xpath);
        xpathObj.setNamespaceURIs(this.params);
        List<Element> elements = xpathObj.selectNodes(parentNode);

        Element element = null;
        Attribute attribute = null;
        if(elements!=null&&elements.size()>0){
            element = elements.get(0);
            attribute =  element.attribute(attributeName);
            if(attribute!=null){
                attribute.setValue(attributeValue);
                List<Attribute> attributes =  new ArrayList<Attribute>();
                attributes.add(attribute);
                element.setAttributes(attributes);
            }
        }
    }

    /**
     * 描述:增加节点属性方法
     * @param	element:		元素实体对象
     * @param	attributeName:	属性名称
     * @param	attributeValue:	属性值
     * **/
    public void	 setNodeAttbiuteByXPath(Element element,String attributeName,String attributeValue){
        Attribute attribute =  element.attribute(attributeName);
        if(attribute!=null){
            attribute.setValue(attributeValue);
            element.add(attribute);
        }
    }

    /**
     * 描述:XML节点转换为文本方法(XPath方式)
     * @param	nodePath:xPath路径
     * **/
    public String convertXMLNodeForTextByXPath(String parentPath,String xpath){
        Node node = null;
        if(xpath==null){
            node = this.document.selectSingleNode(parentPath);
        }else{
            node = this.findSingleElementByXPath(parentPath, xpath);
        }
        if(node!=null){
            return node.asXML();
        }

        return null;
    }

    public String  getEncoding() {
        return this.encoding;
    }

    public void  setEncoding(String encoding) {
        this.encoding = encoding;
    }

    public void  setXsd(String xsd) {
        this.xsd = xsd;
    }

    public void  setNamespace(String qname,String namespace) {
        this.params.put(qname,namespace);
    }

    /**
     * 描述:销毁资源方法
     * **/
    public void close(){
        this.document = null;
        this.errorHandler = null;
        this.validator = null;

        if(this.params!=null){
            this.params.clear();
            this.params = null;
        }

        this.encoding = null;
        this.xsd = null;
    }
}
