import slick.jdbc.MySQLProfile.api._


val actions = new Actions()
val db = Database.forConfig("mysql")

//db.run(actions.insertChat)

actions.exec(actions.insertEmail)
