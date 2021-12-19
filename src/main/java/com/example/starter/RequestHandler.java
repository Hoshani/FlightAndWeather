package com.example.starter;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;

import java.util.HashMap;

public class RequestHandler extends AbstractVerticle
{

  @Override
  public void start()
  {
    Router router = Router.router(vertx);
    router.get("/all").handler(this::fetchFlights);

    vertx.createHttpServer().requestHandler(router::accept)
      .listen(9000, ar ->
      {
        if (!ar.succeeded())
        {
          System.out.println("failed to start : " + RequestHandler.class.getName());
          return;
        }

        System.out.println("9000 : started successfully");
      });
  }

  private void fetchFlights(RoutingContext routingContext)
  {
    JsonObject params = getRequestParams(routingContext.request());

    vertx.eventBus().send("getFlights", params, reply ->
    {

      if (!reply.succeeded())
      {
        reply.cause().printStackTrace();
        routingContext.response().end(reply.cause().getMessage());
        return;
      }

      JsonObject cityByIcao = JsonObject.mapFrom(getIcaoMap((JsonObject) reply.result().body()));

      vertx.eventBus().send("getWeather", cityByIcao, reply2 ->
      {
        // add weather to arrival city
        if (reply2.succeeded())
        {
          JsonObject cityWeatherByIcao = (JsonObject) reply2.result().body();
          for (Object data : ((JsonObject) reply.result().body()).getJsonArray("result"))
          {
            for (Object icao : ((JsonObject) data).getJsonArray("data"))
            {
              try
              {
                JsonObject jIcao = (JsonObject) icao;
                JsonObject cityWeather = cityWeatherByIcao.getJsonObject(jIcao.getJsonObject("arrival")
                  .getString("icao"));
                jIcao.getJsonObject("arrival").put("weather", cityWeather);
              }
              catch (Exception exception)
              {
                JsonObject jIcao = (JsonObject) icao;
                jIcao.getJsonObject("arrival")
                  .put("weather", "unavailable");
              }
            }
          }

          routingContext.response().end(((JsonObject) reply.result().body()).encodePrettily());
        }
      });

    });
  }

  private JsonObject getRequestParams(HttpServerRequest request)
  {
    JsonObject object = new JsonObject();

    //offset
    String param = request.getParam("offset");
    if (Helper.Parameter.isNumber(param))
      object.put("offset", param);

    //status
    param = request.getParam("flight_status");
    if (Helper.Parameter.isAcceptedFlightStatus(param))
      object.put("flight_status", param);

    //icao
    param = request.getParam("arr_icao");
    object.put("arr_icao", param);

    return object;
  }

  private HashMap<String, String> getIcaoMap(JsonObject result)
  {
    HashMap<String, String> citiesByIcao = new HashMap();

    for (Object object : result.getJsonArray("result"))
    {
      for (Object data : ((JsonObject) object).getJsonArray("data"))
      {
        String icao = ((JsonObject) data).getJsonObject("arrival").getString("icao");

        if (CityNameFinder.Cities.getCity(icao).isBlank())
        {
          continue;
        }

        citiesByIcao.put(icao, CityNameFinder.Cities.getCity(icao));
      }
    }

    return citiesByIcao;
  }

}
