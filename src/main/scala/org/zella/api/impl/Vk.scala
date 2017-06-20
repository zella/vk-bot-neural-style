package org.zella.api.impl

import java.io.File

import com.typesafe.scalalogging.Logger
import com.vk.api.sdk.client.VkApiClient
import com.vk.api.sdk.client.actors.GroupActor
import com.vk.api.sdk.httpclient.HttpTransportClient
import org.zella.api.IVk

import scala.concurrent.{ExecutionContext, Future}

/**
  * @author zella.
  */
class Vk(groupId: Int, token: String) extends IVk {

  val log = Logger[Vk]

  private val ex = ExecutionContext.global

  private val client = new HttpTransportClient
  private val vk = new VkApiClient(client)
  private val actor = new GroupActor(groupId, token)


  private def sendMsgSync(text: String, userId: Int): Int = {
    val getResponse = vk.messages().send(actor).message(text).userId(userId).execute()
    getResponse
  }

  private def sendFileSync(text: String, file: File, userId: Int): Int = {
    val resp = vk.photos().getMessagesUploadServer(actor).execute()
    val uploadResp = vk.upload().photoMessage(resp.getUploadUrl, file).execute()
    val photoList = vk.photos().saveMessagesPhoto(actor, uploadResp.getPhoto)
      .server(uploadResp.getServer)
      .hash(uploadResp.getHash)
      .execute()
    val photo = photoList.get(0)
    val attachId = "photo" + photo.getOwnerId + "_" + photo.getId
    val getResponse = vk.messages().send(actor).message(text).userId(userId).attachment(attachId).execute()
    getResponse
  }

  override def sendFile(text: String, file: File, userId: Int): Future[Unit] = {
    Future[Unit] {
      //TODO
      if (sendFileSync(text, file, userId) != 1) Future.failed(new RuntimeException("Ошибка отправки картиник в вк"))
    }(ex)
  }

  override def sendMsg(text: String, userId: Int): Future[Unit] = {
    Future[Unit] {
      val code = sendMsgSync(text, userId)
      if (code == -1) {
        throw new RuntimeException("Send msg fail")
      }
    }(ex)
  }

}
