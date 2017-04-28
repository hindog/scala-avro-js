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

	protected def enumGeneratorSealedTrait(tpe: Type): EnumGenerator = {
		val subclasses = collectKnownSubTypes(tpe)

		val symbols = q"..${subclasses.map(s => q"${s.typeSymbol.asClass.thisPrefix.typeSymbol.asClass.module}")}"
		val toSymbol = q"{ case ..${subclasses.map(s => cq"_: ${s.typeSymbol.asClass.thisPrefix.typeSymbol.asClass.module} => ${s.typeSymbol.asType.name.toString}")} }"
		val fromSymbol = q"{ case ..${subclasses.map(s => cq"${s.typeSymbol.asType.name.toString} => ${s.typeSymbol.asType.asClass.module}")} }"

		new EnumGenerator(tpe, symbols, toSymbol, fromSymbol)
	}

	protected def enumGenerator(tpe: Type): EnumGenerator = {
		val TypeRef(enclosing, _, _) = tpe
		val companion = enclosing.typeSymbol.asClass.module
		new EnumGenerator(tpe, q"$companion.values", q"(v: $tpe) => v.toString", q"(s: String) => $companion.withName(s)")
	}


	class EnumGenerator(tpe: Type, symbols: Tree, toSymbol: Tree, fromSymbol: Tree) extends Generator {
		def schema: Tree = q"""new _root_.com.hindog.codec.schema.EnumSchema with _root_.com.hindog.codec.avro.AvroSchema[$tpe] {
			def symbols = $symbols
		}"""

		def codec: Tree = q"_root_.com.hindog.codec.avro.AvroCodec.createEnumCodec[$tpe](${typeName(tpe)}, $symbols, $toSymbol, $fromSymbol)"
	}
}
