package micro.messager

import akka.actor.{Actor, ActorSelection, Props}

// TODO: defines its content
object UserActor {

  /** This trait is used to describe all actions available for [[UserActor]]. */
  sealed trait Action

  /** This action is used to send the [[message]] to the [[target]]. */
  case class SendToUser(target: String, message: String) extends Action

  /** (Internal use only) This action is used to accept a received [[message]] from [[source]]. */
  private case class Received(source: String, message: String) extends Action

  /** This function is used to create the Actor with its username. */
  def props(username: String): Props = Props(new UserActor(username))
}

class UserActor(val username: String) extends Actor {

  import UserActor._

  var receivedMessages = List[String]()

  override def receive: Receive = {
    case SendToUser(target, message) =>
      // We send the message to the targeted actor
      getUserActor(target) ! Received(username, message)
    case Received(source, message) =>
      // We received a message, we save it.
      receivedMessages = s"[$source] $message" :: receivedMessages
  }

  // Because we never use this actor directly, but always pass by receive,
  // We can miss the private keyword.

  /** Helper to return the reference to the actor with given `target` as username. */
  def getUserActor(target: String): ActorSelection = context.actorSelection(context.parent.path / target)
}