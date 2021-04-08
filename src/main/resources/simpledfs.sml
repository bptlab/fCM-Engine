exception Violating of CPNToolsModel.state
fun combinator (h2, h1) = Word.<<(h1, 0w2) + h1 + h2 + 0w17
val hash = CPNToolsHashFunction combinator
fun none _ = false
fun dead (_, events) = List.null events

fun dfs predicate states =
let fun equals (a, b) = a = b
	val storage = HashTable.mkTable (hash, equals) (1000, LibBase.NotFound)
	fun dfs'' state [] = ()
	  | dfs'' state (event::events) = let val successors = CPNToolsModel.nextStates (state, event)
										  val _ = dfs' successors
									  in dfs'' state events
									  end
	and dfs' [] = ()
	  | dfs' ((state, events)::rest) = if Option.isSome (HashTable.find storage state)
									   then dfs' rest
									   else let val _ = HashTable.insert storage (state, ())
									    val violates = predicate (state, events)
										in if violates
											then raise Violating state
											else (dfs'' state events; dfs' rest)
										end
in (dfs' states; (NONE, storage)) handle Violating state => (SOME state, storage)
end