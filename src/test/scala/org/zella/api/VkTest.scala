package org.zella.api

import java.nio.file.{Files, Paths}

import akka.testkit.{TestActorRef, TestProbe}
import com.typesafe.scalalogging.Logger
import org.junit.Test
import org.zella.api.impl.Vk
import org.zella.conf.impl.BotConfig
import org.zella.helpers.Helpers._
import org.zella.response.ResponseSender.VkResponse
import org.zella.test.WithActorSystem
import org.zella.worker.ComputeWorker.DoJob

/**
  * @author zella.
  */
class VkTest extends WithActorSystem {

  val configRaw = com.typesafe.config.ConfigFactory.load("test.conf")
  val conf = new BotConfig(configRaw)


  @Test
  def sendTextMsgToVkUser(): Unit = {

    val vk = new Vk(conf.vkGroupId, conf.vkToken)

    val user = configRaw.getInt("test.userWhoReceiveMsgInTest")

    vk.sendMsg("Тест из теста", user).waitResult()
    println(s"Please verify that user with id $user receive Тест из теста ")
  }

  @Test
  def sendFileMsgToVkUser(): Unit = {

    val vk = new Vk(conf.vkGroupId, conf.vkToken)

    val user = configRaw.getInt("test.userWhoReceiveMsgInTest")

    vk.sendFile("Тест из теста",
      Paths.get(ClassLoader.getSystemResource("testInput.jpg").toURI).toFile,
      user).waitResult()
    println(s"Please verify that user with id $user receive Тест из теста and image!")

  }

}
