package moe.wolfgirl.probejs.events;

import dev.latvian.mods.kubejs.event.KubeEvent;
import moe.wolfgirl.probejs.lang.snippet.Snippet;
import moe.wolfgirl.probejs.lang.snippet.SnippetDump;

import java.util.function.Consumer;

public class SnippetGenerationEventJS implements KubeEvent {

    private final SnippetDump dump;

    public SnippetGenerationEventJS(SnippetDump dump) {

        this.dump = dump;
    }

    public void create(String name, Consumer<Snippet> handler) {
        handler.accept(dump.snippet(name));
    }
}