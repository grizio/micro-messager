package micro.messager

import akka.actor.{Actor, Props}

// TODO: defines its content
object UserActor {

  /** This function is used to create the Actor with its username. */
  def props(username: String): Props = Props(new UserActor(username))
}

class UserActor(val username: String) extends Actor {
  // TODO: defines its behavior
  override def receive: Receive = {
    case _ => // TODO: implements behavior
  }
}