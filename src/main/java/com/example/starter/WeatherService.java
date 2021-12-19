package com.example.starter;

import io.vertx.circuitbreaker.CircuitBreaker;
import io.vertx.circuitbreaker.CircuitBreakerOptions;
import io.vertx.core.*;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class WeatherService extends AbstractVerticle
{
  CircuitBreaker breaker;
  private String key, baseUrl, uri;
  private long timeoutLimit;

  @Override
  public void init(Vertx vertx, Context context)
  {
    super.init(vertx, context);
    key = config().getString("key");
    baseUrl = config().getString("base-url");
    uri = config().getString("uri");
    timeoutLimit = config().getLong("timeout");
    breaker = CircuitBreaker.create("cb", vertx,
      new CircuitBreakerOptions()
        .setMaxFailures(3)
        .setMaxRetries(0) // 1 retry == 2 api calls
        .setTimeout(timeoutLimit)
        .setResetTimeout(60000)
    );
  }

  @Override
  public void start()
  {
    vertx.eventBus().consumer("getWeather").handler(this::weatherFetcher);
  }

  private void weatherFetcher(Message<Object> objectMessage)
  {
    Promise<JsonObject> finalPromise = Promise.promise();

    finalPromise.future().onComplete(ar -> objectMessage.reply(finalPromise.future().result()));

    List<Promise> promiseArrayList = new ArrayList<Promise>();

    List<Promise> breakerPromiseArrayList = new ArrayList<Promise>();
    JsonObject cityByIcao = (JsonObject) objectMessage.body();

    for (Map.Entry<String, Object> icaoCity : cityByIcao)
    {
      Promise<JsonObject> promise = Promise.promise();
      breakerPromiseArrayList.add(promise);
      promiseArrayList.add(promise);
    }

    breaker.<JsonObject>execute(ar ->
    {
      for (Map.Entry<String, Object> icaoCity : cityByIcao)
      {
        for (Promise promise:breakerPromiseArrayList)
        {
          apiCall(promise, icaoCity.getValue().toString());
        }
      }

      List<Future> breakerFutureList = new ArrayList<>();
      for (Promise promise : breakerPromiseArrayList)
      {
        breakerFutureList.add(promise.future());
      }

      CompositeFuture.join(breakerFutureList).onComplete(res ->
      {
        for (Object object : res.result().list())
        {
          breakerPromiseArrayList.remove(object);
        }
      });
    });

    List<Future> futureArrayList = new ArrayList<>();
    for (Promise promise : promiseArrayList)
    {
      futureArrayList.add(promise.future());
    }

    CompositeFuture.join(futureArrayList).onComplete(ar ->
    {
      for (Future future : futureArrayList)
      {
        if (!future.succeeded())
        {
          continue;
        }

        JsonObject result = (JsonObject) future.result();
        if (result == null)
        {
          continue;
        }

        for (Map.Entry<String, Object> icaoCity : cityByIcao)
        {
          if (!(icaoCity.getValue() instanceof String))
          {
            continue;
          }

          String cityName = (String) icaoCity.getValue();

          if (cityName.equals(result.getString("name")))
          {
            icaoCity.setValue(result);
          }
        }
      }

      finalPromise.complete(cityByIcao);
    });
  }

  private void apiCall(Promise future, String cityName)
  {
    ExternalApiCaller.RequestOptions options =
      new ExternalApiCaller
        .RequestOptions(baseUrl, uri, timeoutLimit);

    options.addQueryParam("appid", key);
    options.addQueryParam("q", cityName);
    options.addQueryParam("units", "metric");

    ExternalApiCaller.Caller.call(future, options, vertx);
  }

}
