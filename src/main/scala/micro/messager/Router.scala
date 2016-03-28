package micro.messager

import java.util.concurrent.TimeUnit

import akka.actor.ActorSystem
import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.pattern.ask
import akka.util.Timeout

import scala.concurrent.ExecutionContext

object Router {
  implicit val timeout = Timeout(1, TimeUnit.SECONDS)

  def userPath(username: String)(implicit system: ActorSystem) = system.actorSelection(s"user/users/$username")

  def route(implicit system: ActorSystem, ec: ExecutionContext) =
    createUserRoute ~
      uiRoute

  /**
   * Returns the route used to create a user.
   * {{{
   * request:
   * | POST /users/<username> [no body]
   * response:
   * | 200 created user: <actorPath>
   * | 400 existing user
   * }}}
   */
  def createUserRoute(implicit system: ActorSystem, ec: ExecutionContext) =
  // request: path = /users
    pathPrefix("users") {
      // request: path = /users/<username>
      path(Segment) { username =>
        // request: POST /users/<username>
        post {
          // Complete the route with nested result
          complete {
            // We ask the UsersActor to create the user
            (system.actorSelection("user/users") ? UsersActor.Create(username))
              .mapTo[UsersActor.Response] // We know the resulting type is a UsersActors.Response by convention.
              .map {
              case UsersActor.Created(path) =>
                // The user was created, we return OK
                HttpResponse(StatusCodes.OK, entity = s"created user: ${path.path}")
              case UsersActor.Existing =>
                // The user already existed, we return a BadRequest
                HttpResponse(StatusCodes.BadRequest, entity = "existing user")
            }
          }
        }
      }
    }

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
