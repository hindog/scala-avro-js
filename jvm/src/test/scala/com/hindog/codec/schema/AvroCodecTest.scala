package com.hindog.codec.schema

import com.hindog.codec.avro.{AvroCodec, AvroSchema}
import com.hindog.codec.schema.types.ComplexRecord
import org.scalatest.FunSuite
import shapeless.Lazy

/*
 *    __   _         __         
 *   / /  (_)__  ___/ /__  ____
 *  / _ \/ / _ \/ _  / _ \/ _  /
 * /_//_/_/_//_/\_,_/\___/\_, / 
 *                       /___/
 */
class AvroCodecTest extends FunSuite {
//
//	test("foo record") {
//		val codecL = implicitly[Lazy[AvroCodec[ComplexRecord]]]
//		val codec = codecL.value
//		//println(codec.schema.name)
//		//println(codec.schema.asInstanceOf[RecordSchema].fields)
////		codec.schema.asInstanceOf[RecordSchema].fields.foreach(f => {
////			println(f.name + " [" + f.schema.value.name + s"], default: ${f.defaultValue}")
////		})
//
//		val start = System.currentTimeMillis()
//		for(i <- 0 until 1000000) {
//			val con = codec(ComplexRecord(10, opt = Some(false)))
////			println(con)
////			println(codec.inverse(con))
//		}
//		println((System.currentTimeMillis() - start) / 1000.0)
//	}
}
