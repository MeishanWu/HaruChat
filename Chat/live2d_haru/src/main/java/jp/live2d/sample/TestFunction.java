package jp.live2d.sample;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

import java.io.File;
import java.io.StringReader;

public class TestFunction {
    public static void main(String[] args) {
        try{
            System.out.println(Weather.weather("weather urbana"));
        }catch (Exception e){

        }



    }
}
