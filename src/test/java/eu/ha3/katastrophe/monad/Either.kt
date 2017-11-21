package eu.ha3.katastrophe.monad

import eu.ha3.katastrophe.monad.Either.Left
import org.junit.jupiter.api.Test
import java.time.DateTimeException
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter
import java.time.temporal.UnsupportedTemporalTypeException

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
        is Either.Left<L, R> -> Left<L, Rp>(this.l)
        is Either.Right<L, R> -> f(this.r)
    }

    infix fun <Rp> map(f: (R) -> (Rp)): Either<L, Rp> = when (this) {
        is Either.Left<L, R> -> Left<L, Rp>(this.l)
        is Either.Right<L, R> -> Either.Right(f(this.r))
    }

    infix fun <Rp> seq(e: Either<L, Rp>): Either<L, Rp> = e

    infix fun otherwise(t: (L) -> (R)): R = when (this) {
        is Either.Left<L, R> -> t(this.l)
        is Either.Right<L, R> -> this.r
    }

    infix fun verify(leftGenerator: (R) -> (L?)): Either<L, R> = when (this) {
        is Either.Left<L, R> -> Left<L, R>(this.l)
        is Either.Right<L, R> -> leftGenerator(this.r)?.let { Left<L, R>(it) } ?: Right(this.r)
    }

    infix fun verifyMonad(leftMonadGenerator: (R) -> (Either<L, *>)): Either<L, R> = when (this) {
        is Either.Left<L, R> -> Left<L, R>(this.l)
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
class EitherTest {
    enum class Err(val status: Int) {
        AUTHORIZATION_HEADER_MISSING(401),
        TOO_MANY_AUTHORIZATION_HEADERS(400),
        AUTHORIZATION_HEADER_HAS_TRAILING_WHITESPACE(400),
        NOT_AUTHENTICATED(401),
        NOT_BEARER_TOKEN(400),
        TOKEN_HAS_TRAILING_WHITESPACE(400),
        PARAMETER_MISSING_ID(400),
        UNKNOWN_ERROR_WITH_MESSAGE_REPOSITORY(500),
        DATE_FORMAT_IS_INVALID(400),
        PARAMETER_MISSING_BEGIN_DATE(400),
        PARAMETER_MISSING_END_DATE(400)
    }

    data class RequestModel(val headers: List<Header>, val parameters: List<Parameter>, val method: String?) {
        constructor(headers: List<Header>, parameters: List<Parameter>) : this(headers, parameters, null)
    }
    data class ResponseModel(val status: Int, val body: String)

    data class Header(val key: String, val values: List<String>)
    data class Parameter(val key: String, val value: String)
    data class FetchMessageQuery(val id: String)
    data class CountBetweenQuery(val begin: LocalDate, val end: LocalDate)
    data class SomeMessage(val author: String, val content: String)

    @Test
    fun either() {
        val controller = Controller(UseCase(MessageRepository()), AuthenticationService())

        println(controller.processRequest(RequestModel(
                headers = listOf(),
                parameters = listOf(Parameter("id", "1992"))
        )))
        println(controller.processRequest(RequestModel(
                headers = listOf(Header("Authorization ", listOf("Bearer hello"))),
                parameters = listOf(Parameter("id", "1992"))
        )))
        println(controller.processRequest(RequestModel(
                headers = listOf(Header("Authorization", listOf("Bearer hello", "Bearer bad"))),
                parameters = listOf(Parameter("id", "1992"))
        )))
        println(controller.processRequest(RequestModel(
                headers = listOf(Header("Authorization", listOf("Berer not a good token"))),
                parameters = listOf(Parameter("id", "1992"))
        )))
        println(controller.processRequest(RequestModel(
                headers = listOf(Header("Authorization", listOf("Bearer unknown token"))),
                parameters = listOf(Parameter("id", "1992"))
        )))
        println(controller.processRequest(RequestModel(
                headers = listOf(Header("Authorization", listOf("Bearer hello "))),
                parameters = listOf(Parameter("id", "1992"))
        )))
        println(controller.processRequest(RequestModel(
                headers = listOf(Header("Authorization", listOf("Bearer hello"))),
                parameters = listOf(Parameter("id", "1992"))
        )))
        println(controller.processRequest(RequestModel(
                headers = listOf(Header("Authorization", listOf("Bearer hello"))),
                parameters = listOf(Parameter("id", "1111"))
        )))
        println(controller.processRequest(RequestModel(
                headers = listOf(Header("Authorization", listOf("Bearer hello"))),
                parameters = listOf(Parameter("id", "9999"))
        )))
        println(controller.processRequest(RequestModel(
                headers = listOf(Header("Authorization", listOf("Bearer hello"))),
                parameters = listOf(Parameter("id", "9999"))
        )))
        println(controller.processRequest(RequestModel(
                headers = listOf(Header("Authorization", listOf("Bearer hello"))),
                parameters = listOf(Parameter("begin", "2012-01-01")),
                method = "countBetween"
        )))
        println(controller.processRequest(RequestModel(
                headers = listOf(Header("Authorization", listOf("Bearer hello"))),
                parameters = listOf(Parameter("end", "2012-04-20")),
                method = "countBetween"
        )))
        println(controller.processRequest(RequestModel(
                headers = listOf(Header("Authorization", listOf("Bearer hello"))),
                parameters = listOf(Parameter("begin", "2012-01-40"), Parameter("end", "2012-04-20")),
                method = "countBetween"
        )))
        println(controller.processRequest(RequestModel(
                headers = listOf(Header("Authorization", listOf("Bearer hello"))),
                parameters = listOf(Parameter("begin", "2012-01-01"), Parameter("end", "2012-04-40")),
                method = "countBetween"
        )))
        println(controller.processRequest(RequestModel(
                headers = listOf(Header("Authorization", listOf("Bearer hello"))),
                parameters = listOf(Parameter("begin", "2012-01-01"), Parameter("end", "2012-04-20")),
                method = "countBetween"
        )))
    }

    interface IIAuthenticationService {
        fun checkIfAuthenticated(it: String): Err?
    }

    class AuthenticationService : IIAuthenticationService {
        override fun checkIfAuthenticated(it: String): Err? = when (it) {
            "hello" -> null
            else -> Err.NOT_AUTHENTICATED
        }
    }

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

    interface IUseCase {
        fun fetchMessage(it: FetchMessageQuery): Either<Err, SomeMessage?>
        fun getCountBetween(it: CountBetweenQuery): Either<Err, Long>
    }

    class UseCase(private val messageRepository: MessageRepository) : IUseCase {
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

    interface IController {
        fun processRequest(request: RequestModel): ResponseModel
    }

    class Controller(private val useCase: UseCase, authenticationService: AuthenticationService) : IController {
        private val controllerAuthenticationLogic: ControllerAuthenticationLogic

        override fun processRequest(request: RequestModel) = Either.ret<Err, RequestModel>(request)
                .verify(controllerAuthenticationLogic::checkAuthentication)
                .bind(this::resolveAndExecuteAction)
                .otherwise(this::toError)

        private fun resolveAndExecuteAction(request: RequestModel): Either<Err, ResponseModel> {
            val ret = Either.ret<Err, RequestModel>(request)

            return request.method?.let {
                when (it) {
                    "countBetween" -> ret.bind(this::daysBetweenMethod)
                    else -> ret.bind(this::defaultMethod)
                }
            } ?: ret.bind(this::defaultMethod)
        }

        private fun daysBetweenMethod(request: RequestModel): Either<Err, ResponseModel> = Either.ret<Err, RequestModel>(request)
                .bind(this::asCountBetweenQuery)
                .bind(useCase::getCountBetween)
                .map(this::countAsResponse)

        private fun asCountBetweenQuery(it: RequestModel): Either<Err, CountBetweenQuery> = Either.ret<Err, RequestModel>(it)
                .verify(this::validateRequestForCountBetweenQuery)
                .bind(this::requestCountBetweenQuery)

        private fun requestCountBetweenQuery(it: RequestModel): Either<Err, CountBetweenQuery> {
            try {
                return Either.ret(CountBetweenQuery(extractLocalDate(it, "begin"), extractLocalDate(it, "end")))

            } catch (e: UnsupportedTemporalTypeException) {
                return Left(Err.DATE_FORMAT_IS_INVALID)

            } catch (e: DateTimeException) {
                return Left(Err.DATE_FORMAT_IS_INVALID)
            }
        }

        private fun extractLocalDate(it: RequestModel, param: String) =
                LocalDate.from(DateTimeFormatter.ISO_DATE.parse(it.parameters.first { it.key == param }.value))

        private fun validateRequestForCountBetweenQuery(it: RequestModel): Err? = when {
            !it.parameters.any { it.key == "begin" } -> Err.PARAMETER_MISSING_BEGIN_DATE
            !it.parameters.any { it.key == "end" } -> Err.PARAMETER_MISSING_END_DATE
            else -> null
        }

        private fun defaultMethod(request: RequestModel): Either<Err, ResponseModel> = Either.ret<Err, RequestModel>(request)
                        .bind(this::asFetchMessageQuery)
                        .bind(useCase::fetchMessage)
                        .map(this::someMessageAsResponse)

        private fun asFetchMessageQuery(it: RequestModel): Either<Err, FetchMessageQuery> = Either.ret<Err, RequestModel>(it)
                .verify(this::validateRequestForFetchMessageQuery)
                .map(this::requestToFetchMessageQuery)

        private fun validateRequestForFetchMessageQuery(it: RequestModel): Err? = when {
            !it.parameters.any { it.key == "id" } -> Err.PARAMETER_MISSING_ID
            else -> null
        }

        private fun requestToFetchMessageQuery(it: RequestModel): FetchMessageQuery =
                FetchMessageQuery(it.parameters.first { it.key == "id" }.value)

        private fun someMessageAsResponse(it: SomeMessage?): ResponseModel = when {
            it != null -> ResponseModel(200, it.author + ": " + it.content)
            else -> ResponseModel(404, "Not found")
        }
        private fun countAsResponse(it: Long): ResponseModel = ResponseModel(200, it.toString())


        private fun toError(it: Err): ResponseModel = ResponseModel(it.status, it.name)

        class ControllerAuthenticationLogic(private val authenticationService: AuthenticationService) {
            fun checkAuthentication(me: RequestModel): Err? = Either.ret<Err, RequestModel>(me)
                    .verify(this::validateAuthHeader)
                    .map(this::extractAuthorizationHeader)
                    .bind(this::validateTokenType)
                    .map(this::extractBearerToken)
                    .verify(authenticationService::checkIfAuthenticated)
                    .left()

            private fun validateAuthHeader(it: RequestModel): Err? = when {
                it.headers.any { it.key.startsWith("Authorization ") } -> Err.AUTHORIZATION_HEADER_HAS_TRAILING_WHITESPACE
                !it.headers.any { it.key == "Authorization" } -> Err.AUTHORIZATION_HEADER_MISSING
                it.headers.first { it.key == "Authorization" }.values.size > 1 -> Err.TOO_MANY_AUTHORIZATION_HEADERS
                else -> null
            }

            private fun extractAuthorizationHeader(it: RequestModel): String = it.headers.filter { it.key == "Authorization" }.first().values.first()

            private fun validateTokenType(it: String): Either<Err, String> = when {
                !it.startsWith("Bearer ") -> Left(Err.NOT_BEARER_TOKEN)
                it.trim() != it -> Left(Err.TOKEN_HAS_TRAILING_WHITESPACE)
                else -> Either.ret(it)
            }
            private fun extractBearerToken(it: String): String = it.substring("Bearer ".length)

        }

        init {
            this.controllerAuthenticationLogic = ControllerAuthenticationLogic(authenticationService)
        }
    }
}