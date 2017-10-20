package com.hindog.codec.avro

import scala.collection._
import scala.scalajs.js
import scala.scalajs.js.annotation.{JSExport, JSGlobal}

/**
  * Created by Aaron Hiniker (ahiniker@atomtickets.com) 
  * 8/31/17
  * Copyright (c) Atom Tickets, LLC
  */
@js.native
@JSGlobal
class RecordTypeBuilder(name: String) extends AvroType {
  def recordConstructor: js.Function = js.native
  var fields: Seq[js.Object] = js.native
}
