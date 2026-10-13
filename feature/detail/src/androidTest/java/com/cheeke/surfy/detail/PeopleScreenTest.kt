package com.cheeke.surfy.detail

import androidx.compose.runtime.getValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.cheeke.surfy.analytics.AnalyticsEvent
import com.cheeke.surfy.analytics.AnalyticsHelper
import com.cheeke.surfy.detail.people.PeopleScreen
import com.cheeke.surfy.detail.people.PeopleState
import com.cheeke.surfy.detail.people.PeopleVM
import com.cheeke.surfy.domain.GetPeopleDetailUseCase
import com.cheeke.surfy.model.LocaleOption
import com.cheeke.surfy.model.People
import com.cheeke.surfy.model.PosterSize
import com.cheeke.surfy.model.SurfyAppData
import com.cheeke.surfy.model.getRelatedMovie
import com.cheeke.surfy.network.model.SurfyNetworkException
import com.cheeke.surfy.testing.model.combineCreditsTestData
import com.cheeke.surfy.testing.model.configurationTestData
import com.cheeke.surfy.testing.model.externalIdsTestData
import com.cheeke.surfy.testing.model.genreListTestData
import com.cheeke.surfy.testing.model.languageListTestData
import com.cheeke.surfy.testing.model.peopleDetailTestData
import com.cheeke.surfy.testing.model.regionTestData
import com.cheeke.surfy.testing.utils.TestMovieAppDataManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertEquals

class PeopleScreenTest {
    @get:Rule
    val composeTestRule = createComposeRule()
    private lateinit var viewModel: PeopleVM
    private lateinit var savedStateHandle: SavedStateHandle
    private lateinit var getPeopleDetail: GetPeopleDetailUseCase
    private lateinit var testDatabaseRepository: TestPeopleDatabaseRepository
    private lateinit var testDetailRepository: TestPeopleDetailRepository
    private lateinit var testMovieAppDataManager: TestMovieAppDataManager
    private val surfyAppData = SurfyAppData(
        secureBaseUrl = configurationTestData.images?.secureBaseUrl.orEmpty(),
        movieGenres = genreListTestData.genres.orEmpty(),
        region = regionTestData.results?.map { region ->
            LocaleOption(code = region.iso31661 ?: "KR", label = region.nativeName ?: "KR", isSelected = region.iso31661 == "KR")
        }.orEmpty(),
        language = languageListTestData.map { language ->
            LocaleOption(code = language.iso6391 ?: "ko", label = language.englishName ?: "Korean", isSelected = language.iso6391 == "ko")
        },
        posterSize = configurationTestData.images?.posterSizes?.map {
            PosterSize(size = it, isSelected = it == "original")
        }.orEmpty()
    )

    @Before
    fun setup() {
        savedStateHandle = SavedStateHandle(initialState = mapOf("id" to 0))
        testMovieAppDataManager = TestMovieAppDataManager()
        testDatabaseRepository = TestPeopleDatabaseRepository()
        testDetailRepository = TestPeopleDetailRepository()
        getPeopleDetail = GetPeopleDetailUseCase(
            peopleRepository = testDetailRepository,
            peopleDataBaseRepository = testDatabaseRepository
        )
        viewModel = PeopleVM(
            id = 0,
            getPeopleDetail = getPeopleDetail,
            peopleRepository = testDatabaseRepository,
            analyticsHelper = object : AnalyticsHelper {
                override fun logEvent(event: AnalyticsEvent) {
                    println("event: $event")
                }
            }
        )
        testMovieAppDataManager.setMovieAppData(surfyAppData)
    }

    @Test
    fun peopleScreenLoadingTest() {
        composeTestRule.apply {
            setContent {
                val people by viewModel.people.collectAsStateWithLifecycle()

                PeopleScreen(
                    peopleState = people,
                    goToBack = {},
                    insertFavoritePeople = viewModel::insertPeople,
                    deleteFavoritePeople = viewModel::deletePeople,
                    goToTv = {},
                    goToMovie = {},
                    onShowSnackbar = { _, _ -> true },
                    restart = viewModel::restart
                )
            }

            onNodeWithContentDescription(label = "peopleDetailLoading").assertExists().assertIsDisplayed()
        }
    }

    @Test
    fun peopleScreenErrorTest() {
        composeTestRule.apply {
            setContent {
                PeopleScreen(
                    peopleState = PeopleState.Error(throwable = SurfyNetworkException(throwable = Throwable("something wrong..."))),
                    goToBack = {},
                    insertFavoritePeople = viewModel::insertPeople,
                    deleteFavoritePeople = viewModel::deletePeople,
                    goToTv = {},
                    goToMovie = {},
                    onShowSnackbar = { _, _ -> true },
                    restart = viewModel::restart
                )
            }

            onNodeWithText(text = "통신 실패").assertExists().assertIsDisplayed()
            onNodeWithText(text = "재시도").assertExists().assertIsDisplayed()
            onNodeWithText(text = "돌아가기").assertExists().assertIsDisplayed()
        }
    }

    @Test
    fun peopleScreenSuccessTest() {
        composeTestRule.apply {
            setContent {
                val people by viewModel.people.collectAsStateWithLifecycle()

                PeopleScreen(
                    peopleState = people,
                    goToBack = {},
                    insertFavoritePeople = viewModel::insertPeople,
                    deleteFavoritePeople = viewModel::deletePeople,
                    goToTv = {},
                    goToMovie = {},
                    onShowSnackbar = { _, _ -> true },
                    restart = viewModel::restart
                )
            }

            runBlocking {
                testDetailRepository.setPeopleDetail(people = peopleDetailTestData)
                testDetailRepository.setCombineCredits(credits = combineCreditsTestData)
                testDetailRepository.setExternalIds(ids = externalIdsTestData)
                testDatabaseRepository.insert(media = peopleDetailTestData)
            }

            onNodeWithContentDescription(label = "peopleImageHorizontalPager").assertExists().assertIsDisplayed()
            onNodeWithText(text = peopleDetailTestData.title!!).assertExists().assertIsDisplayed()
            onNodeWithText(text = peopleDetailTestData.birthday!!).assertExists().assertIsDisplayed()
            onNodeWithContentDescription(label = "facebookId").assertExists().assertIsDisplayed()
            onNodeWithContentDescription(label = "instagramId").assertExists().assertIsDisplayed()
            onNodeWithContentDescription(label = "youtubeId").assertExists().assertIsDisplayed()
            onNodeWithContentDescription(label = "peopleBiography").assertExists().assertTextEquals(peopleDetailTestData.biography!!).assertIsDisplayed()
            peopleDetailTestData.combineCredits?.getRelatedMovie()?.forEach {
                onNodeWithTag(testTag = "${it.posterPath}").assertExists().assertIsDisplayed()
            }
        }
    }

    @Test
    fun insertPeopleTest() = runTest {
        composeTestRule.apply {
            setContent {
                val people by viewModel.people.collectAsStateWithLifecycle()

                PeopleScreen(
                    peopleState = people,
                    goToBack = {},
                    insertFavoritePeople = viewModel::insertPeople,
                    deleteFavoritePeople = viewModel::deletePeople,
                    goToTv = {},
                    goToMovie = {},
                    onShowSnackbar = { _, _ -> true },
                    restart = viewModel::restart
                )
            }

            testDetailRepository.setPeopleDetail(people = peopleDetailTestData)
            testDetailRepository.setCombineCredits(credits = combineCreditsTestData)
            testDetailRepository.setExternalIds(ids = externalIdsTestData)
            testDatabaseRepository.insert(media = People(id = 123))

            onNodeWithContentDescription(label = "peopleImageHorizontalPager").assertExists().assertIsDisplayed()
            onNodeWithText(text = peopleDetailTestData.title!!).assertExists().assertIsDisplayed()
            onNodeWithText(text = peopleDetailTestData.birthday!!).assertExists().assertIsDisplayed()
            onNodeWithContentDescription(label = "facebookId").assertExists().assertIsDisplayed()
            onNodeWithContentDescription(label = "instagramId").assertExists().assertIsDisplayed()
            onNodeWithContentDescription(label = "youtubeId").assertExists().assertIsDisplayed()
            onNodeWithContentDescription(label = "peopleBiography").assertExists().assertTextEquals(peopleDetailTestData.biography!!).assertIsDisplayed()
            peopleDetailTestData.combineCredits?.getRelatedMovie()?.forEach {
                onNodeWithTag(testTag = "${it.posterPath}").assertExists().assertIsDisplayed()
            }
            onNodeWithContentDescription(label = "unFavorite").assertIsDisplayed()
            onNodeWithContentDescription(label = "favorite").assertIsNotDisplayed()
            onNodeWithContentDescription(label = "unFavorite").performClick()
            onNodeWithContentDescription(label = "unFavorite").assertIsNotDisplayed()
            onNodeWithContentDescription(label = "favorite").assertIsDisplayed()
            assertEquals(
                expected = testDatabaseRepository.peopleDatabase.first().find { it.id == peopleDetailTestData.id }?.id,
                actual = peopleDetailTestData.id
            )
        }
    }

    @Test
    fun deletePeopleTest() = runTest {
        composeTestRule.apply {
            setContent {
                val people by viewModel.people.collectAsStateWithLifecycle()

                PeopleScreen(
                    peopleState = people,
                    goToBack = {},
                    insertFavoritePeople = viewModel::insertPeople,
                    deleteFavoritePeople = viewModel::deletePeople,
                    goToTv = {},
                    goToMovie = {},
                    onShowSnackbar = { _, _ -> true },
                    restart = viewModel::restart
                )
            }

            testDetailRepository.setPeopleDetail(people = peopleDetailTestData)
            testDetailRepository.setCombineCredits(credits = combineCreditsTestData)
            testDetailRepository.setExternalIds(ids = externalIdsTestData)
            testDatabaseRepository.insert(media = peopleDetailTestData)

            onNodeWithContentDescription(label = "peopleImageHorizontalPager").assertExists().assertIsDisplayed()
            onNodeWithText(text = peopleDetailTestData.title!!).assertExists().assertIsDisplayed()
            onNodeWithText(text = peopleDetailTestData.birthday!!).assertExists().assertIsDisplayed()
            onNodeWithContentDescription(label = "facebookId").assertExists().assertIsDisplayed()
            onNodeWithContentDescription(label = "instagramId").assertExists().assertIsDisplayed()
            onNodeWithContentDescription(label = "youtubeId").assertExists().assertIsDisplayed()
            onNodeWithContentDescription(label = "peopleBiography").assertExists().assertTextEquals(peopleDetailTestData.biography!!).assertIsDisplayed()
            peopleDetailTestData.combineCredits?.getRelatedMovie()?.forEach {
                onNodeWithTag(testTag = "${it.posterPath}").assertExists().assertIsDisplayed()
            }
            onNodeWithContentDescription(label = "unFavorite").assertIsNotDisplayed()
            onNodeWithContentDescription(label = "favorite").assertIsDisplayed()
            onNodeWithContentDescription(label = "favorite").performClick()
            onNodeWithContentDescription(label = "unFavorite").assertIsDisplayed()
            onNodeWithContentDescription(label = "favorite").assertIsNotDisplayed()
            assertEquals(
                expected = testDatabaseRepository.peopleDatabase.first().find { it.id == peopleDetailTestData.id }?.id,
                actual = null
            )
        }
    }
}