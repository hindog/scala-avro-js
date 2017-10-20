package com.hindog.codec.avro.macros

import scala.reflect.macros.blackbox

/*
 *    __   _         __         
 *   / /  (_)__  ___/ /__  ____
 *  / _ \/ / _ \/ _  / _ \/ _  /
 * /_//_/_/_//_/\_,_/\___/\_, / 
 *                       /___/
 */
trait EnumMacros { this: AvroMacros =>

	val c: blackbox.Context

	import c.universe._

	class EnumGenerator(tpe: Type, symbols: Tree, toSymbol: Tree, fromSymbol: Tree) extends Generator {
		def schema: Tree = q"""new _root_.com.hindog.codec.schema.EnumSchema with _root_.com.hindog.codec.avro.AvroSchema[$tpe] {
			def symbols = $symbols
		}"""

		def codec: Tree = q"_root_.com.hindog.codec.avro.AvroCodec.enumCodec[$tpe](${typeName(tpe)}, $symbols, $toSymbol, $fromSymbol)"
	}

	def enumForSealedTrait(tpe: Type): EnumGenerator = {
		val subclasses = collectKnownSubTypes(tpe)

		val symbols = q"..${subclasses.map(s => q"${s.typeSymbol.asClass.thisPrefix.typeSymbol.asClass.module}")}"
		val toSymbol = q"{ case ..${subclasses.map(s => cq"_: ${s.typeSymbol.asClass.thisPrefix.typeSymbol.asClass.module} => ${s.typeSymbol.asType.name.toString}")} }"
		val fromSymbol = q"{ case ..${subclasses.map(s => cq"${s.typeSymbol.asType.name.toString} => ${s.typeSymbol.asType.asClass.module}")} }"

		new EnumGenerator(tpe, symbols, toSymbol, fromSymbol)
	}

	def enumForScalaEnum(tpe: Type): EnumGenerator = {
		val TypeRef(enclosing, _, _) = tpe
		val companion = enclosing.typeSymbol.asClass.module
		new EnumGenerator(tpe, q"$companion.values.map(_.toString).toSeq", q"(s: String) => $companion.withName(s)", q"(v: $tpe) => v.toString")
	}

}
