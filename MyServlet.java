package MyPackage;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.Scanner;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

/**
 * Servlet implementation class MyServlet
 */
@WebServlet("/MyServlet")
public class MyServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public MyServlet() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		response.getWriter().append("Served at: ").append(request.getContextPath());
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		// String inputData=request.getParameter("userInput");
	   //System.out.println(inputData);
		
		  // API Setup
		String apiKey = "b2a310dcf16568e234ffbb70aaaba9c7";
		// Get the city from the form input
        String city = request.getParameter("city");
        
      
        // create the URL for the OpenWeather API reguest
        String encodedCity = URLEncoder.encode(city, StandardCharsets.UTF_8);
        String apiUrl = "https://api.openweathermap.org/data/2.5/weather?q="
                        + encodedCity + "&appid=" + apiKey;
        
         // API Intergeratiom
         URL url = new URL(apiUrl);
         HttpURLConnection connection = (HttpURLConnection) url.openConnection();
         connection.setRequestMethod("GET");
        
          
        // it used for create connection and reading  the data from network 
         InputStream inputStream;
         if (connection.getResponseCode() == 200) {
             inputStream = connection.getInputStream();
         } else {
             inputStream = connection.getErrorStream();
         }        
         InputStreamReader reader = new InputStreamReader(inputStream);
        
         //want to store in string
         StringBuilder responseContent = new StringBuilder();
         
        //Input lene ke liye from the reader , will create a Scanner class
         Scanner scanner = new Scanner(reader);

         while (scanner.hasNext()) {
             responseContent.append(scanner.nextLine());
         }
         scanner.close();
         System.out.println(responseContent);
         
         //TypeCasting = Parsing the data into JSON
        Gson gson = new Gson();
        JsonObject jsonObject = gson.fromJson(responseContent.toString(), JsonObject.class);
        if (jsonObject.has("cod") && jsonObject.get("cod").getAsInt() != 200) {
            request.setAttribute("error", "City not found");
            request.getRequestDispatcher("/index.jsp").forward(request, response);
            return; // stop further execution
        }
        System.out.println(jsonObject);
        
        //Date and Time
        long dateTimestamp = jsonObject.get("dt").getAsLong() * 1000;
        String date = new Date(dateTimestamp).toString();
        
        // Temparature
        double temperatureKelvin = jsonObject.getAsJsonObject("main").get("temp").getAsDouble();
        int temperatureCelsius = (int) (temperatureKelvin - 273.15);
        
        // Humidity
        int humidity = jsonObject.getAsJsonObject("main").get("humidity").getAsInt();
        
        // Wind Speed
        double windSpeed = jsonObject.getAsJsonObject("wind").get("speed").getAsDouble();
        
        // Weather Condition
        String weatherCondition = jsonObject.getAsJsonArray("weather").get(0).getAsJsonObject().get("main").getAsString();
        
        
try {
        // Set attributes for JSP
        request.setAttribute("date", date);
        request.setAttribute("city", city);
        request.setAttribute("temperature", temperatureCelsius);
        request.setAttribute("weatherCondition", weatherCondition);
        request.setAttribute("humidity", humidity);
        request.setAttribute("windSpeed", windSpeed);
        
     // Forward the request and response to index.jsp
        request.getRequestDispatcher("/index.jsp").forward(request, response);

        connection.disconnect();
        
    } catch (IOException e) {
    	
    	// Handle network or API-related errors
        request.setAttribute("error", "Network error: Unable to fetch data.");
    	
    	// Forward to index.jsp even if an error occurs
        request.getRequestDispatcher("index.jsp").forward(request, response);
         
    }
   }
	}

