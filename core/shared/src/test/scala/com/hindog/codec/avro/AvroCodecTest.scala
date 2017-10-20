package com.hindog.codec.avro

import org.scalatest.FunSuite

import scala.collection._

/*
 *    __   _         __         
 *   / /  (_)__  ___/ /__  ____
 *  / _ \/ / _ \/ _  / _ \/ _  /
 * /_//_/_/_//_/\_,_/\___/\_, / 
 *                       /___/
 */
class AvroCodecTest extends FunSuite {

	import TestTypes._
	
	def assertCodec[T](value: T)(implicit c: AvroCodec[T]): Unit = {
		val conv = c.apply(value)
		val back = c.inverse(conv)

		assertResult(back)(value)
	}

	test("tuples") {
		assertCodec[(Int, Int)](1 -> 2)
	}

	test("primitives") {
		assertCodec[Int](10)
		assertCodec[Long](10L)
		assertCodec[Float](3.2f)
		assertCodec[Double](3.2)
		assertCodec[Boolean](true)
		assertCodec[Boolean](false)
		//assertCodec[Array[Byte]](Array[Byte](1, 2, 3))
		assertCodec[String]("test string")
	}

	test("map") {
		assertCodec(Map("key1" -> "value1", "key2" -> "value2"))
		assertCodec(Map("key1" -> 1, "key2" -> 2))
		assertCodec(Map(1 -> 1, 2 -> 2))
		assertCodec(mutable.HashMap(1 -> 1, 2 -> 2))
	}

	test("array") {
		assertCodec(Array("value1", "value2"))
		assertCodec(Array(1, 2, 3))
		assertCodec(Seq(1, 2, 3))
		assertCodec(List(1, 2, 3))
		assertCodec(Vector(1, 2, 3))
		assertCodec(mutable.ListBuffer(1, 2, 3))
		assertCodec[Array[Option[Int]]](Array(Option(1), None, Option(3)))
	}

	test("enum (scala)") {
		assertCodec(ScalaEnum.value1)
		assertCodec(ScalaEnum.value2)
		assertCodec(ScalaEnum.value3)
	}

	test("enum (sealed trait)") {
//		assertCodec[Status](Ok(200))
//		assertCodec[Status](Failed(500, "Server Error"))
//		assertCodec[Status](NotFound)
//		val ct = implicitly[ClassTag[NotFound.type]]
//		println(ct.runtimeClass)
	}

	test("complex record") {
		assertCodec(ComplexRecord())
	}

}
