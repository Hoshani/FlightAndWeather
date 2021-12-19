package com.example.starter;

import com.fasterxml.jackson.databind.util.JSONPObject;
import io.vertx.core.json.JsonObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.FileReader;
import java.io.IOException;

public class CityNameFinder
{
  public static class Cities
  {
    static JSONParser parser = new JSONParser();
    static Object object;
    static JsonObject jsonObject;

    public static String getCity(String arrivalIcao)
    {
      try
      {
        if (object == null || jsonObject == null)
        {
          object = parser
            .parse(new FileReader("src/main/conf/icaoToCityMap.json"));
          jsonObject = new JsonObject(object.toString());
        }

        if (jsonObject == null)
          return "";

        JsonObject cityObject = jsonObject
          .getJsonObject(arrivalIcao);

        if (cityObject == null)
          return "";

        return cityObject.getString("city") == null ?
          "" : cityObject.getString("city");
      } catch (IOException e)
      {
        e.printStackTrace();
        return "";
      } catch (ParseException e)
      {
        e.printStackTrace();
        return "";
      }
    }
  }
}
