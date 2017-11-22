package eu.ha3.katastrophe.monad.ws

import eu.ha3.katastrophe.monad.Err
import eu.ha3.katastrophe.monad.Possibly
import eu.ha3.katastrophe.monad.SomeMessage
import java.time.LocalDate
import java.time.Period

interface IMessageRepository {
    fun getById(it: String): Possibly<SomeMessage?>
    fun countBetween(startInclusive: LocalDate, endExclusive: LocalDate): Possibly<Long>
}

class MessageRepository : IMessageRepository {
    override fun getById(it: String): Possibly<SomeMessage?> = when (it) {
        "1992" -> Possibly.ret(SomeMessage("Hay", "1992 Message"))
        "9999" -> Possibly.Fail(Err.UNKNOWN_ERROR_WITH_MESSAGE_REPOSITORY)
        else -> Possibly.ret(null)
    }

    override fun countBetween(startInclusive: LocalDate, endExclusive: LocalDate): Possibly<Long> =
            Possibly.ret(Math.floor(Period.between(startInclusive, endExclusive).days * 1.5).toLong())
}