package micro.messager

import java.util.concurrent.TimeUnit

import akka.actor.ActorSystem
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.util.Timeout

import scala.concurrent.ExecutionContext

object Router {
  implicit val timeout = Timeout(1, TimeUnit.SECONDS)

  def userPath(username: String)(implicit system: ActorSystem) = system.actorSelection(s"user/users/$username")

  def route(implicit system: ActorSystem, ec: ExecutionContext) =
    uiRoute

  /**
   * Returns the routes for UI files (content of `src/main/resources/ui`).
   */
  val uiRoute =
    pathPrefix("ui") {
      pathSingleSlash {
        getFromResource("ui/index.html")
      } ~ pathEnd {
        redirect("/ui/", StatusCodes.PermanentRedirect)
      } ~ {
        getFromResourceDirectory("ui")
      }
    } ~ pathEndOrSingleSlash {
      redirect("/ui/", StatusCodes.PermanentRedirect)
    }
}
