package com.nusmedia.player;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;


public class DomMPDService
{


    public static byte[] getContent(String filePath) throws IOException
    {
        /*
                This function is not used in final version.
        */

        File file = new File(filePath);
        long fileSize = file.length();
        if (fileSize > Integer.MAX_VALUE) {
            System.out.println("file too big...");
            return null;
        }
        FileInputStream fi = new FileInputStream(file);
        byte[] buffer = new byte[(int) fileSize];
        int offset = 0;
        int numRead = 0;
        while (offset < buffer.length
                && (numRead = fi.read(buffer, offset, buffer.length - offset)) >= 0) {
            offset += numRead;
        }
        if (offset != buffer.length) {
            throw new IOException("Could not completely read file "
                    + file.getName());
        }
        fi.close();
        return buffer;
    }

    public static HashMap<String,List<String>> readXML(String fileName) throws Throwable
    {
         /*
                Parse a XML file (MPD), and generate a HashMap of High, Medium, Low List
        */

        File inputFile=new File("/storage/emulated/0/Android/data/com.nusmedia.player/downloads/"+fileName+".mpd");
        if(!inputFile.exists())
        {
            return null;
        }

        HashMap<String,List<String>>VideoURLList=new HashMap<String,List<String>>();
        List<String> VideoURLList_h=new ArrayList<String>();
        List<String> VideoURLList_m=new ArrayList<String>();
        List<String> VideoURLList_l=new ArrayList<String>();


        try
        {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(inputFile);
            doc.getDocumentElement().normalize();

            NodeList nList = doc.getElementsByTagName("Representation");

            for (int temp = 0; temp < nList.getLength(); temp++)
            {
                Node nNode = nList.item(temp);

                if (nNode.getNodeType() == Node.ELEMENT_NODE)
                {
                    Element eElement = (Element) nNode;
                    if(eElement.getAttribute("id").equals("high"))
                    {

                        NodeList segmentListList=eElement.getChildNodes();
                        for(int i=0;i<segmentListList.getLength();i++)
                        {
                            Node nnNode=segmentListList.item(i);
                            if(nnNode.getNodeType()==Node.ELEMENT_NODE)
                            {
                                Element eeElement=(Element) nnNode;

                                NodeList segmentList =eeElement.getChildNodes();
                                for (int j = 0; j< segmentList.getLength(); j++)
                                {
                                    Node segmentNode=segmentList.item(j);
                                    if(segmentNode.getNodeType()==Node.ELEMENT_NODE)
                                    {
                                        Element segmentElement=(Element) segmentNode;
                                        VideoURLList_h.add(segmentElement.getAttribute("media"));
                                    }
                                }
                            }
                        }
                    }
                    else if(eElement.getAttribute("id").equals("medium"))
                    {

                        NodeList segmentListList=eElement.getChildNodes();
                        for(int i=0;i<segmentListList.getLength();i++)
                        {
                            Node nnNode=segmentListList.item(i);
                            if(nnNode.getNodeType()==Node.ELEMENT_NODE)
                            {
                                Element eeElement=(Element) nnNode;

                                NodeList segmentList =eeElement.getChildNodes();
                                for (int j = 0; j< segmentList.getLength(); j++)
                                {
                                    Node segmentNode=segmentList.item(j);
                                    if(segmentNode.getNodeType()==Node.ELEMENT_NODE)
                                    {
                                        Element segmentElement=(Element) segmentNode;
                                        VideoURLList_m.add(segmentElement.getAttribute("media"));
                                    }
                                }
                            }
                        }
                    }
                    else if(eElement.getAttribute("id").equals("low"))
                    {

                        NodeList segmentListList=eElement.getChildNodes();
                        for(int i=0;i<segmentListList.getLength();i++)
                        {
                            Node nnNode=segmentListList.item(i);
                            if(nnNode.getNodeType()==Node.ELEMENT_NODE)
                            {
                                Element eeElement=(Element) nnNode;

                                NodeList segmentList =eeElement.getChildNodes();
                                for (int j = 0; j< segmentList.getLength(); j++)
                                {
                                    Node segmentNode=segmentList.item(j);
                                    if(segmentNode.getNodeType()==Node.ELEMENT_NODE)
                                    {
                                        Element segmentElement=(Element) segmentNode;
                                        VideoURLList_l.add(segmentElement.getAttribute("media"));
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        VideoURLList.put("high",VideoURLList_h);
        VideoURLList.put("medium",VideoURLList_m);
        VideoURLList.put("low",VideoURLList_l);


        return VideoURLList;
    }

    public static HashMap<String,List<String>> UpdateXML(HashMap<String,List<String>> VideoURLList, File MPD_file)
    {

         /*
                This is to parse XML file and update the HashMap in Live mode.
        */

        try
        {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(MPD_file);
            doc.getDocumentElement().normalize();

            NodeList nList = doc.getElementsByTagName("Representation");

            for (int temp = 0; temp < nList.getLength(); temp++)
            {
                Node nNode = nList.item(temp);

                if (nNode.getNodeType() == Node.ELEMENT_NODE)
                {
                    Element eElement = (Element) nNode;
                    if(eElement.getAttribute("id").equals("high"))
                    {

                        NodeList segmentListList=eElement.getChildNodes();
                        for(int i=0;i<segmentListList.getLength();i++)
                        {
                            Node nnNode=segmentListList.item(i);
                            if(nnNode.getNodeType()==Node.ELEMENT_NODE)
                            {
                                Element eeElement=(Element) nnNode;

                                NodeList segmentList =eeElement.getChildNodes();
                                for (int j = 0; j< segmentList.getLength(); j++)
                                {
                                    Node segmentNode=segmentList.item(j);
                                    if(segmentNode.getNodeType()==Node.ELEMENT_NODE)
                                    {
                                        Element segmentElement=(Element) segmentNode;
                                        if(!VideoURLList.get("high").contains(segmentElement.getAttribute("media")))
                                            VideoURLList.get("high").add(segmentElement.getAttribute("media"));
                                    }
                                }
                            }
                        }
                    }
                    else if(eElement.getAttribute("id").equals("medium"))
                    {

                        NodeList segmentListList=eElement.getChildNodes();
                        for(int i=0;i<segmentListList.getLength();i++)
                        {
                            Node nnNode=segmentListList.item(i);
                            if(nnNode.getNodeType()==Node.ELEMENT_NODE)
                            {
                                Element eeElement=(Element) nnNode;

                                NodeList segmentList =eeElement.getChildNodes();
                                for (int j = 0; j< segmentList.getLength(); j++)
                                {
                                    Node segmentNode=segmentList.item(j);
                                    if(segmentNode.getNodeType()==Node.ELEMENT_NODE)
                                    {
                                        Element segmentElement=(Element) segmentNode;
                                        if(!VideoURLList.get("medium").contains(segmentElement.getAttribute("media")))
                                            VideoURLList.get("medium").add(segmentElement.getAttribute("media"));
                                    }
                                }
                            }
                        }
                    }

                    else if(eElement.getAttribute("id").equals("low"))
                    {

                        NodeList segmentListList=eElement.getChildNodes();
                        for(int i=0;i<segmentListList.getLength();i++)
                        {
                            Node nnNode=segmentListList.item(i);
                            if(nnNode.getNodeType()==Node.ELEMENT_NODE)
                            {
                                Element eeElement=(Element) nnNode;

                                NodeList segmentList =eeElement.getChildNodes();
                                for (int j = 0; j< segmentList.getLength(); j++)
                                {
                                    Node segmentNode=segmentList.item(j);
                                    if(segmentNode.getNodeType()==Node.ELEMENT_NODE)
                                    {
                                        Element segmentElement=(Element) segmentNode;
                                        if(!VideoURLList.get("low").contains(segmentElement.getAttribute("media")))
                                            VideoURLList.get("low").add(segmentElement.getAttribute("media"));
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        catch (ParserConfigurationException e)
        {
            e.printStackTrace();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return VideoURLList;
    }


}
