package com.hindog.codec

import scala.collection._

/*
 *    __   _         __         
 *   / /  (_)__  ___/ /__  ____
 *  / _ \/ / _ \/ _  / _ \/ _  /
 * /_//_/_/_//_/\_,_/\___/\_, / 
 *                       /___/
 */
class Resolver[T] {
	private val resolved: mutable.Map[String, T] = mutable.HashMap[String, T]()
	def resolve(name: String, value: => T): T = resolved.getOrElseUpdate(name, value)
	def get(name: String): Option[T] = resolved.get(name)
}
