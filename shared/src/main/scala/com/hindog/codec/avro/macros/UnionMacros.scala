package com.hindog.codec.avro.macros

import scala.reflect.macros.blackbox

/*
 *    __   _         __         
 *   / /  (_)__  ___/ /__  ____
 *  / _ \/ / _ \/ _  / _ \/ _  /
 * /_//_/_/_//_/\_,_/\___/\_, / 
 *                       /___/
 */
trait UnionMacros { this: AvroMacros =>

	val c: blackbox.Context
	import c.universe._

	protected def createUnionSealedTraitCodec(tpe: Type): Tree = {
		unionCodec(tpe, collectKnownSubTypes(tpe))
	}

	protected def unionCodec(tpe: Type, types: Seq[Type]): Tree = {
		val union = types.map(t => t.substituteTypes(t.etaExpand.typeParams, tpe.typeArgs)).foldLeft(
			q"new _root_.com.hindog.codec.avro.UnionCodecBuilder[$tpe]")((acc, cur) => q"$acc.addType[$cur]")
		q"$union.build"
	}

}
