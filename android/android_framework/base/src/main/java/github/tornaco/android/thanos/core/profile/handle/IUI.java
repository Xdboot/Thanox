package github.tornaco.android.thanos.core.profile.handle;

import github.tornaco.android.thanos.core.annotation.NonNull;
import github.tornaco.android.thanos.core.annotation.Nullable;

@HandlerName("ui")
interface IUI {

    void showShortToast(@NonNull Object msg);

    void showLongToast(@NonNull Object msg);

    void showDialog(@Nullable String title,
                    @NonNull String msg,
                    @Nullable String yes);

    void showNotification(
            @NonNull String notificationTag,
            @NonNull String title,
            @NonNull String msg,
            boolean important);

    void cancelNotification(@NonNull String notificationTag);

    void findAndClickViewByText(@NonNull String text);

    void findAndClickViewByText(@NonNull String text, @Nullable String componentNameShortString);

    void findAndClickViewById(@NonNull String id);

    void findAndClickViewById(@NonNull String id, @Nullable String componentNameShortString);

    void clickDelay(int x, int y, long delayMills);
}
