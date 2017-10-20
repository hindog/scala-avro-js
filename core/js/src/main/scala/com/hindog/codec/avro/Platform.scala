package com.hindog.codec.avro

import com.hindog.codec._
import com.hindog.codec.schema._
import com.hindog.log.Logger
import org.scalajs.dom._
import shapeless.Lazy

import scala.collection.{Map, MapLike, Traversable}
import scala.collection.generic.CanBuildFrom
import scala.language.higherKinds
import scala.reflect.ClassTag
import scala.scalajs.js


/**
  * Created by Aaron Hiniker (ahiniker@atomtickets.com) 
  * 8/28/17
  * Copyright (c) Atom Tickets, LLC
  */
private[avro] object Platform extends AvroPlatform {
  override type NativeType = js.Any
  override type NativeSchema = js.Any
  override type NativeRecord = js.Any

  override def logger[T](implicit ct: ClassTag[T]): Logger = new Logger {
    override def error(msg: => String): Unit = console.log(s"[${ct.runtimeClass.getSimpleName}] $msg")
    override def warn(msg: => String): Unit = console.log(s"[${ct.runtimeClass.getSimpleName}] $msg")
    override def info(msg: => String): Unit = console.log(s"[${ct.runtimeClass.getSimpleName}] $msg")
    override def debug(msg: => String): Unit = console.log(s"[${ct.runtimeClass.getSimpleName}] $msg")
    override def trace(msg: => String): Unit = console.log(s"[${ct.runtimeClass.getSimpleName}] $msg")
  }

  override def recordBuilder(schema: Lazy[Schema]): RecordBuilder[js.Any @unchecked] = new NativeRecordBuilder(schema.value.asInstanceOf[RecordSchema])
  override def recordExtractor(record: js.Any @unchecked, field: String): NativeType @unchecked = record.asInstanceOf[js.Dynamic].selectDynamic(field)
  override def nativeSchema(schema: Schema)(implicit resolver: Resolver[NativeSchema @unchecked]): NativeSchema = {
    def resolveSchema(s: Schema): js.Any = {
      if (resolver.get(s.fullName).isDefined) {
        s.fullName
      } else {
        s match {
          case s: NullSchema => "null"
          case s: IntSchema => "int"
          case s: LongSchema => "long"
          case s: FloatSchema => "float"
          case s: DoubleSchema => "double"
          case s: BooleanSchema => "boolean"
          case s: StringSchema => "string"
          case s: BytesSchema => "bytes"
          case s: FixedSchema => js.Dynamic.literal("type" -> "fixed", "name" -> s.fullName, "size" -> s.size)
          case s: MapSchema => js.Dynamic.literal("type" -> "map", "values" -> resolveSchema(s.itemSchema))
          case s: ArraySchema => js.Dynamic.literal("type" -> "array", "items" -> resolveSchema(s.elementSchema))
          case s: UnionSchema => js.Array(s.types.map(resolveSchema): _*)
          case s: RecordSchema => {
            val record = resolver.resolve(s.fullName, js.Dynamic.literal("type" -> "record", "name" -> s.fullName))
            val fields = js.Array(s.fields.map(f => {
              js.Dynamic.literal("name" -> f.name, "type" -> {
                resolver.resolve(f.schema.value.fullName, resolveSchema(f.schema.value))
              })
            }): _*)
            record.asInstanceOf[js.Dynamic].updateDynamic("fields")(fields)
            record
          }
        }
      }
    }

    val native = resolveSchema(schema)
    Avro.Type.forSchema(native.asInstanceOf[js.Object])
  }


  override def wrapUnion(value: Any, schema: Schema): js.Any = js.Dynamic.literal(schema.fullName -> value.asInstanceOf[js.Any])

  override def unwrapUnion(value: Any, schema: UnionSchema): (NativeType @unchecked, Schema) = {
    val field = value.asInstanceOf[js.Dictionary[js.Any]].head
    schema.types.find(_.fullName == field._1).map(s => field._2 -> s).getOrElse(throw CodecException(s"Avro type '${field._1}' not present in union [${schema.types.map(_.fullName).mkString(", ")}]"))
  }

  override def nativeJsonCodec[T](avro: AvroCodec[T]): Codec[T, String] = new Codec[T, String] {
    private val native = nativeSchema(avro.schema)
    override def apply(value: T): String = native.asInstanceOf[AvroType].toString(avro.apply(value).asInstanceOf[js.Any])
    override def inverse(value: String): T = avro.inverse(native.asInstanceOf[AvroType].fromString(value))
  }

  override def nativeBinaryCodec[T](avro: AvroCodec[T]): Codec[T, Array[Byte]] = throw new RuntimeException(s"nativeBinaryCodec not implemented")

  override def intCodec: AvroCodec[Int] = AvroCodec[Int](identity, { case i: Int => i })
  override def longCodec: AvroCodec[Long] = AvroCodec[Long](_.toDouble, { case i: Double => i.toLong })
  override def floatCodec: AvroCodec[Float] = AvroCodec[Float](identity, { case i: Float => i })
  override def doubleCodec: AvroCodec[Double] = AvroCodec[Double](identity, { case i: Double => i })
  override def stringCodec: AvroCodec[String] = AvroCodec[String](identity, { case i: String => i })
  override def booleanCodec: AvroCodec[Boolean] = AvroCodec[Boolean](identity, { case i: Boolean => i })
  override def bytesCodec(implicit s: Lazy[AvroSchema[Array[Byte]]]): AvroCodec[Array[Byte]] = throw new RuntimeException(s"bytesCodec not implemented")
  override def optionCodec[T](implicit codec: Lazy[AvroCodec[T]], schema: Lazy[AvroSchema[Option[T]]]): AvroCodec[Option[T]] = AvroCodec[Option[T]](
    v => v.map(v => codec.value.apply(v).asInstanceOf[js.Object]).getOrElse(null),
    {
      case null => None
      case v => Some(codec.value.inverse(v))
    }
  )
  override def mapStringCodec[M[k, v] <: Map[k, v], K <: String, V](implicit valueCodec: Lazy[AvroCodec[V]], schema: Lazy[AvroSchema[M[K, V]]], cbf: CanBuildFrom[Nothing, (String, V), M[K, V]]): AvroCodec[M[K, V]] = AvroCodec[M[K, V]](
    //TODO: cleanup
    map => js.Dictionary(map.map(kv => kv._1 -> valueCodec.value.apply(kv._2)).toSeq: _*),
    {
      case v: js.Object => {
        val builder = cbf.apply()
        js.Object.properties(v).foreach(f =>
          builder += f -> valueCodec.value.inverse(v.asInstanceOf[js.Dictionary[js.Any]](f))
        )
        builder.result()
      }
    }
  )

  override def mapArrayCodec[M[k, v] <: MapLike[k, v, M[k, v]] with Map[k, v], K, V](implicit kvCodec: Lazy[AvroCodec[(K, V)]], schema: Lazy[AvroSchema[M[K, V]]], cbf: CanBuildFrom[Nothing, (K, V), M[K, V]]): AvroCodec[M[K, V]] = AvroCodec[M[K, V]](
    map => js.Array(map.map(kv => kvCodec.value.apply(kv).asInstanceOf[js.Object]).toSeq: _*),
    {
      case v: js.Array[js.Any @unchecked] => {
        val builder = cbf.apply()
        v.foreach(v =>
          builder += kvCodec.value.inverse(v)
        )
        builder.result()
      }
    }
  )

  def traversableCodec[CC[x] <: Traversable[x], T](implicit elemCodec: Lazy[AvroCodec[T]], schema: Lazy[AvroSchema[CC[T]]], cbf: CanBuildFrom[Nothing, T, CC[T]]): AvroCodec[CC[T]] = AvroCodec[CC[T]](
    arr => js.Array(arr.map(elemCodec.value.apply).toSeq: _*),
    {
      case v: js.Array[js.Any @unchecked] => {
        val builder = cbf.apply()
        v.foreach(v =>
          builder += elemCodec.value.inverse(v)
        )
        builder.result()
      }
    }
  )

  override def arrayCodec[T](implicit elemCodec: Lazy[AvroCodec[T]], schema: Lazy[AvroSchema[Array[T]]], ct: ClassTag[T]): AvroCodec[Array[T]] = AvroCodec[Array[T]](
    arr => js.Array(arr.map(elemCodec.value.apply).toSeq: _*),
    arrayDecoder[T]
  )

  //private def arrayEncoder[T]
  private def arrayDecoder[T](implicit codec: Lazy[AvroCodec[T]], cbf: CanBuildFrom[Nothing, T, Array[T]]): PartialFunction[Any, Array[T]] = {
    case v: js.Array[js.Any @unchecked] => {
      val builder = cbf.apply()
      v.foreach(v =>
        builder += codec.value.inverse(v)
      )
      builder.result()
    }
  }

  /**
    * var con = schema.recordConstructor
    * new (Function.prototype.bind.apply(con, [null, 1, "c"]))
    */
  @SuppressWarnings(Array("unchecked"))
  class NativeRecordBuilder(schema: RecordSchema) extends RecordBuilder[js.Any @unchecked] {
    private val args = Array.ofDim[js.Any](schema.fields.size)

    override def set(idx: Int, name: String, value: Any): this.type = {
      args(idx) = value.asInstanceOf[js.Any]
      this
    }

    override def build(): js.Dynamic = {
      val con = schema.native.asInstanceOf[RecordTypeBuilder].recordConstructor
      js.Dynamic.newInstance(con.bind(con, args: _*))()
    }
  }
}

