package com.hindog.codec.avro

import scala.collection._
import scala.scalajs.js
import scala.scalajs.js.|
import scala.scalajs.js.annotation.JSGlobal

/**
  * Created by Aaron Hiniker (ahiniker@atomtickets.com) 
  * 8/28/17
  * Copyright (c) Atom Tickets, LLC
  */
@js.native
@JSGlobal("avro")
object Avro extends js.Any {

  @js.native
  object Type extends js.Any {
    def forSchema(schema: String, opts: Options): AvroType = js.native
    def forSchema(schema: js.Object, opts: Options = new Options()): AvroType = js.native
    def forTypes(types: js.Array[AvroType]): AvroType = js.native
  }

  @js.native
  class Options(
    val assertLogicalTypes: Boolean = false,
    val wrapUnions: Boolean = true
  ) extends js.Any
}
