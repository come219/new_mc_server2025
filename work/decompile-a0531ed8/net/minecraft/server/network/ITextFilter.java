package net.minecraft.server.network;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public interface ITextFilter {

    ITextFilter DUMMY = new ITextFilter() {
        @Override
        public CompletableFuture<FilteredText> processStreamMessage(String s) {
            return CompletableFuture.completedFuture(FilteredText.passThrough(s));
        }

        @Override
        public CompletableFuture<List<FilteredText>> processMessageBundle(List<String> list) {
            return CompletableFuture.completedFuture((List) list.stream().map(FilteredText::passThrough).collect(ImmutableList.toImmutableList()));
        }
    };

    default void join() {}

    default void leave() {}

    CompletableFuture<FilteredText> processStreamMessage(String s);

    CompletableFuture<List<FilteredText>> processMessageBundle(List<String> list);
}
