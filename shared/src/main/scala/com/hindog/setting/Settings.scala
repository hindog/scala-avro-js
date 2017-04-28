//package com.hindog.setting
//
//import com.hindog.codec.schema.{Precision, Scale}
//import shapeless._
//import shapeless.ops.hlist.{Prepend, Selector}
//
//import scala.annotation.implicitNotFound
//import scala.reflect.runtime.universe._
//
///*
// *    __   _         __
// *   / /  (_)__  ___/ /__  ____
// *  / _ \/ / _ \/ _  / _ \/ _  /
// * /_//_/_/_//_/\_,_/\___/\_, /
// *                       /___/
// */
//class Settings[T, V <: HList, U <: HList](val settings: V) {
//	def set[A](value: A)(implicit ev1: BasisConstraint[A :: V, U], ev2: NotContainsConstraint[V, A]): Settings[T, A :: V, U] = new Settings[T, A :: V, U](value :: settings)
//	def set[A <: HList](values: A)(implicit ev1: BasisConstraint[A, U], ev2: NotContainsConstraint[V, A], pre: Prepend[A, V]): Settings[T, pre.Out, U] = new Settings[T, pre.Out, U](pre(values, settings))
//}
//
//object Settings {
//	def apply[T](implicit define: Define[T]) = new Settings[T, HNil, define.RequiredTypes :: define.OptionalTypes](HNil)
//
//	def get[T, V](implicit s: Setting[T, V]): V = s.apply()
//	def option[T, V](implicit s: Setting[T, V] = null): Option[V] = Option(s).map(_.apply())
//
//	@implicitNotFound("Setting ${V} not defined for ${T}")
//	trait Setting[T, V] {
//	  def apply(): V
//	}
//
//	object Setting {
//		implicit def setting[T, V, H <: HList, RT <: HList, OT <: HList, CT <: HList](implicit define: Define.Aux[T, RT, OT], pp: Prepend.Aux[RT, OT, CT], settings: Settings[T, H, CT], selector: Selector[H, V]): Setting[T, V] = new Setting[T, V] {
//			override def apply(): V = selector.apply(settings.settings)
//		}
//	}
//
//	@implicitNotFound("Please define an implicit value for Define[${T}] and set 'TypesT' to list of allowed setting values")
//	trait Define[T] {
//		type RequiredTypes <: HList
//		type OptionalTypes <: HList
//		type All <: HList
////		val types: Seq[Type]
//	}
//
//	object Define {
////		case class Builder[T, Types <: HList](types: Seq[Type] = Seq.empty) { outer =>
////			def add[A](implicit tt: TypeTag[A]): Builder[T, A :: Types] = Builder[T, A :: Types](types :+ tt.tpe)
////		}
//
//		case class Builder[T, RT <: HList, OT <: HList]() extends Define[T] {
//			override type RequiredTypes = RT
//			override type OptionalTypes = OT
//			def optional[A]: Builder[T, RT, A :: OT] = Builder[T, RT, A :: OT]()
//			def required[A]: Builder[T, A :: RT, OT] = Builder[T, A :: RT, OT]()
//		}
//		
//		type Aux[T, RT <: HList, OT <: HList] = Define[T] {
//			type RequiredTypes = RT
//			type OptionalTypes = OT
//		}
//
//		def apply[T]: Builder[T, HNil, HNil] = Builder[T, HNil, HNil]()
//
//		@implicitNotFound("Missing required settings")
//		trait Satisfied[H <: HList, RT <: HList] {
//			def missing: Seq[Type]
//		}
//
////		def apply[T]: Builder[T, HNil] = Builder[T, HNil]()
//
//		implicit def fromBuilder0[T, RT <: HList, OT <: HList](implicit b: Builder[T, RT, OT]): Define.Aux[T, RT, OT] = new Define[T] {
//			override type RequiredTypes = RT
//			override type OptionalTypes = OT
//		}
//
//
//	}
//
//}
//
