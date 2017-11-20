package eu.ha3.katastrophe.run_length_encoding

/**
 * (Default template)
 * Created on 2017-11-20
 *
 * @author Ha3
 */
internal class IterativeMutableRunLengthEncodingTest : AbstractRunLengthEncodingTest() {
    override fun rle(): RunLengthEncoding<Int> = IterativeMutableRunLengthEncoding()

    class IterativeMutableRunLengthEncoding<T>: RunLengthEncoding<T> {
        override fun runLengthEncodingOf(raws: List<T>): List<Item<T>> {
            val items: MutableList<Item<T>> = raws.map { Obj(it) }.toMutableList()
            encode(items)

            return items.toList()
        }

        private fun <T> encode(items: MutableList<Item<T>>) {
            if (items.size < 2) {
                return
            }

            var i = 0
            while (i < items.size - 1) {
                val lhs = items[i] // as (Run<T> | Obj)
                val rhs = items[i + 1] as Obj

                 when {
                    lhs is Run<T> && lhs.elt == rhs -> combine(lhs.increment(), items, i)
                    lhs is Obj && lhs == rhs -> combine(Run(lhs, 2), items, i)
                    else -> i++
                }
            }
        }

        private fun <T> combine(replacement: Item<T>, items: MutableList<Item<T>>, i: Int) {
            items[i] = replacement
            items.removeAt(i + 1)
        }
    }
}