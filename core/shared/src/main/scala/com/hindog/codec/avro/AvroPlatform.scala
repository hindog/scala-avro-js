package com.hindog.codec.avro

import com.hindog.codec.{Codec, Resolver}
import com.hindog.codec.schema._
import com.hindog.log.Logger
import shapeless.Lazy

import scala.collection.generic.CanBuildFrom
import scala.collection.{Iterable, Map, MapLike, Traversable, TraversableLike, TraversableOnce}
import scala.language.higherKinds
import scala.reflect.ClassTag

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
	type NativeRecord <: NativeType

	def logger[T](implicit ct: ClassTag[T]): Logger
	def recordBuilder(schema: Lazy[Schema]): RecordBuilder[NativeRecord]
	def recordExtractor(record: NativeRecord, field: String): NativeType
	def nativeSchema(schema: Schema)(implicit resolver: Resolver[NativeSchema] = new Resolver[NativeSchema]): NativeSchema

	def wrapUnion(value: Any, schema: Schema): NativeType
	def unwrapUnion(value: Any, schema: UnionSchema): (NativeType, Schema)

	def nativeJsonCodec[T](avro: AvroCodec[T]): Codec[T, String]
	def nativeBinaryCodec[T](avro: AvroCodec[T]): Codec[T, Array[Byte]]

	def intCodec: AvroCodec[Int]
	def longCodec: AvroCodec[Long]
	def floatCodec: AvroCodec[Float]
	def doubleCodec: AvroCodec[Double]
	def stringCodec: AvroCodec[String]
	def booleanCodec: AvroCodec[Boolean]
	def bytesCodec(implicit s: Lazy[AvroSchema[Array[Byte]]]): AvroCodec[Array[Byte]]
	def mapStringCodec[M[k, v] <: Map[k, v], K <: String, V](implicit valueCodec: Lazy[AvroCodec[V]], schema: Lazy[AvroSchema[M[K, V]]], cbf: CanBuildFrom[Nothing, (String, V), M[K, V]]): AvroCodec[M[K, V]]
	def mapArrayCodec[M[k, v] <: MapLike[k, v, M[k, v]] with Map[k, v], K, V](implicit kvCodec: Lazy[AvroCodec[(K, V)]], schema: Lazy[AvroSchema[M[K, V]]], cbf: CanBuildFrom[Nothing, (K, V), M[K, V]]): AvroCodec[M[K, V]]
	def traversableCodec[CC[x] <: Traversable[x], T](implicit elemCodec: Lazy[AvroCodec[T]], schema: Lazy[AvroSchema[CC[T]]], cbf: CanBuildFrom[Nothing, T, CC[T]]): AvroCodec[CC[T]]
	def arrayCodec[T](implicit elemCodec: Lazy[AvroCodec[T]], schema: Lazy[AvroSchema[Array[T]]], ct: ClassTag[T]): AvroCodec[Array[T]]
	def optionCodec[T](implicit codec: Lazy[AvroCodec[T]], schema: Lazy[AvroSchema[Option[T]]]): AvroCodec[Option[T]]

}

trait RecordBuilder[NativeRecord] {
	def set(idx: Int, name: String, value: Any): this.type
	def build(): NativeRecord
}

