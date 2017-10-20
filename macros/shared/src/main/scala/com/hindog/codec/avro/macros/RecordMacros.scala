package com.hindog.codec.avro.macros

import shapeless.SingletonTypeUtils

import scala.reflect.macros._

/*
 *    __   _         __         
 *   / /  (_)__  ___/ /__  ____
 *  / _ \/ / _ \/ _  / _ \/ _  /
 * /_//_/_/_//_/\_,_/\___/\_, / 
 *                       /___/
 */
trait RecordMacros extends SingletonTypeUtils { this: AvroMacros =>

	val c: blackbox.Context
	import c.universe._
	
	case class RecordGenerator private (tpe: Type, fields: Iterable[CaseParam]) extends Generator {

		override def schema: c.universe.Tree = {
			val fields = caseParams(tpe)
			val freshName = TermName(c.freshName("recordCodec"))

			q"""
				implicit object $freshName extends _root_.com.hindog.codec.schema.RecordSchema with _root_.com.hindog.codec.avro.AvroSchema[$tpe] {
					override val fields = Seq(..${fields.map(createField)})
		      override val name = ${typeName(tpe)}
				}
		    $freshName
			"""
		}

		override def codec: c.universe.Tree = {
			val fields = caseParams(tpe)
			val freshName = TermName(c.freshName("recordCodec"))

			q"""
			 implicit object $freshName extends com.hindog.codec.avro.AvroCodec[$tpe] {
				  ..${fields.map(f => q"lazy val ${f.freshName} = ${codecFor(f.appliedTpe)}")}

		      val schema = ${schemaFor(tpe)}.value
			    def apply(value: $tpe): Any = ${toRecord(tpe, schema, fields)}
			    def inverse(value: Any): $tpe = ${fromRecord(tpe, fields)}
		   }
			 ($freshName: com.hindog.codec.avro.AvroCodec[$tpe])
			"""
		}

		protected def toRecord(tpe: Type, schema: Tree, fields: Iterable[CaseParam]): Tree = {
			q"""value match {
			case value: $tpe => {
	  	  val builder = _root_.com.hindog.codec.avro.platform.recordBuilder(schema)
				..${fields.zipWithIndex.map{ case (f, idx) => q"builder.set($idx, ${f.name}, ${f.freshName}.value.apply(value.${f.termName}))" }}
				builder.build().asInstanceOf[Any]
			}
	    case other => throw _root_.com.hindog.codec.CodecException("Unrecognized record value: " + other)
		}"""
		}

		protected def fromRecord(tpe: Type, fields: Iterable[CaseParam]): Tree = {
			q"""value match {
			case record: _root_.com.hindog.codec.avro.platform.NativeRecord @unchecked => ${tpe.typeSymbol.companion}.apply(..${fields.map(f => q"${f.freshName}.value.inverse(_root_.com.hindog.codec.avro.platform.recordExtractor(record, ${f.name}))")})
			case other => throw _root_.com.hindog.codec.CodecException("Unrecognized record value: " + other)
		}"""
		}

		protected def createField(f: CaseParam): Tree = {

			val sym = TermName(f.name)
			//val witness = c.typecheck(q"_root_.shapeless.Witness($sym).T").tpe
			
			def defaultValue: Option[Tree] = if (true)
				f.defaultValue.map(d => q"_root_.com.hindog.codec.schema.RecordSchema.DefaultValue[${f.appliedTpe}]($d)")
			else
				None

			q"""_root_.com.hindog.codec.schema.RecordSchema.Field(
   					${f.name},
   					${schemaFor(f.appliedTpe)},
   					None,
   					Seq.empty,
   					None,
   					_root_.com.hindog.codec.schema.Properties.empty,
   					$defaultValue
   		)"""
		}
	}

	def recordForCaseClass(tpe: Type) = RecordGenerator(tpe, caseParams(tpe))
	def recordForSingleton(tpe: Type) = RecordGenerator(tpe, Iterable.empty)
}
