package app.com.mapinevents.utils;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.view.View;

public class Utils {
    public static void hideView(final View view) {
        view.animate()
                .translationY(view.getHeight())
                .alpha(0.0f)
                .setDuration(500)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        view.setVisibility(View.GONE);
                    }
                });

    }

    public static void showView (View view) {
        view.setVisibility(View.VISIBLE);
        view.animate()
                .translationY(0)
                .alpha(1.0f)
                .setDuration(500)
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);

                    }
                });

    }

    public static String getEmojiByUnicode(int unicode) {
        return new String(Character.toChars(unicode));
    }
}
