package com.mycompany.stocklistapplication

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import java.util.*


private const val TAG = "StockListFragment"

class StockListFragment : Fragment() {


    // Required interface for hosting activities
    interface Callbacks {
        fun onStockSelected(stockId: UUID)
    }

    private var callbacks: Callbacks? = null

    private val stockListViewModel: StockListViewModel by lazy {
        ViewModelProvider(this, ViewModelProvider.NewInstanceFactory()).get(StockListViewModel::class.java)
    }

    private lateinit var stockRecyclerView: RecyclerView
    private var adapter: StockAdapter = StockAdapter(emptyList())

    override fun onAttach(context: Context) {
        super.onAttach(context)
        callbacks = context as Callbacks?
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_stock_list, container, false)
        stockRecyclerView = view.findViewById(R.id.stock_recycler_view) as RecyclerView
        stockRecyclerView.layoutManager = LinearLayoutManager(context)
        stockRecyclerView.adapter = adapter
        return view
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        stockListViewModel.stockListLiveData.observe(
            viewLifecycleOwner,
            Observer { stocks ->
                stocks?.let { Log.i(TAG, "Got stock ${stocks.size}") }
                updateUI(stocks)
            })
    }

    override fun onDetach() {
        super.onDetach()
        callbacks = null
    }


    private fun updateUI(stocks: List<Stock>) {
        adapter = StockAdapter(stocks)
        stockRecyclerView.adapter = adapter
    }

    companion object {
        fun newInstance(): StockListFragment {
            return StockListFragment()
        }
    }

    private inner class StockHolder(view: View) : RecyclerView.ViewHolder(view), View.OnClickListener {
        private val itemNameTextView: TextView = itemView.findViewById(R.id.itemName) as TextView
        private val itemPriceTextView: TextView = itemView.findViewById(R.id.itemPrice) as TextView
        private val itemQuantityTextView: TextView = itemView.findViewById(R.id.itemQuantity) as TextView
        private val itemImageView: ImageView = itemView.findViewById(R.id.imageView) as ImageView
        private val itemRestockDate : TextView = itemView.findViewById(R.id.itemRestockDate) as TextView
        private lateinit var stock: Stock

        init {
            itemView.setOnClickListener(this)
        }

        fun bind(stock: Stock) {
            this.stock = stock
            itemNameTextView.text = stock.name
            itemPriceTextView.text = "Price: RM " + stock.costPerItem.toString()
            itemQuantityTextView.text = "Qty: " + stock.quantity.toString()
            itemRestockDate.text = "Restock Date: \n" + stock.restockDate.toString()



            if (stock.itemPicture != "" ) {
                // Load the image using Glide
                Glide.with(itemView)
                    .load(stock.itemPicture) // Replace 'stock.itemPicture' with the actual URL from the database
                    .placeholder(R.drawable.empty_image) // Empty placeholder image
                    .error(R.drawable.empty_image) // Empty placeholder image for errors (optional)
                    .into(itemImageView)
            }
        }

        /**
         * Called when a view has been clicked.
         *
         * @param v The view that was clicked.
         */
        override fun onClick(v: View?) {
            callbacks?.onStockSelected(stock.id)
        }
    }

    private inner class StockAdapter(var stocks: List<Stock>) : RecyclerView.Adapter<StockHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StockHolder {
            val view = layoutInflater.inflate(R.layout.list_stock, parent, false)
            return StockHolder(view)
        }

        override fun onBindViewHolder(holder: StockHolder, position: Int) {
            val stock = stocks[position]
            holder.bind(stock)
        }

        override fun getItemCount(): Int = stocks.size
    }

    private fun shareStockList() {
        // Create a StringBuilder to hold the stock list data
        val stockListBuilder = StringBuilder()

        // Append all stocks' information to the StringBuilder
        val stocks = adapter.stocks
        for (stock in stocks) {
            stockListBuilder.append("Stock Name: ${stock.name}\n")
            stockListBuilder.append("Price: RM ${stock.costPerItem}\n")
            stockListBuilder.append("Quantity: ${stock.quantity}\n")
            stockListBuilder.append("Restock Date: ${stock.restockDate}\n\n")
        }

        // Get the stock list data as a string
        val stockListData = stockListBuilder.toString()

        // Create an ACTION_SEND intent to share the stock list
        val sendIntent: Intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_SUBJECT, "Stock List")
            putExtra(Intent.EXTRA_TEXT, stockListData)
            type = "text/plain"
        }

        // Start the share activity
        val shareIntent = Intent.createChooser(sendIntent, null)
        startActivity(shareIntent)
    }
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.fragment_stock, menu)

        // Find the share item in the menu
        val shareItem = menu.findItem(R.id.share_stock_list)
        val searchItem = menu.findItem(R.id.search_stock)
        val searchView = searchItem?.actionView as androidx.appcompat.widget.SearchView
        // Set the share icon and show it as an action
        shareItem?.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS)

        searchView.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                // Implement your search logic when the user submits the query
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                // Implement your search logic when the user changes the query text
                val searchStock = stockListViewModel.filterStock(newText)
                updateUI(searchStock)
                return true
            }
        })

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.share_stock_list -> {
                shareStockList()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

}
