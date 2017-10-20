### Avro support for Scala and Scala.js ###

**This library is a work-in-progress, there have been NO official releases yet!**

## Supported Features

* Generate Avro schemas and codecs at compile-time using macros
* Common API to use across JVM and Scala.js platforms (eg: `AvroSchema` and `AvroCodec`)
* Recursive types!
* Scala.js integration using [Avro for JavaScript](https://github.com/mtth/avsc) library
 
## Type Mappings

|Scala Type|Avro Type|
|----------|---------|
|Boolean|boolean|
|Array[Byte]|bytes|
|java.nio.ByteBuffer|bytes|
|String|string or fixed|
|Int|int|
|Long|long|
|Double|double|
|Float|float|
|Java Enums|enum|
|sealed trait T|union|
|sealed trait with only case objects|enum|
|Array[T]|array|
|List[T]\*|array|
|Seq[T]\*|array|
|Iterable[T]\*|array|
|Set[T]\*|array|
|Map[String, T]\*|map|
|Map[K, V]\*|array|
|Option[T]|union:null,T|
|T|record|

\* Supports both `immutable` and `mutable` collection types, as well as any other type with a `CanBuildFrom` conversion from `Traversable`, for example: `mutable.ListBuffer[T]` or `immutable.HashSet[T]`.
 
## Custom Type Mappings   

Custom type mappings can be created by providing an implicit value for `AvroCodec[T]`
