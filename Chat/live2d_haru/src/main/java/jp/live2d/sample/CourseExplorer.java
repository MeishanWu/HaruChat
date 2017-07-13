package jp.live2d.sample;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;


public class CourseExplorer {

    /**
     * pull request and read xml to get the information
     * @param urlToRead the url
     * @return xml file to a string
     * @throws Exception
     */
    public static String getHTML(String urlToRead) throws Exception {
        StringBuilder result = new StringBuilder();
        URL url = new URL(urlToRead);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String line;
        while ((line = rd.readLine()) != null) {
            result.append(line);
        }
        rd.close();
        return result.toString();
    }

    /**
     * parse user's input into an url string
     * @param input
     * @return
     */
    public static String urlParser(String input){
        ArrayList<String> course = new ArrayList<>(Arrays.asList(input.split(" ")));
        String urlStr = "https://courses.illinois.edu/cisapp/explorer/schedule/";

        //default term with crn
        if(course.size() == 4){
                int year = Calendar.getInstance().get(Calendar.YEAR);
                int month = Calendar.getInstance().get(Calendar.MONTH)+1; //return is from 0 to 11
                if(month >= 10){
                    urlStr += (year + 1) + "/spring/" + course.get(1).toUpperCase() + "/"
                            + course.get(2) + "/" + course.get(3) + ".xml";
                }else if (month <= 3){
                    urlStr +=  year + "/spring/" + course.get(1).toUpperCase() + "/"
                            + course.get(2) + "/" + course.get(3) + ".xml";
                }else{
                    urlStr +=  year + "/fall/" + course.get(1).toUpperCase() + "/"
                            + course.get(2) + "/" + course.get(3) + ".xml";
                }
        }

        //selected term with crn
        else if (course.size() == 6){
            urlStr +=  course.get(1) + "/" + course.get(2) + "/" + course.get(3).toUpperCase() + "/"
                    + course.get(4) + "/" + course.get(5) + ".xml";
        }

        //without crn
        else if (course.get(course.size() - 1).length() != 5){
            if (course.size() == 5){
                urlStr +=  course.get(1) + "/" + course.get(2) + "/" + course.get(3).toUpperCase() + "/"
                        + course.get(4) + ".xml";
            }else if (course.size() == 3){
                int year = Calendar.getInstance().get(Calendar.YEAR);
                int month = Calendar.getInstance().get(Calendar.MONTH)+1; //return is from 0 to 11
                if(month >= 10){
                    urlStr += (year + 1) + "/spring/" + course.get(1).toUpperCase() + "/"
                            + course.get(2) + ".xml";
                }else if (month <= 3){
                    urlStr +=  year + "/spring/" + course.get(1).toUpperCase() + "/"
                            + course.get(2) + ".xml";
                }else{
                    urlStr +=  year + "/fall/" + course.get(1).toUpperCase() + "/"
                            + course.get(2) + ".xml";
                }
            }
        }
        return urlStr;
    }

    /**
     * get the status by the crn
     * @param input
     * @return
     */
    public static String status(String input){
        String url = urlParser(input);
        int count = input.length() - input.replace(" ", "").length();
        if (count == 2 || count == 4){
            return getSection(url);
        }
        if (url.equals("https://courses.illinois.edu/cisapp/explorer/schedule/")) {
            return "Which course would you like to register?" +
                    "Enter \"course subject coursenumber crn\" or \"course year term subject course number crn\" " +
                    "or click \"?\" for more information";
        }
        try{
            String xml = CourseExplorer.getHTML(url);
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            InputSource src = new InputSource();
            src.setCharacterStream(new StringReader(xml));
            Document doc = builder.parse(src);
            String status = doc.getElementsByTagName("enrollmentStatus").item(0).getTextContent();
            return statusToSentence(status);
        }catch (Exception e){
            e.printStackTrace();
            return statusToSentence("unknown");
        }
    }

    /**
     * get section name and crn by crn for a course
     * @param url
     * @return
     */
    public static String getSection(String url) {
        try{

            String ret = "Which section would you like to know? \n";
                String xml = CourseExplorer.getHTML(url);
                DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                InputSource src = new InputSource();
                src.setCharacterStream(new StringReader(xml));
                Document doc = builder.parse(src);

                int n = doc.getElementsByTagName("section").getLength();
                for (int i = 0; i < n; i++) {
                    Element elem = (Element) doc.getElementsByTagName("section").item(i);
                    ret = ret + (elem.getAttribute("id") + ": "
                            + doc.getElementsByTagName("section").item(i).getTextContent()) + "\n";

                }

            return ret;
        }catch (Exception e){
            e.printStackTrace();
            return statusToSentence("unknown");
        }
    }

    /**
     * return a sentence according to the status of a selected section
     * @param status
     * @return
     */
    public static String statusToSentence(String status) {
        status = status.toLowerCase();
        if(status.contains("closed")){
            return "The course is already closed. " + Kaomoji.randomkaomoji("sad");
        }
        if (status.contains("open")){
            return "Yeah! The course is opening! " + Kaomoji.randomkaomoji("happy");
        }

        if (status.contains("unknown")){
            return "What's the status of this course? Sorry I don't know. " + Kaomoji.randomkaomoji("?");
        }
        return "Status: " + status;
    }




}