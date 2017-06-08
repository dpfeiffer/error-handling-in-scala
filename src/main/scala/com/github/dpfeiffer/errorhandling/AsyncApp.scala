package com.github.dpfeiffer.errorhandling

import java.util.concurrent.Executors

import scala.concurrent.{ExecutionContext, Future}

trait AsyncApp {

  protected def run(implicit ec: ExecutionContext): Future[_]

  final def main(args: Array[String]): Unit = {
    val executor    = Executors.newFixedThreadPool(10)
    implicit val ec = ExecutionContext.fromExecutor(executor)

    run.onComplete(_ => executor.shutdown())
  }

}
