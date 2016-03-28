package micro.messager

import akka.actor.{Actor, ActorRef, ActorSelection, Props}

object UserActor {

  /** This trait is used to describe all actions available for [[UserActor]]. */
  sealed trait Action

  /** This action is used to send the [[message]] to the [[target]]. */
  case class SendToUser(target: String, message: String) extends Action

  /** This action is used to send a message to all subscribers. */
  case class Send(message: String) extends Action

  /** This action is used to ask the actor to return the whole list of received messages. */
  case object Pull extends Action

  /** This action is used to ask the actor to return the whole list of received messages until last fetch. */
  case object Fetch extends Action

  /** This action is used to ask the actor to return the whole list of sent messages. */
  case object Sent extends Action

  /** The actor subscribes to [[target]] actor */
  case class Subscribe(target: String) extends Action

  /** (Internal use only) This action is used to accept a received [[message]] from [[source]]. */
  private case class Received(source: String, message: String) extends Action

  /** The sender is subscribed to current actor. */
  private case object Subscribed extends Action

  /** This trait is used to describe all responses returnable by [[UserActor]]. */
  sealed trait Response

  /** The list of requested messages */
  case class Messages(messages: Seq[String]) extends Response

  /** This function is used to create the Actor with its username. */
  def props(username: String): Props = Props(new UserActor(username))
}

class UserActor(val username: String) extends Actor {

  import UserActor._

  var receivedMessages = List[String]()

  /** This list contains only messages until last fetch request. */
  var messagesToFetch = List[String]()

  var sentMessages = List[String]()

  /** List of subscribers */
  var subscribers = List[ActorRef]()

  override def receive: Receive = {
    // Public API
    case SendToUser(target, message) =>
      // We send the message to the targeted actor
      getUserActor(target) ! Received(username, message)
      sentMessages = s"[$username] $message" :: sentMessages
    case Send(message) =>
      // Send the message to all subscribers
      subscribers.foreach(_ ! Received(username, message))
      // Save the message into sent messages
      sentMessages = message :: sentMessages
    case Pull =>
      // We send the whole list of received messages
      sender ! Messages(receivedMessages)
    case Fetch =>
      sender ! Messages(messagesToFetch)
      messagesToFetch = List()
    case Sent =>
      // We send the whole list of sent messages
      sender ! Messages(sentMessages)
    case Subscribe(target) =>
      // Send the subscription to target actor
      getUserActor(target) ! Subscribed

    // Private API
    case Received(source, message) =>
      val savedMessage = s"[$source] $message"
      // We received a message, we save it.
      receivedMessages = savedMessage :: receivedMessages
      // We also save it into the list of messages to fetch (see property documentation)
      messagesToFetch = savedMessage :: messagesToFetch
    case Subscribed =>
      // Add the sender the subscribers
      subscribers = sender :: subscribers
  }

  // Because we never use this actor directly, but always pass by receive,
  // We can miss the private keyword.

  /** Helper to return the reference to the actor with given `target` as username. */
  def getUserActor(target: String): ActorSelection = context.actorSelection(context.parent.path / target)
}