package com.hindog.codec.avro.macros

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

	def codec[T : WeakTypeTag]: Tree = generator(c.weakTypeOf[T], "AvroCodec").codec
	def schema[T : WeakTypeTag]: Tree = generator(c.weakTypeOf[T], "AvroSchema").schema

	def generator(tpe: Type, typeName: String): Generator = {
		lazy val knownSubTypes = collectKnownSubTypes(tpe)

		tpe.typeSymbol match {
			case t: ModuleSymbol =>
				recordForSingleton(tpe)

			case t: ClassSymbol if t.isCaseClass =>
				recordForCaseClass(tpe)

			case t: ClassSymbol if t.isSealed && knownSubTypes.forall(_.typeSymbol.isModule) =>
				enumForSealedTrait(tpe)

			case t: ClassSymbol if t.isSealed && knownSubTypes.forall(_.typeSymbol.asClass.isCaseClass) =>
				unionForSealedTrait(tpe)

			case t if tpe <:< weakTypeOf[scala.Enumeration#Value] =>
				enumForScalaEnum(tpe)
				
			case _ => c.abort(c.enclosingPosition, s"Unable to materialize $typeName[$tpe]! This means that there is \n1) no implicit $typeName[$tpe] in scope and \n2) '$tpe' is not a valid type candidate for an auto-generated codec")
		}
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
