package com.cheeke.surfy.domain

import com.cheeke.surfy.data.repository.PeopleRepository
import com.cheeke.surfy.model.People
import io.reactivex.rxjava3.core.BackpressureStrategy
import io.reactivex.rxjava3.core.Flowable
import javax.inject.Inject

class GetPeopleDetailUseCase @Inject constructor(
    private val peopleRepository: PeopleRepository
) {
    operator fun invoke(personId: Int): Flowable<People> =
        Flowable.combineLatest(
            peopleRepository.getData(id = personId).toFlowable(),
            peopleRepository.getCombineCredits(personId = personId).toFlowable(),
            peopleRepository.getExternalIds(personId = personId).toFlowable(),
            peopleRepository.isFavorite(id = personId).toFlowable(BackpressureStrategy.LATEST)
        ) { peopleDetail, combineCredits, externalIds, isFavorite ->
            peopleDetail.copy(
                combineCredits = combineCredits,
                externalIds = externalIds,
                isFavorite = isFavorite
            )
        }
}