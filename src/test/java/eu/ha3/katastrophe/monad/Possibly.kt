package eu.ha3.katastrophe.monad

/**
 * https://kotlin.link/articles/Exploring-an-Either-Monad-in-Kotlin.html
 * Created on 2017-11-20
 *
 * @author Ha3
 */
sealed class Possibly<V> {
    class Fail<V>(val fail: Err) : Possibly<V>() {
        override fun toString(): String = "Fail $fail"
    }

    class Vex<V>(val vex: V) : Possibly<V>() {
        override fun toString(): String = "Vex $vex"
    }

    infix fun <Vp> bind(f: (V) -> (Possibly<Vp>)): Possibly<Vp> = when (this) {
        is Possibly.Fail<V> -> Fail(this.fail)
        is Possibly.Vex<V> -> f(this.vex)
    }

    infix fun <Vp> map(f: (V) -> (Vp)): Possibly<Vp> = when (this) {
        is Possibly.Fail<V> -> Fail(this.fail)
        is Possibly.Vex<V> -> Possibly.Vex(f(this.vex))
    }

    infix fun <Vp> seq(e: Possibly<Vp>): Possibly<Vp> = e

    infix fun otherwise(t: (Err) -> (V)): V = when (this) {
        is Possibly.Fail<V> -> t(this.fail)
        is Possibly.Vex<V> -> this.vex
    }

    infix fun verify(leftGenerator: (V) -> (Err?)): Possibly<V> = when (this) {
        is Possibly.Fail<V> -> Fail(this.fail)
        is Possibly.Vex<V> -> leftGenerator(this.vex)?.let { Fail<V>(it) } ?: Vex(this.vex)
    }

    infix fun verifyMonad(leftMonadGenerator: (V) -> (Possibly<*>)): Possibly<V> = when (this) {
        is Possibly.Fail<V> -> Fail(this.fail)
        is Possibly.Vex<V> -> this.verify { leftMonadGenerator(this.vex).left() }
    }

    fun left(): Err? = when (this) {
        is Possibly.Fail<V> -> this.fail
        is Possibly.Vex<V> -> null
    }

    companion object {
        fun <V> ret(a: V) = Vex(a)
    }
}