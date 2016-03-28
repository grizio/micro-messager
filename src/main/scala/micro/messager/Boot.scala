package micro.messager

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.stream.ActorMaterializer

object Boot extends App {
  implicit val system = ActorSystem("micro-messager")
  implicit val materializer = ActorMaterializer()
  implicit val dispatcher = system.dispatcher

  // Creates the main actor UsersActor
  system.actorOf(UsersActor.props, "users")

  Http().bindAndHandle(Router.route, "localhost", 8080)
}
