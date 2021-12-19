package com.example.starter;

import io.vertx.core.Future;
import io.vertx.core.json.DecodeException;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

public class Helper
{
  public static class JsonFileReader
  {
    public static JsonObject read(File config)
    {
      JsonObject conf = new JsonObject();
      if (config.isFile())
      {
        System.out.println("Reading config file: " + config.getAbsolutePath());
        try (Scanner scanner = new Scanner(config).useDelimiter("\\A"))
        {
          String sconf = scanner.next();
          try
          {
            conf = new JsonObject(sconf);
          } catch (DecodeException e)
          {
            System.err.println("Configuration file " + sconf + " does not contain a valid JSON object");
          }
        } catch (FileNotFoundException e)
        {
          // Ignore it.
        }
      } else
      {
        System.out.println("Config file not found " + config.getAbsolutePath());
      }
      return conf;
    }
  }

  public static class Response
  {
    public static JsonObject Join(String source, List<Future> futureList,String key)
    {
      if (futureList.size() == 0)
      {
        throw new IllegalArgumentException();
      }

      JsonArray resultArray = new JsonArray();
      JsonArray errorsArray = new JsonArray();

      for (Future future: futureList)
      {
        if (future.succeeded())
        {
          resultArray.add(future.result());
        }
        else
        {
          // WARNING: remove access keys from response
          future.cause().getMessage().replace(key,"000");
          errorsArray.add(future.cause().getMessage());
        }
      }

      return new JsonObject()
        .put("success", resultArray.size() > 0)
        .put("source", source)
        .put("result", resultArray)
        .put("errors", errorsArray);
    }
  }

  public static class Parameter
  {
    public static boolean isNumber(String string)
    {
      try
      {
        int x = Integer.parseInt(string);
        return true;
      }
      catch (Exception ex)
      {
        return false;
      }
    }

    public static boolean isAcceptedFlightStatus(String string)
    {
      String[] statuses = {"diverted","incident","cancelled",
        "landed","active","scheduled"
      };

      return Arrays.asList(statuses).contains(string);
    }
  }
}
