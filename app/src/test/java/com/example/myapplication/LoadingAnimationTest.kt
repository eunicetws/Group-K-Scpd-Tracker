package com.example.myapplication

import com.example.myapplication.ui.components.LoadingAnimation
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class LoadingAnimationTest {

    @Test
    fun animalLoops() { // duck loop to goose then back
        val visibleCounts = (0 until LoadingAnimation.CHANGE)
            .map { step -> LoadingAnimation.animalCount(step) }
        assertEquals(listOf(0, 1, 2, 3, 2, 1), visibleCounts)
    }

    @Test
    fun animalLoopsBack() { // goose to duck
        for (step in 0 until LoadingAnimation.CHANGE) {
            assertEquals(
                LoadingAnimation.animalCount(step),
                LoadingAnimation.animalCount(step + LoadingAnimation.CHANGE)
            )
        }
    }

    @Test
    fun countOfThree() { //only 3 goose and duck appear
        for (step in 0..100) {
            val visibleCount = LoadingAnimation.animalCount(step)
            assertTrue(
                "Step $step showed $visibleCount animals",
                visibleCount in 0..LoadingAnimation.ANIMAL_COUNT
            )
        }
    }
}
