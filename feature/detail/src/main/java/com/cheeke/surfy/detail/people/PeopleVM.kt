package com.cheeke.surfy.detail.people

import androidx.compose.ui.util.trace
import androidx.lifecycle.ViewModel
import com.cheeke.surfy.analytics.AnalyticsHelper
import com.cheeke.surfy.analytics.logSelectContent
import com.cheeke.surfy.common.Log
import com.cheeke.surfy.common.Result
import com.cheeke.surfy.data.repository.PeopleRepository
import com.cheeke.surfy.domain.GetPeopleDetailUseCase
import com.cheeke.surfy.model.People
import com.cheeke.surfy.network.model.SurfyNetworkException
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.processors.BehaviorProcessor

@HiltViewModel(assistedFactory = PeopleVM.Factory::class)
class PeopleVM @AssistedInject constructor(
    @Assisted val id: Int,
    getPeopleDetail: GetPeopleDetailUseCase,
    private val peopleRepository: PeopleRepository,
    private val analyticsHelper: AnalyticsHelper
) : ViewModel() {
    companion object {
        private const val TAG = "PeopleVM"
    }

    @AssistedFactory
    interface Factory {
        fun create(id: Int): PeopleVM
    }

    private val disposable = CompositeDisposable()
    private val reload = BehaviorProcessor.createDefault<Unit>(Unit)
    private val detail = reload
        .switchMap {
            val start = System.nanoTime()
            trace("GetPeopleDetail") {
                getPeopleDetail(id)
                    .map<Result<People>> { Result.Success(data = it) }
                    .startWithItem(Result.Loading)
                    .onErrorReturn { Result.Error(throwable = it) }
                    .doOnSubscribe {
                        val elapsed = System.nanoTime() - start
                        Log.i(TAG, "GetPeopleDetail: ${elapsed / 1_000_000.0} ms")
                    }
            }
        }
    val people = detail.map { result ->
        when (result) {
            is Result.Loading -> PeopleState.Loading
            is Result.Success -> {
                analyticsHelper.logSelectContent(contentType = "movie", media = result.data)
                PeopleState.Success(data = result.data)
            }
            is Result.Error -> PeopleState.Error(result.throwable as SurfyNetworkException)
        }
    }.replay(1)
        .refCount()

    init {
        reload.onNext(Unit)
    }

    fun restart() {
        reload.onNext(Unit)
    }

    fun insertPeople(people: People) {
        peopleRepository.insert(media = people).subscribe().addTo(disposable)
    }

    fun deletePeople(people: People) {
        peopleRepository.delete(media = people).subscribe().addTo(disposable)
    }

    override fun onCleared() {
        super.onCleared()
        disposable.clear()
    }
}

sealed interface PeopleState {
    data object Loading : PeopleState
    data class Success(val data: People) : PeopleState
    data class Error(val throwable: SurfyNetworkException) : PeopleState
}