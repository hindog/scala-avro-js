//package com.hindog.codec
//
//import com.hindog.codec.avro.AvroSchema
//import com.hindog.codec.schema.RecordSchema.Field.:\
//import com.hindog.codec.schema.{Doc, Properties, RecordSchema}
//import com.sun.scenario.Settings
//import shapeless.ops.hlist.Prepend
//import shapeless.ops.record.Selector
//import shapeless.{HList, LabelledGeneric}
//
///*
// *    __   _         __
// *   / /  (_)__  ___/ /__  ____
// *  / _ \/ / _ \/ _  / _ \/ _  /
// * /_//_/_/_//_/\_,_/\___/\_, /
// *                       /___/
// */
//package object schema extends LowPriorityImplicits {
//
//	implicit def numericSettings[T](implicit n: Numeric[T]) = Settings.Define[T].optional[Scale].optional[Precision]
//
//	implicit def combineFieldType[M[x] <: AvroSchema[x], T, W, FT, GenR <: HList, Out1 <: HList, Out2 <: HList](implicit gen: LabelledGeneric.Aux[T, GenR], s: Selector.Aux[GenR, W, FT], mb: Settings.Define.Aux[M[T] :\ W, Out1], tb: Settings.Define.Aux[FT, Out2], prepend: Prepend[Out1, Out2]): Define.Aux[M[T] :\ W, prepend.Out] = new Define[M[T] :\ W] {
//		override type TypesT = prepend.Out
//	}
//
//}
//
//trait LowPriorityImplicits {
//	implicit def schemaDefine[T, AS[x] <: AvroSchema[x], Out <: HList] = Settings.Define[AS[T]].optional[com.hindog.codec.schema.Name].optional[Doc].optional[Properties]
//	implicit def fieldDefine[T, AS[x] <: AvroSchema[x], W, Out <: HList](implicit gen: LabelledGeneric.Aux[T, Out], s: Selector[Out, W]) = Settings.Define.apply[AS[T] :\ W].optional[RecordSchema.Field.FieldName].optional[Doc].optional[RecordSchema.Field.Aliases].optional[RecordSchema.Field.Ordered]
//}
