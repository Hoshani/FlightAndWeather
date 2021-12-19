package com.example.starter;

import io.vertx.core.*;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.List;


public class AviationService extends AbstractVerticle
{
  private String accessKey, baseUrl, uri;
  private long timeoutLimit;


  @Override
  public void init(Vertx vertx, Context context)
  {
    super.init(vertx, context);
    uri = "/v1/flights";
    accessKey = config().getString("access-key");
    baseUrl = config().getString("base-url");
    timeoutLimit = 30000;//config().getLong("timeout");
  }

  @Override
  public void start()
  {
    vertx.eventBus().consumer("getFlights").handler(this::flightsFetcher);
  }

  private void flightsFetcher(Message<Object> objectMessage)
  {
    Future<JsonObject> finalFuture = Future.future();

    finalFuture.onComplete(ar ->
    {
      objectMessage.reply((finalFuture.result()));
    });

    Future<JsonObject>
      firstPageFlights = Future.future(),
      secondPageFlights = Future.future();

    JsonObject messageJson = (JsonObject) objectMessage.body();
    apiCall(firstPageFlights, messageJson);
    apiCall(secondPageFlights, messageJson);

    List<Future> futureArrayList = new ArrayList<Future>();
    futureArrayList.add(firstPageFlights);
    futureArrayList.add(secondPageFlights);

    CompositeFuture.join(futureArrayList).setHandler(ar ->
    {

      JsonObject result = Helper.Response.Join("api", futureArrayList,accessKey);
      finalFuture.complete(result);
    });
  }

  private void apiCall(Handler<AsyncResult<JsonObject>> handler, JsonObject messageJson)
  {
    apiCall(handler, messageJson, false);
  }

  private void apiCall(Handler<AsyncResult<JsonObject>> handler, JsonObject messageJson, boolean isSecondFlight)
  {
    ExternalApiCaller.RequestOptions options =
      new ExternalApiCaller.RequestOptions(baseUrl, uri, timeoutLimit);

    options.addQueryParam("access_key", accessKey);
    options.addQueryParam("limit", "10");

    if (messageJson.getString("flight_status") != null)
      options.addQueryParam("flight_status",messageJson.getString("flight_status"));

    if (messageJson.getString("offset") != null)
      options.addQueryParam("offset",messageJson.getString("offset"));

    if (messageJson.getString("arr_icao") != null)
      options.addQueryParam("arr_icao",messageJson.getString("arr_icao"));

    if (isSecondFlight)
    {
      if (messageJson.getString("offset") == null)
      {
        options.addQueryParam("offset", "10");
      }
      else
      {
        int offset = Integer.parseInt(messageJson.getString("offset"));
        options.addQueryParam("offset", Integer.toString(offset*2));
      }
    }

    ExternalApiCaller.Caller.call(handler, options, vertx);
  }

}

