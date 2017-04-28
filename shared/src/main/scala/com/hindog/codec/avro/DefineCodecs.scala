package com.hindog.codec.avro

import shapeless.Lazy

import scala.collection._
import scala.collection.generic.CanBuildFrom
import scala.language.higherKinds

/*
 *    __   _         __         
 *   / /  (_)__  ___/ /__  ____
 *  / _ \/ / _ \/ _  / _ \/ _  /
 * /_//_/_/_//_/\_,_/\___/\_, / 
 *                       /___/
 */
trait DefineCodecs {

	def intCodec: AvroCodec[Int]
	def longCodec: AvroCodec[Long]
	def floatCodec: AvroCodec[Float]
	def doubleCodec: AvroCodec[Double]
	def stringCodec: AvroCodec[String]
	def booleanCodec: AvroCodec[Boolean]
	def optionCodec[T](implicit codec: Lazy[AvroCodec[T]], schema: Lazy[AvroSchema[Option[T]]]): AvroCodec[Option[T]]

	def mapStringCodec[M[k, v] <: Map[k, v], K <: String, V](implicit valueCodec: Lazy[AvroCodec[V]], schema: Lazy[AvroSchema[M[K, V]]], cbf: CanBuildFrom[Nothing, (String, V), M[K, V]]): AvroCodec[M[K, V]]
	def mapArrayCodec[M[k, v] <: MapLike[k, v, M[k, v]] with Map[k, v], K, V](implicit kvCodec: Lazy[AvroCodec[(K, V)]], schema: Lazy[AvroSchema[M[K, V]]], cbf: CanBuildFrom[Nothing, (K, V), M[K, V]]): AvroCodec[M[K, V]]
	def iterableCodec[CC[x] <: TraversableOnce[x], T](implicit elemCodec: Lazy[AvroCodec[T]], schema: Lazy[AvroSchema[CC[T]]], cbf: CanBuildFrom[Nothing, T, CC[T]]): AvroCodec[CC[T]]

}
