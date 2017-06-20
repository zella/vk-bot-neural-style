package org.zella.api
import java.io.File

import scala.concurrent.Future

/**
  * @author zella.
  */
trait IVk {
  def sendFile(text: String, file: File, userId: Int): Future[Unit]
  def sendMsg(text: String, userId: Int): Future[Unit]
}
