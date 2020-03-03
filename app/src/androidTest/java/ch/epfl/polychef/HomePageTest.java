package ch.epfl.polychef;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import androidx.test.espresso.contrib.DrawerActions;
import static androidx.test.espresso.contrib.DrawerMatchers.isClosed;
import static androidx.test.espresso.contrib.DrawerMatchers.isOpen;
import androidx.test.espresso.contrib.NavigationViewActions;
import static androidx.test.espresso.intent.Intents.intended;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent;
import androidx.test.espresso.intent.rule.IntentsTestRule;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class HomePageTest {
    @Rule
    public IntentsTestRule<HomePage> intentsTestRule = new IntentsTestRule<>(HomePage.class);

    @Test
    public void buttonTextIsLogout() {
        onView(withId(R.id.logButton)).check(matches(withText("Log out")));
    }

    @Test
    public void onClickLogoutGoToEntryPage() {
        onView(withId(R.id.logButton)).perform(click());
        intended(hasComponent(EntryPage.class.getName()));
    }

    @Test
    public void onClickHomeGoesToHome() {
        testNavButton(R.id.nav_home, R.id.homeFragment);
    }

    @Test
    public void onClickFavGoesToFav() {
        testNavButton(R.id.nav_fav, R.id.favouritesFragment);
    }

    @Test
    public void onClickSubscribersGoesToSubscribers() {
        testNavButton(R.id.nav_subscribers, R.id.subscribersFragment);
    }

    @Test
    public void onClickSubscriptionsGoesToSubscriptions() {
        testNavButton(R.id.nav_subscriptions, R.id.subscriptionsFragment);
    }

    @Test
    public void appNavCanBeOpened() {
        onView(withId(R.id.drawer)).perform(DrawerActions.open());
        onView(withId(R.id.drawer)).check(matches(isOpen()));
        onView(withId(R.id.drawer)).perform(DrawerActions.close());
        onView(withId(R.id.drawer)).check(matches(isClosed()));
    }

    private void testNavButton(int id_button, int id_fragment) {
        onView(withId(R.id.drawer)).perform(DrawerActions.open());
        onView(withId(R.id.navigationView)).perform(NavigationViewActions.navigateTo(id_button));
        onView(withId(id_fragment)).check(matches(isDisplayed()));
    }
}
