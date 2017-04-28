package com.hindog.codec.avro

import com.hindog.codec.schema.{Name, UnionSchema}
import com.hindog.codec.{Codec, CodecException}
import shapeless.Lazy

import scala.annotation.implicitNotFound
import scala.collection._
import scala.collection.generic.CanBuildFrom
import scala.language.experimental.macros
import scala.language.higherKinds
import scala.reflect.ClassTag

/*
 *    __   _         __         
 *   / /  (_)__  ___/ /__  ____
 *  / _ \/ / _ \/ _  / _ \/ _  /
 * /_//_/_/_//_/\_,_/\___/\_, / 
 *                       /___/
 */
@implicitNotFound("implicit value not found for AvroCodec[${T}]")
trait AvroCodec[T] extends Codec[T, Any] {
	def schema: AvroSchema[T]
}

object AvroCodec extends DefineCodecs with LowPriorityCodecImplicits {

	def apply[T](a: T => Any, i: Any => T)(implicit avroSchema: Lazy[AvroSchema[T]]): AvroCodec[T] = new AvroCodec[T] {
		override def schema: AvroSchema[T] = avroSchema.value
		override def apply(value: T): Any = a(value)
		override def inverse(value: Any): T = i(value)
	}

	def createEnumCodec[T](name: Name, symbols: Seq[String], fromSymbol: String => T, toSymbol: T => String)(implicit schema: Lazy[AvroSchema[T]]): AvroCodec[T] = apply(
		value => toSymbol(value),
		{
			case s: String => fromSymbol(s)
			case other => throw CodecException(s"$other is not a valid symbol [${symbols.mkString(", ")}]")
		}
	)

	implicit def intCodec: AvroCodec[Int] = platform.intCodec
	implicit def longCodec: AvroCodec[Long] = platform.longCodec
	implicit def floatCodec: AvroCodec[Float] = platform.floatCodec
	implicit def doubleCodec: AvroCodec[Double] = platform.doubleCodec
	implicit def stringCodec: AvroCodec[String] = platform.stringCodec
	implicit def booleanCodec: AvroCodec[Boolean] = platform.booleanCodec
	implicit def optionCodec[T](implicit codec: Lazy[AvroCodec[T]], schema: Lazy[AvroSchema[Option[T]]]): AvroCodec[Option[T]] = platform.optionCodec[T]

	implicit def mapStringCodec[M[k, v] <: Map[k, v], K <: String, V](implicit valueCodec: Lazy[AvroCodec[V]], schema: Lazy[AvroSchema[M[K, V]]], cbf: CanBuildFrom[Nothing, (String, V), M[K, V]]): AvroCodec[M[K, V]] = platform.mapStringCodec

}

trait LowPriorityCodecImplicits {

	implicit def mapArrayCodec[M[k, v] <: MapLike[k, v, M[k, v]] with Map[k, v], K, V](implicit kvCodec: Lazy[AvroCodec[(K, V)]], schema: Lazy[AvroSchema[M[K, V]]], cbf: CanBuildFrom[Nothing, (K, V), M[K, V]]): AvroCodec[M[K, V]] = platform.mapArrayCodec
	implicit def iterableCodec[CC[x] <: TraversableOnce[x], T](implicit elemCodec: Lazy[AvroCodec[T]], schema: Lazy[AvroSchema[CC[T]]], cbf: CanBuildFrom[Nothing, T, CC[T]]): AvroCodec[CC[T]] = platform.iterableCodec

	import scala.language.experimental.macros

	implicit def materialize[T]: AvroCodec[T] = macro com.hindog.codec.avro.macros.AvroMacros.codec[T]
}

case class UnionCodecBuilder[T](typeMap: Map[String, AvroCodec[_]] = Map.empty, classMap: Map[Class[_], AvroCodec[_]] = Map.empty)(implicit avroSchema: Lazy[AvroSchema[T]]) {
	def addType[A](implicit codec: Lazy[AvroCodec[A]], ct: ClassTag[T]): UnionCodecBuilder[T] = {
		copy(typeMap = typeMap + (codec.value.schema.fullName -> codec.value),
				 classMap = classMap + (ct.runtimeClass -> codec.value))
	}

	def build: AvroCodec[T] = new AvroCodec[T] {
		require(avroSchema.value.isInstanceOf[UnionSchema], s"Expected UnionSchema for union codec, but got: ${avroSchema.value.getClass}")

		override val schema: AvroSchema[T] = avroSchema.value

		override def apply(value: T): Any = {
			require(value != null, typeNotFound("null"))
			classMap.getOrElse(value.getClass, typeNotFound(value)).asInstanceOf[AvroCodec[T]].apply(value)
		}

		override def inverse(value: Any): T = {
			val branch = platform.unionBranch(value, schema.asInstanceOf[UnionSchema])
			branch.map(_.fullName).flatMap(typeMap.get).map(_.asInstanceOf[AvroCodec[T]].inverse(value)).getOrElse(throw typeNotFound(value))
		}
	}

	private def typeNotFound(value: Any) = throw CodecException(s"Type not registered for value: $value")
}