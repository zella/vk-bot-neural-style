package org.zella.response

import java.nio.file.Paths

import akka.testkit.TestActorRef
import org.junit.Test
import org.mockito.Mockito._
import org.zella.api.IVk
import org.zella.conf.impl.BotConfig
import org.zella.response.ResponseSender.VkResponse
import org.zella.test.WithActorSystem

/**
  * @author zella.
  */
class ResponseSenderTest extends WithActorSystem {

  val conf = new BotConfig(com.typesafe.config.ConfigFactory.load("test.conf"))

  @Test
  def receiveVkResponseShouldCallApi(): Unit = {

    val vk = mock(classOf[IVk])

    val vkSender = TestActorRef(ResponseSender.props(vk, conf))

    vkSender ! VkResponse(1234: Int, "Hello", Some(Paths.get("test")))
    Thread.sleep(100)
    verify(vk, times(1)).sendFile("Hello", Paths.get("test").toFile, 1234)

    vkSender ! VkResponse(1234: Int, "Hello", None)
    Thread.sleep(100)
    verify(vk, times(1)).sendMsg("Hello", 1234)

  }


}
