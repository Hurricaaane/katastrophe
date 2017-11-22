package eu.ha3.katastrophe.monad

/**
 * https://kotlin.link/articles/Exploring-an-Either-Monad-in-Kotlin.html
 * Created on 2017-11-20
 *
 * @author Ha3
 */
sealed class Either<L, R> {
    class Left<L, R>(val l: L) : Either<L, R>() {
        override fun toString(): String = "Left $l"
    }

    class Right<L, R>(val r: R) : Either<L, R>() {
        override fun toString(): String = "Right $r"
    }

    infix fun <Rp> bind(f: (R) -> (Either<L, Rp>)): Either<L, Rp> = when (this) {
        is Either.Left<L, R> -> Left(this.l)
        is Either.Right<L, R> -> f(this.r)
    }

    infix fun <Rp> map(f: (R) -> (Rp)): Either<L, Rp> = when (this) {
        is Either.Left<L, R> -> Left(this.l)
        is Either.Right<L, R> -> Either.Right(f(this.r))
    }

    infix fun <Rp> seq(e: Either<L, Rp>): Either<L, Rp> = e

    infix fun otherwise(t: (L) -> (R)): R = when (this) {
        is Either.Left<L, R> -> t(this.l)
        is Either.Right<L, R> -> this.r
    }

    infix fun verify(leftGenerator: (R) -> (L?)): Either<L, R> = when (this) {
        is Either.Left<L, R> -> Left(this.l)
        is Either.Right<L, R> -> leftGenerator(this.r)?.let { Left<L, R>(it) } ?: Right(this.r)
    }

    infix fun verifyMonad(leftMonadGenerator: (R) -> (Either<L, *>)): Either<L, R> = when (this) {
        is Either.Left<L, R> -> Left(this.l)
        is Either.Right<L, R> -> this.verify { leftMonadGenerator(this.r).left() }
    }

    fun left(): L? = when (this) {
        is Either.Left<L, R> -> this.l
        is Either.Right<L, R> -> null
    }

    companion object {
        fun <L, R> ret(a: R) = Right<L, R>(a)
    }
}