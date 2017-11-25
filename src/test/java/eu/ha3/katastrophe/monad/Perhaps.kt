package eu.ha3.katastrophe.monad

/**
 * https://kotlin.link/articles/Exploring-an-Either-Monad-in-Kotlin.html
 * Created on 2017-11-20
 *
 * @author Ha3
 */
sealed class Perhaps<V> {
    class Fail<V>(val fail: Err) : Perhaps<V>() {
        override fun toString(): String = "Fail $fail"
    }

    class Vex<V>(val vex: V) : Perhaps<V>() {
        override fun toString(): String = "Vex $vex"
    }

    infix fun <Vp> bind(f: (V) -> (Perhaps<Vp>)): Perhaps<Vp> = when (this) {
        is Perhaps.Fail<V> -> Fail(this.fail)
        is Perhaps.Vex<V> -> f(this.vex)
    }

    infix fun <Vp> applying(f: (Perhaps<(V) -> (Vp)>)): Perhaps<Vp> = when (this) {
        is Perhaps.Fail<V> -> Fail(this.fail)
        is Perhaps.Vex<V> -> when (f) {
            is Perhaps.Fail<(V) -> (Vp)> -> Fail(f.fail)
            is Perhaps.Vex<(V) -> (Vp)> -> Perhaps.ret(f.vex(this.vex))
        }
    }

    infix fun <Vp> map(f: (V) -> (Vp)): Perhaps<Vp> = when (this) {
        is Perhaps.Fail<V> -> Fail(this.fail)
        is Perhaps.Vex<V> -> Perhaps.Vex(f(this.vex))
    }

    infix fun <Vp> seq(e: Perhaps<Vp>): Perhaps<Vp> = e

    infix fun otherwise(t: (Err) -> (V)): V = when (this) {
        is Perhaps.Fail<V> -> t(this.fail)
        is Perhaps.Vex<V> -> this.vex
    }

    infix fun verify(leftGenerator: (V) -> (Err?)): Perhaps<V> = when (this) {
        is Perhaps.Fail<V> -> Fail(this.fail)
        is Perhaps.Vex<V> -> leftGenerator(this.vex)?.let { Fail<V>(it) } ?: Vex(this.vex)
    }

    infix fun verifyMonad(leftMonadGenerator: (V) -> (Perhaps<*>)): Perhaps<V> = when (this) {
        is Perhaps.Fail<V> -> Fail(this.fail)
        is Perhaps.Vex<V> -> this.verify { leftMonadGenerator(this.vex).left() }
    }

    fun left(): Err? = when (this) {
        is Perhaps.Fail<V> -> this.fail
        is Perhaps.Vex<V> -> null
    }

    companion object {
        fun <V> ret(a: V) = Vex(a)
    }
}