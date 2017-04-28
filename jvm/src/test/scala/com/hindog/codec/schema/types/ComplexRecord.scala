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
	opt: Option[Boolean] = Some(true),
	parent: Option[ComplexRecord] = Some(ComplexRecord(i = 4, opt = Some(true), parent = None))) {
}

case class AnnotatedRecord(
	field1: String,
	field2: Int
)

object AnnotatedRecord {
	import shapeless._
	import syntax.singleton._
	implicit val settings =
		("field1" ->> Doc("asdf")) ::
		("field2" ->> Doc("field2 doc")) :: HNil
			
//	implicit val field1Settings = Settings[AvroSchema[AnnotatedRecord] :\ Field.`'field1`.T].set(FieldName("field1Override") :: Doc("field1 doc") :: HNil)
//	implicit val field2Settings = Settings[AvroSchema[AnnotatedRecord] :\ Field.`'field2`.T].set(Doc("field2 doc") :: HNil)

}