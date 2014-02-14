package common

import akka.agent.Agent
import play.api.libs.concurrent.{Akka => PlayAkka}
import scala.concurrent.duration._
import play.api.Play
import scala.concurrent.ExecutionContext
import scala.util.{Success, Try}

trait ExecutionContexts {
  private lazy val currentThreadContext = new ExecutionContext {
    def execute(runnable: Runnable) {
      runnable.run()
    }
    def reportFailure(t: Throwable) {
      ExecutionContext.defaultReporter(t)
    }
  }

  implicit def executionContext = currentThreadContext
}

object AkkaAgent {
  def apply[T](value: T) = Agent(value)(PlayAkka.system(Play.current))
}

object AkkaAsync extends ExecutionContexts {

  def apply(body: => Unit): Unit = after(1.second){ body }

  def after(delay: FiniteDuration)(body: => Unit): Unit = {
    PlayAkka.system(Play.current).scheduler.scheduleOnce(delay) { body }
  }
}