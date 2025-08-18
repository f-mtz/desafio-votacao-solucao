package sicredi.votacao.api.perf

import io.gatling.core.Predef._
import io.gatling.http.Predef._
import scala.concurrent.duration._

class VotoSimulation extends Simulation {

  val baseUrl = System.getProperty("baseUrl", "http://localhost:8080")

  val httpProtocol = http
    .baseUrl(baseUrl)
    .contentTypeHeader("application/json")

  // Helpers
  def cpf(i: Int): String = {
    val s = f"${i}%011d"
    if (s.matches("(\\d)\\1{10}")) "12345678901" else s
  }

  // Setup: cria pauta e abre sessão
  val createAgenda = exec(
    http("criar_pauta")
      .post("/api/agenda")
      .body(StringBody("""{ "title":"Perf Test", "description":"Carga" }"""))
      .check(status.is(201))
      .check(jsonPath("$.id").saveAs("agendaId"))
  )

  val openSession = exec(
    http("abrir_sessao")
      .post("/api/agenda/abrir-sessao")
      .body(StringBody(session => s"""{ "agendaId": ${session("agendaId").as[String]}, "durationSeconds": 600 }"""))
      .check(status.is(200))
      .check(jsonPath("$.sessionId").saveAs("sessionId"))
  )

  val votar = exec(session => session.set("cpf", cpf(session.userId.toInt + 100000)))
    .exec(
      http("votar")
        .post("/api/agenda/votar")
        .body(StringBody(session => s"""{ "sessionId": ${session("sessionId").as[String]}, "associateId": "${session("cpf").as[String]}", "vote": "SIM" }"""))
        .check(status.in(200, 404)) // devido ao client aleatório
    )

  val scn = scenario("Votos")
    .exec(createAgenda)
    .exec(openSession)
    .pause(1)
    .repeat(1) { exec(votar) }

  setUp(
    scn.inject(
      rampUsersPerSec(10).to(200).during(60.seconds) // rampa até 200 rps em 1 min
    )
  ).protocols(httpProtocol)
}


