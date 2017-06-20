package org.zella.worker


import java.nio.file.{Files, Path, Paths}

import akka.testkit.{TestActorRef, TestProbe}
import org.apache.commons.io.FileUtils
import org.junit.{After, Before, Test}
import org.zella.conf.impl.BotConfig
import org.zella.response.ResponseSender.VkResponse
import org.zella.test.WithActorSystem
import org.zella.worker.ComputeWorker.DoJob

/**
  * @author zella.
  */
class ComputeWorkerTest extends WithActorSystem {


  val conf = new BotConfig(com.typesafe.config.ConfigFactory.load("test.conf"))

  //TODO tmp dir
  @Before
  def prepareInput(): Unit = {
    Files.createDirectories(conf.pathUploads)
    Files.copy(
      Paths.get(ClassLoader.getSystemResource("testInput.jpg").toURI),
      conf.pathUploads.resolve("testInput.jpg")
    )
    Files.copy(
      Paths.get(ClassLoader.getSystemResource("testInput2.jpg").toURI),
      conf.pathUploads.resolve("testInput2.jpg")
    )
  }

  @After
  def cleanFiles(): Unit = {
    if (conf.pathUploads.toFile.exists())
      FileUtils.deleteDirectory(conf.pathUploads.toFile)
  }


  @Test
  def doValidJobShouldGenOutputResponseToSender(): Unit = {

    val initiator = TestProbe()

    val worker = TestActorRef(ComputeWorker.props(conf))
    val inputImage = conf.pathUploads.resolve("testInput.jpg")
    val inputImage2 = conf.pathUploads.resolve("testInput2.jpg")

    worker.tell(DoJob(inputImage, conf.modelPathById("starry_night").get, 12345), initiator.ref)
    worker.tell(DoJob(inputImage2, conf.modelPathById("la_muse").get, 12345), initiator.ref)

    initiator expectMsg
      VkResponse(12345, conf.textSuccess, Some(inputImage.getParent.resolve("out_" + inputImage.getFileName.toString)))

    Files.list(conf.pathUploads)
      .count() shouldBe 2 //first output second input
    val inputFileOpt = Files.list(conf.pathUploads)
      .filter((t: Path) => t.getFileName.toString.equals("out_testInput.jpg") && t.toFile.length() > 1000).findAny()
    inputFileOpt should not be None

    initiator expectMsg
      VkResponse(12345, conf.textSuccess, Some(inputImage2.getParent.resolve("out_" + inputImage2.getFileName.toString)))

    Files.list(conf.pathUploads)
      .count() shouldBe 2   //first second output
    val inputFileOpt2 = Files.list(conf.pathUploads)
      .filter((t: Path) => t.getFileName.toString.equals("out_testInput2.jpg") && t.toFile.length() > 1000).findAny()
    inputFileOpt2 should not be None
  }

  @Test
  def doInValidJobShouldResponseToSender(): Unit = {

    val initiator = TestProbe()

    val worker = TestActorRef(ComputeWorker.props(conf))
    val inputImage = conf.pathUploads.resolve("testInput.jpg")

    //simulate inValidParam, that fail cmd
    worker.tell(DoJob(inputImage, null, 12345), initiator.ref)

    initiator expectMsg
      VkResponse(12345, conf.textInternalError, None)

    Files.list(conf.pathUploads)
      .count() shouldBe 1 //because on fail removes!
  }
}
