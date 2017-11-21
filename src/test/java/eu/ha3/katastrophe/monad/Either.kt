package eu.ha3.katastrophe.monad

import eu.ha3.katastrophe.monad.Either.Left
import org.junit.jupiter.api.Test

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
        UNKNOWN_ERROR_WITH_MESSAGE_REPOSITORY(500)
    }

    data class RequestModel(val headers: List<Header>, val parameters: List<Parameter>)
    data class ResponseModel(val status: Int, val body: String)

    data class Header(val key: String, val values: List<String>)
    data class Parameter(val key: String, val value: String)
    data class FetchMessageQuery(val id: String)
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
    }

    class MessageRepository : IMessageRepository {
        override fun getById(it: String): Either<Err, SomeMessage?> = when (it) {
            "1992" -> Either.ret(SomeMessage("Hay", "1992 Message"))
            "9999" -> Either.Left(Err.UNKNOWN_ERROR_WITH_MESSAGE_REPOSITORY)
            else -> Either.ret(null)
        }
    }

    interface IUseCase {
        fun fetchMessage(it: FetchMessageQuery): Either<Err, SomeMessage?>
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
    }

    interface IController {
        fun processRequest(request: RequestModel): ResponseModel
    }

    class Controller(private val useCase: UseCase, authenticationService: AuthenticationService) : IController {
        private val controllerAuthenticationLogic: ControllerAuthenticationLogic

        override fun processRequest(request: RequestModel) = Either.ret<Err, RequestModel>(request)
                .verify(controllerAuthenticationLogic::checkAuthentication)
                .bind(this::asFetchMessageQuery)
                .bind(useCase::fetchMessage)
                .map(this::toResponse)
                .otherwise(this::toError)

        private fun asFetchMessageQuery(it: RequestModel): Either<Err, FetchMessageQuery> = Either.ret<Err, RequestModel>(it)
                .verify(this::validateRequestForMessageQuery)
                .map(this::requestToFetchMessageQuery)

        private fun validateRequestForMessageQuery(it: RequestModel): Err? = when {
            !it.parameters.any { it.key == "id" } -> Err.PARAMETER_MISSING_ID
            else -> null
        }

        private fun requestToFetchMessageQuery(it: RequestModel): FetchMessageQuery =
                FetchMessageQuery(it.parameters.first { it.key == "id" }.value)

        private fun toResponse(it: SomeMessage?): ResponseModel = when {
            it != null -> ResponseModel(200, it.author + ": " + it.content)
            else -> ResponseModel(404, "Not found")
        }

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