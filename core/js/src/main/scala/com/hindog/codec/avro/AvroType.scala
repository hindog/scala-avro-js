package com.hindog.codec.avro

import scala.collection._
import scala.scalajs.js
import scala.scalajs.js.annotation.{JSExport, JSGlobal}

/**
  * Created by Aaron Hiniker (ahiniker@atomtickets.com) 
  * 8/28/17
  * Copyright (c) Atom Tickets, LLC
  */
@js.native
@JSGlobal("avro.Type")
class AvroType extends js.Object {
  def schema: js.Object = js.native
  def opts: js.Object = js.native
  def toBuffer(v: js.Any): js.Any = js.native
  def toString(v: js.Any): String = js.native
  def fromString(v: String): js.Any = js.native
}

