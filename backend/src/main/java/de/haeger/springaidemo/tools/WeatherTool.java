package de.haeger.springaidemo.tools;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Component;

/**
 * Weather tool for AI function calling integration.
 * Demonstrates Spring AI 1.0.3 tool integration patterns.
 */
@Component
public class WeatherTool {

  /**
   * Gets current weather information for a specified city.
   * This is a demo implementation that returns mock weather data.
   * 
   * @param city the name of the city to get weather for
   * @return weather information string
   */
  @Tool(description = "Get the current weather information for a specific city. " +
      "Use this tool when the user asks about weather conditions, temperature, or climate in a location. " +
      "Input is the city name.")
  public String getLocalWeather(String city) {
    if (city == null || city.trim().isEmpty()) {
      return "Error: City name is required";
    }
    return String.format("Weather in %s: sunny, 24°C (Demo data)", city.trim());
  }
}