package eu.ha3.katastrophe.monad.ws

import eu.ha3.katastrophe.monad.Either
import eu.ha3.katastrophe.monad.SomeMessage
import java.time.LocalDate
import java.time.Period

interface IMessageRepository {
    fun getById(it: String): Either<Err, SomeMessage?>
    fun countBetween(startInclusive: LocalDate, endExclusive: LocalDate): Either<Err, Long>
}

class MessageRepository : IMessageRepository {
    override fun getById(it: String): Either<Err, SomeMessage?> = when (it) {
        "1992" -> Either.ret(SomeMessage("Hay", "1992 Message"))
        "9999" -> Either.Left(Err.UNKNOWN_ERROR_WITH_MESSAGE_REPOSITORY)
        else -> Either.ret(null)
    }

    override fun countBetween(startInclusive: LocalDate, endExclusive: LocalDate): Either<Err, Long> =
            Either.ret(Math.floor(Period.between(startInclusive, endExclusive).days * 1.5).toLong())
}