package eu.ha3.katastrophe.run_length_encoding

/**
 * (Default template)
 * Created on 2017-11-20
 *
 * @author Ha3
 */
internal class FunctionalMutableFoldRunLengthEncodingTest : AbstractRunLengthEncodingTest() {
    override fun rle(): RunLengthEncoding<Int> = FunctionalMutableFoldRunLengthEncoding()

    class FunctionalMutableFoldRunLengthEncoding<T>: RunLengthEncoding<T> {
        override fun runLengthEncodingOf(raws: List<T>): List<Item<T>> = raws
                .fold<T, MutableList<Item<T>>>(mutableItemsOf()) { acc, obj -> encodingFold(acc, obj) }
                .toList()

        private fun <T> encodingFold(acc: MutableList<Item<T>>, rhs: T): MutableList<Item<T>> {
            val lhs: Item<T> by lazy { acc.last() }

            return when {
                acc.isEmpty() -> append(acc, rhs)
                lhs is Run<T> && lhs.elt.raw == rhs -> replaceTail(acc, lhs.increment())
                lhs is Obj && lhs.raw == rhs -> replaceTail(acc, Run(lhs, 2))
                else -> append(acc, rhs)

            } as MutableList<Item<T>>
        }

        private fun <T> replaceTail(acc: MutableList<Item<T>>, lhs: Run<T>): List<Item<T>> {
            acc.removeAt(acc.size - 1)
            acc.add(lhs)
            return acc
        }

        private fun <T> append(acc: MutableList<Item<T>>, rhs: T): MutableList<Item<T>> {
            acc.add(Obj(rhs))
            return acc
        }

        private fun <T> mutableItemsOf(vararg items: Item<T>): MutableList<Item<T>> = mutableListOf(*items)
    }
}