package micro.messager

import akka.actor.{Actor, ActorRef, Props}

/**
 * This actor manages is the supervisor of all users.
 * It is responsible to create them and to define the strategy when a user failed.
 */
object UsersActor {

  /** This trait is used to describe all actions available for [[UsersActor]]. */
  sealed trait Action

  /** Creates a new user with given username */
  case class Create(username: String) extends Action

  /** This trait is used to describe all responses returnable by [[UsersActor]]. */
  sealed trait Response

  /** The user was created. It is available to given [[ActorRef]]. */
  case class Created(actorRef: ActorRef) extends Response

  /** The user already exist. */
  case object Existing extends Response

  /** This function is used to create the Actor. */
  def props: Props = Props[UsersActor]
}

class UsersActor extends Actor {

  import UsersActor._

  override def receive: Receive = {
    case UsersActor.Create(username: String) =>
      // We check if the actor already exist
      if (context.child(username).isEmpty) {
        // The actor does not exist, we create it and return its path to sender
        sender ! Created(context.actorOf(UserActor.props(username), username))
      } else {
        // The actor already exists, we inform the sender
        sender ! Existing
      }
  }
}

