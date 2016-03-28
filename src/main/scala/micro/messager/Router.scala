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

  /** Helper to get the Actor reference in terms of its username. */
  def userPath(username: String)(implicit system: ActorSystem) = system.actorSelection(s"user/users/$username")

  def route(implicit system: ActorSystem, ec: ExecutionContext) =
    createUserRoute ~
      sendMessageToUserRoute ~
      sendMessageRoute ~
      pullReceivedMessages ~
      fetchReceivedMessages ~
      pullSentMessages ~
      subscribeRoute ~
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
   * Returns the route used to send a message to a user
   * {{{
   * request:
   * | POST /<user>/send/<target> [String body]
   * response:
   * | 200 OK
   * }}}
   */
  def sendMessageToUserRoute(implicit system: ActorSystem, ec: ExecutionContext) =
  // request: path = /<user>
    pathPrefix(Segment) { currentUsername =>
      // request: path = /<user>/send
      pathPrefix("send") {
        // request: path = /<user>/send/<target>
        path(Segment) { username =>
          // request: POST /<user>/send/<target>
          post {
            // Body as String
            entity(as[String]) { message =>
              // We send the message to the actor
              // Usage of "fire and forget", so we do not know if the message arrive (design choice)
              userPath(currentUsername) ! UserActor.SendToUser(username, message)
              // Always complete with OK
              complete("OK")
            }
          }
        }
      }
    }

  /**
   * Returns the route used to send a message to all subscribers.
   * {{{
   * request:
   * | POST /<user>/send [String body]
   * response:
   * | 200 OK
   * }}}
   */
  def sendMessageRoute(implicit system: ActorSystem, ec: ExecutionContext) =
  // request: path = /<user>
    pathPrefix(Segment) { currentUsername =>
      // request: path = /<user>/send
      pathPrefix("send") {
        // We avoid conflict with sendMessageToUserRoute by indicating the end of the route
        pathEndOrSingleSlash {
          // request: POST /<user>/send
          post {
            // Body as String
            entity(as[String]) { message =>
              // We send the message to the actor
              // Usage of "fire and forget", so we do not know if the message arrive (design choice)
              userPath(currentUsername) ! UserActor.Send(message)
              // Always complete with OK
              complete("OK")
            }
          }
        }
      }
    }

  /**
   * Returns the route used to pull received messages
   * {{{
   * request:
   * | GET /<user>/pull [no body]
   * response:
   * | 200 <Messages(List[String])>
   * }}}
   */
  def pullReceivedMessages(implicit system: ActorSystem, ec: ExecutionContext) =
  // request: path = /<user>
    pathPrefix(Segment) { currentUsername =>
      // request: path = /<user>/pull
      path("pull") {
        // request: GET /<user>/pull
        get {
          // Complete the route with nested result
          complete {
            // We ask the whole list of received messages
            // It will be our response (converted to simple string in this project).
            // You can use libraries to transform into JSON or XML or other.
            // See http://doc.akka.io/docs/akka/2.4.2/scala/http/common/json-support.html
            (userPath(currentUsername) ? UserActor.Pull).mapTo[UserActor.Response].map(_.toString)
          }
        }
      }
    }

  /**
   * Returns the route used to fetch received messages until last fetch
   * {{{
   * request:
   * | GET /<user>/fetch [no body]
   * response:
   * | 200 <Messages(List[String])>
   * }}}
   */
  def fetchReceivedMessages(implicit system: ActorSystem, ec: ExecutionContext) =
  // request: path = /<user>
    pathPrefix(Segment) { currentUsername =>
      // request: path = /<user>/pull
      path("fetch") {
        // request: GET /<user>/fetch
        get {
          // Complete the route with nested result
          complete {
            // We ask the whole list of received messages
            // It will be our response (converted to simple string in this project).
            // You can use libraries to transform into JSON or XML or other.
            // See http://doc.akka.io/docs/akka/2.4.2/scala/http/common/json-support.html
            (userPath(currentUsername) ? UserActor.Fetch).mapTo[UserActor.Response].map(_.toString)
          }
        }
      }
    }

  /**
   * Returns the route used to pull sent messages
   * {{{
   * request:
   * | GET /<user>/sent [no body]
   * response:
   * | 200 <Messages(List[String])>
   * }}}
   */
  def pullSentMessages(implicit system: ActorSystem, ec: ExecutionContext) =
  // request: path = /<user>
    pathPrefix(Segment) { currentUsername =>
      // request: path = /<user>/sent
      path("sent") {
        // request: GET /<user>/sent
        get {
          // Complete the route with nested result
          complete {
            // We ask the whole list of sent messages
            // It will be our response (converted to simple string in this project).
            // You can use libraries to transform into JSON or XML or other.
            // See http://doc.akka.io/docs/akka/2.4.2/scala/http/common/json-support.html
            (userPath(currentUsername) ? UserActor.Sent).mapTo[UserActor.Response].map(_.toString)
          }
        }
      }
    }

  /**
   * Returns the route used to subscribe to another user.
   * {{{
   * request:
   * | POST /<user>/subscribe/<target> [no body]
   * response:
   * | 200 OK
   * }}}
   */
  def subscribeRoute(implicit system: ActorSystem, ec: ExecutionContext) =
  // request: path = /<user>
    pathPrefix(Segment) { currentUsername =>
      // request: path = /<user>/subscribe
      pathPrefix("subscribe") {
        // request: path = /<user>/subscribe/<target>
        path(Segment) { target =>
          // request: POST /<user>/subscribe/<target>
          post {
            // We send the subscription to the actor
            // Usage of "fire and forget", so we do not know if the message arrive (design choice)
            userPath(currentUsername) ! UserActor.Subscribe(target)
            // Always complete with OK
            complete("OK")
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
