package com.poovam.currencyconverter.view

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doOnTextChanged
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.poovam.currencyconverter.R
import com.poovam.currencyconverter.core.network.Resource
import com.poovam.currencyconverter.core.network.Status
import com.poovam.currencyconverter.viewmodel.CurrencyDataViewModel
import com.poovam.currencyconverter.viewmodel.CurrencyViewModel
import kotlinx.android.synthetic.main.activity_main.*
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.lang.Exception


class MainActivity : AppCompatActivity() {
    private val viewModel by viewModel<CurrencyViewModel>()

    private val adapter = ResultsAdapter(ArrayList())

    private val observer = Observer<Resource<List<CurrencyDataViewModel>>> { resource ->
        when (resource.status) {
            Status.SUCCESS -> resource.data?.let { onCurrenciesLoaded(it) }
            Status.ERROR -> {
                onErrorLoadingCurrencies(resource.message)
            }
            Status.LOADING -> {
                onLoadingCurrencies(resource.data)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        results.layoutManager = GridLayoutManager(this, 3, GridLayoutManager.VERTICAL, false)
        results.adapter = adapter
        viewModel.getCurrencies().observe(this, observer)

        viewModel.getSelectedCurrency().observe(this) {
            it?.let {
                sourceCurrencyCode.text = it.code
            }
        }

        viewModel.getLastSyncedOn().observe(this) { it?.let { lastSyncedOnValue.text = it } }

        swipeRefresh.setOnRefreshListener { viewModel.refreshCurrencySource() }

        actionButton.setOnClickListener { openSearchFragment() }

        conversionValue.doOnTextChanged { text, _, _, _ ->
            try {
                text?.toString()?.toDouble()?.let { viewModel.setAmountEntered(it) }
            } catch (e: Exception) {
                viewModel.setAmountEntered(1.0)
            }
        }

        searchView?.setOnQueryTextListener(object :
            androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                viewModel.setResultSearch(query)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.setResultSearch(newText)
                return true
            }
        })
    }

    private fun onCurrenciesLoaded(currencies: List<CurrencyDataViewModel>) {
        adapter.currencies = currencies
        adapter.notifyDataSetChanged()
        swipeRefresh.isRefreshing = false
        if (currencies.isNotEmpty()) {
            emptyInfo.visibility = View.GONE
            results.visibility = View.VISIBLE
        } else {
            emptyInfo.visibility = View.VISIBLE
            results.visibility = View.GONE
        }
    }

    private fun onErrorLoadingCurrencies(errorMessage: String?) {
        swipeRefresh.isRefreshing = false
        errorMessage?.let { Toast.makeText(this, it, Toast.LENGTH_SHORT).show() }
    }

    private fun onLoadingCurrencies(currencies: List<CurrencyDataViewModel>?) {
        swipeRefresh.isRefreshing = true
        currencies?.let {
            adapter.currencies = it
            adapter.notifyDataSetChanged()
        }
    }

    private fun openSearchFragment() {
        val bottomSheetDialogFragment: BottomSheetDialogFragment = SearchFragment()
        bottomSheetDialogFragment.show(supportFragmentManager, bottomSheetDialogFragment.tag)
    }
}
