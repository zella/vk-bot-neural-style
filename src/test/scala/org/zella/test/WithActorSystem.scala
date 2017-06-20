package org.zella.test


import akka.actor.ActorSystem
import akka.testkit.TestKit
import com.typesafe.scalalogging.Logger
import org.junit.{After, Before}
import org.scalatest.Matchers
import org.scalatest.junit.AssertionsForJUnit


/**
  * @author zella.
  */
trait WithActorSystem extends AssertionsForJUnit with Matchers {

  val log = Logger[WithActorSystem]

  protected implicit var system: ActorSystem = _

  @Before
  def setup(): Unit = {
    system = ActorSystem.create("test_chat_system")
    log.debug("Create actor system...")
  }

  @After
  def tearDown(): Unit = {
    TestKit.shutdownActorSystem(system)
    log.debug("Shutdown actor system...")
    system = null
  }


}