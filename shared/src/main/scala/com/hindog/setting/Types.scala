package com.hindog.setting

import shapeless.{HList, HNil}
import shapeless.ops.hlist.IsHCons

import scala.collection._
import scala.reflect.runtime.universe._

/*
 *    __   _         __         
 *   / /  (_)__  ___/ /__  ____
 *  / _ \/ / _ \/ _  / _ \/ _  /
 * /_//_/_/_//_/\_,_/\___/\_, / 
 *                       /___/
 */
trait Types[Types <: HList] {
	def types: Seq[Type]
}

object Types {
	def apply[T <: HList](implicit t: Types[T]): Types[T] = t

	implicit def hlistTypes[L <: HList, HD, TL <: HList](implicit s: IsHCons.Aux[L, HD, TL], htt: TypeTag[HD], tail: Types[TL]): Types[L] = new Types[L] {
		override def types: Seq[Type] = Seq(htt.tpe) ++ Option(tail).toSeq.flatMap(_.types)
	}

	implicit def hlistSingle[L <: HList, HD](implicit s: IsHCons.Aux[L, HD, HNil], htt: TypeTag[HD], tail: Types[HNil]): Types[L] = new Types[L] {
		override def types: Seq[Type] = Seq(htt.tpe) ++ Option(tail).toSeq.flatMap(_.types)
	}

//	implicit def define[T, Out <: HList](implicit df: Settings.Define.Aux[T, Out], types: Types[Out]): Types[Settings.Define.Aux[T, Out]] = types

	implicit val emptyTypes: Types[HNil] = new Types[HNil] {
		override def types: Seq[Type] = Seq.empty
	}
}
