package eu.ha3.katastrophe.monad.ws

import eu.ha3.katastrophe.monad.CountBetweenQuery
import eu.ha3.katastrophe.monad.Either
import eu.ha3.katastrophe.monad.FetchMessageQuery
import eu.ha3.katastrophe.monad.SomeMessage
import java.time.LocalDate

/**
 * (Default template)
 * Created on 2017-11-22
 *
 * @author Ha3
 */


interface IMessageService {
    fun fetchMessage(it: FetchMessageQuery): Either<Err, SomeMessage?>
    fun getCountBetween(it: CountBetweenQuery): Either<Err, Long>
}

class MessageService(private val messageRepository: IMessageRepository) : IMessageService {
    override fun fetchMessage(it: FetchMessageQuery): Either<Err, SomeMessage?> {
        return Either.ret<Err, FetchMessageQuery>(it)
                .bind(this::getMessageByIdFromRepository)
    }

    private fun getMessageByIdFromRepository(it: FetchMessageQuery): Either<Err, SomeMessage?> {
        return Either.ret<Err, FetchMessageQuery>(it)
                .map { it.id }
                .bind(messageRepository::getById)
    }

    override fun getCountBetween(it: CountBetweenQuery): Either<Err, Long> {
        val curriedCountBetween = { start: LocalDate ->
            { end: LocalDate ->
                messageRepository.countBetween(start, end)
            }
        }
        val curriedGenerator: (CountBetweenQuery) -> Either<Err, Long> = { curriedCountBetween(it.begin)(it.end) }
        val adapter: (CountBetweenQuery) -> Either<Err, Long> = { query: CountBetweenQuery ->
            messageRepository.countBetween(query.begin, query.end)
        }

        return Either.ret<Err, CountBetweenQuery>(it)
//                    .bind(test)
                .bind(curriedGenerator)
    }
}