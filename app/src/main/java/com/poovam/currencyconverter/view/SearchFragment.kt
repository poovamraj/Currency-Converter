package com.poovam.currencyconverter.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.poovam.currencyconverter.R
import com.poovam.currencyconverter.core.network.Status
import com.poovam.currencyconverter.viewmodel.CurrencyDataViewModel
import com.poovam.currencyconverter.viewmodel.CurrencyViewModel
import kotlinx.android.synthetic.main.search_fragment.view.*
import org.koin.androidx.viewmodel.ext.android.sharedViewModel


class SearchFragment : BottomSheetDialogFragment() {

    private val viewModel by sharedViewModel<CurrencyViewModel>()

    private val adapter = ResultsAdapter(ArrayList())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView: View = inflater.inflate(R.layout.search_fragment, container)

        //below line is to push bottom sheet above keyboard when keyboard is opened
        dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

        rootView.searchList.adapter = adapter
        rootView.searchList.layoutManager =
            GridLayoutManager(activity, 3, GridLayoutManager.VERTICAL, false)
        adapter.onClickListener = {
            viewModel.setSelectedCurrency(it.code)
            dismiss()
        }

        rootView.sourceCurrencySearch.requestFocus()
        rootView.sourceCurrencySearch.setQuery(viewModel.sourceCurrencySearchQuery, false)
        rootView.sourceCurrencySearch.setOnQueryTextListener(object :
            SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                viewModel.setSourceCurrencySearch(query)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.setSourceCurrencySearch(newText)
                return true
            }

        })

        viewModel.getSearchCurrency().observe(viewLifecycleOwner) { resource ->
            when (resource.status) {
                Status.SUCCESS -> resource.data?.let { onSearchSourceLoaded(rootView, it) }
                Status.ERROR -> {
                    onErrorLoadingSearchSource(rootView, resource.message)
                }
                Status.LOADING -> {
                    searchSourceLoading(rootView, resource.data)
                }
            }
        }
        return rootView
    }

    private fun onSearchSourceLoaded(rootView: View, searchSource: List<CurrencyDataViewModel>) {
        adapter.currencies = searchSource
        adapter.notifyDataSetChanged()
        rootView.progressBar.visibility = View.GONE
        if (searchSource.isNotEmpty()) {
            rootView.emptyInfo.visibility = View.GONE
            rootView.searchList.visibility = View.VISIBLE
        } else {
            rootView.emptyInfo.visibility = View.VISIBLE
            rootView.searchList.visibility = View.GONE
        }
    }

    private fun onErrorLoadingSearchSource(rootView: View, message: String?) {
        rootView.progressBar.visibility = View.GONE
        message?.let { Toast.makeText(activity, it, Toast.LENGTH_SHORT).show() }
    }

    private fun searchSourceLoading(rootView: View, searchSource: List<CurrencyDataViewModel>?) {
        rootView.progressBar.visibility = View.VISIBLE
        searchSource?.let {
            adapter.currencies = it
            adapter.notifyDataSetChanged()
        }
    }
}