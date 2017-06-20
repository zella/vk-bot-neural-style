package org.zella.runner

import akka.actor.ActorSystem
import com.typesafe.scalalogging.Logger
import org.zella.api.impl.Vk
import org.zella.conf.impl.BotConfig
import org.zella.response.ResponseSender
import org.zella.server.HttpServer
import org.zella.worker.ComputeWorker

import scala.concurrent.Await

/**
  * @author zella.
  */
object Runner {
  val log = Logger[Runner.type]

  def main(args: Array[String]): Unit = {

    val system = ActorSystem.create()

    val conf = new BotConfig(com.typesafe.config.ConfigFactory.load())

    val vk = new Vk(conf.vkGroupId,conf.vkToken)

    val responseSender = system.actorOf(ResponseSender.props(vk,conf))

    val computeWorker = system.actorOf(ComputeWorker.props(conf))

    val httpServer = new HttpServer(conf, computeWorker, responseSender)
    httpServer.startAsync()
  }
}
