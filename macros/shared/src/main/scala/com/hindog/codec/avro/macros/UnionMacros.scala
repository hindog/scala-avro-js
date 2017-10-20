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
	
	case class UnionGenerator private (tpe: Type, types: Seq[Type]) extends Generator {
		override def schema: c.universe.Tree = {
			val schemas = types.map(t => q"implicitly[_root_.shapeless.Lazy[_root_.com.hindog.codec.avro.AvroSchema[$t]]]")
			q"""new _root_.com.hindog.codec.schema.UnionSchema with _root_.com.hindog.codec.avro.AvroSchema[$tpe] {
      	def types = Seq(..${schemas.map(s => q"$s.value")})
   		}"""
		}

		override def codec: c.universe.Tree = {
			val union = types.map(t => t.substituteTypes(t.etaExpand.typeParams, tpe.typeArgs)).foldLeft(
				q"new _root_.com.hindog.codec.avro.UnionCodecBuilder[$tpe]")((acc, cur) => q"$acc.addType[$cur]")
			q"$union.build"
		}
	}

	def unionForSealedTrait(tpe: Type): UnionGenerator = UnionGenerator(tpe, collectKnownSubTypes(tpe))

	
}
