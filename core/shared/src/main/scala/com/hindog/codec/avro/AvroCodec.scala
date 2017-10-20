package com.hindog.codec.avro

import com.hindog.codec.{Codec, CodecException}
import com.hindog.codec.schema.{Name, UnionSchema}
import shapeless.Lazy

import scala.annotation.implicitNotFound
import scala.collection.{MapLike, _}
import scala.collection.generic.CanBuildFrom
import scala.concurrent.duration.Duration
import scala.language.higherKinds
import scala.reflect.ClassTag

import java.sql.Timestamp
import java.time.Instant
import java.util.Date
import java.util.concurrent.TimeUnit

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
	// TODO: convert to 'Json' type
	def jsonCodec: Codec[T, String] = platform.nativeJsonCodec[T](this)
}

object AvroCodec extends LowPriorityCodecImplicits {

	def apply[T](a: T => Any, i: Any => T)(implicit avroSchema: Lazy[AvroSchema[T]]): AvroCodec[T] = new AvroCodec[T] {
		override def schema: AvroSchema[T] = avroSchema.value
		override def apply(value: T): Any = a(value)
		override def inverse(value: Any): T = i(value)
	}

	def enumCodec[T](name: Name, symbols: Seq[String], fromSymbol: String => T, toSymbol: T => String)(implicit schema: Lazy[AvroSchema[T]]): AvroCodec[T] = apply(
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
	implicit def bytesCodec(implicit s: Lazy[AvroSchema[Array[Byte]]]): AvroCodec[Array[Byte]] = platform.bytesCodec
	implicit def mapStringCodec[M[k, v] <: Map[k, v], K <: String, V](implicit valueCodec: Lazy[AvroCodec[V]], schema: Lazy[AvroSchema[M[K, V]]], cbf: CanBuildFrom[Nothing, (String, V), M[K, V]]): AvroCodec[M[K, V]] = platform.mapStringCodec


	def bimapCodec[A, B](t: A => B, f: B => A)(implicit codec: Lazy[AvroCodec[B]], schema: Lazy[AvroSchema[A]]): AvroCodec[A] = {
		AvroCodec[A](
			a => codec.value.apply(t(a)),
			b => f(codec.value.inverse(b))
		)
	}
	
	implicit def dateCodec(implicit c: Lazy[AvroCodec[Long]]): AvroCodec[Date] = bimapCodec[Date, Long](_.getTime, millis => new Date(millis))
//	implicit def timestampCodec(implicit c: Lazy[AvroCodec[Long]]): AvroCodec[Timestamp] = bimapCodec[Timestamp, Long](
//		ts => ts.toInstant.getEpochSecond + (ts.toInstant.getNano * 1000000000),
//		nanos => Timestamp.from(Instant.ofEpochSecond(nanos / 1000000000).plusNanos(nanos % 1000000000))
//	)

	implicit def durationCodec(implicit c: Lazy[AvroCodec[Long]]): AvroCodec[Duration] = bimapCodec[Duration, Long](_.toMillis, millis => Duration(millis, TimeUnit.MILLISECONDS))
//
//	implicit def uuidCodec(implicit s1: Lazy[AvroSchema[UUID]], s: Lazy[AvroSchema[Array[Byte]]], c: Lazy[AvroCodec[Array[Byte]]] = bytesCodec): AvroCodec[UUID] = bimapCodec[UUID, Array[Byte]](
//		uuid => {
//			val bb = ByteBuffer.allocate(16)
//			bb.putLong(uuid.getMostSignificantBits)
//			bb.putLong(uuid.getLeastSignificantBits)
//			bb.array()
//		},
//		bytes => {
//			val bb = ByteBuffer.wrap(bytes)
//			new UUID(bb.getLong, bb.getLong)
//		}
//	)

}

trait LowPriorityCodecImplicits {

	implicit def mapArrayCodec[M[k, v] <: MapLike[k, v, M[k, v]] with Map[k, v], K, V](implicit kvCodec: Lazy[AvroCodec[(K, V)]], schema: Lazy[AvroSchema[M[K, V]]], cbf: CanBuildFrom[Nothing, (K, V), M[K, V]]): AvroCodec[M[K, V]] = platform.mapArrayCodec
	implicit def traversableCodec[CC[x] <: Traversable[x], T](implicit elemCodec: Lazy[AvroCodec[T]], schema: Lazy[AvroSchema[CC[T]]], cbf: CanBuildFrom[Nothing, T, CC[T]]): AvroCodec[CC[T]] = platform.traversableCodec
	implicit def arrayCodec[T](implicit elemCodec: Lazy[AvroCodec[T]], schema: Lazy[AvroSchema[Array[T]]], ct: ClassTag[T]): AvroCodec[Array[T]] = platform.arrayCodec

	import scala.language.experimental.macros
	implicit def materialize[T]: AvroCodec[T] = macro com.hindog.codec.avro.macros.AvroMacros.codec[T]
}

case class UnionCodecBuilder[T](typeMap: Map[String, AvroCodec[_]] = Map.empty, classMap: Map[Class[_], AvroCodec[_]] = Map.empty)(implicit avroSchema: Lazy[AvroSchema[T]]) {

	def addType[A](implicit codec: Lazy[AvroCodec[A]], ct: ClassTag[A]): UnionCodecBuilder[T] = {
		copy(typeMap = typeMap + (codec.value.schema.name.toString -> codec.value),
				 classMap = classMap + (ct.runtimeClass -> codec.value))
	}

	def build: AvroCodec[T] = new AvroCodec[T] {
		require(avroSchema.value.isInstanceOf[UnionSchema], s"Expected UnionSchema for union codec, but got: ${avroSchema.value.getClass}")

		override val schema: AvroSchema[T] = avroSchema.value

		override def apply(value: T): Any = {
			require(value != null, typeNotFound("null"))
			val codec = classMap.getOrElse(value.getClass, typeNotFound(value)).asInstanceOf[AvroCodec[T]]
			platform.wrapUnion(codec.apply(value), codec.schema)
		}

		override def inverse(value: Any): T = {
			val (unwrapped, branch) = platform.unwrapUnion(value, schema.asInstanceOf[UnionSchema])
			typeMap.get(branch.fullName).map(_.asInstanceOf[AvroCodec[T]].inverse(unwrapped)).getOrElse(typeNotFound(value))
		}
	}

	private def typeNotFound(value: Any) = throw CodecException(s"Type not registered for value: $value")
}