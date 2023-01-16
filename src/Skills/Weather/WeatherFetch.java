package Skills.Weather;

import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.nio.charset.Charset;

public class WeatherFetch {

    public static String getWeather(String city, String country) throws Exception{
        URIBuilder builder = new URIBuilder("https://weather.visualcrossing.com/VisualCrossingWebServices/rest/services/weatherdata/forecast");
        builder.setParameter("aggregateHours", "24")
                .setParameter("contentType", "csv")
                .setParameter("unitGroup", "metric")
                .setParameter("locationMode", "single")
                .setParameter("key", "1PYNQ6AWUDJE9AFERDCHJHSXK")
                .setParameter("locations", city+","+country);

        HttpGet get = new HttpGet(builder.build());

        CloseableHttpClient httpclient = HttpClients.createDefault();

        CloseableHttpResponse response = httpclient.execute(get);

        try {
            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                //System.out.printf("Bad response status code:%d%n", response.getStatusLine().getStatusCode());
                return "Bad response status code:%d%n" + response.getStatusLine().getStatusCode();
            }

            HttpEntity entity = response.getEntity();
            if (entity != null) {
                String rawResult=EntityUtils.toString(entity, Charset.forName("utf-8"));
                return rawResult;
            }

        } finally {
            response.close();
        }
        return "";
    }

    public static String getHourlyWeather(String city, String country) throws Exception {
        URIBuilder builder = new URIBuilder("https://weather.visualcrossing.com/VisualCrossingWebServices/rest/services/weatherdata/forecast");
        builder.setParameter("forecastDays", "1")
                .setParameter("aggregateHours", "1")
                .setParameter("contentType", "csv")
                .setParameter("unitGroup", "metric")
                .setParameter("locationMode", "single")
                .setParameter("iconSet", "icons1")
                .setParameter("key", "1PYNQ6AWUDJE9AFERDCHJHSXK")
                .setParameter("locations", city+","+country);

        HttpGet get = new HttpGet(builder.build());

        CloseableHttpClient httpclient = HttpClients.createDefault();

        CloseableHttpResponse response = httpclient.execute(get);

        try {
            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                //System.out.printf("Bad response status code:%d%n", response.getStatusLine().getStatusCode());
                return "Bad response status code:%d%n" + response.getStatusLine().getStatusCode();
            }

            HttpEntity entity = response.getEntity();
            if (entity != null) {
                String rawResult=EntityUtils.toString(entity, Charset.forName("utf-8"));
                return rawResult;
            }

        } finally {
            response.close();
        }
        return "";
    }

    public static void retrieveWeatherForecastAsCsv() throws Exception {
        //set up the end point

        URIBuilder builder = new URIBuilder("https://weather.visualcrossing.com/VisualCrossingWebServices/rest/services/weatherdata/forecast");
        builder.setParameter("aggregateHours", "24")
                .setParameter("contentType", "csv")
                .setParameter("unitGroup", "metric")
                .setParameter("locationMode", "single")
                .setParameter("key", "1PYNQ6AWUDJE9AFERDCHJHSXK")
                .setParameter("locations", "Maastricht,NL");

        HttpGet get = new HttpGet(builder.build());

        CloseableHttpClient httpclient = HttpClients.createDefault();

        CloseableHttpResponse response = httpclient.execute(get);

        try {
            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK) {
                System.out.printf("Bad response status code:%d%n", response.getStatusLine().getStatusCode());
                return;
            }

            HttpEntity entity = response.getEntity();
            if (entity != null) {
                String rawResult=EntityUtils.toString(entity, Charset.forName("utf-8"));
                System.out.printf("Result data:%n%s%n", rawResult);
            }

        } finally {
            response.close();
        }
    }

    public static void main(String[] args)  throws Exception {
        //WeatherForecastRetrieval.retrieveWeatherForecastAsCsv();
        WeatherFetch.retrieveWeatherForecastAsCsv();
    }
}