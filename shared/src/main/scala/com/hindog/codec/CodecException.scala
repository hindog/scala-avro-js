package com.hindog.codec

import scala.collection._
import scala.collection.JavaConversions._
/*
 *    __   _         __         
 *   / /  (_)__  ___/ /__  ____
 *  / _ \/ / _ \/ _  / _ \/ _  /
 * /_//_/_/_//_/\_,_/\___/\_, / 
 *                       /___/
 */
class CodecException private (message: String, cause: Throwable) extends RuntimeException(message, cause) {
	setStackTrace(getStackTrace.drop(1))
}

object CodecException {
	def apply(message: String) = new CodecException(message, null)
	def apply(message: String, cause: Throwable) = new CodecException(message, cause)
}
