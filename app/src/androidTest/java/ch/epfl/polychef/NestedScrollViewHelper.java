package ch.epfl.polychef;

import android.view.View;
import android.view.ViewParent;
import android.widget.FrameLayout;

import androidx.core.widget.NestedScrollView;
import androidx.test.espresso.PerformException;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.espresso.util.HumanReadables;

import org.hamcrest.Matcher;
import org.hamcrest.Matchers;

import static androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA;

/**
 * Helper class https://stackoverflow.com/a/46037284/11829577 for {@code NestedScrollView}.
 */
public class NestedScrollViewHelper {
    public static ViewAction nestedScrollTo() {
        return new NestedScrollViewAction();
    }

    private static View findFirstParentLayoutOfClass(View view, Class<? extends View> parentClass) {
        ViewParent parent = new FrameLayout(view.getContext());
        ViewParent incrementView = null;
        int level = 0;
        while (parent != null && !(parent.getClass() == parentClass)) {
            if (level == 0) {
                parent = findParent(view);
            } else {
                parent = findParent(incrementView);
            }
            incrementView = parent;
            level++;
        }
        return (View) parent;
    }

    private static ViewParent findParent(View view) {
        return view.getParent();
    }

    private static ViewParent findParent(ViewParent view) {
        return view.getParent();
    }

    private static class NestedScrollViewAction implements ViewAction {

        @Override
        public Matcher<View> getConstraints() {
            return Matchers.allOf(
                    isDescendantOfA(isAssignableFrom(NestedScrollView.class)),
                    ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE));
        }

        @Override
        public String getDescription() {
            return "View is not NestedScrollView";
        }

        @Override
        public void perform(UiController uiController, View view) {
            try {
                NestedScrollView nestedScrollView = (NestedScrollView)
                        findFirstParentLayoutOfClass(view, NestedScrollView.class);
                if (nestedScrollView != null) {
                    nestedScrollView.scrollTo(0, view.getTop());
                } else {
                    throw new Exception("Unable to find NestedScrollView parent.");
                }
            } catch (Exception e) {
                throw new PerformException.Builder().withActionDescription(this.getDescription())
                        .withViewDescription(HumanReadables.describe(view)).withCause(e)
                        .build();
            }
            uiController.loopMainThreadUntilIdle();
        }

    }
}
