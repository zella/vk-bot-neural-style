package org.zella.worker

import java.nio.file.{Files, Path}

import akka.actor.{Actor, Props}
import org.zella.conf.IConfig
import org.zella.neuralstyle.impl.ComputeFastStyleJob
import org.zella.response.ResponseSender.VkResponse
import org.zella.worker.ComputeWorker.DoJob


/**
  * @author zella.
  */
object ComputeWorker {

  case class DoJob(inputImage: Path, modelPath: Path, fromId: Int)

  def props(conf: IConfig) = Props(new ComputeWorker(conf))
}

class ComputeWorker(conf: IConfig) extends Actor {

  override def receive: Receive = {
    case DoJob(inputImage, modelPath, from) =>
      //TODO error kernel pattern with supervisor???
      val job = ComputeFastStyleJob(inputImage, modelPath, conf.pathFastStyle, conf.pathTorchExe)
      val result = job.runSync()
      //rem input
      //if fails no removes!
      Files.deleteIfExists(inputImage)
      sender ! {
        result match {
          //TODO conf
          case Left(reason) => VkResponse(from, conf.textInternalError, None)
          case Right(path) => VkResponse(from, conf.textSuccess, Some(path))
        }
      }
  }

}

/*

Что тестить
dojob вызывает компутацию и сендер получает VkResponse(from, "Лови", Some(path))
также файл создан
 и что оригинал удален нахуй


 */

