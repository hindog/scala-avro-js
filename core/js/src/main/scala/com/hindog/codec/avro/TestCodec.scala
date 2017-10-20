package com.hindog.codec.avro

import org.scalajs.dom._

import scala.collection._
import scala.reflect.ClassTag
import scala.scalajs.js
import scala.scalajs.js.JSON

import java.sql.Timestamp
import java.util.Date

/**
  * Created by Aaron Hiniker (ahiniker@atomtickets.com) 
  * 8/28/17
  * Copyright (c) Atom Tickets, LLC
  */
object TestCodec extends js.JSApp {

  def testCodec[T](v: T, eq: (T, T) => Boolean = (l: T, r: T) => l == r)(implicit codec: AvroCodec[T], ct: ClassTag[T]): Unit =  {
      console.log(s"AvroCodec[${ct.runtimeClass.getSimpleName}]")
      val jsonCodec = codec.jsonCodec
      val json = jsonCodec(v)
      console.log("json: " + JSON.stringify(json))
      val orig = jsonCodec.inverse(json)
      console.log("orig: " + orig.toString)
      assert(eq(v, orig))
  }
  override def main(): Unit = {
    import TestTypes._

    testCodec("1" -> 1)
    testCodec(10)
    testCodec(1.0)
    testCodec("some string")
    testCodec("1" -> 1)
    testCodec(new Date())
    //testCodec(new Timestamp(System.currentTimeMillis()))
    //testCodec(UUID.randomUUID())
    testCodec(Map("1" -> 1, "2" -> 2))
    testCodec(Map(1 -> 1, 2 -> 2))
    testCodec(Array(1, 2, 3), (l: Array[Int], r: Array[Int]) => l.toSeq == r.toSeq)
    testCodec(Array("a", "b", "c"), (l: Array[String], r: Array[String]) => l.toSeq == r.toSeq)
    testCodec(Seq("a", "b", "c"))
    //testCodec[Status](Ok("some message"))
    testCodec(ComplexRecord())
  }
}

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
    parent: Option[ComplexRecord] = Some(ComplexRecord(i = 4, opt1 = Some(false), opt2 = Some(true), parent = None))) {
  }

  case class Foo(name: String, parent: Option[Foo] = None)

}

//
//sealed trait Status {
//  def message: String
//}
//
//case class Ok(message: String) extends Status
//case class Failed(message: String, code: Int) extends Status