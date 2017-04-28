package com.hindog.codec.avro

import com.hindog.codec.Resolver
import com.hindog.codec.schema._
import shapeless.Lazy

import scala.collection.generic.CanBuildFrom
import scala.collection.{Map, MapLike, TraversableOnce}
import scala.language.higherKinds

//import DefaultPlatform._

/*
 *    __   _         __         
 *   / /  (_)__  ___/ /__  ____
 *  / _ \/ / _ \/ _  / _ \/ _  /
 * /_//_/_/_//_/\_,_/\___/\_, / 
 *                       /___/
 */

trait AvroPlatform {
	type NativeType <: Any
	type NativeSchema
	type RecordType <: NativeType

	def recordBuilder[T](schema: Lazy[AvroSchema[T]]): RecordBuilder[RecordType]
	def recordExtractor(record: RecordType, field: String): NativeType

	def nativeSchema(schema: Schema)(implicit resolver: Resolver[NativeSchema] = new Resolver[NativeSchema]): NativeSchema

	def unionBranch(value: Any, schema: UnionSchema): Option[Schema]

	def intCodec: AvroCodec[Int]
	def longCodec: AvroCodec[Long]
	def floatCodec: AvroCodec[Float]
	def doubleCodec: AvroCodec[Double]
	def stringCodec: AvroCodec[String]
	def booleanCodec: AvroCodec[Boolean]
	def mapStringCodec[M[k, v] <: Map[k, v], K <: String, V](implicit valueCodec: Lazy[AvroCodec[V]], schema: Lazy[AvroSchema[M[K, V]]], cbf: CanBuildFrom[Nothing, (String, V), M[K, V]]): AvroCodec[M[K, V]]
	def mapArrayCodec[M[k, v] <: MapLike[k, v, M[k, v]] with Map[k, v], K, V](implicit kvCodec: Lazy[AvroCodec[(K, V)]], schema: Lazy[AvroSchema[M[K, V]]], cbf: CanBuildFrom[Nothing, (K, V), M[K, V]]): AvroCodec[M[K, V]]
	def iterableCodec[CC[x] <: TraversableOnce[x], T](implicit elemCodec: Lazy[AvroCodec[T]], schema: Lazy[AvroSchema[CC[T]]], cbf: CanBuildFrom[Nothing, T, CC[T]]): AvroCodec[CC[T]]

	def optionCodec[T](implicit codec: Lazy[AvroCodec[T]], schema: Lazy[AvroSchema[Option[T]]]): AvroCodec[Option[T]]

}

trait RecordBuilder[RecordType] {
	def set(name: String, value: Any): this.type
	def build(): RecordType
}

