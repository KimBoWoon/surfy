package com.cheeke.surfy.domain

import com.cheeke.surfy.data.repository.PeopleRepository
import com.cheeke.surfy.model.People
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

class GetPeopleDetailUseCase @Inject constructor(
    private val peopleRepository: PeopleRepository
) {
    operator fun invoke(personId: Int): Flow<People> =
        combine(
            peopleRepository.getData(id = personId),
            peopleRepository.getCombineCredits(personId = personId),
            peopleRepository.getExternalIds(personId = personId),
            peopleRepository.isFavorite(id = personId)
        ) { peopleDetail, combineCredits, externalIds, isFavorite ->
            peopleDetail.copy(
                combineCredits = combineCredits,
                externalIds = externalIds,
                isFavorite = isFavorite
            )
        }
}