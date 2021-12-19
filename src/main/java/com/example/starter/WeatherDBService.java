package com.example.starter;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.sql.SQLConnection;

public class WeatherDBService
{

  public static class FromDB
  {
    public static JDBCClient client;

    public static void fetch(Future future, Vertx vertx, String city)
    {
      if (client == null)
      {
        createClient(vertx);
      }

      client.getConnection(sqlConnection ->
      {
        if (!sqlConnection.succeeded())
        {
          future.fail("failed");
          return;
        }
        SQLConnection connection = sqlConnection.result();
        connection.queryWithParams(
          "SELECT * FROM weather where CityName = ?"
          , new JsonArray().add("riyadh")
          , resultSet ->
          {
            client.close();

            if (!resultSet.succeeded())
            {
              future.fail(resultSet.cause());
              return;
            }

            ResultSet rs = resultSet.result();
            var weatherJson = new JsonObject(rs.getRows().get(0).getString("weatherInfo"));
            future.complete(weatherJson);
          });
      });
    }

    private static void createClient(Vertx vertx)
    {
      client = JDBCClient.createShared(vertx,
        new JsonObject()
          .put("user", "some username")
          .put("password", "some password")
          .put("driver_class", "com.mysql.cj.jdbc.Driver")
          .put("url", "jdbc:mysql://db_link/weather_local"));
    }
  }
}
