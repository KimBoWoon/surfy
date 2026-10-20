package com.cheeke.surfy.detail.api.people

import com.cheeke.surfy.detail.api.Repository
import com.cheeke.surfy.model.CombineCredits
import com.cheeke.surfy.model.ExternalIds
import com.cheeke.surfy.model.People
import kotlinx.coroutines.flow.Flow

interface PeopleRepository : Repository<People> {
    override fun getData(id: Int): Flow<People>
    fun getCombineCredits(personId: Int): Flow<CombineCredits>
    fun getExternalIds(personId: Int): Flow<ExternalIds>
}