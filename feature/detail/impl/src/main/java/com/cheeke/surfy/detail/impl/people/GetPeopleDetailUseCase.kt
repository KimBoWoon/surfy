package com.cheeke.surfy.detail.impl.people

import com.cheeke.surfy.detail.api.people.PeopleRepository
import com.cheeke.surfy.model.People
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

class GetPeopleDetailUseCase @Inject constructor(
    private val repository: PeopleRepository
) {
    operator fun invoke(personId: Int): Flow<People> =
        combine(
            repository.getData(id = personId),
            repository.getCombineCredits(personId = personId),
            repository.getExternalIds(personId = personId),
            repository.isFavorite(id = personId)
        ) { peopleDetail, combineCredits, externalIds, isFavorite ->
            peopleDetail.copy(
                combineCredits = combineCredits,
                externalIds = externalIds,
                isFavorite = isFavorite
            )
        }
}