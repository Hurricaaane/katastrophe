package eu.ha3.katastrophe.monad.ws

import eu.ha3.katastrophe.monad.CountBetweenQuery
import eu.ha3.katastrophe.monad.FetchMessageQuery
import eu.ha3.katastrophe.monad.Perhaps
import eu.ha3.katastrophe.monad.SomeMessage
import java.time.LocalDate

/**
 * (Default template)
 * Created on 2017-11-22
 *
 * @author Ha3
 */


interface IMessageService {
    fun fetchMessage(it: FetchMessageQuery): Perhaps<SomeMessage?>
    fun getCountBetween(it: CountBetweenQuery): Perhaps<Long>
}

class MessageService(private val messageRepository: IMessageRepository) : IMessageService {
    override fun fetchMessage(it: FetchMessageQuery): Perhaps<SomeMessage?> {
        return Perhaps.ret(it)
                .bind(this::getMessageByIdFromRepository)
    }

    private fun getMessageByIdFromRepository(it: FetchMessageQuery): Perhaps<SomeMessage?> {
        return Perhaps.ret(it)
                .map { it.id }
                .bind(messageRepository::getById)
    }

    override fun getCountBetween(it: CountBetweenQuery): Perhaps<Long> {
        val curriedCountBetween = { start: LocalDate ->
            { end: LocalDate ->
                messageRepository.countBetween(start, end)
            }
        }
        val curriedGenerator: (CountBetweenQuery) -> Perhaps<Long> = { curriedCountBetween(it.begin)(it.end) }
        val adapter: (CountBetweenQuery) -> Perhaps<Long> = { query: CountBetweenQuery ->
            messageRepository.countBetween(query.begin, query.end)
        }

        return Perhaps.ret(it)
//                    .bind(test)
                .bind(curriedGenerator)
    }
}