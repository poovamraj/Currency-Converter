package com.poovam.currencyconverter

import android.view.View
import androidx.appcompat.widget.SearchView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.ViewAction
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.rule.ActivityTestRule
import com.poovam.currencyconverter.view.MainActivity
import com.poovam.currencyconverter.view.ResultCell
import org.hamcrest.Matcher
import org.hamcrest.core.AllOf.allOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith


/**
 * Instrumented test, which will execute on an Android device.
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {

    @Rule
    @JvmField
    val rule: ActivityTestRule<MainActivity> = ActivityTestRule(MainActivity::class.java)

    @Test
    fun enterAmountAndSearchForCurrency(){
        onView(withId(R.id.conversionValue)).perform(typeText("123"))
        onView(withId(R.id.searchView)).perform(click()).perform(typeSearchViewText("JPY"))
    }

    @Test
    fun ifSourceAndConvertedAreSameTheCurrencyValueShouldBeSameAsAmount(){
        onView(withId(R.id.conversionValue)).perform(typeText("123"))
        searchAndChangeSourceCurrency("JPY")
        onView(withId(R.id.searchView)).perform(typeSearchViewText("JPY"))
        onView(withId(R.id.currencyCode)).check(matches(withText("JPY")))
        onView(withId(R.id.currencyName)).check(matches(withText("Japanese Yen")))
        onView(withId(R.id.currencyValue)).check(matches(withText("123.000")))
    }

    @Test
    fun searchAndChangeSourceCurrency(){
        searchAndChangeSourceCurrency("JPY")
        onView(withId(R.id.sourceCurrencyCode)).check(matches(withText("JPY")))
    }

    @Test
    fun showEmptyUiInSourceCurrencySearch(){
        onView(withId(R.id.actionButton)).perform(click())
        onView(withId(R.id.sourceCurrencySearch)).perform(typeSearchViewText("EMPTY_TEST"))
        onView(withId(R.id.emptyInfo)).check(matches(isDisplayed()))
    }

    @Test
    fun checkRefreshIsWorking(){
        onView(withId(R.id.swipeRefresh)).perform(swipeDown()).check(matches(isEnabled()))
    }

    private fun searchAndChangeSourceCurrency(currencyCode: String){
        onView(withId(R.id.actionButton)).perform(click())
        onView(withId(R.id.sourceCurrencySearch)).perform(typeSearchViewText(currencyCode))
        onView(withId(R.id.searchList))
            .perform(RecyclerViewActions.actionOnItemAtPosition<ResultCell>(0, click()))
    }
}

//From - https://stackoverflow.com/questions/48037060/how-to-type-text-on-a-searchview-using-espresso
//since SearchView does not support typeText()
fun typeSearchViewText(text: String?): ViewAction? {
    return object : ViewAction {
        override fun getConstraints(): Matcher<View> {
            //Ensure that only apply if it is a SearchView and if it is visible.
            return allOf(isDisplayed(), isAssignableFrom(SearchView::class.java))
        }

        override fun perform(uiController: androidx.test.espresso.UiController?, view: View?) {
            (view as SearchView).setQuery(text, false)
        }

        override fun getDescription(): String {
            return "Change view text"
        }
    }
}