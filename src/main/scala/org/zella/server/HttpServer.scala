package org.zella.server

import java.net.URL
import java.nio.file.Files
import java.util.UUID

import akka.actor.ActorRef
import com.typesafe.scalalogging.Logger
import io.vertx.core.http.HttpServerRequest
import io.vertx.core.{AsyncResult, Handler, Vertx, http}
import io.vertx.ext.web.handler.BodyHandler
import io.vertx.ext.web.{Router, RoutingContext}
import org.zella.conf.IConfig
import org.zella.response.ResponseSender.VkResponse
import org.zella.worker.ComputeWorker.DoJob
import play.api.libs.json.{JsObject, Json}

import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.sys.process._
import scala.util.{Failure, Success}

/**
  * @author zella.
  */
class HttpServer(conf: IConfig, computeWorker: ActorRef, responseSender: ActorRef) {

  private val log = Logger[HttpServer]

  private val vertx = Vertx.vertx

  private var httpServer: io.vertx.core.http.HttpServer = _

  private implicit val ex = new ExecutionContext {
    override def execute(runnable: Runnable): Unit = vertx.runOnContext(new Handler[Void] {
      override def handle(event: Void): Unit = runnable.run()
    })

    override def reportFailure(cause: Throwable): Unit = throw cause
  }

  def startAsync(): Future[Unit] = {

    val startPromise = Promise[Unit]()

    val router = Router.router(vertx)

    router.route().handler(BodyHandler.create())
    router.get("/api/test").handler((ctx: RoutingContext) => {
      ctx.response().end("hello)))")
    })
    router.post("/api/vk").handler((ctx: RoutingContext) => {

      val bodyJs = Json.parse(ctx.getBodyAsString)

      log.debug(bodyJs.toString())

      val _type = bodyJs.\("type").as[String]
      _type match {
        case "confirmation" => ctx.response().end(conf.vkConfirmationCode)
        case "message_new" =>
          ctx.response().end("ok")

          val userId = bodyJs.\("object").\("user_id").as[Int]

          log.info(s"Request from: $userId")

          ctx.put("userId", userId)

          val attachments = bodyJs.\("object").\("attachments").as[Seq[JsObject]]
          val text = bodyJs.\("object").\("body").as[String]

          val modelPathOpt = conf.modelPathById(text.toLowerCase.replace(" ","_")) //hack(((
          if (attachments.size != 1 ||
            !attachments.head.\("type").as[String].equals("photo") ||
            modelPathOpt.isEmpty
          ) {
            responseSender ! VkResponse(userId, conf.textInstruction, None)
          } else {

            val src1280 = attachments.head.\("photo").\("photo_1280").asOpt[String]
            val src807 = attachments.head.\("photo").\("photo_807").asOpt[String]
            val src604 = attachments.head.\("photo").\("photo_604").asOpt[String]
            val src130 = attachments.head.\("photo").\("photo_130").asOpt[String]

            val src = src1280.getOrElse(src807.getOrElse(src604.getOrElse(src130.get)))

            Future {
              Files.createDirectories(conf.pathUploads)
              val localImage = conf.pathUploads.resolve(UUID.randomUUID().toString + ".jpg").toFile
              new URL(src) #> localImage !!

              log.debug("Download done: " + localImage.toString)
              localImage.toPath
            }(ExecutionContext.global) onComplete {
              //TODO bad))) poh, no time
              case Success(file) => computeWorker tell(DoJob(file, modelPathOpt.get, userId), responseSender)
              case Failure(e) =>
                log.error("new_message fail", e)
                responseSender ! VkResponse(userId, conf.textInstruction, None)
            }
          }
      }
    }).failureHandler((ctx: RoutingContext) => {
      log.error("new_message fail", ctx.failure())
      responseSender ! VkResponse(ctx.get("userId"), conf.textInstruction, None)
    })


    httpServer = vertx.createHttpServer().requestHandler((event: HttpServerRequest) => {
      router.accept(event)

    }).listen(conf.netPort, (event: AsyncResult[http.HttpServer]) => {
      log.info(s"Server started at port ${conf.netPort}")
      startPromise.success(Unit)
    })
    startPromise.future
  }


}

/*
что тестить

confirmation отправляет ответ
message_new где нет фото или много итд, шлет VkResponse(userId, conf.instructionText, None) на респонс сендера
message_new где все есть, скачивает картинку из photo_1280 и шлет DoJob



 */