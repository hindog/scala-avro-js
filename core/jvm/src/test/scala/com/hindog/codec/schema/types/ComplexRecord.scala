package com.hindog.codec.schema.types

import com.hindog.codec.schema.Doc

import scala.collection._

/*
 *    __   _         __         
 *   / /  (_)__  ___/ /__  ____
 *  / _ \/ / _ \/ _  / _ \/ _  /
 * /_//_/_/_//_/\_,_/\___/\_, / 
 *                       /___/
 */
case class ComplexRecord(
	i: Int = Int.MaxValue,
	l: Long = Long.MaxValue,
	f: Float = Float.MaxValue,
	d: Double = Double.MaxValue,
	s: String = "test string",
	b: Boolean = true,
	mapString: Map[String, Int] = Map("asdf" -> 4),
	mapInt: Map[Int, Int] = Map(4 -> 4),
	opt1: Option[Boolean] = Some(true),
	opt2: Option[Boolean] = None,
	parent: Option[ComplexRecord] = Some(ComplexRecord(i = 4, opt1 = Some(false), opt2 = Some(true), parent = None))) {
}

case class AnnotatedRecord(
	field1: String,
	field2: Int
)

object AnnotatedRecord {

}

sealed trait Status
case class Ok(code: Int) extends Status
case class Failed(code: Int, message: String) extends Status
case object NotFound extends Status

object ScalaEnum extends Enumeration {
	val value1 = Value(1, "Value 1")
	val value2 = Value(2, "Value 2")
	val value3 = Value(3, "Value 3")
}