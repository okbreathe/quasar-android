package com.okbreathe.quasar.components.tagging

import android.content.Context
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Filter
import java.util.Collections.addAll

class TagAdapter(ctx: Context, id: Int, val tags: List<String>, selected: List<String>?) : ArrayAdapter<String>(ctx, id, tags) {
  private val lock = Any()
  private var available: List<String> = tags.filterNot { selected?.contains(it) ?: false }
  private var filtered = available

  fun setSelected(selected: List<String>) {
    synchronized(lock) {
      available = tags.filterNot { selected.contains(it) }
    }
    notifyDataSetChanged()
  }

  override fun getCount(): Int = filtered.size

  override fun getItem(position: Int) = filtered[position]

  override fun getFilter(): Filter = object : Filter() {
    override fun performFiltering(query: CharSequence?): FilterResults {
      val results = FilterResults()

      if (query.isNullOrBlank()) {
        synchronized(lock) {
          results.values = available
          results.count = available.size
        }
      } else {
        val queryString = query.toString().toLowerCase()
        var values: ArrayList<String> = ArrayList()

        synchronized(lock) {
          values = ArrayList(available)
        }

        val filtered = values.filter {
          it.toLowerCase().startsWith(queryString) ||
            it.split(" ").find { it.startsWith(queryString) } != null
        }

        results.values = filtered
        results.count = filtered.size
      }

      return results
    }

    override fun publishResults(constraint: CharSequence?, results: FilterResults) {
      filtered = results.values as List<String>
      if (count > 0) notifyDataSetChanged() else notifyDataSetInvalidated()
    }
  }
}
