# Oversite

### What is a Oversite? ###

Oversite is an open-source service written in Scala developed by the good people at Growin.

* It is a mechanism of email supervision. A user "A" grants another user "B" the capacity of acessing target chat. With this the user B has all the access to every email, within that chat, that the user A sent, received, was made CC or BCC.

Current version: 0.1

## What do i need to run?

* The project:
* A RDBMS (open source relational database management system), we recommend mysql, since it was the one we used. Besides that, it is the only RDBM which is ready to use without any changes. (The table creations are in a package called sql, and the schema must be called "oversite")
* Postman to make calls to the api

## Running

* First start by cloning this repository
`git clone https://github.com/PedroCorreiaLuis/OverSite.git`

* Then **`cd`** into it

* To execute, `sbt run`

Go to <http://localhost:9000> to see the running web application.
  
  <br/>

---

### Who do I talk to? ###

OverSite is an Open-Source project developed at Growin in our offices in Lisbon.
 <br/> If you have any questions, you can contact:
 
 * Pedro Correia Luís  - pluis@growin.pt
 * Rui Valente         - rvalente@growin.pt
* Valter Fernandes    - vfernandes@growin.pt

Or visit our website: [www.growin.com](https://www.growin.com/)

<br/>

---

### License ###

Open source licensed under the [MIT License](https://opensource.org/licenses/MIT)
