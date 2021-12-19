package com.example.starter;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;

import java.io.File;

public class Starter
{

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

  }

  private static void deployWithConfig(Vertx vertx,String className,String filePath)
  {
    File config = new File(filePath);
    DeploymentOptions options = new DeploymentOptions()
      .setConfig(Helper.JsonFileReader.read(config));
    vertx.deployVerticle(className, options);
  }

}
