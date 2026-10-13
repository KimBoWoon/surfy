package com.cheeke.surfy.detail

import com.cheeke.surfy.detail.people.PeopleState
import com.cheeke.surfy.detail.people.PeopleVM
import com.cheeke.surfy.domain.GetPeopleDetailUseCase
import com.cheeke.surfy.model.People
import com.cheeke.surfy.testing.model.combineCreditsTestData
import com.cheeke.surfy.testing.model.externalIdsTestData
import com.cheeke.surfy.testing.model.peopleDetailTestData
import com.cheeke.surfy.testing.repository.TestPeopleRepository
import com.cheeke.surfy.testing.utils.MainDispatcherRule
import com.cheeke.surfy.testing.utils.TestAnalyticsHelper
import com.cheeke.surfy.testing.utils.TestMovieAppDataManager
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import kotlin.test.assertEquals

@RunWith(RobolectricTestRunner::class)
class PeopleVMTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()
    private lateinit var viewModel: PeopleVM
    private lateinit var testPeopleRepository: TestPeopleRepository
    private lateinit var getPeopleDetailUseCase: GetPeopleDetailUseCase
    private lateinit var testMovieAppDataManager: TestMovieAppDataManager
    private lateinit var testAnalyticsHelper: TestAnalyticsHelper

    @Before
    fun setup() {
        testPeopleRepository = TestPeopleRepository()
        testMovieAppDataManager = TestMovieAppDataManager()
        testAnalyticsHelper = TestAnalyticsHelper()
        getPeopleDetailUseCase = GetPeopleDetailUseCase(
            peopleRepository = testPeopleRepository,
        )
        viewModel = PeopleVM(
            id = 0,
            getPeopleDetail = getPeopleDetailUseCase,
            peopleRepository = testPeopleRepository,
            analyticsHelper = testAnalyticsHelper
        )
        runBlocking {
            testPeopleRepository.insert(media = People(id = 0, title = "people_1", posterPath = "/peopleImagePath.png"))
        }
    }

    @Test
    fun peopleDetailLoadingTest() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.people.collect() }

        assertEquals(expected = viewModel.people.value, actual = PeopleState.Loading)
    }

    @Test
    fun peopleDetailSuccessTest() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.people.collect() }

        assertEquals(expected = viewModel.people.value, actual = PeopleState.Loading)

        testPeopleRepository.setPeopleDetail(peopleDetailTestData)
        testPeopleRepository.setCombineCredits(combineCreditsTestData)
        testPeopleRepository.setExternalIds(externalIdsTestData)

        assertEquals(
            expected = viewModel.people.value,
            actual = PeopleState.Success(
                data = peopleDetailTestData.copy(isFavorite = testPeopleRepository.isFavorite(id = 0).first())
            )
        )
    }

    @Test
    fun insertPeopleTest() = runTest {
        viewModel = PeopleVM(
            id = 124,
            getPeopleDetail = getPeopleDetailUseCase,
            peopleRepository = testPeopleRepository,
            analyticsHelper = testAnalyticsHelper
        )
        backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.people.collect() }

        val people = People(id = 124, combineCredits = combineCreditsTestData, externalIds = externalIdsTestData)

        testPeopleRepository.setPeopleDetail(people)
        testPeopleRepository.setCombineCredits(combineCreditsTestData)
        testPeopleRepository.setExternalIds(externalIdsTestData)
        testPeopleRepository.insert(media = People(id = 124, title = "people_124", posterPath = "/peopleImagePath.png"))

        assertEquals(
            expected = viewModel.people.value,
            actual = PeopleState.Success(
                data = people.copy(isFavorite = testPeopleRepository.isFavorite(id = 124).first())
            )
        )
    }

    @Test
    fun deletePeopleTest() = runTest {
        viewModel = PeopleVM(
            id = 124,
            getPeopleDetail = getPeopleDetailUseCase,
            peopleRepository = testPeopleRepository,
            analyticsHelper = testAnalyticsHelper
        )
        backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.people.collect() }

        testPeopleRepository.setPeopleDetail(peopleDetailTestData)
        testPeopleRepository.setCombineCredits(combineCreditsTestData)
        testPeopleRepository.setExternalIds(externalIdsTestData)
        testPeopleRepository.delete(media = People(id = 124, title = "people_124", posterPath = "/peopleImagePath.png"))

        assertEquals(
            expected = viewModel.people.value,
            actual = PeopleState.Success(
                data = peopleDetailTestData.copy(isFavorite = testPeopleRepository.isFavorite(id = 124).first())
            )
        )
    }

//    @Test
//    fun restartFlowTest() = runTest {
//        backgroundScope.launch(UnconfinedTestDispatcher()) { viewModel.people.collect() }
//
//        val people = PeopleDetail(id = 124, combineCredits = combineCreditsTestData, externalIds = externalIdsTestData, isFavorite = false)
//
//        assertEquals(viewModel.people.value, PeopleState.Loading)
//
//        testDetailRepository.setPeopleDetail(people)
//        testDetailRepository.setCombineCredits(combineCreditsTestData)
//        testDetailRepository.setExternalIds(externalIdsTestData)
//
//        assertEquals(viewModel.people.value, PeopleState.Success(people))
//
//        viewModel.restart()
//
//        assertEquals(viewModel.people.value, PeopleState.Loading)
//    }
}