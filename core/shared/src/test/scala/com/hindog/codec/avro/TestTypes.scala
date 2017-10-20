package com.hindog.codec.avro

import com.hindog.codec.Options
import com.hindog.codec.schema.Doc
import com.hindog.codec.schema.RecordSchema.Field

import scala.collection._

/**
  * Created by Aaron Hiniker (ahiniker@atomtickets.com) 
  * 9/5/17
  * Copyright (c) Atom Tickets, LLC
  */
object TestTypes {

  case class ComplexRecord(
    i: Int = Int.MaxValue,
    l: Long = 1L,
    f: Float = Float.MaxValue,
    d: Double = Double.MaxValue,
    s: String = "test string",
    b: Boolean = true,
    mapString: Map[String, Int] = Map("asdf" -> 4),
    mapInt: Map[Int, Int] = Map(4 -> 4),
    opt1: Option[Boolean] = Some(true),
    opt2: Option[Boolean] = None,
    foo: Option[Foo] = Some(Foo("asdf", Some(Foo("asdf2", None)))),
//    status: Status = Ok("success"),
    parent: Option[ComplexRecord] = Some(ComplexRecord(i = 4, opt1 = Some(false), opt2 = Some(true), parent = None))) {
  }

  case class Foo(name: String, parent: Option[Foo] = None)

  object ScalaEnum extends Enumeration {
    val value1 = Value(1, "value1")
    val value2 = Value(2, "value2")
    val value3 = Value(3, "value3")
  }

  case class RecordWithOptions(name: String, age: Int)

  implicit val nameDoc = Options.forField[RecordWithOptions, Field.`'name`.T] | Doc("name doc")
}

sealed trait Status {
  def message: String
}

case class Ok(message: String) extends Status
case class Failed(message: String, code: Int) extends Status