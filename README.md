# Micro-messager

This project is a little workshop to get start with the actor model with Akka

## What is the objective?

The objective of this project is to show how work the actor model with Akka.
It defines the work to do and how to realize it step by step.


## How to use it?

Each commit of the project includes a new function.
So you can see differences between two versions and what is the purpose of the version (in commit message).

## Can I work with this project?

Of course!

Clone the project, then checkout "dev" branch to start.
If you are blocked, you can always check with master branch.

## TODO list

This part lists the work to do during the project.
Each item is a commit in this project.

- [x] Creates the base (Boot, Router)
- [ ] It is possible to create a new user if it does not exist
- [ ] A user can send a message to another user
- [ ] A user can pull messages he received (all messages)
- [ ] A user can pull messages he sent
- [ ] A user can fetch messages he received (messages until last time he fetch)
- [ ] A user can subscribe to another user and receive messages he globally send

## API

Method | Url                          | Description
---    | ---                          | ---
`POST` | `/users/<username>`          | Creates the user with given `username` if not exist (no body)
`POST` | `/<user>/send/<target>`      | From user `user`, sends a message to `target` user (string body)
`GET`  | `/<user>/pull`               | Pulls all messages the user received
`GET`  | `/<user>/sent`               | Pulls all messages the user sent
`GET`  | `/<user>/fetch`              | Fetches messages the user received until last fetch
`POST` | `/<user>/subscribe/<target>` | Subscribes to `target` to receive messages he sends globally (no body)
`POST` | `<user>/send`                | Sends a message globally (string body)

## Actor hierarchy

```
/                 # reserved to akka
  system          # reserved to akka
  user            # reserved to akka
    users         # Supervisor of all users
      <username1> # User <username1>
      <username2> # User <username2>
      ...
```