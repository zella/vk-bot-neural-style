package org.zella.server

import java.nio.file.Files

import akka.testkit.TestProbe
import com.netaporter.uri.dsl._
import dispatch.Defaults._
import dispatch.{Http, url}
import org.apache.commons.io.FileUtils
import org.junit.{After, Test}
import org.zella.conf.impl.BotConfig
import org.zella.helpers.Helpers._
import org.zella.response.ResponseSender.VkResponse
import org.zella.test.WithActorSystem
import org.zella.worker.ComputeWorker.DoJob
import play.api.libs.json.Json

/**
  * @author zella.
  */
class HttpServerTest extends WithActorSystem {


  val conf = new BotConfig(com.typesafe.config.ConfigFactory.load("test.conf"))

  @After
  def cleanFiles(): Unit = {
    if (conf.pathUploads.toFile.exists())
      FileUtils.deleteDirectory(conf.pathUploads.toFile)
  }


  @Test
  def confirmationRequestShouldReturnConfirmationCode(): Unit = {

    val computeWorker = TestProbe()
    val respSender = TestProbe()

    val httpServer = new HttpServer(conf, computeWorker.ref, respSender.ref)
    httpServer.startAsync().waitResult()

    val _url = ("http://127.0.0.1:" + conf.netPort) / "api/vk"

    val req = url(_url)
      .POST
      .setBodyEncoding("UTF-8")
      .setBody(Json.obj(
        "type" -> "confirmation"
      ).toString())

    val resp = Http(req).waitResult()

    resp.getStatusCode shouldBe 200
    resp.getResponseBody shouldBe conf.vkConfirmationCode
  }

  @Test
  def uploadPhotoWithValidParamsShouldDownloadFileThenRequestComputation(): Unit = {

    val computeWorker = TestProbe()
    val respSender = TestProbe()

    val httpServer = new HttpServer(conf, computeWorker.ref, respSender.ref)
    httpServer.startAsync().waitResult()

    val _url = ("http://127.0.0.1:" + conf.netPort) / "api/vk"

    val req = url(_url)
      .POST
      .setBodyEncoding("UTF-8")
      .setBody(Json.obj(
        "type" -> "message_new",
        "object" -> Json.obj(
          "user_id" -> 12345,
          "body" -> "starry_night", //вангог
          "attachments" -> Seq(Json.obj(
            "type" -> "photo",
            "photo" ->  Json.obj("photo_604" -> "https://upload.wikimedia.org/wikipedia/commons/thumb/1/15/2016-02-23_15-48-06_paris.jpg/800px-2016-02-23_15-48-06_paris.jpg"))))
      ).toString())

    val resp = Http(req).waitResult()

    resp.getStatusCode shouldBe 200
    resp.getResponseBody shouldBe "ok"

    val doJobMsg = computeWorker expectMsgClass classOf[DoJob]

    Files.list(conf.pathUploads)
      .count() shouldBe 1
    val inputFileOpt = conf.pathUploads.toFile.listFiles()
      .find(f => f.getName.endsWith(".jpg") && f.length() > 1000)
    inputFileOpt should not be None

    val d = conf.modelPathById("starry_night")
    doJobMsg shouldBe DoJob(inputFileOpt.get.toPath, d.get, 12345)

  }


  @Test
  def uploadPhotoWithInValidModelShouldSendVkResponse(): Unit = {

    val computeWorker = TestProbe()
    val respSender = TestProbe()

    val httpServer = new HttpServer(conf, computeWorker.ref, respSender.ref)
    httpServer.startAsync().waitResult()

    val _url = ("http://127.0.0.1:" + conf.netPort) / "api/vk"

    val req = url(_url)
      .POST
      .setBodyEncoding("UTF-8")
      .setBody(Json.obj(
        "type" -> "message_new",
        "object" -> Json.obj(
          "user_id" -> 12345,
          "body" -> "starry_night нет такой", //вангог
          "attachments" -> Seq(Json.obj(
            "type" -> "photo",
            "photo" -> Json.obj("photo_604" -> "https://upload.wikimedia.org/wikipedia/commons/thumb/1/15/2016-02-23_15-48-06_paris.jpg/800px-2016-02-23_15-48-06_paris.jpg"))))
      ).toString())

    val resp = Http(req).waitResult()

    resp.getStatusCode shouldBe 200
    resp.getResponseBody shouldBe "ok"

    respSender expectMsgClass classOf[VkResponse]

    computeWorker expectNoMsg
  }

  @Test
  def uploadPhotoWithInValidAttacjShouldSendVkResponse(): Unit = {

    val computeWorker = TestProbe()
    val respSender = TestProbe()

    val httpServer = new HttpServer(conf, computeWorker.ref, respSender.ref)
    httpServer.startAsync().waitResult()

    val _url = ("http://127.0.0.1:" + conf.netPort) / "api/vk"

    val req = url(_url)
      .POST
      .setBodyEncoding("UTF-8")
      .setBody(Json.obj(
        "type" -> "message_new",
        "object" -> Json.obj(
          "user_id" -> 12345,
          "body" -> "starry_night", //вангог
          "attachments" -> Seq(Json.obj(
            "type" -> "photo",
            "photo INVALID" -> Json.obj("photo_604" -> "https://upload.wikimedia.org/wikipedia/commons/thumb/1/15/2016-02-23_15-48-06_paris.jpg/800px-2016-02-23_15-48-06_paris.jpg"))))
      ).toString())

    val resp = Http(req).waitResult()

    resp.getStatusCode shouldBe 200
    resp.getResponseBody shouldBe "ok"

    Thread.sleep(1000)

    respSender expectMsgClass classOf[VkResponse]

    computeWorker expectNoMsg
  }
}
