package com.hindog.codec.avro.macros

import com.hindog.codec.schema.{Doc, Properties}
import com.hindog.codec.schema.RecordSchema.Field
import com.hindog.codec.schema.RecordSchema.Field.FieldName
import com.hindog.macrosupport.CaseClassMacroSupport
import com.sun.org.apache.xalan.internal.xsltc.compiler.sym

import scala.reflect.macros._

/*
 *    __   _         __         
 *   / /  (_)__  ___/ /__  ____
 *  / _ \/ / _ \/ _  / _ \/ _  /
 * /_//_/_/_//_/\_,_/\___/\_, / 
 *                       /___/
 */
trait RecordMacros { this: AvroMacros =>

	val c: blackbox.Context
	import c.universe._

	def recordGenerator(tpe: Type) = new RecordGenerator(tpe, caseParams(tpe))

	class RecordGenerator(tpe: Type, fields: Iterable[CaseParam]) extends Generator {

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
	  	  val builder = _root_.com.hindog.codec.avro.platform.recordBuilder[$tpe](schema)
				..${fields.map(f => q"builder.set(${f.name}, ${f.freshName}.value.apply(value.${f.termName}))")}
				builder.build().asInstanceOf[Any]
			}
	    case other => throw _root_.com.hindog.codec.CodecException("Unrecognized record value: " + other)
		}"""
		}

		protected def fromRecord(tpe: Type, fields: Iterable[CaseParam]): Tree = {
			q"""value match {
			case record: _root_.com.hindog.codec.avro.platform.RecordType => ${tpe.typeSymbol.companion}.apply(..${fields.map(f => q"${f.freshName}.value.inverse(_root_.com.hindog.codec.avro.platform.recordExtractor(record, ${f.name}))")})
			case other => throw _root_.com.hindog.codec.CodecException("Unrecognized record value: " + other)
		}"""
		}

		protected def createField(f: CaseParam): Tree = {
			def defaultValue: Option[Tree] = if (true)
				f.defaultValue.map(d => q"_root_.com.hindog.codec.schema.RecordSchema.DefaultValue[${f.appliedTpe}]($d)")
			else
				None

			q"_root_.com.hindog.codec.schema.RecordSchema.Field(${fieldSetting[FieldName](f)}.map(_.name).getOrElse(${f.name}), ${schemaFor(f.appliedTpe)}, ${fieldSetting[Doc](f)}.map(_.doc), ${fieldSetting[com.hindog.codec.schema.RecordSchema.Field.Aliases](f)}.map(_.aliases.toSeq).getOrElse(Seq.empty), ${fieldSetting[Field.Ordered](f)}.map(_.order), ${fieldSetting[Properties](f)}.getOrElse(_root_.com.hindog.codec.schema.Properties.empty), $defaultValue)"
		}

		protected def fieldSetting[T : TypeTag](f: CaseParam): Tree = {
			val settingT = c.typeOf[T]
			val sym = Symbol(f.name)
		  q"""
				val ${TermName(f.name + "_wt")} = _root_.shapeless.Witness($sym)
		    _root_.com.hindog.setting.Settings.option[_root_.com.hindog.codec.schema.RecordSchema.Field.:\[_root_.com.hindog.codec.avro.AvroSchema[$tpe], ${TermName(f.name + "_wt")}.T], $settingT]
		  """
		}
	}
}
