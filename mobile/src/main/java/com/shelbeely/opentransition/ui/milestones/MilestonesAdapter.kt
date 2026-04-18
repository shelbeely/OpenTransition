/*
 * Copyright © 2018 TransTracks. All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package com.shelbeely.opentransition.ui.milestones

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.shelbeely.opentransition.R
import com.shelbeely.opentransition.data.Milestone
import com.shelbeely.opentransition.ui.theme.OpenTransitionTheme
import com.shelbeely.opentransition.util.RxSchedulers
import com.shelbeely.opentransition.util.openDefault
import com.shelbeely.opentransition.util.setVisibleOrGone
import com.shelbeely.opentransition.util.toFullDateString
import com.shelbeely.opentransition.util.settings.SettingsManager
import com.jakewharton.rxrelay3.PublishRelay
import io.realm.kotlin.Realm
import io.realm.kotlin.ext.isValid
import io.realm.kotlin.query.Sort
import kotlinx.coroutines.rx3.asObservable
import kotterknife.bindView
import java.lang.ref.WeakReference
import java.time.LocalDate

class MilestonesAdapter(
    eventRelay: PublishRelay<MilestonesUiEvent>,
    private val postInitialLoad: (adapter: MilestonesAdapter) -> Unit,
    private val postLoad: (adapter: MilestonesAdapter) -> Unit
) : RecyclerView.Adapter<MilestonesAdapter.BaseViewHolder>() {
    private val milestonesFlow = Realm.openDefault()
        .query(Milestone::class)
        .sort(Milestone.FIELD_EPOCH_DAY, Sort.DESCENDING)
        .find()
        .asFlow()
        .asObservable()
        .observeOn(RxSchedulers.main())
        .subscribe {
            result = it.list
            generateItems()
            if (initialLoad) {
                initialLoad = false
                postInitialLoad.invoke(this@MilestonesAdapter)
            }

            postLoad.invoke(this@MilestonesAdapter)
        }
    private var result = emptyList<Milestone>()

    private var items = ArrayList<MilestonesAdapterItem>()
    private var initialLoad = true

    private val eventRelayRef = WeakReference(eventRelay)

    override fun getItemCount(): Int = items.size

    override fun getItemViewType(position: Int): Int = when {
        items[position].epochDay != null -> TYPE_TITLE
        items[position].milestone != null -> TYPE_MILESTONE
        else -> throw IllegalArgumentException("Unhandled item type")
    }

    override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
        when (holder) {
            is DayTitleViewHolder -> holder.bind(items[position])
            is MilestoneViewHolder -> holder.bind(items[position])
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder {
        return when (viewType) {
            TYPE_TITLE -> {
                val composeView = ComposeView(parent.context).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                    setViewCompositionStrategy(
                        ViewCompositionStrategy.DisposeOnDetachedFromWindowOrReleasedFromPool
                    )
                }
                DayTitleViewHolder(composeView)
            }
            TYPE_MILESTONE -> {
                val view = LayoutInflater.from(parent.context).inflate(viewType, parent, false)
                MilestoneViewHolder(view, eventRelayRef.get())
            }
            else -> throw IllegalArgumentException("Unhandled item type")
        }
    }

    fun getPositionOfDay(day: Long): Int {
        items.forEachIndexed { index, item ->
            if (item.epochDay != null && item.epochDay == day) {
                return index
            }
        }

        return -1
    }

    private fun generateItems() {
        val newItems = ArrayList<MilestonesAdapterItem>()

        result.forEach { milestone ->
            var indexOfTitle = newItems.indexOfFirst { item -> milestone.epochDay == item.epochDay }

            if (indexOfTitle == -1) {
                newItems.add(MilestonesAdapterItem(milestone.epochDay))
                indexOfTitle = newItems.lastIndex
            }

            var indexToInsertAt: Int = newItems.size

            for (i in (indexOfTitle + 1) until newItems.size) {
                //Find the next title, and we will insert at that index
                if (newItems[i].epochDay != null) {
                    indexToInsertAt = i
                }
            }

            newItems.add(indexToInsertAt, MilestonesAdapterItem(milestone))
        }

        val results = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                val old = items[oldItemPosition]
                val new = newItems[newItemPosition]

                if (old.epochDay != null) {
                    return old.epochDay == new.epochDay
                }

                if (old.milestone == null || !old.milestone.isValid() || new.milestone == null
                    || !new.milestone.isValid()
                ) {
                    return false
                }

                return old.milestone.id == new.milestone.id
            }

            override fun getOldListSize(): Int = items.size

            override fun getNewListSize(): Int = newItems.size

            override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
                val old = items[oldItemPosition]
                val new = newItems[newItemPosition]

                if (old.epochDay != null) {
                    return old.epochDay == new.epochDay
                }

                if (old.milestone == null || new.milestone == null) {
                    return false
                }

                return old.milestone == new.milestone && old.milestone.id == new.milestone.id
                        && old.milestone.epochDay == new.milestone.epochDay
                        && old.milestone.timestamp == new.milestone.timestamp
                        && old.milestone.title == new.milestone.title
                        && old.milestone.description == new.milestone.description
            }
        }, true)

        items = newItems
        results.dispatchUpdatesTo(this)
    }

    fun refreshTitleItems() {
        items.forEachIndexed { index, item ->
            if (item.epochDay != null) {
                notifyItemChanged(index)
            }
        }
    }

    class MilestonesAdapterItem {
        val epochDay: Long?

        val milestone: Milestone?

        constructor(epochDay: Long) {
            this.epochDay = epochDay

            milestone = null
        }

        constructor(milestone: Milestone) {
            this.milestone = milestone

            epochDay = null
        }
    }

    abstract class BaseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    class DayTitleViewHolder(private val composeView: ComposeView) : BaseViewHolder(composeView) {
        fun bind(item: MilestonesAdapterItem) {
            val title = LocalDate.ofEpochDay(item.epochDay!!).toFullDateString(composeView.context)
            composeView.setContent {
                OpenTransitionTheme(
                    colorVariant = SettingsManager.getResolvedComposeColorVariant()
                ) {
                    MilestonesDateTitleItem(title = title)
                }
            }
        }
    }

    class MilestoneViewHolder(itemView: View, eventRelay: PublishRelay<MilestonesUiEvent>?) :
        BaseViewHolder(itemView) {
        private val title: TextView by bindView(R.id.milestones_adapter_item_title)
        private val descriptionIcon: View by bindView(R.id.milestones_adapter_item_description_icon)
        private val description: TextView by bindView(R.id.milestones_adapter_item_description)

        private val eventRelayRef = WeakReference(eventRelay)
        private var milestoneId: String = ""

        init {
            itemView.setOnClickListener {
                eventRelayRef.get()?.accept(MilestonesUiEvent.EditMilestone(milestoneId))
            }
        }

        fun bind(item: MilestonesAdapterItem) {
            milestoneId = item.milestone!!.id

            title.text = item.milestone.title

            description.text = item.milestone.description
            val showDescription = item.milestone.description.isNotEmpty()
            descriptionIcon.setVisibleOrGone(showDescription)
            description.setVisibleOrGone(showDescription)
        }
    }

    companion object {
        private const val TYPE_TITLE = 0
        private const val TYPE_MILESTONE = R.layout.milestones_adapter_item
    }
}
