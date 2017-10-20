package com.hindog.codec

import com.hindog.codec.schema.{Doc, Properties}
import com.hindog.codec.schema.RecordSchema.Field
import com.hindog.codec.schema.RecordSchema.Field.HasField
import io.circe.Json
import shapeless._
import shapeless.ops.hlist.{FilterNot, Selector}

import scala.annotation.implicitNotFound
import scala.reflect.ClassTag

/**
  * Created by Aaron Hiniker (ahiniker@atomtickets.com)
  * 9/20/17
  * Copyright (c) Atom Tickets, LLC
  */
object Options {

  type Aux[T, Out0] = Options[T] { type Out = Out0 }

  def forType[T]: Options[T] = empty[T]
  def forField[T, F](implicit ev: T HasField F): Options[T HasField F] = empty[T HasField F]
  //def forField[T](name: String): Options[T HasField F]
  def select[T, O](implicit opt: Options[T] = null, ct: ClassTag[O]): Option[O] = Option(opt).flatMap(_.select[O])

  def empty[T]: Options.Aux[T, HNil] = new Options[T] { type Out = HNil; def value = HNil }
}

trait Options[T] { self =>
  type Out <: HList

  def |[A](v: A): Options.Aux[T, A :: self.Out] = new Options[T] {
    type Out = A :: self.Out
    def value = v :: self.value
  }

  def value: Out
  def select[A](implicit ct: ClassTag[A]): Option[A] = {
    def go(v: HList): Option[A] = v match {
      case ::(ct(head), tail) => Some(head)
      case ::(head, tail) => go(tail)
      case HNil => None
    }

    go(value)    
  }
}

@implicitNotFound("Option value with type: ${A} is not available in ${L}")
private[codec] trait CanBeApplied[L <: HList, A] {
  type Out <: HList
}

object CanBeApplied {
  implicit def app[L <: HList, A, Out0 <: HList](implicit sel: Selector[L, A], filter: FilterNot.Aux[L, A, Out0]): CanBeApplied[L, A] = new CanBeApplied[L, A] {
    type Out = Out0
  }
}

// TODO: remove this
object BuilderApp extends App {

  case class Foo(name: String, age: Int)
  
  implicit val fooOpts = Options.forType[Foo] | Doc("adsf") | Properties()
  implicit val fooNameOpts: Options[Foo HasField Field.`'name`.T] = Options.forField[Foo, Field.`'name`.T] | Doc("field doc") | Properties("prop1" -> Json.fromString("value1")) | (Field.Order.Ascending: Field.Order)

  println(Options.select[Foo HasField Field.`'name`.T, Doc])
}
