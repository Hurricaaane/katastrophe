package eu.ha3.katastrophe.run_length_encoding

/**
 * (Default template)
 * Created on 2017-11-20
 *
 * @author Ha3
 */
interface RunLengthEncoding<T> {
    fun runLengthEncodingOf(raws: List<T>): List<Item<T>>
}

interface Item<T> {
    companion object {
        fun <T> of(vararg items: Item<T>): List<Item<T>> = listOf(*items)
    }
}

data class Run<T>(val elt: Obj<T>, val length: Int) : Item<T> {
    fun increment(): Run<T> = Run(elt, length + 1)
    override fun toString(): String = "{$elt, $length}"
}

data class Obj<T>(val raw: T) : Item<T> {
    override fun toString(): String = raw.toString()
}