package com.example.test;

import com.example.starter.AviationService;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.junit5.VertxExtension;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;

@ExtendWith(VertxExtension.class)
@RunWith(VertxUnitRunner.class)
public class TestMainVerticle {

//  @Rule
//  public RunTestOnContext rule = new RunTestOnContext();
//
//  @Before
//  public void deploy_verticle(TestContext testContext) {
//    Vertx vertx = rule.vertx();
//    vertx.deployVerticle(new AviationService(), testContext.asyncAssertSuccess());
//  }
//
//  @Test
//  public void verticle_deployed(TestContext testContext) throws Throwable {
//    Async async = testContext.async();
//    async.complete();
//  }


  @Test
  public void emptyObject_testMethod_assertTrue() throws Exception
  {
    // arrange
    // arrange objects to send
    JsonObject input = new JsonObject();
    AviationService aviationService = new AviationService();

    // act
    // call for method
    JsonObject output = aviationService.forTestingPurposesOnly(input);

    // assert
    // check input against expected output
    Assert.assertEquals(input,output);
  }

}
