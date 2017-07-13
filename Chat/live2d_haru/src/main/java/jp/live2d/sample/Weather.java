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
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class Weather {

    /**
     * pull request and read xml to get the information
     * @param urlToRead
     * @return
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
     * return the current weather
     * @param input
     * @return
     */
    public static String weather(String input){
        String url = urlParser(input);
        if (!url.contains("http")){
            return url;
        }
        String ret = "";
        try{
            String xml = getHTML(url);
            DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            InputSource src = new InputSource();
            src.setCharacterStream(new StringReader(xml));
            Document doc = builder.parse(src);
            Element city = (Element) doc.getElementsByTagName("city").item(0);
            Element weather = (Element) doc.getElementsByTagName("weather").item(0);
            String weatherStr = weather.getAttribute("value");
            ret += ("Today's weather in " + city.getAttribute("name") + " is " + weatherStr + ".\n");
            Element temperature = (Element) doc.getElementsByTagName("temperature").item(0);
            double min = Double.parseDouble(temperature.getAttribute("min"));
            double max = Double.parseDouble(temperature.getAttribute("max"));
            String unit = temperature.getAttribute("unit");
            ret += ("Today's temperature is from " + min + " to " +
                    max + ", and current temperature is " +
                    temperature.getAttribute("value") + ".\n");
            if ((max - min >= 13 && unit.equals("metric")) || (max - min >= 25 && unit.equals("fahrenheit"))) {
                    ret += "The difference in temperature between day and night is big, " +
                            "so don't forget your coat.\n";
            }
            if (weatherStr.contains("rain")) ret += "Please remember to bring an umbrella with you! ";
            return ret;
        }catch (Exception e){
            e.printStackTrace();
            return "What's the weather like today? Why not go out to feel!";
        }
    }


    /**
     * parse user's input to a string of url
     * use openweather api
     * @param input
     * @return
     */
    public static String urlParser(String input){
        ArrayList<String> weather = new ArrayList<>(Arrays.asList(input.split(" ")));
        String urlStr = "http://api.openweathermap.org/data/2.5/weather?q=";
        if(weather.size() == 2){
            urlStr += (weather.get(1) + "&mode=xml&units=imperial&appid=" + APIKeys.WEATHER_API);
        }
        else if (weather.size() == 3){
            if (weather.get(2).equals("c")){
                urlStr += (weather.get(1) + "&mode=xml&units=metric&appid=" + APIKeys.WEATHER_API);
            }else
                urlStr += (weather.get(1) + "&mode=xml&units=imperial&appid=" + APIKeys.WEATHER_API);
        }
        else return "Enther \"weather city unit(optional)\" to get information about current weather." +
                    " Unit can be \"c\" or \"f\"";
        return urlStr;
    }
}
