package com.hindog.codec.schema

import com.hindog.codec.avro.AvroCodec
import com.hindog.codec.schema.Schema._
import io.circe.Json
import shapeless._
import shapeless.ops.record._

import scala.collection.{Map, Seq}
import scala.language.experimental.macros
import scala.language.{dynamics, existentials}

/*
 *    __   _         __         
 *   / /  (_)__  ___/ /__  ____
 *  / _ \/ / _ \/ _  / _ \/ _  /
 * /_//_/_/_//_/\_,_/\___/\_, / 
 *                       /___/
 */
trait Schema {
	def name: Name = Name(typeOf.name)
	def fullName: String = name.toString

	def typeOf: Schema.Type
	override def toString: String = name.toString
}

case class Name(name: String, namespace: Option[String] = None) {
	override def toString: String = namespace.fold(name)(ns => name + "." + ns)
}

object Name {
	def apply(name: String, namespace: String): Name = Name(name, Some(namespace))
}

case class Doc(doc: String)
case class Property(name: String, value: Json)
case class Properties(props: Seq[Property] = Seq.empty)
object Properties {
	val empty = Properties()
}

trait LogicalType {
	def name: String
}

case class Precision(precision: Int)
case class Scale(scale: Int)

object Schema {

	class Type(val name: String)

	case object Null    extends Type("null")
	case object Int     extends Type("int")
	case object Long    extends Type("long")
	case object Float   extends Type("float")
	case object Double  extends Type("double")
	case object Boolean extends Type("boolean")
	case object String  extends Type("string")
	case object Bytes   extends Type("bytes")
	case object Fixed   extends Type("fixed")
	case object Array   extends Type("array")
	case object Map     extends Type("map")
	case object Union   extends Type("union")
	case object Record  extends Type("record")
}


trait PrimitiveSchema extends Schema {
	final override def name: Name = super.name
}

trait NullSchema extends PrimitiveSchema {
	override def typeOf: Type = Null
}

trait IntSchema extends PrimitiveSchema {
	override def typeOf: Type = Int
}

trait LongSchema extends PrimitiveSchema {
	override def typeOf: Type = Long
}

trait FloatSchema extends PrimitiveSchema {
	override def typeOf: Type = Float
}

trait DoubleSchema extends PrimitiveSchema {
	override def typeOf: Type = Double
}

trait BooleanSchema extends PrimitiveSchema {
	override def typeOf: Type = Boolean
}

trait StringSchema extends PrimitiveSchema {
	override def typeOf: Type = String
}

trait BytesSchema extends Schema {
	override def typeOf: Type = Bytes
}

trait FixedSchema extends Schema {
	override def typeOf: Type = Fixed
	def doc: Option[String] = None
	def size: Int
}

trait ArraySchema extends Schema {
	override def typeOf: Type = Schema.Array
	def elementSchema: Schema
}

trait EnumSchema extends Schema {
	def doc: Option[String] = None
	def symbols: Seq[String]
}

trait MapSchema extends Schema {
	def typeOf: Type = Schema.Map
	def itemSchema: Schema
}

trait UnionSchema extends Schema {
	override def typeOf: Type = Schema.Union
	override def name: Name = Name(types.map(_.fullName).mkString("[", ", ", "]"))

	def types: Seq[Schema]
	protected[codec] lazy val typeMap: Map[String, Schema] = types.map(t => t.fullName -> t).toMap
}

trait RecordSchema extends Schema {
	override def typeOf: Type = Schema.Record
	def doc: Option[String] = None
	def fields: Seq[RecordSchema.Field]
}

object RecordSchema {

	case class Field(name: String, schema: Lazy[Schema], doc: Option[String], aliases: Seq[String], order: Option[Field.Order] = None, properties: Properties = Properties(), defaultValue: Option[DefaultValue[_]]) {
		override def toString: String = name + " :" + schema.value.name
	}

	case class FieldSettings(name: Option[String] = None, doc: Option[String] = None, aliases: Seq[String] = Seq.empty, order: Option[Field.Order] = None, properties: Properties = Properties(), defaultValue: Option[DefaultValue[_]] = None)
	
	object Field extends Dynamic {

		/**
		 * Field select operator
		 */
		trait :#[T, F]
		object :# {
			implicit def hf[T, F, O <: HList](implicit gen: LabelledGeneric.Aux[T, O], s: Selector[O, F]): :#[T, F] = new :#[T, F] {}
		}

		// Singleton implementation that delegates to Shapeless's 'Witness' impl
		def selectDynamic(tpeSelector: String): Any = macro shapeless.SingletonTypeMacros.witnessTypeImpl

		sealed trait Order
		object Order {
			object Ignore extends Order
			object Ascending extends Order
			object Descending extends Order
		}

		case class FieldName(name: String)
		case class Aliases(aliases: String*)
		case class Ordered(order: RecordSchema.Field.Order)
	}

	// TODO: Change codec to generic 'Codec[T]' type
	case class DefaultValue[T](value: T)(implicit val codec: Lazy[AvroCodec[T]])
}







