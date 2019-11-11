package net.liftweb.http

import net.liftweb.common.Logger
import java.util.concurrent.atomic.AtomicInteger

object CcapTrace {
  def logger: Logger = Logger("CcapTrace")

  val requestCounter = new AtomicInteger(0)
}
