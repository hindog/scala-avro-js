package com.hindog.codec.avro.macros

import scala.collection._
import scala.reflect.macros._

/*
 *    __   _         __         
 *   / /  (_)__  ___/ /__  ____
 *  / _ \/ / _ \/ _  / _ \/ _  /
 * /_//_/_/_//_/\_,_/\___/\_, / 
 *                       /___/
 */
trait CaseClassMacroSupport {

	val c: blackbox.Context

	import c.universe._

	case class CaseParam(symbol: MethodSymbol, enclosingType: Type, index: Int, defaultValue: Option[Tree], annotations: List[Annotation]) {

		val tpe = symbol.returnType
		// Applied Type (ie: with type parameters substituted)
		val appliedTpe: Type = symbol.returnType.substituteTypes(enclosingType.etaExpand.typeParams, enclosingType.typeArgs)
		val name = symbol.name.toString
		val termName: TermName = TermName(name)
		val freshName: TermName = TermName(c.freshName(name))
	}

	def caseParams(tpe: Type): Iterable[CaseParam] = {

		def params: Iterable[MethodSymbol] = tpe.decls.collect {
			case m: MethodSymbol if m.isCaseAccessor => m
		}

		// collect default values
		val defaults = {
			(for {
				applyMethod <- tpe.companion.decls.collectFirst{ case m: MethodSymbol if m.name == TermName("apply") && m.isSynthetic => m }
				paramList <- applyMethod.typeSignature.paramLists.headOption
			} yield {
				paramList.zipWithIndex.collect { case (param: TermSymbol, index) if param.isParamWithDefault => {
					val getter = TermName("apply$default$" + (index + 1))
					param.name -> q"${tpe.typeSymbol.companion}.$getter"
				}}.toMap
			}).getOrElse(Map.empty)
		}

		// extract annotated symbols from primary constructor arg list
		val annotations = {
			val params = tpe.decls.collectFirst{ case c: MethodSymbol if c.isPrimaryConstructor => c }.toList.flatMap(_.paramLists).headOption
			params.map(p => p.map(p => p.name.toTermName -> p.annotations).toMap).getOrElse(Map.empty)
		}

		// map to CaseParam
		params.zipWithIndex.map{ case (p, i) => CaseParam(p, tpe, i, defaults.get(p.name), annotations.getOrElse(p.name, List.empty))}
	}

}

