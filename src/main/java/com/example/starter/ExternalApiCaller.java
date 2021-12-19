package com.example.starter;

import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.WebClient;

import java.util.HashMap;
import java.util.Map;

public class ExternalApiCaller
{
  public static class Caller
  {
    public static void call(Promise handler, RequestOptions options, Vertx vertx)
    {
      HttpRequest<Buffer> request = WebClient.create(vertx)
        .get(options.getBaseUrl(), options.getUri());

      if (!options.getQueryParams().isEmpty())
        for (Map.Entry<String, String> entry : options.getQueryParams().entrySet())
        {
          request.addQueryParam(entry.getKey(), entry.getValue());
        }

      request.send(ar ->
      {
        System.out.println(options.getUri() + " - " + ar.succeeded());
        System.out.println(ar.result().statusCode());
        if (ar.succeeded() && ar.result().statusCode() == 200)
        {
          handler.handle(Future.succeededFuture(ar.result().bodyAsJsonObject()));
          return;
        }

        if (ar.succeeded() && ar.result().statusCode() != 200)
        {
          handler.handle(Future.failedFuture(ar.result().bodyAsJsonObject().toString()));
          return;
        }

        handler.handle(Future.failedFuture(ar.cause().getMessage()));

      });
    }
  }

  public static class RequestOptions
  {
    private String baseUrl;
    private String uri;
    private long timeout = 60000;
    private HashMap<String, String> queryParams = new HashMap();

    public RequestOptions(){

    }

    public RequestOptions(String baseUrl,String uri,long timeout){
      this.baseUrl = baseUrl;
      this.uri = uri;
      this.timeout = timeout;
    }

    public String getBaseUrl()
    {
      return baseUrl;
    }

    public void setBaseUrl(String baseUrl)
    {
      this.baseUrl = baseUrl;
    }

    public String getUri()
    {
      return uri;
    }

    public void setUri(String uri)
    {
      this.uri = uri;
    }

    public long getTimeout()
    {
      return timeout;
    }

    public void setTimeout(Long timeout)
    {
      this.timeout = timeout;
    }

    public HashMap<String, String> getQueryParams()
    {
      return this.queryParams;
    }

    public void addQueryParam(String key, String value)
    {
      this.queryParams.put(key, value);
    }
  }
}
