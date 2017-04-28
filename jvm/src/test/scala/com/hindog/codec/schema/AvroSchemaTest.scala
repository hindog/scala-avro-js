package com.hindog.codec.schema

import com.hindog.codec.avro._
import com.hindog.codec.schema.RecordSchema.Field.{:\, FieldName}
import com.sun.scenario.Settings
import org.scalatest.FunSuite
import shapeless._

import scala.language.higherKinds
import scala.reflect.runtime.universe._

/*
 *    __   _         __         
 *   / /  (_)__  ___/ /__  ____
 *  / _ \/ / _ \/ _  / _ \/ _  /
 * /_//_/_/_//_/\_,_/\___/\_, / 
 *                       /___/
 */


trait FieldT[T, W] {
	type FT
	val tpe: Type
}
object FieldT {
	type Aux[T, W, FT0] = FieldT[T, W] { type FT = FT0 }
}

class AvroSchemaTest extends FunSuite with LowPriorityImplicits {

//
//	test("foo record") {
//		val schema = implicitly[AvroSchema[ComplexRecord]].asInstanceOf[RecordSchema]
//		println(schema.fields)
//		schema.fields.foreach(f => {
//			println(f.name + " [" + f.schema.value.name + s"], default: ${f.defaultValue}")
//		})
//		val native = platform.nativeSchema(schema)
//		println(native)
//	}
//
//	test("map") {
//		val schema = implicitly[AvroSchema[Map[String, Int]]]
//		println(platform.nativeSchema(schema).toString)
//	}
//
//	test("union default order") {
//
//		val schema = implicitly[AvroSchema[Foo]]
//		println(platform.nativeSchema(schema).toString)
//	}
//
//	test("option") {
//		val schema = implicitly[AvroSchema[String]]
//		println(schema)
//	}
//
//	test("configuration") {
//		import AvroSchema._
//
//		implicit val settingsT = Settings[AvroSchema[Foo]].set(Doc("Some doc"))
//
//		println(Settings.get[AvroSchema[Foo], Doc])
//	}
//
//	test("settings override") {
//
//		implicit val schema = implicitly[AvroSchema[AnnotatedRecord]].asInstanceOf[RecordSchema]
//
//		println(schema.fields.map(f => f.name + ", doc: " + f.doc).mkString(", "))
//	}

	test("field type") {
		import AvroSchemaTest._
		import com.hindog.codec.schema._

		implicit val fieldOverride = RecordSchema.Field()
//		println(implicitly[Define[Double]])
//
//		Settings[Double].set(Precision(2))
//		Settings[AvroSchema[Foo]].set(Name("foo"))
//		Settings[AvroSchema[Foo] :\ Witness.`'field2`.T].set(FieldName("adsf"))
//		printSettings[AvroSchema[Foo] :\ Witness.`'field2`.T]

	}
}

object AvroSchemaTest {



}
