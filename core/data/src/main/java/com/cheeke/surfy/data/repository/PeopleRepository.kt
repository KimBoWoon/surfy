package com.cheeke.surfy.data.repository

import com.cheeke.surfy.database.model.PeopleEntity
import com.cheeke.surfy.model.CombineCredits
import com.cheeke.surfy.model.ExternalIds
import com.cheeke.surfy.model.People
import kotlinx.coroutines.flow.Flow

interface PeopleRepository : DataBaseRepository<PeopleEntity>, DetailRepository<People> {
    override fun getData(id: Int): Flow<People>
    fun getCombineCredits(personId: Int): Flow<CombineCredits>
    fun getExternalIds(personId: Int): Flow<ExternalIds>
}