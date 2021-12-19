package com.example.starter;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;

public class Starter
{
  private final static Logger LOGGER = LogManager.getLogger("vertxLog");

  public static void main(String[] args)
  {
    Vertx vertx = Vertx.vertx();

    deployWithConfig(vertx, AviationService.class.getName(),"src/main/conf/aviation.json");
    deployWithConfig(vertx, WeatherService.class.getName(),"src/main/conf/weather.json");

    deploy(vertx,RequestHandler.class.getName());
  }

  private static void deploy(Vertx vertx,String className)
  {
    vertx.deployVerticle(className);

    LOGGER.info("verticle : " + className + " was deployed");
  }

  private static void deployWithConfig(Vertx vertx,String className,String filePath)
  {
    File config = new File(filePath);
    DeploymentOptions options = new DeploymentOptions()
      .setConfig(Helper.JsonFileReader.read(config));
    vertx.deployVerticle(className, options);

    LOGGER.info("verticle : " + className + " was deployed");
  }

}
