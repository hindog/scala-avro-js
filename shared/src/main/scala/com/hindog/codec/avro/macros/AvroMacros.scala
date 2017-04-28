package com.hindog.codec.avro.macros

import com.hindog.macrosupport.CaseClassMacroSupport

import scala.reflect.macros._

/*
 *    __   _         __         
 *   / /  (_)__  ___/ /__  ____
 *  / _ \/ / _ \/ _  / _ \/ _  /
 * /_//_/_/_//_/\_,_/\___/\_, / 
 *                       /___/
 */
class AvroMacros(val c: blackbox.Context)
extends CaseClassMacroSupport
with RecordMacros
with UnionMacros
with EnumMacros
{

	import c.universe._

	def codec[T : WeakTypeTag]: Tree = generator(c.weakTypeOf[T]).codec

	def schema[T : WeakTypeTag]: Tree = generator(c.weakTypeOf[T]).schema

	def generator(tpe: Type): Generator = tpe.typeSymbol match {
		case t: ClassSymbol if t.isCaseClass => recordGenerator(tpe)
		case t: ClassSymbol if t.isSealed => enumGeneratorSealedTrait(tpe)
		case t if tpe <:< weakTypeOf[scala.Enumeration#Value] => enumGenerator(tpe)
		case _ => c.abort(c.enclosingPosition, s"implicit value not found for AvroCodec[$tpe]")
	}

	protected def typeName(tpe: Type): Tree = {
		q"_root_.com.hindog.codec.schema.Name(${tpe.typeSymbol.name.toString})"
	}

	protected def collectKnownSubTypes(tpe: Type): Seq[Type] = {
		tpe.typeSymbol.asClass.knownDirectSubclasses.map(_.asType.toType).toSeq match {
			case Nil => c.abort(c.enclosingPosition, s"AvroCodec[$tpe] could not be generated because sub-types could not be determined")
			case other => other
		}
	}

	protected def codecFor(tpe: Type): Tree = {
		q"implicitly[_root_.shapeless.Lazy[_root_.com.hindog.codec.avro.AvroCodec[$tpe]]]"
	}

	protected def schemaFor(tpe: Type): Tree = {
		q"implicitly[_root_.shapeless.Lazy[_root_.com.hindog.codec.avro.AvroSchema[$tpe]]]"
	}

	trait Generator {
		def schema: Tree
		def codec: Tree
	}
}
