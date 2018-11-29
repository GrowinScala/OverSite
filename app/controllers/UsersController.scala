package controllers

import javax.inject._

import play.api.mvc._

class UsersController @Inject()(cc: ControllerComponents) (implicit assetsFinder: AssetsFinder)
  extends AbstractController(cc) {

  /**
    * Create an Action to render an HTML page with a welcome message.
    * The configuration in the `routes` file means that this method
    * will be called when the application receives a `GET` request with
    * a path of `/users`.
    */
  def users: ??? = Action.async {
    Ok(views.html.index("Your user account is created"))
  }

}
