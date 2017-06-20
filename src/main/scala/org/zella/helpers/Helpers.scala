package org.zella.helpers

import java.util.concurrent.TimeUnit

import play.api.libs.json.{JsValue, Json}

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

/**
  * @author zella.
  */
object Helpers {

  implicit class StringToJson(src: String) {

    /**
      * Parse to json. Throws exceptions
      */
    def asJs: JsValue = {
      Json.parse(src)
    }
  }

  implicit class WaitFuture[T](future: Future[T]) {
    def waitResult(): T = Await.result(future, Duration(5, TimeUnit.SECONDS))

    def waitResult(duration: Duration): T = Await.result(future, duration)
  }

}
