package com.hindog.codec.avro

import com.hindog.codec.avro.TestTypes._
import com.hindog.codec.schema.Schema
import org.scalatest.FunSuite
import shapeless.Lazy

import scala.collection._

/**
  * Created by Aaron Hiniker (ahiniker@atomtickets.com) 
  * 9/5/17
  * Copyright (c) Atom Tickets, LLC
  */
class AvroSchemaTest extends FunSuite {

  def assertSchema[T](name: String, typeOf: Schema.Type)(implicit s: AvroSchema[T]): Unit = {
    assertResult(name)(s.fullName)
    assertResult(typeOf)(s.typeOf)
  }

  test("primitives") {
    assertSchema[Int]("int", Schema.Int)
    assertSchema[Long]("long", Schema.Long)
    assertSchema[Float]("float", Schema.Float)
    assertSchema[Double]("double", Schema.Double)
    assertSchema[String]("string", Schema.String)
    assertSchema[Boolean]("boolean", Schema.Boolean)
    assertSchema[Array[Byte]]("bytes", Schema.Bytes)
  }

  test("map") {
    assertSchema[Map[String, Int]]("map", Schema.Map)
    assertSchema[Map[Int, Int]]("array", Schema.Array)
  }

  test("array") {
    assertSchema[Array[String]]("array", Schema.Array)
    assertSchema[Seq[String]]("array", Schema.Array)
  }

  test("option") {
    assertSchema[Option[Foo]]("[null, Foo]", Schema.Union)
  }

  test("union") {
    assertSchema[Status]("""[Ok, Failed]""", Schema.Union)
  }

  test("complex record") {
    assertSchema[ComplexRecord]("ComplexRecord", Schema.Record)
  }

  test("options") {
    val schema = implicitly[AvroSchema[RecordWithOptions]]
    println(schema)
  }
}
