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

package com.example.http4s
package jetty

import cats.effect._
import org.http4s._
import org.http4s.jetty.server.JettyBuilder
import org.http4s.server.Server
import org.http4s.servlet.DefaultFilter

import javax.servlet.FilterChain
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/** 1. Run as `sbt examples/run`
  * 2. Browse to http://localhost:8080/http4s to see `httpRoutes`
  * 3. Browse to http://localhost:8080/raw to see a raw servlet alongside the http4s routes
  * 4. Browse to http://localhost:8080/raw/black-knight to see a route with a servlet filter
  */
object JettyExample extends IOApp {
  override def run(args: List[String]): IO[ExitCode] =
    new JettyExample[IO].resource.use(_ => IO.never).as(ExitCode.Success)
}

class JettyExample[F[_]](implicit F: Async[F]) {
  def httpRoutes = HttpRoutes.of[F] {
    case req if req.method == Method.GET =>
      Sync[F].pure(Response(Status.Ok).withEntity("/pong"))
  }

  // Also supports raw servlets alongside your http4s routes
  val rawServlet = new HttpServlet {
    override def doGet(
        request: HttpServletRequest,
        response: HttpServletResponse,
    ): Unit =
      response.getWriter.print("Raw servlet")
  }

  // Also supports raw filters alongside your http4s routes
  val filter = new DefaultFilter {
    override def doHttpFilter(
        request: HttpServletRequest,
        response: HttpServletResponse,
        chain: FilterChain,
    ): Unit = {
      response.setStatus(403)
      response.getWriter.print("None shall pass!")
    }
  }

  def resource: Resource[F, Server] =
    JettyBuilder[F]
      .bindHttp(8080)
      .mountService(httpRoutes, "/http4s")
      .mountServlet(rawServlet, "/raw/*")
      .mountFilter(filter, "/raw/black-knight/*")
      .resource
}
