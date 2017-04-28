package com.hindog
package codec
package avro

import com.hindog.codec.schema.RecordSchema.DefaultValue
import com.hindog.codec.schema._
import com.hindog.func._
import org.apache._
import org.apache.avro.JsonProperties
import org.apache.avro.generic.GenericData.Record
import org.apache.avro.generic.{GenericData, GenericRecordBuilder}
import org.apache.avro.util.Utf8
import shapeless.Lazy

import scala.collection.JavaConverters._
import scala.collection._
import scala.collection.generic.CanBuildFrom
import scala.language.higherKinds
import scala.util.DynamicVariable

/*
 *    __   _         __
 *   / /  (_)__  ___/ /__  ____
 *  / _ \/ / _ \/ _  / _ \/ _  /
 * /_//_/_/_//_/\_,_/\___/\_, /
 *                       /___/
 */
private[avro] object Platform extends AvroPlatform {
	type NativeType = Any
	type NativeSchema = avro.Schema
	type RecordType = Record

	protected val ignoreDefaultValue = new DynamicVariable(false)
	protected val resolveUnion = new DynamicVariable(true)

	override def recordBuilder[T](schema: Lazy[AvroSchema[T]]): RecordBuilder[Record] = NativeRecordBuilder(schema.value.native.asInstanceOf[NativeSchema])
	override def recordExtractor(record: RecordType, field: String): NativeType = record.get(field)

	override def nativeSchema(schema: Schema)(implicit resolver: Resolver[NativeSchema] = new Resolver[NativeSchema]): NativeSchema = {
	
		resolver.get(schema.fullName) getOrElse {
			schema match {
				case s: NullSchema => avro.Schema.create(avro.Schema.Type.NULL)
				case s: IntSchema => avro.Schema.create(avro.Schema.Type.INT)
				case s: LongSchema => avro.Schema.create(avro.Schema.Type.LONG)
				case s: FloatSchema => avro.Schema.create(avro.Schema.Type.FLOAT)
				case s: DoubleSchema => avro.Schema.create(avro.Schema.Type.DOUBLE)
				case s: BooleanSchema => avro.Schema.create(avro.Schema.Type.BOOLEAN)
				case s: StringSchema => avro.Schema.create(avro.Schema.Type.STRING)
				case s: BytesSchema => avro.Schema.create(avro.Schema.Type.BYTES)
				case s: FixedSchema => avro.Schema.create(avro.Schema.Type.FIXED)
				case s: MapSchema => avro.Schema.createMap(nativeSchema(s.itemSchema))
				case s: ArraySchema => avro.Schema.createArray(nativeSchema(s.elementSchema))
				case s: UnionSchema => avro.Schema.createUnion(s.types.map(nativeSchema).asJava)
				case s: RecordSchema => {
					val record = resolver.resolve(s.fullName, avro.Schema.createRecord(s.name.name, null, s.name.namespace.orNull, false))
					val fields = s.fields.map(f => {
						// 1) resolve native schema for this field
						// 2) check if we're dealing with a union, if so, we'll look for the default value's schema and
						//    clone the schema with the default's schema type pushed to the front of the list (per the Avro spec)
						val fieldSchema = resolver.resolve(f.schema.value.fullName, nativeSchema(f.schema.value)).ifThen(_.getType == avro.Schema.Type.UNION && resolveUnion.value) { s =>
							resolveUnion.withValue(false) {
								val schemaWithDefault = for {
									default <- f.defaultValue
									schemaForDefault <- unionBranch(default.codec.value.asInstanceOf[AvroCodec[Any]].apply(default.value), f.schema.value.asInstanceOf[UnionSchema])
								} yield {
									s.getTypes.asScala.partition(_.getFullName == schemaForDefault.fullName) match {
										case (head, tail) => avro.Schema.createUnion((head ++ tail).asJava)
									}
								}

								schemaWithDefault.getOrElse(s)
							}
						}

						new avro.Schema.Field(f.name, fieldSchema, null, f.defaultValue.map(toDefaultValue).orNull)
					})

					record.setFields(fields.asJava)
					record
				}
				case other => throw CodecException(s"Unhandled schema type: ${other.name} [${other.getClass.getName}]")
			}
		}
	}

	def toDefaultValue(value: DefaultValue[_]): Any = {

		def go(v: Any): Any = {
			v match {
				case null => JsonProperties.NULL_VALUE
				case record: GenericData.Record => record.getSchema.getFields.asScala.foldLeft(immutable.Map.empty[String, Any])((acc, cur) => {
					acc + (cur.name() -> go(record.get(cur.name())))
				}).asJava
				case _ => v
			}
		}

		if (ignoreDefaultValue.value) null else {
			val cast = value.asInstanceOf[DefaultValue[Any]]
			go(ignoreDefaultValue.withValue(true) { cast.codec.value.apply(cast.value) })
		}
	}

	// given a value, resolve the union's matching schema
	override def unionBranch(value: Any, schema: UnionSchema): Option[Schema] = {
		def findFirstSchema(types: String*) = types.foldLeft(None: Option[Schema])((acc, cur) => acc orElse schema.typeMap.get(cur))

		value match {
			case null => findFirstSchema("null")
			case v: java.lang.Integer => findFirstSchema("int")
			case v: java.lang.Long => findFirstSchema("long")
			case v: java.lang.Float => findFirstSchema("float")
			case v: java.lang.Double => findFirstSchema("double")
			case v: java.lang.Boolean => findFirstSchema("boolean")
			case v: java.lang.String => findFirstSchema("string", "enum", "fixed")
			case v: Utf8 => findFirstSchema("string", "enum", "fixed")
			case v: java.nio.ByteBuffer => findFirstSchema("bytes", "fixed")
			case v: java.util.Map[AnyRef, AnyRef] => findFirstSchema("map")
			case v: java.util.Collection[AnyRef] => findFirstSchema("array")
			case v: GenericData.Record => findFirstSchema(v.getSchema.getFullName)
			case other => throw CodecException(s"encountered invalid native value for union: ${other.getClass.getName}")
		}
	}

	override def intCodec: AvroCodec[Int] = AvroCodec[Int](v => v, { case v: Int => v })
	override def longCodec: AvroCodec[Long] = AvroCodec[Long](v => v, { case v: Long => v })
	override def floatCodec: AvroCodec[Float] = AvroCodec[Float](v => v, { case v: Float => v })
	override def doubleCodec: AvroCodec[Double] = AvroCodec[Double](v => v, { case v: Double => v })
	override def stringCodec: AvroCodec[String] = AvroCodec[String](v => new Utf8(v), { case v: Utf8 => v.toString; case s: String => s })
	override def booleanCodec: AvroCodec[Boolean] = AvroCodec[Boolean](v => v, { case v: Boolean => v })
	override def optionCodec[T](implicit codec: Lazy[AvroCodec[T]], schema: Lazy[AvroSchema[Option[T]]]): AvroCodec[Option[T]] = AvroCodec[Option[T]]({
		case Some(v) => codec.value(v)
		case None => null
	}, {
		case null => None
		case value => Some(codec.value.inverse(value))
	})

	//override def arrayCodec[T: ClassTag](implicit elemCodec: Lazy[AvroCodec[T]], schema: Lazy[AvroSchema[Array[T]]]): Unit = AvroCodec[Array[T]]()
	override def mapStringCodec[M[k, v] <: Map[k, v], K <: String, V](implicit valueCodec: Lazy[AvroCodec[V]], schema: Lazy[AvroSchema[M[K, V]]], cbf: CanBuildFrom[Nothing, (String, V), M[K, V]]): AvroCodec[M[K, V]] = AvroCodec[M[K, V]](
		map => map.mapValues(valueCodec.value.apply).asJava,
		{
			case map: java.util.Map[String @unchecked, AnyRef @unchecked] => {
				val builder = cbf.apply()
				builder ++= map.asScala.mapValues(valueCodec.value.inverse).toMap
				builder.result()
			}
		}
	)

	override def mapArrayCodec[M[k, v] <: MapLike[k, v, M[k, v]] with Map[k, v], K, V](implicit kvCodec: Lazy[AvroCodec[(K, V)]], schema: Lazy[AvroSchema[M[K, V]]], cbf: CanBuildFrom[Nothing, (K, V), M[K, V]]): AvroCodec[M[K, V]] = AvroCodec[M[K, V]](
		map => map.map(v => kvCodec.value(v)).asJavaCollection,
		{
			case map: java.util.Collection[AnyRef @unchecked] => {
				val builder = cbf.apply()
				builder ++= map.asScala.map(kvCodec.value.inverse).toMap
				builder.result()
			}
		}
	)

	override def iterableCodec[CC[x] <: TraversableOnce[x], T](implicit elemCodec: Lazy[AvroCodec[T]], schema: Lazy[AvroSchema[CC[T]]], cbf: CanBuildFrom[Nothing, T, CC[T]]): AvroCodec[CC[T]] = AvroCodec[CC[T]](
		col => col.map(elemCodec.value.apply).toList.asJavaCollection,
		{
			case col: java.util.Collection[AnyRef @unchecked] => {
				val builder = cbf.apply()
				builder ++= col.asScala.map(elemCodec.value.inverse)
				builder.result()
			}
		}
	)
}


case class NativeRecordBuilder(schema: avro.Schema) extends RecordBuilder[Record] {
	val builder = new GenericRecordBuilder(schema)
	override def set(name: String, value: Any): this.type = { builder.set(name, value); this }
	override def build(): Record = builder.build()
}