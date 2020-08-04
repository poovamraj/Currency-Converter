package com.poovam.currencyconverter.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.poovam.currencyconverter.R
import com.poovam.currencyconverter.core.roundTo
import com.poovam.currencyconverter.viewmodel.CurrencyDataViewModel
import kotlinx.android.synthetic.main.results_cell.view.*

class ResultsAdapter(var currencies: List<CurrencyDataViewModel>) : RecyclerView.Adapter<ResultCell>() {

    var onClickListener: ((CurrencyDataViewModel) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResultCell {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.results_cell, parent, false)
        return ResultCell(view)
    }

    override fun getItemCount(): Int {
        return currencies.count()
    }

    override fun onBindViewHolder(holder: ResultCell, position: Int) {
        holder.view.setOnClickListener { onClickListener?.invoke(currencies[position]) }
        holder.shortForm.text = currencies[position].code
        holder.currencyName.text = currencies[position].name
        holder.currencyValue.text = String.format("%.${roundTo}f", currencies[position].convertedValue)
    }

}

class ResultCell(val view: View) : RecyclerView.ViewHolder(view) {
    val shortForm: TextView = view.currencyCode
    val currencyName: TextView = view.currencyName
    val currencyValue: TextView = view.currencyValue
}