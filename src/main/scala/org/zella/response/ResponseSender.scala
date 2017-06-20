package org.zella.response

import java.nio.file.{Files, Path}

import akka.actor.{Actor, Props}
import com.typesafe.scalalogging.Logger
import org.zella.api.IVk
import org.zella.conf.IConfig
import org.zella.response.ResponseSender.VkResponse

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}


object ResponseSender {

  case class VkResponse(fromUserId: Int, text: String, image: Option[Path])

  def props(vk: IVk, conf: IConfig) = Props(new ResponseSender(vk, conf))

}

/**
  * @author zella.
  */
class ResponseSender(vk: IVk, conf: IConfig) extends Actor {

  val log = Logger[ResponseSender]

  private implicit val ex = ExecutionContext.global

  override def receive: Receive = {
    case VkResponse(fromUserId, text, imageOpt) =>
      imageOpt match {
        case None => vk.sendMsg(text, fromUserId)
        case Some(photo) =>
          vk.sendFile(text, photo.toFile, fromUserId).onComplete {
            case Success(_) =>
              //Files.deleteIfExists(photo)
            case Failure(e) =>
              log.error("Файл не отправился", e)
              vk.sendMsg(conf.textInternalError, fromUserId)
          }
      }
  }
}

