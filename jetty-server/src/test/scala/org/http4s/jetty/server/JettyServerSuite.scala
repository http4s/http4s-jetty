/*
 * Copyright 2014 http4s.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.http4s
package jetty
package server

import cats.effect.{IO, Resource, Temporal}
import munit.CatsEffectSuite
import org.eclipse.jetty.client.HttpClient
import org.eclipse.jetty.client.api.Request
import org.eclipse.jetty.client.util.StringContentProvider
import org.http4s.dsl.io._
import org.http4s.server.Server

import scala.concurrent.duration._

class JettyServerSuite extends CatsEffectSuite {

  private def builder = JettyBuilder[IO]

  private val client =
    ResourceSuiteLocalFixture(
      "jetty-client",
      Resource.make(IO(new HttpClient()))(c => IO(c.stop())).evalTap(c => IO(c.start())),
    )

  override def munitFixtures: List[Fixture[HttpClient]] = List(client)

  private val serverR =
    builder
      .bindAny()
      .withAsyncTimeout(3.seconds)
      .mountService(
        HttpRoutes.of {
          case GET -> Root / "thread" / "routing" =>
            val thread = Thread.currentThread.getName
            Ok(thread)

          case GET -> Root / "thread" / "effect" =>
            IO(Thread.currentThread.getName).flatMap(Ok(_))

          case req @ POST -> Root / "echo" =>
            Ok(req.body)

          case GET -> Root / "never" =>
            IO.async(_ => IO.pure(Some(IO.unit)))

          case GET -> Root / "slow" =>
            Temporal[IO].sleep(50.millis) *> Ok("slow")
        },
        "/",
      )
      .resource

  private val jettyServer = ResourceFixture[Server](serverR)

  private def fetchBody(req: Request): IO[String] =
    IO.interruptible(req.send().getContentAsString())

  private def get(server: Server, path: String): IO[String] = {
    val req = client().newRequest(s"http://127.0.0.1:${server.address.getPort}$path")
    fetchBody(req)
  }

  private def post(server: Server, path: String, body: String): IO[String] = {
    val req = client()
      .newRequest(s"http://127.0.0.1:${server.address.getPort}$path")
      .method("POST")
      .content(new StringContentProvider(body))
    fetchBody(req)
  }

  jettyServer.test("ChannelOptions should route requests on the service executor") { server =>
    get(server, "/thread/routing").map(_.startsWith("io-compute-")).assert
  }

  jettyServer.test("ChannelOptions should execute the service task on the service executor") {
    server =>
      get(server, "/thread/effect").map(_.startsWith("io-compute-")).assert
  }

  jettyServer.test("ChannelOptions should be able to echo its input") { server =>
    val input = """{ "Hello": "world" }"""
    post(server, "/echo", input).map(_.startsWith(input)).assert
  }

  jettyServer.test("Timeout should not fire prematurely") { server =>
    get(server, "/slow").assertEquals("slow")
  }

  jettyServer.test("Timeout should fire on timeout") { server =>
    get(server, "/never").map(_.contains("Error 500 AsyncContext timeout"))
  }

  jettyServer.test("Timeout should execute the service task on the service executor") { server =>
    get(server, "/thread/effect").map(_.startsWith("io-compute-")).assert
  }
}
