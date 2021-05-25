package com.example

import io.ktor.server.engine.*
import io.ktor.server.netty.*
import com.example.plugins.*
import io.ktor.application.*
import io.ktor.features.*
import io.ktor.jackson.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.ktor.request.receive
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import com.apurebase.kgraphql.*
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource

//val db = Database.connect("jdbc:h2:mem:regular;DB_CLOSE_DELAY=-1;", "org.h2.Driver")

object Products : Table(){
    val id: Column<Int> = integer("id").autoIncrement()
    val name: Column<String> = varchar("name" ,255)
    val description: Column<String> = varchar("description", 255)
    val cost: Column<Int> = integer("cost")

    override val primaryKey = PrimaryKey(id, name="PK_Product_ID")

    fun toProduct(row: ResultRow):Product =
        Product(
            id = row[Products.id],
            name = row[Products.name],
            description = row[Products.description],
            cost = row[Products.cost]
        )
}

fun Application.myapp(){

    install(ContentNegotiation) {
        jackson()
    }

    val config = HikariConfig().apply {
        jdbcUrl         = "jdbc:postgresql://database:5432/ktor"
        driverClassName = "org.postgresql.Driver"
        username        = "username"
        password        = "secret"
        maximumPoolSize = 10
    }
    val dataSource = HikariDataSource(config)
    Database.connect(dataSource)

//    Database.connect("jdbc:h2:mem:regular;DB_CLOSE_DELAY=-1;", "org.h2.Driver")

    transaction {
        SchemaUtils.create(Products)

        Products.insert {
            it[Products.name] = "Milk"
            it[Products.description] = "This is milk"
            it[Products.cost] = 50
        }
        Products.insert {
            it[Products.name] = "Xleb"
            it[Products.description] = "Xleb"
            it[Products.cost] = 50
        }
        Products.insert {
            it[Products.name] = "Paper"
            it[Products.description] = "paper"
            it[Products.cost] = 50
        }
    }

    val schema = KGraphQL.schema {
        query("products"){
            resolver{->
                transaction {
                    Products.selectAll().map{Products.toProduct(it)}
                }
            }
        }
        query("product"){
            resolver{id:Int->
                transaction {
                    Products.select{Products.id eq id}.map{Products.toProduct(it)}
                }
            }
        }
        mutation("updateProduct"){
            resolver{id:Int,cost:Int->
                transaction {
                    Products.update({ Products.id eq id}){
                        it[Products.cost] = cost
                    }
                }

            }
        }
    }

    install(Routing){
        route("/graphql"){
            get("/"){
                val graphRequest = call.receive<GraphQLRequest>()
                call.respond(schema.execute(graphRequest.query))
            }
        }
        route("/products"){
            get("/"){
                val users = transaction {
                    Products.selectAll().map{Products.toProduct(it)}
                }
                call.respond(users)
            }
            get("/{id}"){
                val id = call.parameters["id"]!!.toInt()
                val users = transaction {
                    Products.select{Products.id eq id}.map{Products.toProduct(it)}
                }
                call.respond(users)
            }
            post("/"){
                val product = call.receive<Product>()
                transaction{
                    Products.insert {
                        it[Products.name] = product.name
                        it[Products.description] = product.description
                        it[Products.cost] = product.cost
                    }
                }
                call.respond(product)
            }
        }
    }
}

fun main() {
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::myapp).start(wait = true)
}
//configureSerialization()
//configureRouting()