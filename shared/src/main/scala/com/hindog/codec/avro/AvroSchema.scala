package com.hindog.codec.avro

import com.hindog.codec.schema._
import shapeless.Lazy

import scala.Seq
import scala.annotation.implicitNotFound
import scala.collection._
import scala.language.experimental.macros
import scala.language.higherKinds
/*
 *    __   _         __         
 *   / /  (_)__  ___/ /__  ____
 *  / _ \/ / _ \/ _  / _ \/ _  /
 * /_//_/_/_//_/\_,_/\___/\_, / 
 *                       /___/
 */

@implicitNotFound("implicit value not found for AvroSchema[${T}]")
trait AvroSchema[T] extends Schema {
	protected[avro] lazy val native: platform.NativeSchema = platform.nativeSchema(this)
	def typeOf: Schema.Type
	override def toString: String = typeOf.name
}

object AvroSchema extends LowPriorityImplicits {

	implicit val nullSchema: AvroSchema[Nothing] = new NullSchema with AvroSchema[Nothing]
	implicit val intSchema: AvroSchema[Int] = new IntSchema with AvroSchema[Int]
	implicit val longSchema: AvroSchema[Long] = new LongSchema with AvroSchema[Long]
	implicit val floatSchema: AvroSchema[Float] = new FloatSchema with AvroSchema[Float]
	implicit val doubleSchema: AvroSchema[Double] = new DoubleSchema with AvroSchema[Double]
	implicit val booleanSchema: AvroSchema[Boolean] = new BooleanSchema with AvroSchema[Boolean]
	implicit val stringSchema: AvroSchema[String] = new StringSchema with AvroSchema[String]
	implicit val byteArraySchema: AvroSchema[Array[Byte]] = new BytesSchema with AvroSchema[Array[Byte]]

	implicit def optionSchema[T](implicit schema: Lazy[AvroSchema[T]]): AvroSchema[Option[T]] = new UnionSchema with AvroSchema[Option[T]] {
		override def types: Seq[Schema] = Seq(nullSchema, schema.value)
	}

	implicit def mapStringSchema[M[k, v] <: MapLike[k, v, M[k, v]] with Map[k, v], K <: String, V](implicit valueSchema: Lazy[AvroSchema[V]]): AvroSchema[M[K, V]] = new MapSchema with AvroSchema[M[K, V]] {
		override def itemSchema: Schema = valueSchema.value
	}

	implicit def mapArraySchema[M[k, v] <: MapLike[k, v, M[k, v]] with Map[k, v], K, V](implicit kvSchema: Lazy[AvroSchema[(K, V)]]): AvroSchema[M[K, V]] = new MapSchema with AvroSchema[M[K, V]] {
		override def itemSchema: Schema = kvSchema.value
	}

	implicit def iterableSchema[CC[x] <: TraversableOnce[x], T](implicit elemSchema: Lazy[AvroSchema[T]]): AvroSchema[CC[T]] = new ArraySchema with AvroSchema[CC[T]] {
		override def elementSchema: Schema = elemSchema.value
	}

}

trait LowPriorityImplicits {

//	implicit def combineFieldType[M[x] <: AvroSchema[x], T, W, FT, GenR <: HList, Out1 <: HList, Out2 <: HList](implicit gen: LabelledGeneric.Aux[T, GenR], s: Selector.Aux[GenR, W, FT], mb: Settings.Define.Aux[M[T] :\ W, Out1], tb: Settings.Define.Aux[FT, Out2], prepend: Prepend[Out1, Out2]): Define.Aux[M[T] :\ W, prepend.Out] = new Define[M[T] :\ W] {
//		override type TypesT = prepend.Out
//		override val types: Seq[Type] = mb.types ++ tb.types
//	}
//
//	implicit def schemaDefine[T, AS[x] <: AvroSchema[x], Out <: HList] = Settings.Define[AS[T]].add[com.hindog.codec.schema.Name].add[Doc].add[Properties]
//	implicit def fieldDefine[T, AS[x] <: AvroSchema[x], W, Out <: HList](implicit gen: LabelledGeneric.Aux[T, Out], s: Selector[Out, W]) = Settings.Define[AS[T] :\ W].add[FieldName].add[Doc].add[Aliases].add[com.hindog.codec.schema.RecordSchema.Field.Ordered]

	import scala.language.experimental.macros

	implicit def materialize[T]: AvroSchema[T] = macro com.hindog.codec.avro.macros.AvroMacros.schema[T]

}
