package eu.ha3.katastrophe.monad.ws

import eu.ha3.katastrophe.monad.Err
import eu.ha3.katastrophe.monad.Perhaps
import eu.ha3.katastrophe.monad.SomeMessage
import java.time.LocalDate
import java.time.Period

interface IMessageRepository {
    fun getById(it: String): Perhaps<SomeMessage?>
    fun countBetween(startInclusive: LocalDate, endExclusive: LocalDate): Perhaps<Long>
}

class MessageRepository : IMessageRepository {
    override fun getById(it: String): Perhaps<SomeMessage?> = when (it) {
        "1992" -> Perhaps.ret(SomeMessage("Hay", "1992 Message"))
        "9999" -> Perhaps.Fail(Err.UNKNOWN_ERROR_WITH_MESSAGE_REPOSITORY)
        else -> Perhaps.ret(null)
    }

    override fun countBetween(startInclusive: LocalDate, endExclusive: LocalDate): Perhaps<Long> =
            Perhaps.ret(Math.floor(Period.between(startInclusive, endExclusive).days * 1.5).toLong())
}