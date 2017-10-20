package com.hindog

/*
 *    __   _         __         
 *   / /  (_)__  ___/ /__  ____
 *  / _ \/ / _ \/ _  / _ \/ _  /
 * /_//_/_/_//_/\_,_/\___/\_, / 
 *                       /___/
 */
package object func {

	implicit class AnyExtensions[A](a: A) {
		def ifThen[B >: A](predicate: A => Boolean)(f: A => B): B = if (predicate(a)) f(a) else a
		def ifThen[B >: A](predicate: Boolean)(f: A => B): B = if (predicate) f(a) else a

		def ifThenElse[B >: A](predicate: A => Boolean)(ifTrue: A => B)(ifFalse: A => B): B = if (predicate(a)) ifTrue(a) else ifFalse(a)
		def ifThenElse[B >: A](predicate: Boolean)(ifTrue: A => B)(ifFalse: A => B): B = if (predicate) ifTrue(a) else ifFalse(a)
	}
}
