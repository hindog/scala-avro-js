package com.hindog.log

import scala.annotation.elidable
import scala.collection._

/**
  * Created by Aaron Hiniker (ahiniker@atomtickets.com) 
  * 9/2/17
  * Copyright (c) Atom Tickets, LLC
  */
protected[hindog] trait Logger {
  @elidable(elidable.SEVERE)  def error(msg: => String): Unit
  @elidable(elidable.WARNING) def warn(msg: => String): Unit
  @elidable(elidable.INFO)    def info(msg: => String): Unit
  @elidable(elidable.FINE)    def debug(msg: => String): Unit
  @elidable(elidable.FINER)   def trace(msg: => String): Unit
}
