package controllers

import play.api.mvc._
import play.api.libs.json.{Json, JsValue}
import play.api.libs.iteratee.{Concurrent, Enumeratee}
import play.api.libs.EventSource
import play.api.libs.concurrent.Execution.Implicits._

object SesameApplication extends Controller {

  val (authorisationOut, authorisationChannel) = Concurrent.broadcast[JsValue]

  def authoriseLoginPage(loginPageId: String) = IdentityAuthenticated { implicit request =>
    println(s"loginPageId=$loginPageId user=${request.user}")
    authorisationChannel.push(Json.parse(s"""{"loginPageId":"$loginPageId"}""")); Ok
  }

  /** Controller action serving activity based on room */
  def authenticationFeed(loginPageId: String) = Action { req =>
  /** Enumeratee for filtering messages based on room */
    def filterLoginPage = Enumeratee.filter[JsValue] { json: JsValue => (json \ "loginPageId").as[String] == loginPageId }

    /** Enumeratee for detecting disconnect of SSE stream */
    def connDeathWatch(addr: String): Enumeratee[JsValue, JsValue] =
      Enumeratee.onIterateeDone{ () => println(addr + " - SSE disconnected") }

    println(req.remoteAddress + " - SSE connected")
    Ok.feed(authorisationOut
      &> filterLoginPage
      &> Concurrent.buffer(50)
      &> connDeathWatch(req.remoteAddress)
      &> EventSource()
    ).as("text/event-stream") 
  }

}
