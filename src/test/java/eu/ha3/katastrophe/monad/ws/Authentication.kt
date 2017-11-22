package eu.ha3.katastrophe.monad.ws

/**
 * (Default template)
 * Created on 2017-11-22
 *
 * @author Ha3
 */

interface IAuthenticationService {
    fun checkIfAuthenticated(it: String): Err?
}

class AuthenticationService : IAuthenticationService {
    override fun checkIfAuthenticated(it: String): Err? = when (it) {
        "hello" -> null
        else -> Err.NOT_AUTHENTICATED
    }
}