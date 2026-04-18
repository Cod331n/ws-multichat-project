package ru.geoderma.service.dispatcher.route.filter;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record FilterResult(boolean allowed,
                           String reason,
                           FilterAction action
) {

    @NotNull
    public static FilterResult allow() {
        return new FilterResult(true, null, FilterAction.CONTINUE);
    }

    @NotNull
    public static FilterResult skipRemaining() {
        return new FilterResult(true, null, FilterAction.SKIP_REMAINING);
    }

    @NotNull
    public static FilterResult block(final @Nullable String reason) {
        return new FilterResult(false, reason, FilterAction.BLOCK);
    }

    @NotNull
    public static FilterResult blockAndClose(final @Nullable String reason) {
        return new FilterResult(false, reason, FilterAction.BLOCK_AND_CLOSE);
    }

    public enum FilterAction {
        /**
         * Продолжить
         */
        CONTINUE,
        /**
         * Пропустить оставшиеся фильтры
         */
        SKIP_REMAINING,
        /**
         * Заблокировать
         */
        BLOCK,
        /**
         * Заблокировать и закрыть сессию
         */
        BLOCK_AND_CLOSE,
    }

}